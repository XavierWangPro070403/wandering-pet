package com.wanderingpet.module.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宠物物种分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pet_category")
public class PetCategory extends BaseEntity {

    private String name;

    private String icon;

    private Integer sort;
}
