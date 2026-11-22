package com.wanderingpet.module.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宠物品种
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pet_breed")
public class PetBreed extends BaseEntity {

    private Long categoryId;

    private String name;

    private Integer sort;
}
