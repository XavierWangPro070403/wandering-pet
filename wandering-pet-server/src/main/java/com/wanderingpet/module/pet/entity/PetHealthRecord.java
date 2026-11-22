package com.wanderingpet.module.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 宠物健康档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pet_health_record")
public class PetHealthRecord extends BaseEntity {

    private Long petId;

    /** VACCINE / DEWORM / STERILIZE / TREATMENT / CHECK */
    private String recordType;

    private LocalDate recordDate;

    private String title;

    private String content;

    private String vetName;

    private String attachmentUrl;
}
