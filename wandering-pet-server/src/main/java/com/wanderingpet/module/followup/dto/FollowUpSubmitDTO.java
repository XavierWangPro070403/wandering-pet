package com.wanderingpet.module.followup.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 回访表填写入参（领养人）
 */
@Data
public class FollowUpSubmitDTO {

    /** 体重 kg */
    private BigDecimal weight;

    @Size(max = 64, message = "健康状况最长 64 字")
    private String healthStatus;

    @Size(max = 32, message = "疫苗情况最长 32 字")
    private String vaccineStatus;

    @Size(max = 500, message = "生活近况最长 500 字")
    private String lifeDesc;

    /** 生活照 URL 列表（3 张） */
    private List<String> images;
}
