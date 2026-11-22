package com.wanderingpet.module.shelter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 机构入驻申请入参（C 端用户申请成为救助站）
 */
@Data
public class ShelterApplyDTO {

    @NotBlank(message = "请输入机构名称")
    @Size(max = 64)
    private String name;

    @NotBlank(message = "请输入统一社会信用代码/登记证号")
    @Size(max = 64)
    private String licenseNo;

    @NotBlank(message = "请输入联系人姓名")
    @Size(max = 32)
    private String contactName;

    @NotBlank(message = "请输入联系电话")
    private String contactPhone;

    @NotBlank(message = "请选择省份")
    private String province;

    @NotBlank(message = "请选择城市")
    private String city;

    private String district;

    @NotBlank(message = "请填写机构地址（区+街道）")
    private String address;

    @Size(max = 1000, message = "简介最长 1000 字")
    private String intro;

    private String logo;

    /** 申请账号用户名（默认联系人手机号，首次登录密码为 123456） */
    private String accountUsername;
}
