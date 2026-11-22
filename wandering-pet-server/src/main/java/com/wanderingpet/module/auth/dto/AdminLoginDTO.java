package com.wanderingpet.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台登录入参（管理员 / 救助站）
 */
@Data
public class AdminLoginDTO {

    @NotBlank(message = "请输入账号")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;

    @NotBlank(message = "请输入验证码")
    private String captchaUuid;

    @NotBlank(message = "请输入验证码")
    private String captchaCode;
}
