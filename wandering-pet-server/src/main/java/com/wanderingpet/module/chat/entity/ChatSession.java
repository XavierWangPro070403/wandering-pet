package com.wanderingpet.module.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天会话（消息正文存 MongoDB chat_message，sessionId 即本表 id）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_session")
public class ChatSession extends BaseEntity {

    private Long userId;

    private Long shelterId;

    private String lastMessage;

    private LocalDateTime lastTime;

    private Integer userUnread;

    private Integer shelterUnread;
}
