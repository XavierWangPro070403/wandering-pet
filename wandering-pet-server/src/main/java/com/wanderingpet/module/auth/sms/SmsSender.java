package com.wanderingpet.module.auth.sms;

/**
 * 短信发送通道：开发模式与阿里云实现二选一
 */
public interface SmsSender {

    /** 通道标识：dev / aliyun */
    String provider();

    /**
     * 下发验证码，失败抛 {@code BizException(THIRD_PARTY_ERROR)}
     */
    void send(String phone, String code, String scene);
}
