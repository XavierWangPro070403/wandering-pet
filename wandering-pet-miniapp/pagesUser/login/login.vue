<template>
  <view class="page login">
    <!-- 品牌区 -->
    <view class="login__brand">
      <view class="login__logo">
        <text class="login__logo-paw">🐾</text>
      </view>
      <text class="login__brand-title">流浪宠物领养</text>
      <text class="login__brand-sub">{{ isLoggedIn ? '绑定手机号后可用验证码登录，也方便机构联系你' : '登录后可申请领养、收藏与上报线索' }}</text>
    </view>

    <view class="card login__panel">
      <!-- 未登录：手机号 + 短信验证码（主）/ 微信一键登录（次） -->
      <template v-if="!isLoggedIn">
        <text class="field-label">手机号</text>
        <input class="form-input" v-model="phone" type="number" maxlength="11" placeholder="请输入 11 位手机号" />

        <text class="field-label">验证码</text>
        <view class="code-row">
          <input class="form-input code-row__input" v-model="code" type="number" maxlength="6" placeholder="6 位验证码" />
          <button
            class="code-row__btn"
            :class="{ 'code-row__btn--disabled': counting || sending }"
            :disabled="counting || sending"
            @click="sendCode('login')">
            {{ counting ? countdown + 's 后重发' : '获取验证码' }}
          </button>
        </view>

        <button class="btn-primary login__submit" :loading="loading" @click="submit">登录 / 注册</button>
        <text class="login__hint">未注册的手机号验证通过后将自动创建账号</text>

        <view class="login__divider"><text class="login__divider-text">或用微信登录</text></view>
        <button class="btn-plain login__wx" :loading="wxLoading" @click="doWxLogin">微信一键登录</button>
      </template>

      <!-- 已登录：绑定 / 换绑手机号 -->
      <template v-else>
        <view class="login__bound" v-if="maskedPhone">
          <text class="login__bound-label">当前绑定</text>
          <text class="login__bound-value num">{{ maskedPhone }}</text>
        </view>

        <text class="field-label">{{ maskedPhone ? '换绑新手机号' : '绑定手机号' }}</text>
        <input class="form-input" v-model="phone" type="number" maxlength="11" placeholder="请输入 11 位手机号" />

        <text class="field-label">验证码</text>
        <view class="code-row">
          <input class="form-input code-row__input" v-model="code" type="number" maxlength="6" placeholder="6 位验证码" />
          <button
            class="code-row__btn"
            :class="{ 'code-row__btn--disabled': counting || sending }"
            :disabled="counting || sending"
            @click="sendCode('bind')">
            {{ counting ? countdown + 's 后重发' : '获取验证码' }}
          </button>
        </view>

        <button class="btn-primary login__submit" :loading="loading" @click="submitBind">绑定手机号</button>
        <button class="btn-plain login__logout" @click="doLogout">退出登录</button>
      </template>
    </view>

    <text class="login__tip">演示环境：未配置阿里云短信时验证码会自动填入；配置 AccessKey、签名与模板后即真实下发。</text>
  </view>
</template>

<script>
import { sendSmsCode, smsLogin, smsBind, wxLogin } from '../../common/api.js';

