package com.wanderingpet.module.auth.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.config.props.SmsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信认证通道（dypnsapi 2017-05-25 SendSmsVerifyCode）
 *
 * <p>适用于个人开发者：使用号码认证服务「短信认证」的系统赠送签名与模板
 * （如「速通互联验证码」），无需企业资质与签名申请；
 * 仅在 {@link SmsProperties#devMode()} 为 false 时被调用。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunSmsSender implements SmsSender {

    private final SmsProperties smsProperties;

    private volatile Client client;

    @Override
    public String provider() {
        return "aliyun";
    }

    @Override
    public void send(String phone, String code, String scene) {
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setSignName(smsProperties.getSignName())
                .setTemplateCode(smsProperties.getTemplateCode())
                .setTemplateParam(templateParam(code))
                // 验证码由本服务生成并自行校验，不依赖阿里云端生成/校验闭环
                .setReturnVerifyCode(false)
                .setCodeType(1L)
                // 与本地限流、有效期策略保持一致
                .setInterval((long) smsProperties.getResendSeconds())
                .setValidTime((long) (smsProperties.getCodeExpireSeconds() / 60));
        try {
            SendSmsVerifyCodeResponse response = client().sendSmsVerifyCode(request);
            String resultCode = response.getBody() == null ? null : response.getBody().getCode();
            if (!"OK".equals(resultCode)) {
                log.warn("阿里云短信认证下发失败 phone={} scene={} code={} message={}",
                        mask(phone), scene, resultCode,
                        response.getBody() == null ? null : response.getBody().getMessage());
                throw BizException.of(ResultCode.THIRD_PARTY_ERROR, "短信发送失败，请稍后重试");
            }
            log.info("阿里云短信认证已下发 phone={} scene={} requestId={}",
                    mask(phone), scene, response.getBody() == null ? null : response.getBody().getRequestId());
        } catch (TeaException e) {
            log.error("阿里云短信认证调用异常 phone={} scene={} code={} message={}",
                    mask(phone), scene, e.getCode(), e.getMessage());
            throw BizException.of(ResultCode.THIRD_PARTY_ERROR, "短信发送失败，请稍后重试");
        } catch (Exception e) {
            log.error("阿里云短信认证调用异常 phone={} scene={}", mask(phone), scene, e);
            throw BizException.of(ResultCode.THIRD_PARTY_ERROR, "短信发送失败，请稍后重试");
        }
    }

    /** 赠送模板变量固定为 code（验证码）+ min（有效期分钟） */
    private String templateParam(String code) {
        int min = Math.max(1, smsProperties.getCodeExpireSeconds() / 60);
        return "{\"" + smsProperties.getTemplateParamKey() + "\":\"" + code + "\","
                + "\"" + smsProperties.getTemplateMinKey() + "\":\"" + min + "\"}";
    }

    private Client client() throws Exception {
        Client local = client;
        if (local == null) {
            synchronized (this) {
                local = client;
                if (local == null) {
                    Config config = new Config()
                            .setAccessKeyId(smsProperties.getAccessKeyId())
                            .setAccessKeySecret(smsProperties.getAccessKeySecret())
                            .setEndpoint(smsProperties.getEndpoint());
                    local = new Client(config);
                    client = local;
                }
            }
        }
        return local;
    }

    private String mask(String phone) {
        return phone == null || phone.length() < 11 ? phone : phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
