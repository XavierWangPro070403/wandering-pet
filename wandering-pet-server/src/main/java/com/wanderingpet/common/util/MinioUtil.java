package com.wanderingpet.common.util;

import cn.hutool.core.util.IdUtil;
import com.wanderingpet.config.props.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.UploadObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具：
 * 公共桶 wandering-pet（图片，匿名可读） + 私有桶 wandering-pet-private（协议 PDF，预签名访问）
 * 对象命名：{bizType}/{yyyy}/{MM}/{UUID}.{ext}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /** 删除失败重试调度器（异步清理，失败重试 3 次） */
    private final ScheduledExecutorService retryScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "minio-delete-retry");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    public void initBuckets() {
        createBucketIfAbsent(properties.getBucket(), true);
        createBucketIfAbsent(properties.getPrivateBucket(), false);
    }

    private void createBucketIfAbsent(String bucket, boolean publicRead) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket 已创建: {}", bucket);
            }
            // 公共读桶：每次启动幂等确保匿名读策略。
            // 手动预先创建的桶不会走 makeBucket 分支，若只在新建时设置策略会导致直链 403
            if (publicRead) {
                String policy = """
                        {"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"AWS":["*"]}, \
                        "Action":["s3:GetObject"],"Resource":["arn:aws:s3:::%s/*"]}]}"""
                        .formatted(bucket);
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
            }
        } catch (Exception e) {
            log.warn("MinIO bucket 初始化失败（本地未启动 MinIO 时属正常，上传功能不可用）: {}", e.getMessage());
        }
    }

    /**
     * 上传文件
     *
     * @param bizType  业务类型（pet/avatar/article...），同时作为对象一级目录
     * @param file     服务器端已接收的临时文件
     * @param ext      扩展名（不含点，如 jpg）
     * @param isPrivate 是否存入私有桶
     * @return 对象名（objectName）
     */
    public String upload(String bizType, File file, String ext, boolean isPrivate) throws Exception {
        String bucket = isPrivate ? properties.getPrivateBucket() : properties.getBucket();
        LocalDate now = LocalDate.now();
        String objectName = "%s/%d/%02d/%s.%s".formatted(
                bizType, now.getYear(), now.getMonthValue(), IdUtil.fastSimpleUUID(), ext);
        minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .filename(file.getAbsolutePath())
                .build());
        return objectName;
    }

    /** 公共对象访问 URL（直链） */
    public String publicUrl(String objectName) {
        return properties.getEndpoint() + "/" + properties.getBucket() + "/" + objectName;
    }

    /** 私有对象预签名 URL（默认有效期 30 分钟） */
    public String presignedUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(io.minio.http.Method.GET)
                .bucket(properties.getPrivateBucket())
                .object(objectName)
                .expiry(properties.getPresignExpireMinutes(), TimeUnit.MINUTES)
                .build());
    }

    /**
     * 异步删除对象（失败重试 3 次，间隔 30 秒）
     *
     * @param objectName 对象名
     * @param isPrivate  是否私有桶
     */
    public void deleteAsync(String objectName, boolean isPrivate) {
        String bucket = isPrivate ? properties.getPrivateBucket() : properties.getBucket();
        deleteWithRetry(bucket, objectName, 3);
    }

    private void deleteWithRetry(String bucket, String objectName, int remain) {
        retryScheduler.execute(() -> {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
            } catch (Exception e) {
                if (remain > 1) {
                    log.warn("MinIO 对象删除失败，30 秒后重试（剩余 {} 次）: {}", remain - 1, objectName);
                    retryScheduler.schedule(() -> deleteWithRetry(bucket, objectName, remain - 1),
                            30, TimeUnit.SECONDS);
                } else {
                    log.error("MinIO 对象删除最终失败: {}", objectName, e);
                }
            }
        });
    }
}
