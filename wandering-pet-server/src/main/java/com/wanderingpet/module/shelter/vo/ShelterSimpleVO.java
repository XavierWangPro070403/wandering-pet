package com.wanderingpet.module.shelter.vo;

import lombok.Data;

/**
 * 救助站简要信息（宠物详情/会话等处嵌套使用）
 */
@Data
public class ShelterSimpleVO {

    private Long id;

    private String name;

    private String logo;

    private String city;

    private String district;

    private String address;

    private String contactPhone;
}
