package com.wanderingpet.module.auth.sms;

/**
 * 短信验证码使用场景：不同场景的验证码互不通用
 */
public final class SmsScene {

    private SmsScene() {
    }

    /** 手机号验证码登录（不存在则自动注册） */
    public static final String LOGIN = "login";

    /** 已登录账号绑定/换绑手机号 */
    public static final String BIND = "bind";

    /** 校验并归一化场景，未传时按登录处理 */
    public static String normalize(String scene) {
        return BIND.equalsIgnoreCase(scene) ? BIND : LOGIN;
    }
}
