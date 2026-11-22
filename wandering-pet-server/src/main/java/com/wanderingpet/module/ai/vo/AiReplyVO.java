package com.wanderingpet.module.ai.vo;

import lombok.Data;

/**
 * AI 回复出参
 */
@Data
public class AiReplyVO {

    /** AI 回复内容 */
    private String reply;

    /** 是否演示模式（未配置 DeepSeek API Key 时的本地预置回复） */
    private Boolean demo;
}
