package com.wanderingpet.module.pet.vo;

import lombok.Data;

/**
 * 机构端宠物管理列表项
 */
@Data
public class PetManageListVO {

    private Long id;

    private String petNo;

    private String name;

    private Long categoryId;

    private String categoryName;

    private String breedName;

    private String gender;

    private Integer ageMonth;

    private String coverImage;

    private String status;

    private Integer viewCount;

    private Integer favoriteCount;

    /** 关联进行中申请数 */
    private Integer activeApplicationCount;

    private String updateTime;
}
