package com.wanderingpet.module.notification.vo;

import lombok.Data;

/**
 * 站内消息出参
 */
@Data
public class NotificationVO {

    private Long id;

    private String title;

    private String content;

    private String bizType;

    private Long bizId;

    private Integer isRead;

    private String createTime;
}
