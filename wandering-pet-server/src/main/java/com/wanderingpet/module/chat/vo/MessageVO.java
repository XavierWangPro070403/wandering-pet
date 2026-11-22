package com.wanderingpet.module.chat.vo;

import lombok.Data;

/**
 * 聊天消息出参
 */
@Data
public class MessageVO {

    private String id;

    private String sessionId;

    private Long senderId;

    private String senderType;

    private String contentType;

    private String content;

    private String objectUrl;

    private Integer isRead;

    /** 是否已撤回（撤回后前端显示"对方撤回了一条消息"，不再展示原内容） */
    private Boolean recalled;

    private String createTime;
}
