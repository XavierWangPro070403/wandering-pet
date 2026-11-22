package com.wanderingpet.module.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 开发模式短信通道：不真实发送，仅打印验证码便于本地联调
 */
@Slf4j
@Component
public class DevSmsSender implements SmsSender {

    @Override
    public String provider() {
        return "dev";
    }

    @Override
    public void send(String phone, String code, String scene) {
        log.warn("[开发模式] 未配置阿里云短信，验证码不会真实下发 —— phone={} scene={} code={}", phone, scene, code);
    }
}
