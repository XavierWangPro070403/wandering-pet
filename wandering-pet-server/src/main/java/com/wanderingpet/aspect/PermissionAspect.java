package com.wanderingpet.aspect;

import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 权限校验切面：校验 @RequirePermission 声明的权限点（数据来自登录时加载的权限集合）
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        LoginUser loginUser = UserContext.current();
        if (loginUser == null) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        if (!loginUser.hasPermission(requirePermission.value())) {
            log.warn("权限不足 userId={} userType={} permCode={}",
                    loginUser.getUserId(), loginUser.getUserType(), requirePermission.value());
            throw BizException.of(ResultCode.FORBIDDEN);
        }
    }
}
