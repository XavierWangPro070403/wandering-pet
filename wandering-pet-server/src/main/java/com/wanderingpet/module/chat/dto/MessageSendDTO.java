package com.wanderingpet.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送聊天消息入参
 */
@Data
public class MessageSendDTO {

    /** TEXT / IMAGE */
    @NotBlank(message = "请选择消息类型")
    private String contentType;

    /** 文本内容或图片说明 */
    @Size(max = 1000)
    private String content;

    /** 图片消息的 URL（contentType=IMAGE 时必传） */
    private String objectUrl;
}
