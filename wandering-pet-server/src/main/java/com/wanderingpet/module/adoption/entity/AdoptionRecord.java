package com.wanderingpet.module.adoption.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 领养正式记录（协议主体）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("adoption_record")
public class AdoptionRecord extends BaseEntity {

    private String adoptionNo;

    private Long applicationId;

    private Long petId;

    private Long userId;

    private Long shelterId;

    private LocalDate signDate;

    /** 私有桶对象名 */
    private String agreementObjectName;

    /** 预签名 URL */
    private String agreementUrl;

    /** ADOPTED / RETURNED */
    private String status;

    private String returnReason;

    private LocalDateTime returnTime;
}
