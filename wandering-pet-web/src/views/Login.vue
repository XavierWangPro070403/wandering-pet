<template>
  <div class="login-page">
    <div class="wrap login-grid">
      <!-- 左侧：说明 -->
      <div class="login-intro">
        <router-link to="/" class="login-intro__brand">
          <img src="/imgs/logo.svg" alt="" class="login-intro__logo" />
          <span>流浪宠物领养</span>
        </router-link>
        <h1 class="login-intro__title">工作台登录</h1>
        <p class="login-intro__desc">
          救助站机构账号登录后可管理宠物档案、审核领养申请、登记回访；平台管理员账号登录后进入管理端。
          领养人请从门户首页登录。
        </p>
        <ul class="login-intro__list">
          <li>演示机构账号：shelter1 / 123456（毛窝窝救助站）</li>
          <li>演示管理员账号：admin / 123456</li>
        </ul>
      </div>

      <!-- 右侧：表单卡片 -->
      <div class="login-form-card">
        <el-tabs v-model="mode" stretch>
          <el-tab-pane label="账号登录" name="login">
            <el-form ref="loginForm" :model="loginForm" :rules="loginRules" label-position="top">
              <el-form-item label="账号" prop="account">
                <el-input v-model.trim="loginForm.account" placeholder="用户名" autocomplete="username" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model.trim="loginForm.password" type="password" placeholder="密码" show-password autocomplete="current-password" />
              </el-form-item>
              <el-form-item label="验证码" prop="captchaCode">
                <div class="captcha-row">
                  <el-input v-model.trim="loginForm.captchaCode" placeholder="输入右侧字符" maxlength="4" @keyup.enter.native="doLogin" />
                  <img v-if="captchaImg" class="captcha-img" :src="captchaImg" alt="图形验证码" title="点击刷新" @click="loadCaptcha" />
                  <div v-else class="captcha-img captcha-img--loading"><i class="el-icon-loading"></i></div>
                </div>
              </el-form-item>
              <el-button type="primary" class="login-form-card__submit" :loading="loading" @click="doLogin">登录</el-button>
              <p class="login-form-card__forgot">
                <router-link to="/" class="link-underline">返回门户首页</router-link>
                <a href="javascript:void(0)" class="link-underline" @click="forgotVisible = true">忘记密码</a>
              </p>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="领养人注册" name="register">
            <el-form ref="regForm" :model="regForm" :rules="regRules" label-position="top">
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model.trim="regForm.nickname" placeholder="如何称呼你" maxlength="32" />
              </el-form-item>
              <el-form-item label="账号" prop="username">
                <el-input v-model.trim="regForm.username" placeholder="4-32 位字母数字下划线" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model.trim="regForm.password" type="password" placeholder="6-32 位" show-password />
              </el-form-item>
              <el-form-item label="验证码" prop="captchaCode">
                <div class="captcha-row">
                  <el-input v-model.trim="regForm.captchaCode" placeholder="输入右侧字符" maxlength="4" @keyup.enter.native="doRegister" />
                  <img v-if="captchaImg" class="captcha-img" :src="captchaImg" alt="图形验证码" title="点击刷新" @click="loadCaptcha" />
                  <div v-else class="captcha-img captcha-img--loading"><i class="el-icon-loading"></i></div>
                </div>
              </el-form-item>
              <el-button type="primary" class="login-form-card__submit" :loading="loading" @click="doRegister">注册并登录</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 找回密码弹窗 -->
    <el-dialog title="找回密码" :visible.sync="forgotVisible" width="420px">
      <el-form :model="forgotForm" label-position="top">
        <el-form-item label="账号" required>
          <el-input v-model.trim="forgotForm.account" placeholder="注册时的账号" />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model.trim="forgotForm.newPassword" type="password" placeholder="6-32 位" show-password />
        </el-form-item>
        <el-form-item label="验证码" required>
          <div class="captcha-row">
            <el-input v-model.trim="forgotForm.captchaCode" placeholder="输入右侧字符" maxlength="4" />
            <img v-if="captchaImg" class="captcha-img" :src="captchaImg" alt="图形验证码" title="点击刷新" @click="loadCaptcha" />
            <div v-else class="captcha-img captcha-img--loading"><i class="el-icon-loading"></i></div>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="forgotVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="doReset">重置密码</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { captcha, resetPassword } from '@/api/auth';

