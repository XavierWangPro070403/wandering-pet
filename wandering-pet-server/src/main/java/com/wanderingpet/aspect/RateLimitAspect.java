package com.wanderingpet.aspect;

import com.wanderingpet.common.annotation.RateLimit;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.IpUtil;
import com.wanderingpet.common.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 接口限流切面：@RateLimit 注解按「IP + 方法」在时间窗口内计数
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisUtil redisUtil;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (request != null) {
            String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                    + "." + joinPoint.getSignature().getName();
            String key = RedisKeyConstant.LIMIT_API.formatted(IpUtil.getClientIp(request), method);
            Long count = redisUtil.incr(key, Duration.ofSeconds(rateLimit.windowSeconds()));
            if (count != null && count > rateLimit.maxRequests()) {
                throw BizException.of(ResultCode.TOO_MANY_REQUESTS);
            }
        }
        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }
}
