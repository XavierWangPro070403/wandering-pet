package com.wanderingpet.module.auth.controller;

import com.wanderingpet.common.annotation.RateLimit;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.auth.dto.*;
import com.wanderingpet.module.auth.service.AuthService;
import com.wanderingpet.module.auth.service.SmsService;
import com.wanderingpet.module.auth.vo.CaptchaVO;
import com.wanderingpet.module.auth.vo.LoginVO;
import com.wanderingpet.module.auth.vo.WechatQrVO;
import com.wanderingpet.module.auth.vo.SmsSendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口：登录/注册/验证码/微信登录/找回密码
 */
@Tag(name = "认证", description = "后台登录、门户登录注册、小程序微信登录、验证码")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final SmsService smsService;

    @Operation(summary = "图形验证码")
    @RateLimit(maxRequests = 10, windowSeconds = 60)
    @GetMapping("/captcha")
    public R<CaptchaVO> captcha() {
        return R.ok(authService.captcha());
    }

    @Operation(summary = "后台登录（管理员/救助站）")
    @PostMapping("/admin-login")
    public R<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDTO dto) {
        return R.ok(authService.adminLogin(dto));
    }

    @Operation(summary = "Web 用户端登录")
    @PostMapping("/portal-login")
    public R<LoginVO> portalLogin(@Valid @RequestBody PortalLoginDTO dto) {
        return R.ok(authService.portalLogin(dto));
    }

    @Operation(summary = "Web 用户端注册")
    @PostMapping("/register")
    public R<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return R.ok(authService.register(dto));
    }

    @Operation(summary = "小程序微信登录")
    @PostMapping("/wx-login")
    public R<LoginVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        return R.ok(authService.wxLogin(dto));
    }

    @Operation(summary = "找回密码")
    @PostMapping("/reset-password")
    public R<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return R.ok();
    }

    @Operation(summary = "发送短信验证码（手机号登录 / 绑定手机号）")
    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @PostMapping("/sms/send")
    public R<SmsSendVO> sendSmsCode(@Valid @RequestBody SmsSendDTO dto) {
        return R.ok(smsService.sendCode(dto));
    }

    @Operation(summary = "手机号 + 短信验证码登录（不存在则自动注册）")
    @RateLimit(maxRequests = 10, windowSeconds = 60)
    @PostMapping("/sms-login")
    public R<LoginVO> smsLogin(@Valid @RequestBody SmsLoginDTO dto) {
        return R.ok(authService.smsLogin(dto));
    }

    @Operation(summary = "绑定/换绑手机号（需短信验证码；手机号属于其他账号时自动串联合并并可能换发 token）")
    @PostMapping("/sms-bind")
    public R<LoginVO> smsBind(@Valid @RequestBody SmsBindDTO dto) {
        return R.ok(authService.smsBind(dto));
    }

    @Operation(summary = "绑定微信（USER 登录态；临时微信账号冲突时自动串联合并）")
    @PostMapping("/bind-wechat")
    public R<LoginVO> bindWechat(@Valid @RequestBody WxLoginDTO dto) {
        return R.ok(authService.bindWechat(dto));
    }

    // ---------- Web 端微信扫码登录 ----------

    @Operation(summary = "微信扫码登录-创建二维码（未配置开放平台时为演示模式）")
    @PostMapping("/wechat/qr/create")
    public R<WechatQrVO> createWechatQr() {
        return R.ok(authService.createWechatQrLogin());
    }

    @Operation(summary = "微信扫码登录-轮询状态（PENDING/SUCCESS/EXPIRED，SUCCESS 含 token）")
    @GetMapping("/wechat/qr/check")
    public R<Map<String, Object>> checkWechatQr(@RequestParam String uuid) {
        return R.ok(authService.checkWechatQrLogin(uuid));
    }

    @Operation(summary = "微信开放平台授权回调（真实模式：code 换 openid 绑定登录）")
    @GetMapping("/wechat/qr-callback")
    public void wechatQrCallback(@RequestParam String uuid, @RequestParam String code,
                                 HttpServletResponse response) throws Exception {
        authService.wechatQrCallback(uuid, code);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("<!DOCTYPE html><html><body style=\"text-align:center;padding-top:80px;"
                + "font-family:sans-serif\"><h2>✅ 登录成功</h2>"
                + "<p>请返回网页，登录将自动完成。</p></body></html>");
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @Operation(summary = "当前登录人信息")
    @GetMapping("/me")
    public R<LoginVO> me() {
        return R.ok(authService.me());
    }
}
