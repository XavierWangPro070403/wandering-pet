package com.wanderingpet.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解：标注在关键写操作接口上，由 OpLogAspect 异步落 MongoDB operation_log
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {

    /** 业务模块，如 pet / adoption / shelter */
    String module();

    /** 操作动作，如 发布宠物档案 / 审核领养申请 */
    String action();
}
