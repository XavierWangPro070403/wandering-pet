package com.wanderingpet.module.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** 管理端商品新增/编辑入参 */
@Data
public class ProductSaveDTO {

    @NotBlank(message = "请输入商品名称")
    private String name;

    @NotBlank(message = "请选择分类")
    private String category;

    @NotNull(message = "请输入单价")
    private BigDecimal price;

    @NotNull(message = "请输入库存")
    private Integer stock;

    private String coverImage;

    private String description;

    /** ON_SHELF / OFF_SHELF */
    private String status;
}
