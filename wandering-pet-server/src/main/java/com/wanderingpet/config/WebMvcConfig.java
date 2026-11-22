package com.wanderingpet.config;

import cn.hutool.core.util.IdUtil;
import com.wanderingpet.common.util.AesUtil;
import com.wanderingpet.config.props.CorsProperties;
import com.wanderingpet.interceptor.AuthInterceptor;
import com.wanderingpet.interceptor.RateLimitInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：CORS 白名单、鉴权拦截器、限流拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final CorsProperties corsProperties;

    @Value("${wandering.aes.key}")
    private String aesKey;

    /** 启动时把 AES 密钥注入静态工具（供 MyBatis TypeHandler 使用） */
    @PostConstruct
    public void initAesKey() {
        AesUtil.setKey(aesKey);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 鉴权拦截器：所有 /api/** 请求，白名单在 AuthProperties 中配置
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        // 限流拦截器：登录/注册等敏感接口 5 次/分钟/IP
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/auth/admin-login", "/api/v1/auth/portal-login",
                        "/api/v1/auth/register", "/api/v1/auth/wx-login")
                .order(0);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(RequestMethod.GET.name(), RequestMethod.POST.name(),
                        RequestMethod.PUT.name(), RequestMethod.DELETE.name(), RequestMethod.OPTIONS.name())
                .allowedHeaders("*")
                .exposedHeaders("X-Trace-Id")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
