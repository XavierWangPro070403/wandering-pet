package com.wanderingpet.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户地址
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_address")
public class UserAddress extends BaseEntity {

    private Long userId;

    private String receiver;

    private String phone;

    private String province;

    private String city;

    private String district;

    /** 详细地址（区+街道，不含门牌号） */
    private String detail;

    /** 1-默认地址 */
    private Integer isDefault;
}
