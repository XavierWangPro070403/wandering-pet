package com.wanderingpet.module.chat.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 聊天消息（MongoDB chat_message 集合）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_message")
@CompoundIndex(name = "idx_session_create", def = "{'sessionId': 1, 'createTime': -1}")
public class ChatMessage {

    @Id
    private String id;

    /** 会话 ID（MySQL chat_session.id 的字符串形式） */
    private String sessionId;

    private Long senderId;

    /** USER / SHELTER */
    private String senderType;

    private Long receiverId;

    /** TEXT / IMAGE */
    private String contentType;

    private String content;

    /** 图片消息的对象 URL */
    private String objectUrl;

    /** 0-未读 1-已读 */
    private Integer isRead;

    /** 0-正常 1-已撤回（仅发送者 2 分钟内可撤回） */
    private Integer recalled;

    private LocalDateTime recalledAt;

    /** 0-正常 1-该端已删除（删除仅对自己生效，像微信「删除」） */
    private Integer deletedByUser;

    private Integer deletedByShelter;

    private LocalDateTime createTime;
}
