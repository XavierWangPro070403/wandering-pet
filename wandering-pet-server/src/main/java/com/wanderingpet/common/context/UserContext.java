package com.wanderingpet.common.context;

/**
 * 登录用户 ThreadLocal 上下文：由 AuthInterceptor 写入、afterCompletion 清理
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前登录用户，未登录返回 null（可选登录场景） */
    public static LoginUser current() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
