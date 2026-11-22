package com.wanderingpet.module.ai.service;

import com.wanderingpet.module.ai.dto.AiChatDTO;
import com.wanderingpet.module.ai.vo.AiReplyVO;

/**
 * AI 宠物顾问（DeepSeek）
 */
public interface AiChatService {

    /** 回答宠物相关问题；未配置 API Key 时进入演示模式返回本地预置回复 */
    AiReplyVO chat(AiChatDTO dto);
}
