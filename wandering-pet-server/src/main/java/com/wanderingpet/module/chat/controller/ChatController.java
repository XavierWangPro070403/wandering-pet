package com.wanderingpet.module.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.chat.service.ChatService;
import com.wanderingpet.module.chat.vo.MessageVO;
import com.wanderingpet.module.chat.vo.SessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 在线咨询接口（用户与机构）
 */
@Tag(name = "在线咨询", description = "会话、消息、未读数")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "创建/获取与机构的会话")
    @PostMapping("/sessions")
    public R<SessionVO> getOrCreate(@RequestBody Map<String, Long> body) {
        Long shelterId = body.get("shelterId");
        return R.ok(chatService.getOrCreateSession(shelterId));
    }

    @Operation(summary = "我的会话列表")
    @GetMapping("/sessions")
    public R<List<SessionVO>> sessions() {
        return R.ok(chatService.mySessions());
    }

    @Operation(summary = "会话消息分页（进入即已读）")
    @GetMapping("/sessions/{id}/messages")
    public R<Page<MessageVO>> messages(@PathVariable Long id,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "20") Integer pageSize) {
        return R.ok(chatService.pageMessages(id, pageNum, Math.min(pageSize, 100)));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/sessions/{id}/messages")
    public R<Void> send(@PathVariable Long id, @RequestBody Map<String, String> body) {
        chatService.sendMessage(id, body.getOrDefault("contentType", "TEXT"),
                body.get("content"), body.get("objectUrl"));
        return R.ok();
    }

    @Operation(summary = "标记会话已读")
    @PostMapping("/sessions/{id}/read")
    public R<Void> read(@PathVariable Long id) {
        chatService.markRead(id);
        return R.ok();
    }

    @Operation(summary = "撤回消息（仅发送者，2 分钟内）")
    @PostMapping("/messages/{id}/recall")
    public R<Void> recall(@PathVariable String id) {
        chatService.recallMessage(id);
        return R.ok();
    }

    @Operation(summary = "删除消息（仅对自己隐藏）")
    @DeleteMapping("/messages/{id}")
    public R<Void> delete(@PathVariable String id) {
        chatService.deleteMessage(id);
        return R.ok();
    }

    @Operation(summary = "未读总数")
    @GetMapping("/unread-total")
    public R<Long> unreadTotal() {
        return R.ok(chatService.unreadTotal());
    }
}
