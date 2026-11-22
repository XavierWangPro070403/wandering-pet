package com.wanderingpet.module.file.service.impl;

import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.MinioUtil;
import com.wanderingpet.module.file.service.FileService;
import com.wanderingpet.module.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 文件上传服务实现：校验真实 MIME（文件头魔数，不信任扩展名）+ 大小限制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final Map<String, Set<String>> BIZ_ALLOW = Map.of(
            BizConstant.FILE_BIZ_PET, Set.of("jpg", "jpeg", "png", "webp"),
            BizConstant.FILE_BIZ_AVATAR, Set.of("jpg", "jpeg", "png", "webp"),
            BizConstant.FILE_BIZ_ARTICLE, Set.of("jpg", "jpeg", "png", "webp"),
            BizConstant.FILE_BIZ_DONATION, Set.of("jpg", "jpeg", "png", "webp"),
            BizConstant.FILE_BIZ_REPORT, Set.of("jpg", "jpeg", "png", "webp"),
            BizConstant.FILE_BIZ_DYNAMIC, Set.of("jpg", "jpeg", "png", "webp", "mp4"),
            BizConstant.FILE_BIZ_PRODUCT, Set.of("jpg", "jpeg", "png", "webp"),
            BizConstant.FILE_BIZ_AGREEMENT, Set.of("pdf"));

    private final MinioUtil minioUtil;

    @Override
    public FileVO upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw BizException.of(ResultCode.FILE_EMPTY);
        }
        if (bizType == null || bizType.isBlank()) {
            throw BizException.of(ResultCode.FILE_TYPE_NOT_SUPPORT, "不支持的业务类型或文件格式");
        }
        String biz = bizType.toLowerCase(Locale.ROOT);
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = extOf(original);
        Set<String> allowed = BIZ_ALLOW.get(biz);
        if (allowed == null) {
            throw BizException.of(ResultCode.FILE_TYPE_NOT_SUPPORT, "不支持的业务类型或文件格式");
        }
        if (!allowed.contains(ext)) {
            throw BizException.of(ResultCode.FILE_TYPE_NOT_SUPPORT,
                    "该业务仅支持 " + String.join("/", allowed));
        }
        long max = "mp4".equals(ext) ? BizConstant.VIDEO_MAX_SIZE
                : ("pdf".equals(ext) ? BizConstant.DOC_MAX_SIZE : BizConstant.IMAGE_MAX_SIZE);
        if (file.getSize() > max) {
            throw BizException.of(ResultCode.FILE_TOO_LARGE);
        }
        // 魔数校验：不信任扩展名，内容真实类型必须与扩展名一致
        byte[] head = readHead(file);
        String magicType = sniffMagic(head);
        if (magicType == null || !matches(magicType, ext)) {
            throw BizException.of(ResultCode.FILE_TYPE_NOT_SUPPORT, "文件内容与扩展名不符");
        }
        java.io.File temp = null;
        try {
            temp = java.io.File.createTempFile("wp-upload-", "." + ext);
            file.transferTo(temp);
            // 桶路由：仅领养协议 PDF 存私有桶（wandering-pet-private，预签名临时访问）；
            // 其余业务——含头像 avatar——一律存公共桶 wandering-pet，直链长期可读
            boolean isPrivate = BizConstant.FILE_BIZ_AGREEMENT.equals(biz);
            String objectName = minioUtil.upload(biz, temp, ext, isPrivate);
            String url = isPrivate ? minioUtil.presignedUrl(objectName) : minioUtil.publicUrl(objectName);
            return new FileVO(objectName, url);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw BizException.of(ResultCode.FILE_UPLOAD_ERROR);
        } finally {
            if (temp != null) {
                temp.delete();
            }
        }
    }

    private String extOf(String name) {
        int idx = name.lastIndexOf('.');
        return idx < 0 ? "" : name.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private byte[] readHead(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(16);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /** 魔数嗅探：jpg/png/webp/mp4/pdf；未知返回 null */
    private String sniffMagic(byte[] head) {
        if (head.length < 4) {
            return null;
        }
        if ((head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if ((head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G') {
            return "png";
        }
        // WebP：完整头 RIFF....WEBP（前 12 字节），避免把 AVI/WAV 等 RIFF 容器误判为 webp
        if (head.length >= 12
                && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') {
            return "webp";
        }
        // MP4：offset 4 处为 ftyp，需至少 8 字节（不足时提前判空避免越界）
        if (head.length >= 8 && head[4] == 'f' && head[5] == 't' && head[6] == 'y' && head[7] == 'p') {
            return "mp4";
        }
        if (head[0] == '%' && head[1] == 'P' && head[2] == 'D' && head[3] == 'F') {
            return "pdf";
        }
        return null;
    }

    /** 扩展名与真实类型比对（jpg/jpeg 同源归一） */
    private boolean matches(String magic, String ext) {
        return magic.equals(ext) || ("jpg".equals(magic) && "jpeg".equals(ext));
    }
}
