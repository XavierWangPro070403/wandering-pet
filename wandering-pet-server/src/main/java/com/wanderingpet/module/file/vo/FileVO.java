package com.wanderingpet.module.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传出参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileVO {

    /** MinIO 对象名（数据库存这个） */
    private String objectName;

    /** 访问 URL（公共桶直链；私有桶为预签名 URL） */
    private String url;
}
