package com.wanderingpet.module.pet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 宠物状态变更入参（单个 / 批量）
 */
@Data
public class PetStatusDTO {

    /** AVAILABLE / MEDICAL / OFFLINE（PENDING/ADOPTED 由领养流程联动，不开放手工设置） */
    @NotBlank(message = "请选择目标状态")
    private String status;

    /** 批量操作时使用 */
    private List<Long> ids;
}
