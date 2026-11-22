package com.wanderingpet.module.donation.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 捐赠明细出参（公开列表，姓名已脱敏）
 */
@Data
public class DonationRecordVO {

    private Long id;

    private Long projectId;

    private String donorName;

    private BigDecimal amount;

    private Integer isAnonymous;

    private String message;

    private String payTime;
}
