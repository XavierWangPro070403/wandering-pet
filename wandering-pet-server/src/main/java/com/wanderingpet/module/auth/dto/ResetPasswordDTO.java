package com.wanderingpet.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 找回密码入参（演示环境：账号 + 新密码 + 图形验证码，不接入短信）
 */
@Data
public class ResetPasswordDTO {

    @NotBlank(message = "请输入账号")
    private String account;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32, message = "密码长度为 6-32 位")
    private String newPassword;

    @NotBlank(message = "请输入验证码")
    private String captchaUuid;

    @NotBlank(message = "请输入验证码")
    private String captchaCode;
}
