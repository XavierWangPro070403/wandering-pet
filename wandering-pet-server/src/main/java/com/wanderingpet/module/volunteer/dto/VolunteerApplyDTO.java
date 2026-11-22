package com.wanderingpet.module.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 志愿者报名入参
 */
@Data
public class VolunteerApplyDTO {

    @NotNull(message = "请选择救助站")
    private Long shelterId;

    @NotBlank(message = "请输入真实姓名")
    @Size(max = 32)
    private String realName;

    @NotBlank(message = "请输入联系电话")
    private String phone;

    @Size(max = 255)
    private String skills;

    @Size(max = 255)
    private String availableTime;
}
