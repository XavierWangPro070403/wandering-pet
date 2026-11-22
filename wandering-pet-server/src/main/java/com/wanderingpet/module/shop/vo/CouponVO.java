package com.wanderingpet.module.shop.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 可兑换优惠券（领券中心） */
@Data
public class CouponVO {

    private Long id;

    private String name;

    private BigDecimal discount;

    private BigDecimal minSpend;

    private Integer pointsCost;

    private Integer totalCount;

    private Integer receivedCount;

    private Boolean canExchange;
}
