package com.wanderingpet.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek AI 配置属性：api-key 为空时进入演示模式（本地预置回复，便于无 Key 联调）
 */
@Data
@Component
@ConfigurationProperties(prefix = "wandering.deepseek")
public class DeepSeekProperties {

    /** DeepSeek API Key（platform.deepseek.com 申请） */
    private String apiKey = "";

    /** OpenAI 兼容接口地址 */
    private String baseUrl = "https://api.deepseek.com";

    /** 模型名 */
    private String model = "deepseek-chat";

    /** 是否演示模式（未配置 api-key） */
    public boolean devMode() {
        return apiKey == null || apiKey.isBlank();
    }
}
