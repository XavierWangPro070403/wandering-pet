package com.wanderingpet.module.pet.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 宠物列表项出参（门户 / 小程序）
 */
@Data
public class PetListVO {

    private Long id;

    private String petNo;

    private String name;

    private Long categoryId;

    private String categoryName;

    private Long breedId;

    private String breedName;

    private String gender;

    private Integer ageMonth;

    private BigDecimal weight;

    private String size;

    private String color;

    private Integer sterilized;

    private Integer vaccinated;

    private Integer dewormed;

    private String healthStatus;

    private String characterDesc;

    private String coverImage;

    private Long shelterId;

    private String shelterName;

    /** 城市（来自救助站） */
    private String city;

    private String status;

    private Integer viewCount;

    private Integer favoriteCount;

    /** 发布时间 yyyy-MM-dd HH:mm:ss */
    private String publishTime;

    /** 当前登录用户是否已收藏（未登录为 false） */
    private Boolean favorited;
}
