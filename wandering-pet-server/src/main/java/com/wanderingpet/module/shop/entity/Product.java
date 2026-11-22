package com.wanderingpet.module.shop.entity;

import com.wanderingpet.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 宠物用品商品 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private String productNo;

    private String name;

    /** FOOD-粮食 TOY-玩具 CLEAN-清洁 CARE-护理 OTHER-其他 */
    private String category;

    private BigDecimal price;

    private Integer stock;

    private String coverImage;

    private String description;

    /** ON_SHELF-在售 OFF_SHELF-下架 */
    private String status;

    private Integer salesCount;
}