export default {
  name: 'LoginPage',
  data() {
    return {
      mode: this.$route.query.mode === 'register' ? 'register' : 'login',
      loading: false,
      captchaImg: '',
      loginForm: { account: '', password: '', captchaUuid: '', captchaCode: '' },
      regForm: { username: '', password: '', nickname: '', captchaUuid: '', captchaCode: '' },
      forgotVisible: false,
      forgotForm: { account: '', newPassword: '', captchaUuid: '', captchaCode: '' },
      loginRules: {
        account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      regRules: {
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]{4,32}$/, message: '账号为 4-32 位字母数字下划线', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
        ],
        captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      }
    };
  },
  mounted() {
    this.loadCaptcha();
  },
  methods: {
    async loadCaptcha() {
      try {
        const data = await captcha();
        this.captchaImg = data.imageBase64;
        this.loginForm.captchaUuid = data.uuid;
        this.regForm.captchaUuid = data.uuid;
        this.forgotForm.captchaUuid = data.uuid;
      } catch (e) {
        /* 网络异常提示由拦截器统一处理 */
      }
    },
    /**
     * 登录后要去哪：只有「该角色能访问」的 redirect 才采用，
     * 否则回角色首页——避免带着 /admin/xxx、/me 这类地址登录后又被守卫弹走。
     */
    resolveTarget(user) {
      const home = user.userType === 'ADMIN' ? '/admin/dashboard'
        : user.userType === 'SHELTER' ? '/shelter/workbench' : '/';
      const redirect = this.$route.query.redirect;
      if (!redirect || !this.canAccess(redirect, user.userType)) return home;
      return redirect;
    },
    canAccess(path, userType) {
      const adminOnly = path === '/admin' || path.startsWith('/admin/');
      const shelterOnly = path === '/shelter' || path.startsWith('/shelter/');
      const userOnly = ['/me', '/my/', '/adopt/apply/', '/report', '/volunteer']
        .some((p) => path === p || path.startsWith(p));
      if (userType === 'USER') return !adminOnly && !shelterOnly;
      if (userType === 'SHELTER') return !adminOnly && !userOnly;
      if (userType === 'ADMIN') return !shelterOnly && !userOnly;
      return true;
    },
    async doLogin() {
      const valid = await this.$refs.loginForm.validate().catch(() => false);
      if (!valid) return;
      this.loading = true;
      try {
        const user = await this.$store.dispatch('user/login', {
          username: this.loginForm.account,
          password: this.loginForm.password,
          captchaUuid: this.loginForm.captchaUuid,
          captchaCode: this.loginForm.captchaCode
        });
        this.$message.success('登录成功');
        await this.$router.replace(this.resolveTarget(user));
      } catch (e) {
        this.loadCaptcha();
      } finally {
        this.loading = false;
      }
    },
    async doRegister() {
      const valid = await this.$refs.regForm.validate().catch(() => false);
      if (!valid) return;
      this.loading = true;
      try {
        await this.$store.dispatch('user/register', this.regForm);
        this.$message.success('注册成功');
        this.$router.replace('/');
      } catch (e) {
        this.loadCaptcha();
      } finally {
        this.loading = false;
      }
    },
    async doReset() {
      if (!this.forgotForm.account || !this.forgotForm.newPassword || !this.forgotForm.captchaCode) {
        this.$message.warning('请填写完整信息');
        return;
      }
      this.loading = true;
      try {
        await resetPassword(this.forgotForm);
        this.$message.success('密码已重置，请使用新密码登录');
        this.forgotVisible = false;
        this.mode = 'login';
        this.loadCaptcha();
      } catch (e) {
        this.loadCaptcha();
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.login-page {
  min-height: 100vh;
  background: $bg-page;
  display: flex;
  align-items: stretch;
}

.login-grid {
  display: grid;
  grid-template-columns: 1fr 460px;
  gap: 64px;
  align-items: center;
  padding-top: 80px;
  padding-bottom: 80px;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
    gap: 32px;
  }
}

.login-intro {
  position: relative;

  // 左上角一枚淡爪印（小动物元素）
  &::before {
    content: "";
    position: absolute;
    top: -48px;
    right: 16px;
    width: 168px;
    height: 168px;
    background-color: $color-primary;
    opacity: 0.05;
    pointer-events: none;
    -webkit-mask: $paw-svg center / contain no-repeat;
    mask: $paw-svg center / contain no-repeat;
  }

  &__brand {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 17px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 48px;
  }

  &__logo {
    width: 32px;
    height: 32px;
  }

  &__title {
    font-size: $font-h2;
    line-height: $lh-h2;
    margin-bottom: 12px;
  }

  &__desc {
    font-size: 16px;
    line-height: 28px;
    color: $text-regular;
    max-width: 520px;
    margin-bottom: 16px;
  }

  &__list {
    margin: 0;
    padding: 0;
    list-style: none;

    li {
      font-size: 14px;
      color: $text-secondary;
      line-height: 26px;

      &::before {
        content: "";
        display: inline-block;
        width: 12px;
        height: 12px;
        margin-right: 8px;
        vertical-align: -1px;
        background-color: $color-primary;
        opacity: 0.6;
        -webkit-mask: $paw-svg center / contain no-repeat;
        mask: $paw-svg center / contain no-repeat;
      }
    }
  }
}

.login-form-card {
  @include card;

  background: #fff;
  padding: 8px 32px 32px;
}

.login-form-card__submit {
  width: 100%;
  margin-top: 8px;
}

.login-form-card__forgot {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 14px;
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
</style>
