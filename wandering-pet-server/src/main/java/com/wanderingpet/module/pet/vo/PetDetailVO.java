package com.wanderingpet.module.pet.vo;

import com.wanderingpet.module.shelter.vo.ShelterSimpleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 宠物详情出参
 */
@Data
public class PetDetailVO {

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

    private String adoptionRequirements;

    private String story;

    private String coverImage;

    /** 相册（含封面，按 sort 排序） */
    private List<PetImageVO> images;

    /** 健康档案（按日期倒序） */
    private List<HealthRecordVO> healthRecords;

    private ShelterSimpleVO shelter;

    private String status;

    private Integer viewCount;

    private Integer favoriteCount;

    private String publishTime;

    /** 当前登录用户是否已收藏 */
    private Boolean favorited;
}
