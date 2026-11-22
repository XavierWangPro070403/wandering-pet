package com.wanderingpet.module.pet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 健康档案保存入参
 */
@Data
public class HealthRecordDTO {

    @NotNull(message = "宠物不能为空")
    private Long petId;

    @NotBlank(message = "请选择记录类型")
    private String recordType;

    private String recordDate;

    @NotBlank(message = "请输入记录标题")
    private String title;

    private String content;

    private String vetName;

    private String attachmentUrl;
}
