package com.wanderingpet.module.auth.dto;

import com.wanderingpet.module.auth.sms.SmsScene;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送短信验证码入参
 */
@Data
public class SmsSendDTO {

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 使用场景：login-验证码登录，bind-绑定手机号 */
    @Pattern(regexp = "^(login|bind)$", message = "验证码场景不合法")
    private String scene = SmsScene.LOGIN;
}
