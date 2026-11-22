package com.wanderingpet.module.shop.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVO {

    private Long id;

    private String productNo;

    private String name;

    private String category;

    private BigDecimal price;

    private Integer stock;

    private Integer salesCount;

    private String coverImage;

    private String description;

    private String status;
}
