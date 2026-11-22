package com.wanderingpet.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴权配置属性：免登录路径白名单
 */
@Data
@Component
@ConfigurationProperties(prefix = "wandering.auth")
public class AuthProperties {

    /** 免登录路径（Ant 风格） */
    private List<String> ignoreUrls = new ArrayList<>();
}
