package com.wanderingpet.module.chat.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 聊天 WS 连接处理器：仅维护在线注册与心跳。
 * 消息发送走 HTTP 接口（校验/持久化/未读数都在那里），本通道只负责实时推送。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatPushService pushService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userType = (String) session.getAttributes().get("userType");
        Long userId = (Long) session.getAttributes().get("userId");
        Long shelterId = (Long) session.getAttributes().get("shelterId");
        // 机构账号按 shelterId 寻址（与 chat_session / 推送目标一致）
        Long keyId = shelterId != null ? shelterId : userId;
        pushService.connect(userType, keyId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 心跳保活：{"type":"PING"} → {"type":"PONG"}；其余上行一律忽略
        String body = message.getPayload();
        if (body.contains("PING")) {
            try {
                session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userType = (String) session.getAttributes().get("userType");
        Long userId = (Long) session.getAttributes().get("userId");
        Long shelterId = (Long) session.getAttributes().get("shelterId");
        if (userType != null && userId != null) {
            Long keyId = shelterId != null ? shelterId : userId;
            pushService.disconnect(userType, keyId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        try {
            session.close();
        } catch (Exception ignored) {
        }
    }
}
