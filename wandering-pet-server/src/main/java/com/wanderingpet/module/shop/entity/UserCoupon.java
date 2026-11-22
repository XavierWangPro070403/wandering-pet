package com.wanderingpet.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 用户优惠券（含券面快照） */
@Data
@TableName("user_coupon")
public class UserCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long couponId;

    private String couponName;

    private BigDecimal discount;

    private BigDecimal minSpend;

    /** UNUSED-未使用 USED-已使用 */
    private String status;

    private LocalDateTime expireTime;

    private LocalDateTime usedTime;

    private Long usedOrderId;

    private LocalDateTime createTime;
}
