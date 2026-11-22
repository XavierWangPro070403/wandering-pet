package com.wanderingpet.module.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宠物相册
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pet_image")
public class PetImage extends BaseEntity {

    private Long petId;

    /** MinIO 对象名 */
    private String objectName;

    private String url;

    private Integer sort;

    /** IMAGE / VIDEO */
    private String type;
}
