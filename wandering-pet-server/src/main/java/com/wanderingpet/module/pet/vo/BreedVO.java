package com.wanderingpet.module.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 品种出参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreedVO {

    private Long id;

    private Long categoryId;

    private String name;
}
