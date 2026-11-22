package com.wanderingpet.module.pet.vo;

import lombok.Data;

/**
 * 管理端宠物全量列表项
 */
@Data
public class AdminPetVO {

    private Long id;

    private String petNo;

    private String name;

    private String categoryName;

    private String breedName;

    private String gender;

    private Integer ageMonth;

    private String coverImage;

    private String status;

    private Long shelterId;

    private String shelterName;

    private String city;

    private Integer viewCount;

    private Integer favoriteCount;

    private String publishTime;

    private String createTime;
}
