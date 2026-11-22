package com.wanderingpet.module.pet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 宠物相册图条目
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageItem {

    private String objectName;

    private String url;

    private Integer sort;
}
