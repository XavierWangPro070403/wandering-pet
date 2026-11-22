package com.wanderingpet.module.chat.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天在线注册表 + 实时推送
 *
 * <p>key 为 userType:userId；发送走 HTTP 接口（单一持久化路径），
 * 接收方在线则通过 WebSocket 实时推送，离线则靠未读数 + 拉取兜底。</p>
 */
@Slf4j
@Component
public class ChatPushService {

    private final Map<String, WebSocketSession> online = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ChatPushService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static String key(String userType, Long userId) {
        return userType + ":" + userId;
    }

    public void connect(String userType, Long userId, WebSocketSession session) {
        WebSocketSession old = online.put(key(userType, userId), session);
        if (old != null && old.isOpen()) {
            // 同一账号多端登录：顶掉旧连接（新连接为准）
            try {
                old.close();
            } catch (IOException ignored) {
            }
        }
        log.info("聊天 WS 上线 {}（当前在线 {}）", key(userType, userId), online.size());
    }

    public void disconnect(String userType, Long userId, WebSocketSession session) {
        online.remove(key(userType, userId), session);
        log.info("聊天 WS 下线 {}（当前在线 {}）", key(userType, userId), online.size());
    }

    public boolean isOnline(String userType, Long userId) {
        WebSocketSession s = online.get(key(userType, userId));
        return s != null && s.isOpen();
    }

    /** 实时推送（离线静默跳过，由未读数 + 拉取兜底） */
    public void pushTo(String userType, Long userId, Object payload) {
        WebSocketSession session = online.get(key(userType, userId));
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            log.warn("聊天 WS 推送失败 {}: {}", key(userType, userId), e.getMessage());
        }
    }

    /** 消息推送负载 */
    public Map<String, Object> chatMessagePayload(Object messageVO) {
        return Map.of("type", "CHAT_MESSAGE", "message", messageVO);
    }

    /** 撤回推送负载 */
    public Map<String, Object> chatRecallPayload(String sessionId, String messageId) {
        return Map.of("type", "CHAT_RECALL", "sessionId", sessionId, "messageId", messageId);
    }
}
