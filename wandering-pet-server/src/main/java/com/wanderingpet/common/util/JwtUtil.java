package com.wanderingpet.common.util;

import com.wanderingpet.common.constant.AuthConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具（jjwt 0.12.x）：Token 仅存 userId + userType，权限数据走 Redis
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${wandering.jwt.secret}")
    private String secret;

    @Value("${wandering.jwt.expire-minutes}")
    private long expireMinutes;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     *
     * @param userId   用户 ID
     * @param userType 用户类型（ADMIN/SHELTER/USER）
     * @return 签名后的 JWT 字符串
     */
    public String createToken(Long userId, String userType) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireMinutes * 60 * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("userType", userType)
                .issuedAt(now)
                .expiration(expire)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token JWT 字符串
     * @return Claims；无效/过期返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /** Token 剩余有效期（秒），用于 Redis 登录态 TTL 对齐 */
    public long expireSeconds() {
        return expireMinutes * 60;
    }

    /** 从 Claims 提取 userId */
    public static Long getUserId(Claims claims) {
        Object v = claims.get("userId");
        if (v instanceof Number n) {
            return n.longValue();
        }
        return Long.valueOf(String.valueOf(v));
    }

    /** 从 Claims 提取 userType */
    public static String getUserType(Claims claims) {
        return String.valueOf(claims.get("userType"));
    }

    /** 校验 userType 是否合法 */
    public static boolean validUserType(String userType) {
        return AuthConstant.USER_TYPE_ADMIN.equals(userType)
                || AuthConstant.USER_TYPE_SHELTER.equals(userType)
                || AuthConstant.USER_TYPE_USER.equals(userType);
    }
}
