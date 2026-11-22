package com.wanderingpet.module.pet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏/取消收藏入参
 */
@Data
public class FavoriteDTO {

    @NotBlank(message = "收藏对象类型不能为空")
    private String targetType;

    @NotNull(message = "收藏对象不能为空")
    private Long targetId;
}
