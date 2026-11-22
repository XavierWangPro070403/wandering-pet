package com.wanderingpet.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Web 用户端登录入参（账号密码）
 */
@Data
public class PortalLoginDTO {

    @NotBlank(message = "请输入账号")
    private String account;

    @NotBlank(message = "请输入密码")
    private String password;

    @NotBlank(message = "请输入验证码")
    private String captchaUuid;

    @NotBlank(message = "请输入验证码")
    private String captchaCode;
}
