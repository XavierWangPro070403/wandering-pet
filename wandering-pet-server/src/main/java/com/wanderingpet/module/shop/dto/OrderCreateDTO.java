package com.wanderingpet.module.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 下单入参（单商品直接购买；地址取用户地址簿） */
@Data
public class OrderCreateDTO {

    @NotNull(message = "请选择商品")
    private Long productId;

    @Min(value = 1, message = "数量至少 1 件")
    @Max(value = 99, message = "单笔最多 99 件")
    private Integer quantity = 1;

    @NotNull(message = "请选择收货地址")
    private Long addressId;

    /** 可选：使用的用户优惠券 ID（UNUSED） */
    private Long couponId;
}
