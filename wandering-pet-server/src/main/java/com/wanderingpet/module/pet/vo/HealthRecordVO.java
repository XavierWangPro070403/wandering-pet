package com.wanderingpet.module.pet.vo;

import lombok.Data;

/**
 * 健康档案出参
 */
@Data
public class HealthRecordVO {

    private Long id;

    private Long petId;

    private String recordType;

    private String recordDate;

    private String title;

    private String content;

    private String vetName;

    private String attachmentUrl;
}
