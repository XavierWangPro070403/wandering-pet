package com.wanderingpet.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图形验证码出参
 */
@Data
@AllArgsConstructor
public class CaptchaVO {

    /** 验证码 uuid，登录时回传 */
    private String uuid;

    /** PNG 图片 Base64（data:image/png;base64, 前缀） */
    private String imageBase64;
}
