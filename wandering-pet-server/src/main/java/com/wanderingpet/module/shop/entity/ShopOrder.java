package com.wanderingpet.module.shop.entity;

import com.wanderingpet.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 宠物用品订单 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop_order")
public class ShopOrder extends BaseEntity {

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private Long couponId;

    /** 本单获得积分（1元=1积分） */
    private Integer pointsEarned;

    /** PENDING_PAYMENT / PAID / SHIPPED / CANCELLED */
    private String status;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private LocalDateTime payTime;
}
