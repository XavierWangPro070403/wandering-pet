package com.wanderingpet.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS 白名单配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wandering.cors")
public class CorsProperties {

    /** 允许的来源（禁止使用 * 与 allowCredentials 组合） */
    private List<String> allowedOrigins = new ArrayList<>();
}
