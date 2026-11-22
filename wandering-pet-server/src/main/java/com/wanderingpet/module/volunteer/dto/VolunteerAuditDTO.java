package com.wanderingpet.module.volunteer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 志愿者审核入参
 */
@Data
public class VolunteerAuditDTO {

    @NotNull(message = "请选择审核结果")
    private Boolean pass;

    @Size(max = 255)
    private String remark;
}
