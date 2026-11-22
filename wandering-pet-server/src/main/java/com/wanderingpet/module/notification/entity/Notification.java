package com.wanderingpet.module.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 站内消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    private Long userId;

    /** USER / SHELTER / ADMIN */
    private String userType;

    private String title;

    private String content;

    /** APPLICATION / FOLLOW_UP / DONATION / SHELTER / REPORT / SYSTEM */
    private String bizType;

    private Long bizId;

    /** 0-未读 1-已读 */
    private Integer isRead;

    private LocalDateTime readTime;
}
