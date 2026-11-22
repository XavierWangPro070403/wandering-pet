package com.wanderingpet.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解：按「IP + 方法」维度计数（Redis INCR，60 秒窗口），由 RateLimitAspect 校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 时间窗口内允许的最大请求数 */
    int maxRequests() default 5;

    /** 时间窗口（秒） */
    int windowSeconds() default 60;
}
