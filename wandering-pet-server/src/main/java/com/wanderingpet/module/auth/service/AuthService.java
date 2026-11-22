package com.wanderingpet.module.auth.service;

import com.wanderingpet.module.auth.dto.*;
import com.wanderingpet.module.auth.vo.CaptchaVO;
import com.wanderingpet.module.auth.vo.WechatQrVO;
import com.wanderingpet.module.auth.vo.LoginVO;

/**
 * 认证服务：后台登录 / 门户登录注册 / 微信登录 / 验证码 / 找回密码
 */
public interface AuthService {

    /** 生成图形验证码（Redis 存储，5 分钟有效） */
    CaptchaVO captcha();

    /** 后台登录（管理员 / 救助站账号） */
    LoginVO adminLogin(AdminLoginDTO dto);

    /** Web 用户端账号密码登录 */
    LoginVO portalLogin(PortalLoginDTO dto);

    /** Web 用户端注册 */
    LoginVO register(RegisterDTO dto);

    /** 小程序微信登录（开发模式下任意 code 可登录） */
    LoginVO wxLogin(WxLoginDTO dto);

    /** 手机号 + 短信验证码登录（不存在则自动注册，Web 与小程序共用） */
    LoginVO smsLogin(SmsLoginDTO dto);

    /**
     * 已登录账号绑定微信（当前 USER 登录态 + wx.login 的 code）。
     * 若该微信属于一个无用户名、无手机号的临时微信账号，则将其并入当前账号（串联）；
     * 若属于完整账号则拒绝。
     *
     * @return 登录信息（绑定成功后的最新用户态；发生合并时 openid 转移到当前账号）
     */
    LoginVO bindWechat(WxLoginDTO dto);

    /**
     * 已登录账号绑定 / 换绑手机号（需短信验证码）。
     * 手机号已被其他账号绑定时自动执行账号串联合并：凭据互补转移、业务数据迁移，
     * 从账号下线并删除。
     *
     * @return 登录信息；发生合并且主账号非当前账号时会换发新 token，客户端必须更新本地登录态
     */
    LoginVO smsBind(SmsBindDTO dto);

    /** 找回密码（账号 + 新密码 + 验证码） */
    void resetPassword(ResetPasswordDTO dto);

    /**
     * Web 端微信扫码登录-创建二维码：
     * 已配置开放平台（open-appid/open-secret/open-redirect-base）时返回微信授权链接二维码；
     * 未配置时进入演示模式（二维码展示数秒后自动"扫码成功"，便于无资质环境联调）。
     */
    WechatQrVO createWechatQrLogin();

    /**
     * Web 端微信扫码登录-轮询状态：
     * PENDING-等待扫码 / SUCCESS-登录成功（data 含 token）/ EXPIRED-二维码过期。
     * 演示模式下创建约 3 秒后自动成功。
     */
    java.util.Map<String, Object> checkWechatQrLogin(String uuid);

    /** 微信开放平台授权回调（真实模式）：code 换 openid 并绑定登录结果到 uuid */
    void wechatQrCallback(String uuid, String code);

    /** 退出登录：删除 Redis 登录态（强制下线） */
    void logout();

    /** 当前登录人信息（按 userType 构造） */
    LoginVO me();
}
