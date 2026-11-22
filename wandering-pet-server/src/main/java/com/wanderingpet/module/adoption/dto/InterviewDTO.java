package com.wanderingpet.module.adoption.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面谈排期入参
 */
@Data
public class InterviewDTO {

    /** 面谈时间 yyyy-MM-dd HH:mm:ss */
    private String interviewTime;

    @Size(max = 255, message = "备注最长 255 字")
    private String remark;
}
