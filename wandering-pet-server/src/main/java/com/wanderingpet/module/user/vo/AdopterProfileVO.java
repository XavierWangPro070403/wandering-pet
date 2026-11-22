package com.wanderingpet.module.user.vo;

import lombok.Data;

/**
 * 领养资料出参（本人视角）：申请领养时自动填充，多宠物申请免重复填写
 */
@Data
public class AdopterProfileVO {

    /** 默认申请人姓名：优先实名姓名，其次领养资料姓名，最后昵称 */
    private String adopterName;

    /** 完整手机号（仅本人可见，申请表单直接填充） */
    private String phone;

    private String city;

    private Integer adopterAge;

    private String occupation;

    /** OWN-自有 RENT-租赁 */
    private String housingType;

    private Integer hasPetExperience;

    private Integer familyAgree;

    private Integer hasChildren;

    private Integer hasAllergy;

    /** 是否已补全（关键项：姓名/手机号/城市/年龄/住房类型均不为空） */
    private Boolean complete;
}
