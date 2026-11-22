package com.wanderingpet.common.constant;

/**
 * 认证相关常量
 */
public final class AuthConstant {

    private AuthConstant() {
    }

    /** 用户类型：平台管理员 */
    public static final String USER_TYPE_ADMIN = "ADMIN";
    /** 用户类型：救助站机构账号 */
    public static final String USER_TYPE_SHELTER = "SHELTER";
    /** 用户类型：C 端普通用户 */
    public static final String USER_TYPE_USER = "USER";

    /** 请求头中的 Token 属性名（ThreadLocal 上下文透传） */
    public static final String ATTR_LOGIN_USER = "LOGIN_USER";
}