export default {
  data() {
    return {
      phone: '',
      code: '',
      loading: false,
      sending: false,
      wxLoading: false,
      counting: false,
      countdown: 0,
      timer: null
    };
  },
  computed: {
    isLoggedIn() {
      return this.$store.getters.isLoggedIn;
    },
    userInfo() {
      return this.$store.state.userInfo || {};
    },
    maskedPhone() {
      return this.userInfo.phone || '';
    }
  },
  onUnload() {
    this.stopCountdown();
  },
  methods: {
    validPhone() {
      if (!/^1[3-9]\d{9}$/.test(this.phone)) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' });
        return false;
      }
      return true;
    },
    async sendCode(scene) {
      const sceneValue = scene === 'bind' ? 'bind' : 'login';
      if (this.counting || this.sending) return;
      if (!this.validPhone()) return;
      this.sending = true;
      try {
        const data = await sendSmsCode(this.phone, sceneValue);
        if (data && data.devMode && data.code) {
          // 开发模式：后端回传验证码，直接填入便于联调
          this.code = data.code;
          uni.showToast({ title: '开发模式：验证码已填入', icon: 'none' });
        } else {
          uni.showToast({ title: '验证码已发送', icon: 'none' });
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
      this.counting = true;
      this.timer = setInterval(() => {
        this.countdown -= 1;
        if (this.countdown <= 0) this.stopCountdown();
      }, 1000);
    },
    stopCountdown() {
      if (this.timer) clearInterval(this.timer);
      this.timer = null;
      this.counting = false;
      this.countdown = 0;
    },
    validCode() {
      if (!/^\d{6}$/.test(this.code)) {
        uni.showToast({ title: '请输入 6 位验证码', icon: 'none' });
        return false;
      }
      return true;
    },
    async submit() {
      if (!this.validPhone() || !this.validCode()) return;
      this.loading = true;
      try {
        const vo = await smsLogin(this.phone, this.code);
        await this.$store.dispatch('loginSuccess', vo);
        uni.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => this.backOrHome(), 700);
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.loading = false;
      }
    },
    /** 登录成功后返回来源页；若登录页是栈底（如分享直达），回首页，避免停在这里 */
    backOrHome() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.switchTab({ url: '/pages/index/index' });
      }
    },
    async submitBind() {
      if (!this.validPhone() || !this.validCode()) return;
      this.loading = true;
      try {
        const vo = await smsBind(this.phone, this.code);
        // 手机号原本属于其他账号时会触发账号串联合并并换发新 token，必须整体替换本地登录态
        if (vo && vo.token) {
          await this.$store.dispatch('loginSuccess', vo);
        } else {
          await this.$store.dispatch('loadMe');
        }
        this.code = '';
        uni.showToast({ title: '绑定成功，账号已串联', icon: 'success' });
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.loading = false;
      }
    },
    doWxLogin() {
      this.wxLoading = true;
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          try {
            const vo = await wxLogin(loginRes.code, '', '');
            await this.$store.dispatch('loginSuccess', vo);
            uni.showToast({ title: '登录成功', icon: 'success' });
            setTimeout(() => this.backOrHome(), 700);
          } catch (e) {
            // 拦截器已提示
          } finally {
            this.wxLoading = false;
          }
        },
        fail: () => {
          this.wxLoading = false;
          uni.showToast({ title: '微信登录失败，请重试', icon: 'none' });
        }
      });
    },
    async doLogout() {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({ title: '退出登录', content: '确定退出当前账号吗？', success: (r) => resolve(r.confirm) });
      });
      if (!confirmed) return;
      await this.$store.dispatch('logout');
      uni.showToast({ title: '已退出', icon: 'none' });
    }
  }
};
</script>

<style scoped>
.login {
  padding: 48rpx 0 64rpx;
}

.login__brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 48rpx 40rpx;
}

.login__logo {
  width: 112rpx;
  height: 112rpx;
  border-radius: 32rpx;
  background: #FFF1EB;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login__logo-paw {
  font-size: 56rpx;
  line-height: 1;
}

.login__brand-title {
  margin-top: 24rpx;
  font-size: 40rpx;
  font-weight: 600;
  color: #1F2733;
  letter-spacing: 1rpx;
}

.login__brand-sub {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #667080;
  text-align: center;
  line-height: 36rpx;
}

.login__panel {
  margin: 0 32rpx;
  padding: 40rpx 32rpx 36rpx;
}

.field-label {
  display: block;
  font-size: 24rpx;
  color: #667080;
  margin: 24rpx 0 12rpx;
}

.field-label:first-child {
  margin-top: 0;
}

.form-input {
  height: 88rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #1F2733;
}

.code-row {
  display: flex;
  align-items: center;
}

.code-row__input {
  flex: 1;
}

.code-row__btn {
  margin-left: 16rpx;
  height: 88rpx;
  line-height: 88rpx;
  padding: 0 24rpx;
  border: none;
  border-radius: 16rpx;
  background: #FFF1EB;
  color: #B23A0E;
  font-size: 26rpx;
  flex-shrink: 0;
}

.code-row__btn::after {
  border: none;
}

.code-row__btn--disabled {
  background: #F2F3F5;
  color: #A9B0BC;
}

.login__submit {
  margin-top: 40rpx;
}

.login__hint {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  color: #A9B0BC;
  text-align: center;
}

.login__divider {
  display: flex;
  align-items: center;
  margin: 32rpx 0 24rpx;
  color: #A9B0BC;
  font-size: 22rpx;
}

.login__divider::before,
.login__divider::after {
  content: '';
  flex: 1;
  height: 2rpx;
  background: #E5E6EB;
}

.login__divider-text {
  padding: 0 20rpx;
}

.login__wx {
  margin-top: 0;
}

.login__bound {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  padding: 20rpx 24rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
}

.login__bound-label {
  font-size: 24rpx;
  color: #667080;
}

.login__bound-value {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2733;
}

.login__logout {
  margin-top: 20rpx;
}

.login__tip {
  display: block;
  margin: 32rpx 48rpx 0;
  font-size: 22rpx;
  color: #A9B0BC;
  line-height: 34rpx;
  text-align: center;
}
</style>
