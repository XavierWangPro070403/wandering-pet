package com.wanderingpet.module.chat.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 聊天 WebSocket 端点：/ws/chat?token=xxx
 * （uni.connectSocket 与浏览器原生 WebSocket 直连，无需 STOMP）
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 同时注册 /ws 别名，兼容连接 URL 写为 /ws 的客户端，避免 404 落进静态资源处理器
        registry.addHandler(chatWebSocketHandler, "/ws/chat", "/ws")
                .addInterceptors(chatHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
