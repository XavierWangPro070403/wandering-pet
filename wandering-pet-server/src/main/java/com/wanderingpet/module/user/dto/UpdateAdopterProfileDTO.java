package com.wanderingpet.module.user.dto;

import lombok.Data;

/**
 * 领养资料补全入参（手机号不在此修改，走绑定/换绑手机号流程）
 */
@Data
public class UpdateAdopterProfileDTO {

    private String adopterName;

    private String city;

    private Integer adopterAge;

    private String occupation;

    /** OWN-自有 RENT-租赁 */
    private String housingType;

    private Integer hasPetExperience;

    private Integer familyAgree;

    private Integer hasChildren;

    private Integer hasAllergy;
}
