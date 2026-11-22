package com.wanderingpet.module.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户资料更新入参
 */
@Data
public class UpdateProfileDTO {

    @Size(max = 32, message = "昵称最长 32 位")
    private String nickname;

    private String avatar;

    /** 0-未知 1-男 2-女 */
    private Integer gender;

    @Size(max = 32, message = "城市最长 32 位")
    private String city;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
