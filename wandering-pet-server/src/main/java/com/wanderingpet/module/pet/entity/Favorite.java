package com.wanderingpet.module.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏（宠物/文章通用）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("favorite")
public class Favorite extends BaseEntity {

    private Long userId;

    /** PET / ARTICLE */
    private String targetType;

    private Long targetId;
}
