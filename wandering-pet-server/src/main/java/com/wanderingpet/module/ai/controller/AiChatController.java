package com.wanderingpet.module.ai.controller;

import com.wanderingpet.common.annotation.RateLimit;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.ai.dto.AiChatDTO;
import com.wanderingpet.module.ai.service.AiChatService;
import com.wanderingpet.module.ai.vo.AiReplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 宠物顾问接口（DeepSeek；未配置 Key 时为演示回复）
 */
@Tag(name = "AI 宠物顾问", description = "接入 DeepSeek 的宠物咨询问答")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "AI 咨询（需登录；带最近几轮 history 可维持上下文）")
    @RateLimit(maxRequests = 10, windowSeconds = 60)
    @PostMapping("/chat")
    public R<AiReplyVO> chat(@Valid @RequestBody AiChatDTO dto) {
        return R.ok(aiChatService.chat(dto));
    }
}
