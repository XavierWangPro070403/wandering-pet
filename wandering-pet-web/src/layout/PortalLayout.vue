<template>
  <div class="portal">
    <a class="skip-link" href="#main">跳到主要内容</a>

    <!-- 顶栏：滚动后白底 + 1px 线，无阴影跳变 -->
    <header class="portal-header" :class="{ scrolled: isScrolled }">
      <div class="wrap header-inner">
        <router-link to="/" class="brand" aria-label="流浪宠物领养系统首页">
          <img class="brand__logo" src="/imgs/logo.svg" alt="" />
          <span class="brand__name">流浪宠物领养</span>
        </router-link>

        <nav class="nav" aria-label="主导航">
          <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="nav__item" :class="{ active: isActive(item.path) }">
            {{ item.label }}
          </router-link>
        </nav>

        <div class="header-actions">
          <template v-if="!userInfo">
            <el-button type="text" class="login-btn" @click="openLogin">登录</el-button>
            <el-button type="primary" size="small" @click="openLogin">注册账号</el-button>
          </template>
          <template v-else>
            <!-- 站内消息只有 C 端用户有；机构/管理员在各自工作台处理咨询 -->
            <el-badge v-if="isUser" :value="unread" :hidden="!unread" class="msg-badge">
              <button class="icon-btn" aria-label="消息中心" @click="$router.push('/me?tab=messages')">
                <i class="el-icon-bell" aria-hidden="true"></i>
              </button>
            </el-badge>
            <el-dropdown trigger="click" @command="onCommand">
              <span class="user-chip" role="button" tabindex="0">
                <img class="user-chip__avatar" :src="userInfo.avatar || defaultAvatar" alt="" />
                <span class="user-chip__name">{{ userInfo.nickname || userInfo.username }}</span>
                <i class="el-icon-arrow-down" aria-hidden="true"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <!-- 按角色给入口：机构/管理员不出现 USER 专属页，避免被守卫弹回 -->
                <template v-if="isAdmin">
                  <el-dropdown-item command="admin-console">管理后台</el-dropdown-item>
                </template>
                <template v-else-if="isShelter">
                  <el-dropdown-item command="shelter-console">机构工作台</el-dropdown-item>
                  <el-dropdown-item command="shelter-chat">在线咨询</el-dropdown-item>
                </template>
                <template v-else>
                  <el-dropdown-item command="me">个人中心</el-dropdown-item>
                  <el-dropdown-item command="applications">我的申请</el-dropdown-item>
                </template>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
          <button class="menu-toggle" :aria-expanded="mobileMenuOpen ? 'true' : 'false'" aria-label="打开菜单" @click="mobileMenuOpen = !mobileMenuOpen">
            <i :class="mobileMenuOpen ? 'el-icon-close' : 'el-icon-s-unfold'" aria-hidden="true"></i>
          </button>
        </div>
      </div>

      <!-- 移动端全屏菜单 -->
      <transition name="fade">
        <div v-if="mobileMenuOpen" class="mobile-menu" role="dialog" aria-label="导航菜单">
          <div class="wrap mobile-menu__body">
            <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="mobile-menu__item" @click.native="mobileMenuOpen = false">
              {{ item.label }}
            </router-link>
            <el-button type="primary" class="mobile-menu__cta" @click="onConsole">
              {{ consoleLabel }}
            </el-button>
          </div>
        </div>
      </transition>
    </header>

    <main id="main" class="portal-main">
      <router-view />
    </main>

    <!-- 页脚：链接组 + 联系方式 + 一行图片署名 + 版权 -->
    <footer class="portal-footer">
      <div class="wrap footer-grid">
        <div class="footer-col">
          <h3 class="footer-col__title">领养服务</h3>
          <router-link to="/pets" class="footer-link">待领养</router-link>
            <router-link to="/shop" class="footer-link">宠物商城</router-link>
          <router-link to="/my/applications" class="footer-link">我的申请</router-link>
          <router-link to="/articles?category=STORY" class="footer-link">救助故事</router-link>
        </div>
        <div class="footer-col">
          <h3 class="footer-col__title">参与公益</h3>
          <router-link to="/donation" class="footer-link">捐赠中心</router-link>
          <router-link to="/volunteer" class="footer-link">志愿者报名</router-link>
          <router-link to="/report" class="footer-link">流浪动物上报</router-link>
        </div>
        <div class="footer-col">
          <h3 class="footer-col__title">帮助</h3>
          <router-link to="/articles?category=RAISE" class="footer-link">饲养指导</router-link>
          <router-link to="/articles?category=KNOWLEDGE" class="footer-link">领养科普</router-link>
          <a href="mailto:contact@wanderingpet.example" class="footer-link">contact@wanderingpet.example</a>
        </div>
        <div class="footer-col">
          <h3 class="footer-col__title">工作台</h3>
          <a href="/login" class="footer-link">机构 / 管理员登录</a>
        </div>
      </div>
      <div class="wrap footer-meta">
        <span>图片来自 Unsplash 与 Pexels，遵循各自许可</span>
        <span>© 2026 流浪宠物领养平台（演示环境）</span>
      </div>
    </footer>

    <!-- 登录 / 注册弹窗 -->
    <LoginDialog ref="loginDialog" />
  </div>
