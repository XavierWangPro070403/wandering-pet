package com.wanderingpet.module.shelter.vo;

import lombok.Data;

/**
 * 救助站公开信息出参（前台展示：地址只到区+街道，不含坐标）
 */
@Data
public class ShelterVO {

    private Long id;

    private String name;

    private String logo;

    private String city;

    private String district;

    private String address;

    private String intro;

    /** 联系人（公开信息） */
    private String contactName;

    /** 联系电话（公开信息） */
    private String contactPhone;

    /** 在册待领养宠物数 */
    private Long petCount;
}
