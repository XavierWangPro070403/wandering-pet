package com.wanderingpet.interceptor;

import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.result.R;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.JwtUtil;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.config.props.AuthProperties;
import com.wanderingpet.module.sys.entity.SysUser;
import com.wanderingpet.module.sys.mapper.SysUserMapper;
import com.wanderingpet.module.sys.service.PermissionService;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 鉴权拦截器：JWT + Redis 双校验
 *
 * <p>Token 仅存 userId + userType；Redis Key auth:token:{type}:{id} 存在且与 Token 一致才放行，
 * 支持强制下线（删除 Redis Key）。白名单路径仍会尝试解析 Token（可选登录场景，如游客浏览宠物列表）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final AuthProperties authProperties;
    private final SysUserMapper sysUserMapper;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        LoginUser loginUser = resolveLoginUser(request);
        if (loginUser != null) {
            UserContext.set(loginUser);
        }

        String path = request.getRequestURI();
        if (isIgnored(path)) {
            return true;
        }

        if (loginUser == null) {
            writeJson(response, R.fail(ResultCode.UNAUTHORIZED));
            return false;
        }

        // 路径级粗粒度角色控制：/api/v1/admin/** 仅管理员、/api/v1/shelter/** 仅机构
        if (path.startsWith("/api/v1/admin/") && !loginUser.isAdmin()) {
            writeJson(response, R.fail(ResultCode.FORBIDDEN));
            return false;
        }
        if (path.startsWith("/api/v1/shelter/") && !loginUser.isShelter()) {
            writeJson(response, R.fail(ResultCode.FORBIDDEN));
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 解析 Token 并构建登录上下文；无效/被强制下线返回 null */
    private LoginUser resolveLoginUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            return null;
        }
        Long userId = JwtUtil.getUserId(claims);
        String userType = JwtUtil.getUserType(claims);
        if (userId == null || !JwtUtil.validUserType(userType)) {
            return null;
        }
        // Redis 双校验：不一致视为已失效（改密/强制下线场景）
        String redisToken = redisUtil.get(RedisKeyConstant.AUTH_TOKEN.formatted(userType, userId));
        if (!token.equals(redisToken)) {
            return null;
        }
        return buildLoginUser(userId, userType);
    }

    private LoginUser buildLoginUser(Long userId, String userType) {
        if (AuthConstant.USER_TYPE_USER.equals(userType)) {
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() == null || user.getStatus() != 1) {
                return null;
            }
            // 普通用户授予文件上传权限（头像/上报图片/聊天图片等本人内容上传）
            return new LoginUser(userId, userType, user.getUsername(), null,
                    new HashSet<>(List.of("file:upload")));
        }
        // ADMIN / SHELTER
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null || sysUser.getStatus() == null || sysUser.getStatus() != 1) {
            return null;
        }
        return new LoginUser(userId, userType, sysUser.getUsername(), sysUser.getShelterId(),
                permissionService.loadPermissionCodes(sysUser.getRoleCode(), userType, userId));
    }

    private boolean isIgnored(String path) {
        for (String pattern : authProperties.getIgnoreUrls()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private void writeJson(HttpServletResponse response, R<?> body) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
