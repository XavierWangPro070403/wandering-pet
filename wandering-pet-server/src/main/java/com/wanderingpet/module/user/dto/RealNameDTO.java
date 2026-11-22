package com.wanderingpet.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 实名认证入参
 */
@Data
public class RealNameDTO {

    @NotBlank(message = "请输入真实姓名")
    @Size(max = 32, message = "姓名最长 32 位")
    private String realName;

    @NotBlank(message = "请输入身份证号")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCardNo;
}
