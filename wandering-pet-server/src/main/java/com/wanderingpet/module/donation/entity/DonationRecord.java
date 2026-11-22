package com.wanderingpet.module.donation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐赠明细（模拟支付）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("donation_record")
public class DonationRecord extends BaseEntity {

    private Long projectId;

    private Long userId;

    /** 展示名（匿名时显示"匿名爱心人士"） */
    private String donorName;

    private BigDecimal amount;

    private String payNo;

    /** SUCCESS / FAILED */
    private String payStatus;

    private Integer isAnonymous;

    private String message;

    private LocalDateTime payTime;
}
