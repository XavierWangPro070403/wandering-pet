package com.wanderingpet.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Web 用户端注册入参
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "请输入账号")
    @Size(min = 4, max = 32, message = "账号长度为 4-32 位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号仅支持字母、数字与下划线")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 32, message = "密码长度为 6-32 位")
    private String password;

    @NotBlank(message = "请输入昵称")
    @Size(max = 32, message = "昵称最长 32 位")
    private String nickname;

    @NotBlank(message = "请输入验证码")
    private String captchaUuid;

    @NotBlank(message = "请输入验证码")
    private String captchaCode;
}
