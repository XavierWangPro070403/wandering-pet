package com.wanderingpet.module.donation.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 捐赠榜出参
 */
@Data
public class DonorRankVO {

    private Integer rank;

    private String donorName;

    private BigDecimal totalAmount;

    private Long donationCount;
}
