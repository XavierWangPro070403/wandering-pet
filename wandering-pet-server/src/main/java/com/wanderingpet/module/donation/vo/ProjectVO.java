package com.wanderingpet.module.donation.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 募捐项目出参
 */
@Data
public class ProjectVO {

    private Long id;

    private Long shelterId;

    private String shelterName;

    private String shelterCity;

    private String title;

    private String cover;

    private BigDecimal targetAmount;

    private BigDecimal raisedAmount;

    private Integer donorCount;

    private String startDate;

    private String endDate;

    private String status;

    /** 筹款进度 0-100（整数百分比） */
    private Integer progress;

    private String content;

    private String createTime;
}
