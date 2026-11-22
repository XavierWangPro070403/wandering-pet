package com.wanderingpet.module.shop.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCouponVO {

    private Long id;

    private String couponName;

    private BigDecimal discount;

    private BigDecimal minSpend;

    private String status;

    private LocalDateTime expireTime;
}
