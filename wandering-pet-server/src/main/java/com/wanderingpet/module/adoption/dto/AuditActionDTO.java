package com.wanderingpet.module.adoption.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 驳回 / 通过入参
 */
@Data
public class AuditActionDTO {

    @Size(max = 255, message = "备注最长 255 字")
    private String remark;
}
