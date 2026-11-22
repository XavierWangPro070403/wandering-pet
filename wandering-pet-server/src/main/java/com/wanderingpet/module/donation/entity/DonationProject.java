package com.wanderingpet.module.donation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 募捐项目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("donation_project")
public class DonationProject extends BaseEntity {

    private Long shelterId;

    private String title;

    private String cover;

    private BigDecimal targetAmount;

    private BigDecimal raisedAmount;

    private Integer donorCount;

    private LocalDate startDate;

    private LocalDate endDate;

    /** ONGOING / ENDED / CLOSED */
    private String status;

    private String content;
}
