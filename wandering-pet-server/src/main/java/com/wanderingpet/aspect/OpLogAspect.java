package com.wanderingpet.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.document.OperationLog;
import com.wanderingpet.common.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 操作日志切面：关键写操作落 MongoDB operation_log 集合（异步，不影响主流程）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OpLogAspect {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint joinPoint, OpLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            saveLog(joinPoint, opLog, System.currentTimeMillis() - start, error);
        }
    }

    @Async
    protected void saveLog(ProceedingJoinPoint joinPoint, OpLog opLog, long duration, Throwable error) {
        try {
            LoginUser user = UserContext.current();
            OperationLog entity = OperationLog.builder()
                    .userId(user == null ? null : user.getUserId())
                    .userType(user == null ? null : user.getUserType())
                    .module(opLog.module())
                    .action(opLog.action())
                    .method(joinPoint.getSignature().getDeclaringTypeName() + "#" + joinPoint.getSignature().getName())
                    .params(toParamsJson(joinPoint.getArgs()))
                    .ip(IpUtil.getClientIp(currentRequest()))
                    .duration(duration)
                    .success(error == null)
                    .errorMessage(error == null ? null : error.getMessage())
                    .createTime(LocalDateTime.now())
                    .build();
            mongoTemplate.save(entity);
        } catch (Exception e) {
            log.warn("操作日志写入失败: {}", e.getMessage());
        }
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private String toParamsJson(Object[] args) {
        try {
            StringBuilder sb = new StringBuilder("[");
            for (Object arg : args) {
                if (arg == null || arg instanceof MultipartFile || arg instanceof HttpServletRequest
                        || arg instanceof jakarta.servlet.ServletResponse) {
                    continue;
                }
                if (sb.length() > 1) {
                    sb.append(',');
                }
                sb.append(objectMapper.writeValueAsString(arg));
                if (sb.length() > 1500) {
                    break;
                }
            }
            sb.append(']');
            return sb.length() > 2000 ? sb.substring(0, 2000) : sb.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
}
