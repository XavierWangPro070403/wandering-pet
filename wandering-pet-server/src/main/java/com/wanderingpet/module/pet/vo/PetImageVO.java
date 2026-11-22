package com.wanderingpet.module.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 相册图出参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetImageVO {

    private Long id;

    private String url;

    private String objectName;

    private Integer sort;

    private String type;
}
