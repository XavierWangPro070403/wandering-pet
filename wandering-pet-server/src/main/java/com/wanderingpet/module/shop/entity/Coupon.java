package com.wanderingpet.module.shop.entity;

import com.wanderingpet.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 优惠券模板（积分兑换） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {

    private String name;

    /** 面额（元） */
    private BigDecimal discount;

    /** 使用门槛（元），0 无门槛 */
    private BigDecimal minSpend;

    /** 兑换所需积分 */
    private Integer pointsCost;

    private Integer totalCount;

    private Integer receivedCount;

    /** ACTIVE-发放中 OFF-停发 */
    private String status;

    /** 领取后有效天数 */
    private Integer expireDays;
}
