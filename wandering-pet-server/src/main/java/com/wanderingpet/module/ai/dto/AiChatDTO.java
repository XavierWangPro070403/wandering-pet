package com.wanderingpet.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * AI 咨询入参：message 为本次提问；history 为最近几轮对话（可选，维持上下文）
 */
@Data
public class AiChatDTO {

    @NotBlank(message = "请输入你的问题")
    @Size(max = 1000, message = "问题太长啦，请精简到 1000 字以内")
    private String message;

    /** 最近对话轮次（最多保留 6 条），形如 {role: user|assistant, content: ...} */
    private List<HistoryItem> history;

    @Data
    public static class HistoryItem {
        private String role;
        private String content;
    }
}
