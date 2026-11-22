package com.wanderingpet.module.donation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 捐赠入参（模拟支付）
 */
@Data
public class DonateDTO {

    @NotNull(message = "请输入捐赠金额")
    @DecimalMin(value = "0.01", message = "捐赠金额不低于 0.01 元")
    @Digits(integer = 8, fraction = 2, message = "金额格式不正确")
    private BigDecimal amount;

    /** 是否匿名展示 */
    private Integer isAnonymous;

    @Size(max = 255, message = "留言最长 255 字")
    private String message;
}
