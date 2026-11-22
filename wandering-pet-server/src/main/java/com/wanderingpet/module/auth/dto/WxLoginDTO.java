package com.wanderingpet.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 小程序微信登录入参
 */
@Data
public class WxLoginDTO {

    /** wx.login 返回的临时凭证 code */
    @NotBlank(message = "code 不能为空")
    private String code;

    /** 微信昵称（首次登录时传入） */
    private String nickname;

    /** 微信头像（首次登录时传入） */
    private String avatar;
}
