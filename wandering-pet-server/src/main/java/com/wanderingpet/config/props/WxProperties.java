package com.wanderingpet.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置属性：appid/secret 为空时进入开发模式
 */
@Data
@Component
@ConfigurationProperties(prefix = "wandering.wx")
public class WxProperties {

    private String appid = "";

    private String secret = "";

    /** 是否开发模式（未配置 appid 时，任意 code 可登录，便于本地联调） */
    public boolean devMode() {
        return appid == null || appid.isBlank();
    }

    /** 微信开放平台-网站应用 appid（Web 端扫码登录）；为空时扫码登录走演示模式 */
    private String openAppid = "";

    /** 微信开放平台-网站应用 secret */
    private String openSecret = "";

    /** 扫码回调地址前缀（须与开放平台配置的授权回调域一致），如 https://pet.example.com */
    private String openRedirectBase = "";

    /** 是否已配置开放平台（Web 扫码登录走真实流程） */
    public boolean hasOpenPlatform() {
        return openAppid != null && !openAppid.isBlank()
                && openSecret != null && !openSecret.isBlank()
                && openRedirectBase != null && !openRedirectBase.isBlank();
    }
}
