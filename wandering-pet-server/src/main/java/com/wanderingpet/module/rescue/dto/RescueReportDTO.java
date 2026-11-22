package com.wanderingpet.module.rescue.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 流浪动物线索上报入参
 */
@Data
public class RescueReportDTO {

    /** 物种（可空则选“其他”） */
    private Long categoryId;

    @NotBlank(message = "请描述动物特征与所处环境")
    @Size(max = 500, message = "描述最长 500 字")
    private String description;

    /** 现场图片 URL（最多 9 张） */
    private List<String> images;

    @NotBlank(message = "请选择省份")
    private String province;

    @NotBlank(message = "请选择城市")
    private String city;

    private String district;

    @NotBlank(message = "请填写位置（区+街道/地标）")
    @Size(max = 255)
    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    /** 1-一般 2-紧急 3-危急 */
    @Min(value = 1, message = "紧急程度不正确")
    @Max(value = 3, message = "紧急程度不正确")
    private Integer urgency;
}
