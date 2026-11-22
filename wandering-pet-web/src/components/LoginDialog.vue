<template>
  <div v-if="visible" class="login-mask" @click.self="close">
    <div class="login-card" role="dialog" aria-modal="true" aria-label="登录或注册">
      <button class="login-card__close" aria-label="关闭" @click="close">
        <i class="el-icon-close" aria-hidden="true"></i>
      </button>

      <h2 class="login-card__title">{{ title }}</h2>
      <p class="login-card__desc">{{ desc }}</p>

      <!-- 登录方式：两个选项左右各半、整行铺满；选中态用主色浅底 + 爪印水印 -->
      <div v-if="mode === 'login'" class="login-switch" role="tablist" aria-label="登录方式">
        <button
          class="login-switch__item"
          role="tab"
          :aria-selected="type === 'password' ? 'true' : 'false'"
          :class="{ active: type === 'password' }"
          @click="switchType('password')">
          <svg class="login-switch__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
               stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
            <!-- 猫头：账密登录 -->
            <path d="M7.1 8.4 5.9 3.9l4.2 2" />
            <path d="M16.9 8.4 18.1 3.9l-4.2 2" />
            <circle cx="12" cy="13.6" r="6.7" />
            <circle cx="9.6" cy="12.7" r="0.75" fill="currentColor" stroke="none" />
            <circle cx="14.4" cy="12.7" r="0.75" fill="currentColor" stroke="none" />
            <path d="M12 14.7v1" />
            <path d="M10.8 16.6c.5.6 1.9.6 2.4 0" />
          </svg>
          <span class="login-switch__label">账密登录</span>
          <svg class="login-switch__paw" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
            <ellipse cx="12" cy="15.6" rx="4.4" ry="3.7" fill="currentColor" />
            <ellipse cx="6.6" cy="10" rx="1.8" ry="2.2" fill="currentColor" transform="rotate(-20 6.6 10)" />
            <ellipse cx="10.9" cy="7.6" rx="1.7" ry="2.3" fill="currentColor" />
            <ellipse cx="15.4" cy="7.8" rx="1.7" ry="2.3" fill="currentColor" />
            <ellipse cx="19.1" cy="10.4" rx="1.8" ry="2.2" fill="currentColor" transform="rotate(20 19.1 10.4)" />
          </svg>
        </button>
        <button
          class="login-switch__item"
          role="tab"
          :aria-selected="type === 'sms' ? 'true' : 'false'"
          :class="{ active: type === 'sms' }"
          @click="switchType('sms')">
          <svg class="login-switch__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
               stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
            <!-- 狗头：手机号登录 -->
            <ellipse cx="5.4" cy="14.1" rx="2.1" ry="4.9" transform="rotate(-16 5.4 14.1)" />
            <ellipse cx="18.6" cy="14.1" rx="2.1" ry="4.9" transform="rotate(16 18.6 14.1)" />
            <circle cx="12" cy="12.6" r="6.1" />
            <circle cx="9.9" cy="11.7" r="0.75" fill="currentColor" stroke="none" />
            <circle cx="14.1" cy="11.7" r="0.75" fill="currentColor" stroke="none" />
            <ellipse cx="12" cy="14.6" rx="1.05" ry="0.8" fill="currentColor" stroke="none" />
          </svg>
          <span class="login-switch__label">手机号登录</span>
          <svg class="login-switch__paw" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
            <ellipse cx="12" cy="15.6" rx="4.4" ry="3.7" fill="currentColor" />
            <ellipse cx="6.6" cy="10" rx="1.8" ry="2.2" fill="currentColor" transform="rotate(-20 6.6 10)" />
            <ellipse cx="10.9" cy="7.6" rx="1.7" ry="2.3" fill="currentColor" />
            <ellipse cx="15.4" cy="7.8" rx="1.7" ry="2.3" fill="currentColor" />
            <ellipse cx="19.1" cy="10.4" rx="1.8" ry="2.2" fill="currentColor" transform="rotate(20 19.1 10.4)" />
          </svg>
        </button>
        <button
          class="login-switch__item"
          role="tab"
          :aria-selected="type === 'wechat' ? 'true' : 'false'"
          :class="{ active: type === 'wechat' }"
          @click="switchType('wechat')">
          <svg class="login-switch__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
               stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
            <!-- 微信气泡：扫码登录 -->
            <path d="M4 5.5h16v11H9l-5 3.5z" />
            <circle cx="9.2" cy="11" r="0.9" fill="currentColor" stroke="none" />
            <circle cx="13" cy="11" r="0.9" fill="currentColor" stroke="none" />
            <circle cx="16.8" cy="11" r="0.9" fill="currentColor" stroke="none" />
            <path d="M15 19.5c4 0 7-2.2 7.5-5" />
          </svg>
          <span class="login-switch__label">微信扫码</span>
        </button>
      </div>

      <!-- 账号密码 / 注册 -->
      <el-form
        v-if="mode === 'register' || type === 'password'"
        ref="form"
        :model="form"
        :rules="formRules"
        label-position="top"
        size="medium"
        @submit.native.prevent>
        <template v-if="mode === 'register'">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model.trim="form.nickname" placeholder="如何称呼你" maxlength="32" />
          </el-form-item>
        </template>
        <el-form-item :label="mode === 'login' ? '账号' : '账号（4-32 位字母数字下划线）'" prop="account">
          <el-input v-model.trim="form.account" placeholder="请输入账号" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model.trim="form.password" type="password" placeholder="6-32 位" show-password autocomplete="current-password" />
        </el-form-item>
        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model.trim="form.captchaCode" placeholder="输入右侧字符" maxlength="4" @keyup.enter.native="submit" />
            <img v-if="captchaImg" class="captcha-img" :src="captchaImg" alt="图形验证码" title="点击刷新" @click="loadCaptcha" />
            <div v-else class="captcha-img captcha-img--loading"><i class="el-icon-loading"></i></div>
          </div>
        </el-form-item>
        <el-button type="primary" class="login-card__submit" :loading="loading" @click="submit">
          {{ mode === 'login' ? '登录' : '注册' }}
        </el-button>
      </el-form>

      <!-- 手机号 + 短信验证码 -->
      <el-form
        v-else
        ref="smsForm"
        :model="sms"
        :rules="smsRules"
        label-position="top"
        size="medium"
        @submit.native.prevent>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="sms.phone" placeholder="11 位手机号" maxlength="11" autocomplete="tel" />
        </el-form-item>
        <el-form-item label="短信验证码" prop="code">
          <div class="sms-row">
            <el-input v-model.trim="sms.code" placeholder="6 位验证码" maxlength="6" @keyup.enter.native="submitSms" />
            <el-button class="sms-send" :disabled="countdown > 0 || sending" :loading="sending" @click="sendCode">
              {{ countdown > 0 ? countdown + 's 后重发' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-button type="primary" class="login-card__submit" :loading="loading" @click="submitSms">
          登录 / 注册
        </el-button>
        <p class="login-card__hint">未注册的手机号验证通过后将自动创建账号</p>
      </el-form>

      <!-- 微信扫码登录 -->
      <el-form v-if="mode === 'login' && type === 'wechat'" label-position="top" class="wechat-panel" @submit.native.prevent>
        <div class="wechat-panel__qr">
          <img v-if="wechatQr.qrBase64" :src="wechatQr.qrBase64" alt="微信登录二维码" />
          <div v-else class="wechat-panel__loading"><i class="el-icon-loading"></i></div>
          <div class="wechat-panel__scanned" v-if="wechatStatus === 'SUCCESS'">
            <i class="el-icon-success"></i><span>扫码成功</span>
          </div>
        </div>
        <p class="wechat-panel__tip">{{ wechatTip }}</p>
        <el-button size="small" round :disabled="wechatCountdown > 0" @click="refreshWechatQr">
          {{ wechatCountdown > 0 ? wechatCountdown + 's 后刷新' : '刷新二维码' }}
        </el-button>
      </el-form>

      <div class="login-card__switch">
        <template v-if="mode === 'login'">
          <a href="javascript:void(0)" class="link-underline" @click="switchMode('register')">没有账号？注册</a>
          <a v-if="type === 'password'" href="javascript:void(0)" class="link-underline" @click="openForgot">忘记密码</a>
          <a v-else href="javascript:void(0)" class="link-underline" @click="switchType('password')">用账号密码登录</a>
        </template>
        <a v-else href="javascript:void(0)" class="link-underline" @click="switchMode('login')">已有账号？登录</a>
      </div>
      <p class="login-card__tip">机构账号请在<a href="/login?mode=admin" class="link-underline">工作台入口</a>登录</p>
    </div>
  </div>
</template>

<script>
import { captcha, sendSmsCode, createWechatQr, checkWechatQr } from '@/api/auth';

export default {
  name: 'LoginDialog',
  data() {
    return {
      visible: false,
      mode: 'login',
      type: 'password',
      loading: false,
      sending: false,
      countdown: 0,
      timer: null,
      captchaImg: '',
      form: { account: '', password: '', nickname: '', captchaUuid: '', captchaCode: '' },
      sms: { phone: '', code: '' },
      wechatQr: { uuid: '', qrBase64: '', demo: false },
      wechatStatus: 'PENDING',
      wechatCountdown: 0,
      wechatSubmitting: false
    };
  },
  computed: {
    title() {
      if (this.mode === 'register') return '注册账号';
      return this.type === 'sms' ? '手机号登录' : this.type === 'wechat' ? '微信扫码登录' : '登录';
    },
    desc() {
      if (this.mode === 'register') return '注册后即可提交领养申请';
      if (this.type === 'sms') return '用手机号接收验证码，登录后即可申请领养、收藏与上报线索';
      if (this.type === 'wechat') return '用微信扫一扫，扫码确认后即可登录，与小程序账号互通';
      return '登录后可申请领养、收藏与上报线索';
    },
    formRules() {
      return {
        account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }],
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      };
    },
    smsRules() {
      return {
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' }
        ]
      };
    }
  },
  beforeDestroy() {
    this.stopWechatPolling();
    this.stopCountdown();
  },
  methods: {
    open(mode = 'login') {
      this.mode = mode;
      this.visible = true;
      this.resetForm();
      if (mode === 'login') this.loadCaptcha();
    },
    openForgot() {
      this.close();
      this.$router.push('/login?mode=forgot');
    },
    close() {
      this.visible = false;
      this.stopCountdown();
      this.stopWechatPolling();
    },
    switchMode(mode) {
      this.mode = mode;
      this.resetForm();
      if (mode === 'login') this.loadCaptcha();
    },
    /** 清理微信扫码轮询 */
    stopWechatPolling() {
      if (this.wechatPollTimer) { clearInterval(this.wechatPollTimer); this.wechatPollTimer = null; }
      if (this.wechatCdTimer) { clearInterval(this.wechatCdTimer); this.wechatCdTimer = null; }
    },
    /** 打开微信扫码面板：创建二维码并开始轮询 */
    async startWechatQr() {
      this.wechatStatus = 'PENDING';
      this.wechatQr = { uuid: '', qrBase64: '', demo: false };
      try {
        const data = await createWechatQr();
        this.wechatQr = data;
        // 轮询扫码状态（演示模式约 3 秒自动成功）
        this.wechatPollTimer = setInterval(async () => {
          if (this.wechatStatus !== 'PENDING' || !this.wechatQr.uuid) return;
          const r = await checkWechatQr(this.wechatQr.uuid).catch(() => null);
          if (!r || !r.data) return;
          if (r.data.status === 'SUCCESS') {
            this.stopWechatPolling();
            this.wechatStatus = 'SUCCESS';
            // 登录态落地：token + 用户信息
            await this.$store.dispatch('user/qrLoginSuccess', r.data);
            this.$message.success('微信登录成功');
            this.afterLogin();
          } else if (r.data.status === 'EXPIRED') {
            this.stopWechatPolling();
            this.wechatStatus = 'EXPIRED';
          }
        }, 2000);
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    /** 刷新二维码 */
    refreshWechatQr() {
      if (this.wechatCountdown > 0) return;
      this.wechatCountdown = 5;
      this.wechatCdTimer = setInterval(() => {
        this.wechatCountdown -= 1;
        if (this.wechatCountdown <= 0) { clearInterval(this.wechatCdTimer); this.startWechatQr(); }
      }, 1000);
    },
    switchType(type) {
      if (type !== 'wechat') this.stopWechatPolling();
      this.type = type;
      this.resetForm();
      if (type === 'password') this.loadCaptcha();
      if (type === 'wechat') this.startWechatQr();
    },
    resetForm() {
      this.form = { account: '', password: '', nickname: '', captchaUuid: '', captchaCode: '' };
      this.sms = { phone: '', code: '' };
      this.$refs.form && this.$refs.form.clearValidate();
      this.$refs.smsForm && this.$refs.smsForm.clearValidate();
    },
    async loadCaptcha() {
      try {
        const data = await captcha();
        this.captchaImg = data.imageBase64;
        this.form.captchaUuid = data.uuid;
      } catch (e) {
        this.captchaImg = '';
      }
    },
    async sendCode() {
      if (!/^1[3-9]\d{9}$/.test(this.sms.phone)) {
        this.$message.warning('请输入正确的手机号');
        return;
      }
      this.sending = true;
      try {
        const data = await sendSmsCode({ phone: this.sms.phone, scene: 'login' });
        this.$message.success('验证码已发送');
        // 开发模式（未配置阿里云短信）后端会回传验证码，直接填入便于联调
        if (data && data.devMode && data.code) {
          this.sms.code = data.code;
          this.$message.info(`开发模式：验证码已自动填入 ${data.code}`);
        }
        this.startCountdown((data && data.resendSeconds) || 60);
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.sending = false;
      }
    },
    startCountdown(seconds) {
      this.stopCountdown();
      this.countdown = seconds;
      this.timer = setInterval(() => {
        this.countdown -= 1;
        if (this.countdown <= 0) this.stopCountdown();
      }, 1000);
    },
    stopCountdown() {
      if (this.timer) clearInterval(this.timer);
      this.timer = null;
      this.countdown = 0;
    },
    async submitSms() {
      const valid = await this.$refs.smsForm.validate().catch(() => false);
      if (!valid) return;
      this.loading = true;
      try {
        await this.$store.dispatch('user/smsLogin', { phone: this.sms.phone, code: this.sms.code });
        this.$message.success('登录成功');
        this.afterLogin();
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.loading = false;
      }
    },
    async submit() {
      const valid = await this.$refs.form.validate().catch(() => false);
      if (!valid) return;
      this.loading = true;
      try {
        if (this.mode === 'login') {
          await this.$store.dispatch('user/portalLogin', {
            account: this.form.account,
            password: this.form.password,
            captchaUuid: this.form.captchaUuid,
            captchaCode: this.form.captchaCode
          });
          this.$message.success('登录成功');
        } else {
          await this.$store.dispatch('user/register', {
            username: this.form.account,
            password: this.form.password,
            nickname: this.form.nickname,
            captchaUuid: this.form.captchaUuid,
            captchaCode: this.form.captchaCode
          });
          this.$message.success('注册成功，已自动登录');
        }
        this.afterLogin();
      } catch (e) {
        this.loadCaptcha();
      } finally {
        this.loading = false;
      }
    },
    afterLogin() {
      this.close();
      const redirect = this.$route.query.redirect;
      if (redirect) {
        this.$router.push(redirect);
      } else {
        this.$router.go(0);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.login-mask {
  position: fixed;
  inset: 0;
  background: $mask;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.login-card {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: $radius-card;
  padding: 32px;
  position: relative;
  box-shadow: $shadow-2;

  &__close {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    font-size: 18px;
    color: $text-secondary;
    cursor: pointer;
    border-radius: $radius-circle;

    &:hover {
      background: $bg-page;
      color: $text-primary;
    }
  }

  &__title {
    font-size: 22px;
    line-height: 32px;
    margin-bottom: 4px;
  }

  &__desc {
    font-size: 14px;
    color: $text-secondary;
    margin-bottom: 20px;
  }

  &__submit {
    width: 100%;
    margin-top: 4px;
  }

  &__switch {
    display: flex;
    justify-content: space-between;
    margin-top: 16px;
    font-size: 14px;
  }

  &__tip {
    margin-top: 16px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__hint {
    margin-top: 12px;
    font-size: 12px;
    color: $text-secondary;
    text-align: center;
  }
}

// 登录方式：左右各占一半、整行铺满的分段选择器（宠物风：圆润 + 爪印）
.login-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;

  &__item {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    height: 74px;
    padding: 0 8px;
    overflow: hidden;
    border: 1px solid $border-color;
    border-radius: $radius-card;
    background: $bg-card;
    color: $text-secondary;
    font-size: 14px;
    cursor: pointer;
    transition: border-color $duration-hover-in $ease-out-quart,
                background-color $duration-hover-in $ease-out-quart,
                color $duration-hover-in $ease-out-quart,
                box-shadow $duration-hover-in $ease-out-quart,
                transform $duration-hover-in $ease-out-quart;

    &:hover {
      border-color: rgba(255, 122, 69, 0.55);
      color: $text-primary;
    }

    &:active {
      transform: scale(0.985);
    }

    // 选中：主色描边 + 浅主色底 + 深主色字（不违反“白字不压橙底”）
    &.active {
      border-color: $color-primary;
      background: $color-primary-light;
      color: $color-primary-text;
      font-weight: 600;
      box-shadow: 0 4px 14px rgba(255, 122, 69, 0.18);
    }
  }

  &__icon {
    width: 27px;
    height: 27px;
    flex-shrink: 0;
  }

  &__label {
    line-height: 1;
    letter-spacing: 0.2px;
  }

  // 选中时从右下角“踩”出来的爪印水印
  &__paw {
    position: absolute;
    right: -8px;
    bottom: -10px;
    width: 48px;
    height: 48px;
    color: $color-primary;
    opacity: 0;
    transform: rotate(-12deg) scale(0.86);
    pointer-events: none;
    transition: opacity 180ms $ease-out-quart, transform 180ms $ease-out-quart;
  }

  &__item.active &__paw {
    opacity: 0.18;
    transform: rotate(-12deg) scale(1);
  }
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: $radius-base;
  border: 1px solid $border-color;
  cursor: pointer;
  flex-shrink: 0;

  &--loading {
    display: flex;
    align-items: center;
    justify-content: center;
    color: $text-secondary;
  }
}

.sms-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.sms-send {
  flex-shrink: 0;
  min-width: 116px;
}

// 微信扫码登录
.wechat-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 6px 0 2px;

  &__qr {
    position: relative;
    width: 220px;
    height: 220px;
    border: 1px solid $border-color;
    border-radius: $radius-base;
    overflow: hidden;
    background: #fff;

    img { width: 100%; height: 100%; display: block; }
  }

  &__loading {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    font-size: 26px;
    color: $text-secondary;
  }

  &__scanned {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: rgba(255, 255, 255, 0.94);

    i { font-size: 42px; color: $color-secondary; }
    span { font-size: 15px; font-weight: 600; color: $text-primary; }
  }

  &__tip {
    font-size: 12px;
    line-height: 18px;
    color: $text-secondary;
    text-align: center;
  }
}
</style>
