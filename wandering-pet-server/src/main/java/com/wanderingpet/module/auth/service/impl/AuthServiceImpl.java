package com.wanderingpet.module.auth.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.AesUtil;
import com.wanderingpet.common.util.JwtUtil;
import com.wanderingpet.common.util.MaskUtil;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.config.props.WxProperties;
import com.wanderingpet.module.auth.dto.*;
import com.wanderingpet.module.auth.mapper.AccountMergeMapper;
import com.wanderingpet.module.auth.service.AuthService;
import com.wanderingpet.module.auth.service.SmsService;
import com.wanderingpet.module.auth.sms.SmsScene;
import com.wanderingpet.module.auth.vo.CaptchaVO;
import com.wanderingpet.module.auth.vo.LoginVO;
import com.wanderingpet.module.auth.vo.WechatQrVO;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import com.wanderingpet.module.sys.entity.SysUser;
import com.wanderingpet.module.sys.mapper.SysUserMapper;
import com.wanderingpet.module.sys.service.PermissionService;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 认证服务实现
 *
 * <p>登录态双写：JWT（2h）+ Redis auth:token:{type}:{id}（TTL 对齐），
 * 改密/禁用/强制下线通过删除 Redis Key 实现。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserMapper userMapper;
    private final ShelterMapper shelterMapper;
    private final AccountMergeMapper mergeMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;
    private final WxProperties wxProperties;
    private final SmsService smsService;

    /** 微信扫码登录二维码有效期（秒） */
    private static final long WECHAT_QR_EXPIRE_SECONDS = 300;
    /** 演示模式：二维码展示多少毫秒后自动"扫码成功" */
    private static final long WECHAT_QR_DEMO_DELAY_MS = 3000;

    @Override
    public CaptchaVO captcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 8);
        String uuid = IdUtil.fastSimpleUUID();
        redisUtil.set(RedisKeyConstant.AUTH_CAPTCHA.formatted(uuid),
                captcha.getCode().toLowerCase(), Duration.ofMinutes(5));
        return new CaptchaVO(uuid, captcha.getImageBase64Data());
    }

    @Override
    public LoginVO adminLogin(AdminLoginDTO dto) {
        verifyCaptcha(dto.getCaptchaUuid(), dto.getCaptchaCode());
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (sysUser == null || !passwordEncoder.matches(dto.getPassword(), sysUser.getPassword())) {
            throw BizException.of(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) {
            throw BizException.of(ResultCode.ACCOUNT_DISABLED);
        }
        sysUser.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(sysUser);
        return buildSysUserLogin(sysUser);
    }

    @Override
    public LoginVO portalLogin(PortalLoginDTO dto) {
        verifyCaptcha(dto.getCaptchaUuid(), dto.getCaptchaCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getAccount()));
        if (user == null || user.getPassword() == null
                || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BizException.of(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw BizException.of(ResultCode.ACCOUNT_DISABLED);
        }
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        return buildUserLogin(user);
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        verifyCaptcha(dto.getCaptchaUuid(), dto.getCaptchaCode());
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw BizException.of(ResultCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setAvatar("");
        user.setGender(0);
        user.setCity("");
        user.setRealNameStatus(0);
        user.setStatus(1);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.insert(user);
        return buildUserLogin(user);
    }

    @Override
    public LoginVO wxLogin(WxLoginDTO dto) {
        String openid = resolveOpenid(dto.getCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                    ? "领养人" + openid.substring(Math.max(0, openid.length() - 4)) : dto.getNickname());
            user.setAvatar(dto.getAvatar() == null ? "" : dto.getAvatar());
            user.setGender(0);
            user.setCity("");
            user.setRealNameStatus(0);
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            if (user.getStatus() == null || user.getStatus() != 1) {
                throw BizException.of(ResultCode.ACCOUNT_DISABLED);
            }
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return buildUserLogin(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO bindWechat(WxLoginDTO dto) {
        LoginUser current = requireUser();
        String openid = resolveOpenid(dto.getCode());
        User user = userMapper.selectById(current.getUserId());
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        if (openid.equals(user.getOpenid())) {
            // 已绑定当前账号，幂等返回
            return buildUserLogin(user, false);
        }
        User owner = findByOpenid(openid);
        if (owner != null && !owner.getId().equals(user.getId())) {
            if (hasPasswordCredential(owner) || owner.getPhone() != null) {
                // 微信已挂在完整账号（有账号密码或手机号）上，不允许抢绑
                throw BizException.of(ResultCode.WECHAT_ALREADY_BOUND);
            }
            // 该微信是一个无用户名、无手机号的临时账号 → 并入当前账号，串联为一个账号；
            // 当前账号的微信身份以本次绑定为准（一个账号对应一个微信）
            mergeAccounts(user, owner, null, openid);
            return buildUserLogin(user, false);
        }
        // openid 空闲，直接绑定（一个账号对应一个微信，重复绑定即替换）
        user.setOpenid(openid);
        userMapper.updateById(user);
        return buildUserLogin(user, false);
    }

    @Override
    public LoginVO smsLogin(SmsLoginDTO dto) {
        String phone = dto.getPhone().trim();
        smsService.verifyCode(phone, dto.getCode(), SmsScene.LOGIN);

        User user = findByPhone(phone);
        if (user == null) {
            // 手机号首次登录即注册：无 username/password/openid，昵称按后四位生成
            user = new User();
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            user.setAvatar("");
            user.setPhone(phone);
            user.setGender(0);
            user.setCity("");
            user.setRealNameStatus(0);
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
            log.info("手机号验证码登录自动注册 userId={} phone={}", user.getId(), MaskUtil.maskPhone(phone));
        } else {
            if (user.getStatus() == null || user.getStatus() != 1) {
                throw BizException.of(ResultCode.ACCOUNT_DISABLED);
            }
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return buildUserLogin(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO smsBind(SmsBindDTO dto) {
        LoginUser current = requireUser();
        String phone = dto.getPhone().trim();
        smsService.verifyCode(phone, dto.getCode(), SmsScene.BIND);

        User user = userMapper.selectById(current.getUserId());
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        if (phone.equals(user.getPhone())) {
            // 已绑定该手机号，幂等返回
            return buildUserLogin(user, false);
        }
        User owner = findByPhone(phone);
        if (owner != null && !owner.getId().equals(user.getId())) {
            // 双方都是完整账号（都有用户名密码）：凭据无法互补，不自动合并
            if (hasPasswordCredential(user) && hasPasswordCredential(owner)) {
                throw BizException.of(ResultCode.PHONE_ALREADY_BOUND);
            }
            // 串联合并：有用户名密码的一方为主账号；都没有则手机号所属账号为主
            User primary = hasPasswordCredential(user) ? user : owner;
            User absorbed = primary.getId().equals(user.getId()) ? owner : user;
            mergeAccounts(primary, absorbed, phone, null);
            boolean switched = primary.getId().equals(owner.getId());
            // 主账号非当前登录账号时，当前账号已被注销，必须换发新 token
            return buildUserLogin(userMapper.selectById(primary.getId()), switched);
        }
        // 手机号空闲，直接绑定（TypeHandler 在 updateById 时自动加密写入）
        user.setPhone(phone);
        userMapper.updateById(user);
        return buildUserLogin(user, false);
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        verifyCaptcha(dto.getCaptchaUuid(), dto.getCaptchaCode());
        // Web 用户端账号找回
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getAccount()));
        if (user != null) {
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userMapper.updateById(user);
            redisUtil.delete(RedisKeyConstant.AUTH_TOKEN.formatted(AuthConstant.USER_TYPE_USER, user.getId()));
            return;
        }
        // 后台账号找回
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getAccount()));
        if (sysUser == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        sysUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserMapper.updateById(sysUser);
        redisUtil.delete(RedisKeyConstant.AUTH_TOKEN.formatted(sysUser.getRoleCode(), sysUser.getId()));
    }

    // ---------------------------------------------------------------- Web 微信扫码登录

    @Override
    public WechatQrVO createWechatQrLogin() {
        String uuid = IdUtil.fastSimpleUUID();
        boolean demo = !wxProperties.hasOpenPlatform();
        String qrContent;
        if (demo) {
            // 演示模式：二维码内容仅作展示，数秒后由 check 自动"扫码成功"
            qrContent = "wandering-pet://wechat-qr/" + uuid;
        } else {
            // 真实模式：微信开放平台网站应用授权链接（snsapi_login）
            String redirect = wxProperties.getOpenRedirectBase()
                    + "/api/v1/auth/wechat/qr-callback?uuid=" + uuid;
            String encode = java.net.URLEncoder.encode(redirect, java.nio.charset.StandardCharsets.UTF_8);
            qrContent = "https://open.weixin.qq.com/connect/qrconnect"
                    + "?appid=" + wxProperties.getOpenAppid()
                    + "&redirect_uri=" + encode
                    + "&response_type=code&scope=snsapi_login&state=" + uuid
                    + "#wechat_redirect";
        }
        saveQrState(uuid, "PENDING", null, null);
        // hutool QrCodeUtil（运行时依赖 zxing）
        String qrBase64 = "data:image/png;base64,"
                + Base64.encode(QrCodeUtil.generatePng(qrContent, new QrConfig(220, 220)));
        WechatQrVO vo = new WechatQrVO();
        vo.setUuid(uuid);
        vo.setQrBase64(qrBase64);
        vo.setDemo(demo);
        vo.setExpireSeconds((int) WECHAT_QR_EXPIRE_SECONDS);
        return vo;
    }

    @Override
    public Map<String, Object> checkWechatQrLogin(String uuid) {
        String raw = redisUtil.get(RedisKeyConstant.AUTH_WECHAT_QR.formatted(uuid));
        if (raw == null) {
            return Map.of("status", "EXPIRED");
        }
        JSONObject state = JSONUtil.parseObj(raw);
        if ("SUCCESS".equals(state.getStr("status"))) {
            // 一次性：成功状态取走后即删除，防止重复使用
            redisUtil.delete(RedisKeyConstant.AUTH_WECHAT_QR.formatted(uuid));
            LoginVO user = JSONUtil.toBean(state.getJSONObject("user").toString(), LoginVO.class);
            Map<String, Object> out = new HashMap<>();
            out.put("status", "SUCCESS");
            out.put("token", user.getToken());
            out.put("id", user.getId());
            out.put("nickname", user.getNickname());
            out.put("avatar", user.getAvatar());
            out.put("userType", user.getUserType());
            return out;
        }
        // 演示模式：二维码展示约 3 秒后自动模拟"扫码成功"（无开放平台资质时联调用）
        if (!wxProperties.hasOpenPlatform()) {
            long createAt = state.getLong("createAt", 0L);
            if (System.currentTimeMillis() - createAt >= WECHAT_QR_DEMO_DELAY_MS) {
                LoginVO vo = loginByOpenid("dev_webqr_" + uuid);
                markQrSuccess(uuid, vo);
                Map<String, Object> out = new HashMap<>();
                out.put("status", "SUCCESS");
                out.put("token", vo.getToken());
                out.put("id", vo.getId());
                out.put("nickname", vo.getNickname());
                out.put("avatar", vo.getAvatar());
                out.put("userType", vo.getUserType());
                return out;
            }
        }
        return Map.of("status", "PENDING");
    }

    @Override
    public void wechatQrCallback(String uuid, String code) {
        JSONObject state = readQrState(uuid);
        if (state == null || !"PENDING".equals(state.getStr("status"))) {
            throw BizException.of(ResultCode.BAD_REQUEST, "二维码已失效，请刷新后重新扫码");
        }
        // 微信开放平台：授权码换 access_token 与 openid
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token"
                + "?appid=" + wxProperties.getOpenAppid()
                + "&secret=" + wxProperties.getOpenSecret()
                + "&code=" + code + "&grant_type=authorization_code";
        Map<?, ?> result = new RestTemplate().getForObject(url, Map.class);
        String openid = result == null ? null : (String) result.get("openid");
        if (openid == null || openid.isBlank()) {
            throw BizException.of(ResultCode.THIRD_PARTY_ERROR, "微信授权失败，请重新扫码");
        }
        LoginVO vo = loginByOpenid(openid);
        markQrSuccess(uuid, vo);
    }

    /** 扫码成功：把登录结果绑定到二维码会话（供前端轮询取走），并记录扫码用户 */
    private void markQrSuccess(String uuid, LoginVO vo) {
        JSONObject state = readQrState(uuid);
        if (state == null) return;
        state.set("status", "SUCCESS");
        state.set("user", JSONUtil.parseObj(JSONUtil.toJsonStr(vo)));
        redisUtil.set(RedisKeyConstant.AUTH_WECHAT_QR.formatted(uuid), state.toString(),
                Duration.ofSeconds(120));
    }

    private JSONObject readQrState(String uuid) {
        String raw = redisUtil.get(RedisKeyConstant.AUTH_WECHAT_QR.formatted(uuid));
        return raw == null ? null : JSONUtil.parseObj(raw);
    }

    private void saveQrState(String uuid, String status, Long createAt, String token) {
        JSONObject state = new JSONObject();
        state.set("status", status);
        state.set("createAt", createAt == null ? System.currentTimeMillis() : createAt);
        if (token != null) state.set("token", token);
        redisUtil.set(RedisKeyConstant.AUTH_WECHAT_QR.formatted(uuid), state.toString(),
                Duration.ofSeconds(WECHAT_QR_EXPIRE_SECONDS + 60));
    }

    /** 按 openid 登录（存在即登录该账号——与小程序微信登录同一 openid 空间；不存在则创建微信用户） */
    private LoginVO loginByOpenid(String openid) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户" + openid.substring(Math.max(0, openid.length() - 4)));
            user.setAvatar("");
            user.setGender(0);
            user.setCity("");
            user.setRealNameStatus(0);
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            if (user.getStatus() == null || user.getStatus() != 1) {
                throw BizException.of(ResultCode.ACCOUNT_DISABLED);
            }
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return buildUserLogin(user);
    }

    @Override
    public void logout() {
        LoginUser current = UserContext.current();
        if (current != null) {
            redisUtil.delete(RedisKeyConstant.AUTH_TOKEN.formatted(current.getUserType(), current.getUserId()));
        }
    }

    @Override
    public LoginVO me() {
        LoginUser current = UserContext.current();
        if (current == null) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        if (AuthConstant.USER_TYPE_USER.equals(current.getUserType())) {
            User user = userMapper.selectById(current.getUserId());
            if (user == null) {
                throw BizException.of(ResultCode.USER_NOT_FOUND);
            }
            // 只读：不续签 token，避免覆盖 Redis 里客户端正在用的 token
            return buildUserLogin(user, false);
        }
        SysUser sysUser = sysUserMapper.selectById(current.getUserId());
        if (sysUser == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return buildSysUserLogin(sysUser, false);
    }

    // ---------------------------------------------------------------- private

    /** 校验图形验证码：一次性，验证后即删除 */
    private void verifyCaptcha(String uuid, String code) {
        String key = RedisKeyConstant.AUTH_CAPTCHA.formatted(uuid);
        String cached = redisUtil.get(key);
        if (cached == null) {
            throw BizException.of(ResultCode.CAPTCHA_ERROR);
        }
        redisUtil.delete(key);
        if (!cached.equalsIgnoreCase(code)) {
            throw BizException.of(ResultCode.CAPTCHA_ERROR);
        }
    }

    /** 解析 openid：开发模式返回 dev_ 前缀，生产模式调用微信 code2session */
    private String resolveOpenid(String code) {
        if (wxProperties.devMode()) {
            return "dev_" + code;
        }
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code"
                    .formatted(wxProperties.getAppid(), wxProperties.getSecret(), code);
            RestTemplate restTemplate = new RestTemplate();
            String body = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);
            Object openid = result.get("openid");
            Object errcode = result.get("errcode");
            if (openid == null || (errcode != null && !Objects.equals(0, ((Number) errcode).intValue()))) {
                log.warn("微信 code2session 失败: {}", body);
                throw BizException.of(ResultCode.THIRD_PARTY_ERROR, "微信登录失败，请重试");
            }
            return String.valueOf(openid);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            throw BizException.of(ResultCode.THIRD_PARTY_ERROR);
        }
    }

    private LoginVO buildUserLogin(User user) {
        return buildUserLogin(user, true);
    }

    /**
     * 构造 C 端登录信息
     *
     * @param renewToken true=登录场景，签发新 token 并写入 Redis；
     *                   false=只读场景（/auth/me），不签发、不覆盖 Redis，
     *                   否则客户端手里的旧 token 会立刻失效，导致下一次请求 401
     */
    private LoginVO buildUserLogin(User user, boolean renewToken) {
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUserType(AuthConstant.USER_TYPE_USER);
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone() == null ? null : MaskUtil.maskPhone(user.getPhone()));
        vo.setCity(user.getCity());
        vo.setRealNameStatus(user.getRealNameStatus());
        vo.setUsernameBound(hasPasswordCredential(user));
        vo.setWechatBound(user.getOpenid() != null && !user.getOpenid().isBlank());
        vo.setPoints(user.getPoints() == null ? 0 : user.getPoints());
        vo.setPermissions(new java.util.ArrayList<>());
        if (renewToken) {
            String token = jwtUtil.createToken(user.getId(), AuthConstant.USER_TYPE_USER);
            vo.setToken(token);
            redisUtil.set(RedisKeyConstant.AUTH_TOKEN.formatted(AuthConstant.USER_TYPE_USER, user.getId()),
                    token, Duration.ofSeconds(jwtUtil.expireSeconds()));
        }
        return vo;
    }

    private LoginVO buildSysUserLogin(SysUser sysUser) {
        return buildSysUserLogin(sysUser, true);
    }

    /** 同上：renewToken=false 时不续签、不覆盖 Redis（/auth/me 使用） */
    private LoginVO buildSysUserLogin(SysUser sysUser, boolean renewToken) {
        LoginVO vo = new LoginVO();
        vo.setId(sysUser.getId());
        vo.setUserType(sysUser.getRoleCode());
        vo.setNickname(sysUser.getRealName());
        vo.setAvatar(sysUser.getAvatar());
        vo.setPhone(sysUser.getPhone() == null ? null : MaskUtil.maskPhone(sysUser.getPhone()));
        vo.setShelterId(sysUser.getShelterId());
        if (sysUser.getShelterId() != null) {
            Shelter shelter = shelterMapper.selectById(sysUser.getShelterId());
            vo.setShelterName(shelter == null ? null : shelter.getName());
        }
        vo.setPermissions(new java.util.ArrayList<>(
                permissionService.loadPermissionCodes(sysUser.getRoleCode(), sysUser.getRoleCode(), sysUser.getId())));
        if (renewToken) {
            String token = jwtUtil.createToken(sysUser.getId(), sysUser.getRoleCode());
            vo.setToken(token);
            redisUtil.set(RedisKeyConstant.AUTH_TOKEN.formatted(sysUser.getRoleCode(), sysUser.getId()),
                    token, Duration.ofSeconds(jwtUtil.expireSeconds()));
        }
        return vo;
    }

    private LoginUser requireUser() {
        LoginUser current = UserContext.current();
        if (current == null || !current.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return current;
    }

    /**
     * 按手机号查用户：phone 为 AES 固定 IV 加密（确定性），可直接用密文等值查询
     */
    private User findByPhone(String phone) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .apply("phone = {0}", AesUtil.encrypt(phone)));
        return users.isEmpty() ? null : users.get(0);
    }

    private User findByOpenid(String openid) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid));
    }

    /** 是否拥有账号密码凭据（可用于 Web 账号密码登录） */
    private boolean hasPasswordCredential(User user) {
        return user.getUsername() != null && !user.getUsername().isBlank()
                && user.getPassword() != null && !user.getPassword().isBlank();
    }

    /**
     * 账号串联合并：把 absorbed 的凭据与业务数据全部并入 primary，然后注销 absorbed。
     *
     * <p>凭据互补转移：primary 缺用户名密码 / 实名信息时从 absorbed 补齐；
     * forceOpenid 不为空时强制写入（绑定微信场景，以本次验证的微信为准），
     * 否则 primary 缺 openid 时从 absorbed 补齐；phone 不为空时写到 primary（绑定手机号场景）。
     * 业务数据覆盖 10 张 user_id 关联表；收藏与聊天会话先去重再迁移；
     * 双方对同一宠物都有进行中申请时抛出业务异常（uk_pet_user_active 无法迁移）。</p>
     */
    private void mergeAccounts(User primary, User absorbed, String phone, String forceOpenid) {
        if (primary.getId().equals(absorbed.getId())) {
            return;
        }
        if (mergeMapper.countConflictingActiveApplications(absorbed.getId(), primary.getId()) > 0) {
            throw BizException.of(ResultCode.BAD_REQUEST,
                    "两个账号存在同一宠物的进行中申请，请先撤回或等待审核完成后再绑定");
        }
        // 迁移前去重，避免撞 favorite / chat_session 的唯一索引
        mergeMapper.dropDuplicateFavorites(absorbed.getId(), primary.getId());
        mergeMapper.dropDuplicateChatSessions(absorbed.getId(), primary.getId());
        // 业务数据整体迁移
        mergeMapper.migrateApplications(absorbed.getId(), primary.getId());
        mergeMapper.migrateRecords(absorbed.getId(), primary.getId());
        mergeMapper.migrateFavorites(absorbed.getId(), primary.getId());
        mergeMapper.migrateNotifications(absorbed.getId(), primary.getId());
        mergeMapper.migrateChatSessions(absorbed.getId(), primary.getId());
        mergeMapper.migrateComments(absorbed.getId(), primary.getId());
        mergeMapper.migrateDonations(absorbed.getId(), primary.getId());
        mergeMapper.migrateRescueReports(absorbed.getId(), primary.getId());
        mergeMapper.migrateVolunteers(absorbed.getId(), primary.getId());
        mergeMapper.migrateAddresses(absorbed.getId(), primary.getId());
        // 先注销从账号（清空全局唯一凭据 username/openid，避免与主账号撞唯一索引）+ 逻辑删除
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, absorbed.getId())
                .setSql("username = NULL, openid = NULL, status = 0"));
        userMapper.deleteById(absorbed);
        redisUtil.delete(RedisKeyConstant.AUTH_TOKEN.formatted(AuthConstant.USER_TYPE_USER, absorbed.getId()));
        // 再做凭据互补转移（openid/用户名密码/实名信息）与手机号绑定
        if (forceOpenid != null) {
            primary.setOpenid(forceOpenid);
        } else if (primary.getOpenid() == null || primary.getOpenid().isBlank()) {
            primary.setOpenid(absorbed.getOpenid());
        }
        if (!hasPasswordCredential(primary) && hasPasswordCredential(absorbed)) {
            primary.setUsername(absorbed.getUsername());
            primary.setPassword(absorbed.getPassword());
        }
        if ((primary.getRealNameStatus() == null || primary.getRealNameStatus() == 0)
                && absorbed.getRealNameStatus() != null && absorbed.getRealNameStatus() == 1) {
            primary.setRealName(absorbed.getRealName());
            primary.setIdCardNo(absorbed.getIdCardNo());
            primary.setRealNameStatus(1);
        }
        if (phone != null) {
            primary.setPhone(phone);
        }
        userMapper.updateById(primary);
        log.info("账号串联合并完成 absorbed={} -> primary={}", absorbed.getId(), primary.getId());
    }
}
