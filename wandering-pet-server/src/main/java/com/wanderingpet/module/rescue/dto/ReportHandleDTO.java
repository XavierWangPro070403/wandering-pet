package com.wanderingpet.module.rescue.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 线索分派 / 处理 / 关闭入参
 */
@Data
public class ReportHandleDTO {

    /** 分派对象：救助站账号 sys_user.id */
    @NotNull(message = "请选择处理方")
    private Long handlerId;

    @Size(max = 255)
    private String remark;
}
