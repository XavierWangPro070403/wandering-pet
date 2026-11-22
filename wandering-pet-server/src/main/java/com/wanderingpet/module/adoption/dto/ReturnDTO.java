package com.wanderingpet.module.adoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 退养入参
 */
@Data
public class ReturnDTO {

    @NotBlank(message = "请填写退养原因")
    @Size(max = 255, message = "退养原因最长 255 字")
    private String reason;
}
