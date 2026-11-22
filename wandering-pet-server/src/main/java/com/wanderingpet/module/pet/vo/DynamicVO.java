package com.wanderingpet.module.pet.vo;

import lombok.Data;

/**
 * 宠物动态出参（MongoDB pet_dynamic）
 */
@Data
public class DynamicVO {

    private String id;

    private Long petId;

    private String content;

    private java.util.List<String> images;

    private Integer likeCount;

    private String createTime;
}
