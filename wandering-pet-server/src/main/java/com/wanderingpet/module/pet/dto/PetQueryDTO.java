package com.wanderingpet.module.pet.dto;

import lombok.Data;

/**
 * 宠物列表查询入参（门户 / 机构端 / 管理端共用）
 */
@Data
public class PetQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long categoryId;

    private Long breedId;

    /** MALE / FEMALE / UNKNOWN */
    private String gender;

    /** SMALL / MEDIUM / LARGE */
    private String size;

    /** 最小月龄 */
    private Integer ageMin;

    /** 最大月龄 */
    private Integer ageMax;

    /** 城市（来自救助站） */
    private String city;

    private Long shelterId;

    /** 宠物状态（机构/管理端筛选；门户默认 AVAILABLE） */
    private String status;

    /** 关键词：按名称/性格描述全文检索 */
    private String keyword;

    /** 排序字段：publish_time（默认）/ view_count / favorite_count / age_month */
    private String orderBy;

    /** asc / desc（默认 desc） */
    private String orderDirection;
}
