package com.wanderingpet.module.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 消息批量推送入参（管理员）
 */
@Data
public class BroadcastDTO {

    /** 接收方类型 USER / SHELTER / ADMIN */
    @NotBlank(message = "请选择接收方类型")
    private String userType;

    @NotBlank(message = "请输入标题")
    @Size(max = 64)
    private String title;

    @NotBlank(message = "请输入内容")
    @Size(max = 1000)
    private String content;
}
