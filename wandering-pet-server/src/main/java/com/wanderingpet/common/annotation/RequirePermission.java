package com.wanderingpet.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解：标注在 Controller 方法上，由 PermissionAspect 校验
 *
 * <p>权限点存于 sys_permission，登录后按角色加载并缓存至 Redis。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /** 权限编码，如 pet:create */
    String value();
}