</template>

<script>
import LoginDialog from '@/components/LoginDialog.vue';

export default {
  name: 'PortalLayout',
  components: { LoginDialog },
  data() {
    return {
      isScrolled: false,
      mobileMenuOpen: false,
      unread: 0,
      timer: null,
      defaultAvatar: '/imgs/avatar-default.svg',
      navItems: [
        { path: '/', label: '首页' },
        { path: '/pets', label: '待领养' },
        { path: '/shop', label: '商城' },
        { path: '/articles', label: '资讯' },
        { path: '/donation', label: '捐赠中心' },
        { path: '/volunteer', label: '志愿者' },
        { path: '/report', label: '流浪上报' }
      ]
    };
  },
  computed: {
    userInfo() {
      return this.$store.state.user.userInfo;
    },
    /** 当前登录角色：ADMIN / SHELTER / USER（未登录为空串） */
    userRole() {
      return (this.userInfo && this.userInfo.userType) || '';
    },
    isAdmin() {
      return this.userRole === 'ADMIN';
    },
    isShelter() {
      return this.userRole === 'SHELTER';
    },
    isUser() {
      return this.userRole === 'USER';
    },
    /** 账号入口目标：各角色回自己的工作台，不会落到 USER 专属页 */
    consolePath() {
      if (this.isAdmin) return '/admin/dashboard';
      if (this.isShelter) return '/shelter/workbench';
      return '/me';
    },
    consoleLabel() {
      if (this.isAdmin) return '管理后台';
      if (this.isShelter) return '机构工作台';
      return this.userInfo ? '个人中心' : '登录 / 注册';
    }
  },
  watch: {
    $route() {
      this.mobileMenuOpen = false;
      this.refreshUnread();
    }
  },
  mounted() {
    window.addEventListener('scroll', this.onScroll, { passive: true });
    this.refreshUnread();
    this.timer = setInterval(this.refreshUnread, 30000);
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.onScroll);
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    onScroll() {
      this.isScrolled = window.scrollY > 8;
    },
    isActive(path) {
      if (path === '/') return this.$route.path === '/';
      return this.$route.path.startsWith(path);
    },
    openLogin() {
      this.mobileMenuOpen = false;
      this.$refs.loginDialog.open();
    },
    onCommand(cmd) {
      if (cmd === 'logout') {
        this.$store.dispatch('user/logout').then(() => {
          this.$message.success('已退出登录');
          this.$router.push('/');
        });
        return;
      }
      const target = {
        'admin-console': '/admin/dashboard',
        'shelter-console': '/shelter/workbench',
        'shelter-chat': '/shelter/chat',
        me: '/me',
        applications: '/my/applications'
      }[cmd];
      if (target) this.$router.push(target);
    },
    /** 移动端菜单底部按钮：未登录去登录，已登录按角色去工作台 */
    onConsole() {
      if (!this.userInfo) {
        this.openLogin();
        return;
      }
      this.mobileMenuOpen = false;
      this.$router.push(this.consolePath);
    },
    refreshUnread() {
      // 站内未读数只对 C 端用户有意义（机构/管理员的咨询在各自工作台）
      if (!this.isUser) {
        this.unread = 0;
        return;
      }
      import('@/api/chat').then(({ chatUnreadTotal }) => {
        chatUnreadTotal().then((n) => { this.unread = n || 0; }).catch(() => {});
      });
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.portal {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: $bg-card;
}

.portal-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: $portal-header-h;
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid transparent;
  transition: border-color $duration-hover-out $ease-out-quart, background-color $duration-hover-out $ease-out-quart;

  &.scrolled {
    background: #fff;
    border-bottom-color: $border-color;
  }
}

.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: $space-8;
}

