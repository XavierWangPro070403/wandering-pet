package com.wanderingpet.module.adoption.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 家访排期入参
 */
@Data
public class HomeVisitDTO {

    private String homeVisitTime;

    /** 家访情况记录（通过时填写） */
    @Size(max = 255, message = "记录最长 255 字")
    private String remark;
}
