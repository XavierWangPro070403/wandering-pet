package com.wanderingpet.module.donation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 募捐项目保存入参（机构端）
 */
@Data
public class ProjectSaveDTO {

    @NotBlank(message = "请输入项目标题")
    @Size(max = 128)
    private String title;

    private String cover;

    @NotNull(message = "请输入目标金额")
    @DecimalMin(value = "1", message = "目标金额不低于 1 元")
    private BigDecimal targetAmount;

    private String startDate;

    private String endDate;

    @Size(max = 5000, message = "项目说明过长")
    private String content;
}
