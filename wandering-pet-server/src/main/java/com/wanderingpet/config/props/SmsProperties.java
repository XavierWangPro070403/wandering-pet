package com.wanderingpet.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信认证配置（号码认证服务 dypnsapi）：access-key-id / sign-name / template-code 任一为空即进入开发模式
 *
 * <p>开发模式不会真实下发短信：验证码写入 Redis，并在接口响应中回传（仅联调用）。
 * 生产环境通过环境变量注入，切到 {@code aliyun} 通道。</p>
 *
 * <p>个人开发者无企业资质时，可在号码认证服务控制台开通「短信认证」，
 * 使用系统赠送的固定签名（如「速通互联验证码」）与验证码模板，无需自行申请签名。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "wandering.sms")
public class SmsProperties {

    /** 阿里云 AccessKeyId（建议使用 RAM 子账号，仅授予号码认证服务 dypnsapi 权限） */
    private String accessKeyId = "";

    /** 阿里云 AccessKeySecret */
    private String accessKeySecret = "";

    /** 短信签名：号码认证服务控制台系统赠送的固定签名，如「速通互联验证码」 */
    private String signName = "";

    /** 验证码模板 CODE：号码认证服务控制台系统赠送的模板编号 */
    private String templateCode = "";

    /** 模板中验证码变量名（默认 code，对应 ${code}） */
    private String templateParamKey = "code";

    /** 模板中有效期变量名（默认 min，对应 ${min}，单位分钟） */
    private String templateMinKey = "min";

    /** 短信认证服务地域 */
    private String regionId = "cn-hangzhou";

    /** 短信认证服务接入地址 */
    private String endpoint = "dypnsapi.aliyuncs.com";

    /** 验证码有效期（秒） */
    private int codeExpireSeconds = 300;

    /** 同一手机号重发间隔（秒） */
    private int resendSeconds = 60;

    /** 同一手机号每日发送上限 */
    private int dailyLimit = 10;

    /** 同一验证码允许校验失败次数 */
    private int maxVerifyAttempts = 5;

    /** 是否开发模式（未配置阿里云凭证即不真实发送） */
    public boolean devMode() {
        return isBlank(accessKeyId) || isBlank(accessKeySecret) || isBlank(signName) || isBlank(templateCode);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
