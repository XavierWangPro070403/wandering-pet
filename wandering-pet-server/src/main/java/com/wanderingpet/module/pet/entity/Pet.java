package com.wanderingpet.module.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物主档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pet")
public class Pet extends BaseEntity {

    /** 档案编号 PET+日期+序号 */
    private String petNo;

    private String name;

    private Long categoryId;

    private Long breedId;

    /** MALE / FEMALE / UNKNOWN */
    private String gender;

    private Integer ageMonth;

    private BigDecimal weight;

    /** SMALL / MEDIUM / LARGE */
    private String size;

    private String color;

    /** 是否绝育 */
    private Integer sterilized;

    /** 是否接种疫苗 */
    private Integer vaccinated;

    /** 是否驱虫 */
    private Integer dewormed;

    /** HEALTHY / GENERAL / ILL */
    private String healthStatus;

    private String characterDesc;

    private String adoptionRequirements;

    private String story;

    private String coverImage;

    private Long shelterId;

    /** AVAILABLE / PENDING / ADOPTED / MEDICAL / OFFLINE */
    private String status;

    private Integer viewCount;

    private Integer favoriteCount;

    private LocalDateTime publishTime;
}
