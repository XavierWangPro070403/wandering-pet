package com.wanderingpet.module.shelter.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 机构信息更新入参（SHELTER 自己维护）
 */
@Data
public class ShelterUpdateDTO {

    private String logo;

    @Size(max = 1000, message = "简介最长 1000 字")
    private String intro;

    private String contactName;

    private String contactPhone;

    private String address;
}
