package com.wanderingpet.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户地址保存入参
 */
@Data
public class AddressDTO {

    @NotBlank(message = "请输入收件人姓名")
    @Size(max = 32)
    private String receiver;

    @NotBlank(message = "请输入联系电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请选择省份")
    private String province;

    @NotBlank(message = "请选择城市")
    private String city;

    @NotBlank(message = "请选择区县")
    private String district;

    @NotBlank(message = "请填写详细地址（区+街道）")
    @Size(max = 255, message = "详细地址过长")
    private String detail;

    /** 1-设为默认地址 */
    /** 是否默认地址（接受 true/false 或 1/0） */
    private Boolean isDefault;
}
