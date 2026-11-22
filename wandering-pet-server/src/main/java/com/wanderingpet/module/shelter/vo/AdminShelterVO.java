package com.wanderingpet.module.shelter.vo;

import lombok.Data;

/**
 * 管理端救助站出参
 */
@Data
public class AdminShelterVO {

    private Long id;

    private String name;

    private String logo;

    private String licenseNo;

    private String contactName;

    private String contactPhone;

    private String province;

    private String city;

    private String district;

    private String address;

    private String intro;

    /** 0-待审核 1-已通过 2-已驳回 */
    private Integer auditStatus;

    private String auditRemark;

    private String auditTime;

    /** 关联账号用户名 */
    private String accountUsername;

    private String createTime;
}
