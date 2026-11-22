package com.wanderingpet.module.shelter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 机构入驻审核入参（管理员）
 */
@Data
public class ShelterAuditDTO {

    /** true-通过 false-驳回 */
    @NotNull(message = "请选择审核结果")
    private Boolean pass;

    @Size(max = 255, message = "备注最长 255 字")
    private String remark;
}
