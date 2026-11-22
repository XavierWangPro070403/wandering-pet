package com.wanderingpet.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wandering.minio")
public class MinioProperties {

    /** 服务端点，如 http://localhost:9000 */
    private String endpoint;

    private String accessKey;

    private String secretKey;

    /** 公共桶（图片，匿名可读） */
    private String bucket = "wandering-pet";

    /** 私有桶（协议 PDF，需预签名） */
    private String privateBucket = "wandering-pet-private";

    /** 预签名 URL 有效期（分钟） */
    private Integer presignExpireMinutes = 30;
}
