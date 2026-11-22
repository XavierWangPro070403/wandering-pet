package com.wanderingpet.module.pet.vo;

import lombok.Data;

import java.util.List;

/**
 * 我的收藏出参（宠物）
 */
@Data
public class FavoritePetVO {

    private Long targetId;

    private String name;

    private String coverImage;

    private String categoryName;

    private String breedName;

    private Integer ageMonth;

    private String status;

    private String shelterName;

    private String city;

    private String createTime;
}
