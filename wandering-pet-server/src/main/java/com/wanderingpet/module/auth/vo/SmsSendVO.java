package com.wanderingpet.module.auth.vo;

import lombok.Data;

/**
 * 发送短信验证码出参
 */
@Data
public class SmsSendVO {

    /** 是否开发模式（未配置阿里云凭证，不会真实下发短信） */
    private boolean devMode;

    /** 开发模式下直接回传验证码，便于联调；真实发送时为 null */
    private String code;

    /** 验证码有效期（秒） */
    private int expireSeconds;

    /** 允许重发间隔（秒） */
    private int resendSeconds;

    /** 发送通道：dev / aliyun */
    private String provider;
}
