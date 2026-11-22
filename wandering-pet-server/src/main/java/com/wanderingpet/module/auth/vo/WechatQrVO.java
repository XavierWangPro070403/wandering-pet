package com.wanderingpet.module.auth.vo;

import lombok.Data;

/**
 * 微信扫码登录-二维码创建出参
 */
@Data
public class WechatQrVO {

    /** 登录会话 ID（轮询 check 用） */
    private String uuid;

    /** 二维码图片（base64 PNG，data:image/png;base64, 前缀） */
    private String qrBase64;

    /** 是否演示模式（未配置开放平台时 true，二维码展示后数秒自动"扫码成功"） */
    private Boolean demo;

    /** 二维码有效期（秒） */
    private Integer expireSeconds;
}