.brand {
  display: flex;
  align-items: center;
  gap: $space-2;

  &__logo {
    width: 32px;
    height: 32px;
  }

  &__name {
    font-size: 17px;
    font-weight: 600;
    color: $text-primary;
  }
}

.nav {
  display: flex;
  gap: $space-6;
  flex: 1;

  &__item {
    position: relative;
    height: $portal-header-h;
    display: inline-flex;
    align-items: center;
    font-size: 15px;
    color: $text-regular;
    transition: color $duration-hover-in $ease-out-quart;

    &:hover {
      color: $text-primary;
    }

    &.active {
      color: $text-primary;
      font-weight: 500;

      &::after {
        content: "";
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 2px;
        background: $color-primary;
        border-radius: 1px;
      }
    }
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: $space-4;

  .login-btn {
    color: $color-primary-text;
    padding: 0;
  }
}

.icon-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: $radius-circle;
  font-size: 18px;
  color: $text-regular;
  cursor: pointer;

  &:hover {
    background: $color-primary-light;
    color: $color-primary-text;
  }
}

.msg-badge {
  line-height: 1;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: $radius-base;

  &:hover {
    background: $bg-page;
  }

  &__avatar {
    width: 32px;
    height: 32px;
    border-radius: $radius-circle;
    object-fit: cover;
  }

  &__name {
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $text-primary;
    font-size: 14px;
  }
}

.menu-toggle {
  display: none;
  width: 44px;
  height: 44px;
  border: none;
  background: transparent;
  font-size: 22px;
  color: $text-primary;
  cursor: pointer;
}

.mobile-menu {
  position: fixed;
  inset: $portal-header-h 0 0 0;
  background: #fff;
  z-index: 99;

  &__body {
    padding-top: $space-8;
    display: flex;
    flex-direction: column;
  }

  &__item {
    padding: 16px 0;
    font-size: 18px;
    color: $text-primary;
    border-bottom: 1px solid $border-color;
  }

  &__cta {
    margin-top: $space-8;
  }
}

.portal-main {
  flex: 1;
}

.portal-footer {
  background: $bg-page;
  border-top: 1px solid $border-color;
  margin-top: 0;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-8;
  padding: $space-12 0;

  @media (max-width: 767px) {
    grid-template-columns: repeat(2, 1fr);
    padding: $space-8 0;
  }
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: $space-3;

  &__title {
    font-size: 14px;
    margin-bottom: $space-1;
  }
}

.footer-link {
  font-size: 14px;
  color: $text-secondary;
  transition: color $duration-hover-in $ease-out-quart;

  &:hover {
    color: $color-primary-text;
  }
}

.footer-meta {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: $space-2;
  padding: $space-5 0 $space-8;
  border-top: 1px solid $border-color;
  font-size: 12px;
  color: $text-secondary;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity $duration-panel $ease-out-quart;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 991px) {
  .nav {
    display: none;
  }

  .menu-toggle {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .user-chip__name {
    display: none;
  }
}
</style>
