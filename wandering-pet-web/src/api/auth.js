import request from '@/utils/request';

/** 图形验证码 */
export const captcha = () => request.get('/auth/captcha');

/** 后台登录 */
export const login = (data) => request.post('/auth/admin-login', data);

/** 门户登录 */
export const portalLogin = (data) => request.post('/auth/portal-login', data);

/** 门户注册 */
export const register = (data) => request.post('/auth/register', data);

/** 找回密码 */
export const resetPassword = (data) => request.post('/auth/reset-password', data);

/** 发送短信验证码（scene: login | bind） */
export const sendSmsCode = (data) => request.post('/auth/sms/send', data);

/** 手机号 + 短信验证码登录（未注册自动创建账号） */
export const smsLogin = (data) => request.post('/auth/sms-login', data);

/** 已登录账号绑定手机号（需短信验证码） */
export const smsBind = (data) => request.post('/auth/sms-bind', data);

/** Web 微信扫码登录-创建二维码（demo: true 时为演示模式） */
export const createWechatQr = () => request.post('/auth/wechat/qr/create');

/** Web 微信扫码登录-轮询扫码状态 */
export const checkWechatQr = (uuid) => request.get('/auth/wechat/qr/check', { params: { uuid } });

/** 退出登录 */
export const logout = () => request.post('/auth/logout');

/** 当前登录人信息 */
export const getMe = () => request.get('/auth/me');
