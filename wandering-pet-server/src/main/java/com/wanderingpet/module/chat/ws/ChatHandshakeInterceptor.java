package com.wanderingpet.module.chat.ws;

import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.util.JwtUtil;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.module.sys.entity.SysUser;
import com.wanderingpet.module.sys.mapper.SysUserMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 聊天 WS 握手鉴权：?token=xxx（JWT + Redis 双校验，与 HTTP 拦截器同规则）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserMapper userMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;
        if (request.getURI().getQuery() != null) {
            for (String pair : request.getURI().getQuery().split("&")) {
                if (pair.startsWith("token=")) {
                    token = pair.substring(6);
                    break;
                }
            }
        }
        if (token == null || token.isBlank()) {
            return false;
        }
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            return false;
        }
        Long userId = JwtUtil.getUserId(claims);
        String userType = JwtUtil.getUserType(claims);
        if (userId == null || !JwtUtil.validUserType(userType)) {
            return false;
        }
        // Redis 双校验：与 HTTP 拦截器一致，支持强制下线
        String redisToken = redisUtil.get(RedisKeyConstant.AUTH_TOKEN.formatted(userType, userId));
        if (!token.equals(redisToken)) {
            return false;
        }
        // 账号状态校验
        if (AuthConstant.USER_TYPE_USER.equals(userType)) {
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() == null || user.getStatus() != 1) {
                return false;
            }
        } else {
            SysUser sysUser = sysUserMapper.selectById(userId);
            if (sysUser == null || sysUser.getStatus() == null || sysUser.getStatus() != 1) {
                return false;
            }
            // 机构账号：聊天推送按 shelterId 寻址（与 chat_session 表一致），而非后台账号 id
            attributes.put("shelterId", sysUser.getShelterId());
        }
        attributes.put("userId", userId);
        attributes.put("userType", userType);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
