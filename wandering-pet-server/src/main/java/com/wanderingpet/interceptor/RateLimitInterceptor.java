package com.wanderingpet.interceptor;

import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.result.R;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.IpUtil;
import com.wanderingpet.common.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 敏感接口限流拦截器：登录/注册类接口 5 次/分钟/IP（Redis INCR 滑动窗口计数）
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String key = RedisKeyConstant.LIMIT_API.formatted(IpUtil.getClientIp(request), request.getRequestURI());
        Long count = redisUtil.incr(key, WINDOW);
        if (count != null && count > MAX_REQUESTS) {
            writeJson(response, R.fail(ResultCode.TOO_MANY_REQUESTS));
            return false;
        }
        return true;
    }

    private void writeJson(HttpServletResponse response, R<?> body) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
