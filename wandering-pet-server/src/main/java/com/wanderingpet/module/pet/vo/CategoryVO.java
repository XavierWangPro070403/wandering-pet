package com.wanderingpet.module.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物种分类出参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {

    private Long id;

    private String name;

    private String icon;

    private Integer sort;
}
