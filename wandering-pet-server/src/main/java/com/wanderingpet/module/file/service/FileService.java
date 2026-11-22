package com.wanderingpet.module.file.service;

import com.wanderingpet.module.file.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file    文件
     * @param bizType 业务类型（pet/avatar/article/donation/report/agreement/dynamic）
     * @return objectName + url（私有类型走预签名）
     */
    FileVO upload(MultipartFile file, String bizType);
}
