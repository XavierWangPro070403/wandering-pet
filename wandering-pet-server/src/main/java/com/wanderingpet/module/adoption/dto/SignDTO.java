package com.wanderingpet.module.adoption.dto;

import lombok.Data;

/**
 * 签约入参
 */
@Data
public class SignDTO {

    /** 签署日期 yyyy-MM-dd，默认今天 */
    private String signDate;

    /** 协议 PDF 的 MinIO 对象名（可后补上传） */
    private String agreementObjectName;
}
