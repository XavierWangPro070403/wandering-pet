<template>
  <div class="admin-layout">
    <!-- 左侧 220 深色导航（唯一深色底例外） -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar__brand">
        <img src="/imgs/logo-white.svg" alt="" class="sidebar__logo" />
        <span v-if="!collapsed" class="sidebar__title">流浪宠物领养</span>
      </div>
      <nav class="sidebar__nav" aria-label="工作台导航">
        <div v-for="group in menus" :key="group.title" class="sidebar__group">
          <p v-if="group.title && !collapsed" class="sidebar__group-title">{{ group.title }}</p>
          <router-link
            v-for="menu in group.items"
            :key="menu.path"
            :to="menu.path"
            class="sidebar__item"
            :class="{ active: isActive(menu.path) }"
            :title="menu.label">
            <i :class="menu.icon" aria-hidden="true"></i>
            <span v-if="!collapsed">{{ menu.label }}</span>
          </router-link>
        </div>
      </nav>
    </aside>

    <div class="admin-body">
      <!-- 顶部 60 白工具条 -->
      <header class="topbar">
        <button class="topbar__collapse" :aria-label="collapsed ? '展开侧栏' : '折叠侧栏'" @click="toggleSidebar">
          <i :class="collapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'" aria-hidden="true"></i>
        </button>

        <el-breadcrumb separator="/" class="topbar__breadcrumb" aria-label="面包屑">
          <el-breadcrumb-item v-for="(crumb, idx) in breadcrumbs" :key="idx" :to="idx === breadcrumbs.length - 1 ? null : { path: crumb.path }">
            {{ crumb.label }}
          </el-breadcrumb-item>
        </el-breadcrumb>

        <div class="topbar__actions">
          <el-badge :value="unread" :hidden="!unread" class="topbar__badge">
            <button class="topbar__icon-btn" aria-label="站内消息" @click="goNotifications">
              <i class="el-icon-bell" aria-hidden="true"></i>
            </button>
          </el-badge>
          <el-dropdown trigger="click" @command="onCommand">
            <span class="topbar__user" role="button" tabindex="0">
              <img class="topbar__avatar" :src="avatar" alt="" />
              <span class="topbar__username">{{ displayName }}</span>
              <i class="el-icon-arrow-down" aria-hidden="true"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item disabled>{{ roleLabel }}</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 #F7F8FA + 24 内边距 -->
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import { unreadCount } from '@/api/notification';

/**
 * 工作台布局（机构端/管理端共用，按角色渲染菜单，§8.11）
 */
export default {
  name: 'AdminLayout',
  data() {
    return {
      unread: 0,
      timer: null
    };
  },
  computed: {
    collapsed() {
      return this.$store.state.app.sidebarCollapsed;
    },
    userInfo() {
      return this.$store.state.user.userInfo || {};
    },
    isAdmin() {
      return this.userInfo.userType === 'ADMIN';
    },
    avatar() {
      return this.userInfo.avatar || '/imgs/avatar-default.svg';
    },
    displayName() {
      return this.userInfo.nickname || this.userInfo.username || '';
    },
    roleLabel() {
      return this.isAdmin ? '平台管理员' : '救助站账号';
    },
    menus() {
      if (this.isAdmin) {
        return [
          {
            title: '',
            items: [
              { path: '/admin/dashboard', label: '数据看板', icon: 'el-icon-odometer' },
              { path: '/admin/shelters', label: '机构审核', icon: 'el-icon-office-building' },
              { path: '/admin/users', label: '用户管理', icon: 'el-icon-user' },
              { path: '/admin/pets', label: '宠物全量管理', icon: 'el-icon-notebook-2' },
              { path: '/admin/articles', label: '内容管理', icon: 'el-icon-document' },
              { path: '/admin/rescues', label: '线索管理', icon: 'el-icon-map-location' },
              { path: '/admin/settings', label: '系统设置', icon: 'el-icon-setting' }
            ]
          }
        ];
      }
      return [
        {
          title: '',
          items: [
            { path: '/shelter/workbench', label: '工作台', icon: 'el-icon-odometer' },
            { path: '/shelter/pets', label: '宠物管理', icon: 'el-icon-notebook-2' },
            { path: '/shelter/applications', label: '领养审核', icon: 'el-icon-finished' },
            { path: '/shelter/followups', label: '回访管理', icon: 'el-icon-camera' },
            { path: '/shelter/donations', label: '募捐项目', icon: 'el-icon-present' },
            { path: '/shelter/donation-records', label: '捐赠明细', icon: 'el-icon-wallet' },
            { path: '/shelter/volunteers', label: '志愿者审核', icon: 'el-icon-thumb' },
            { path: '/shelter/chat', label: '在线咨询', icon: 'el-icon-chat-dot-round' }
          ]
        }
      ];
    },
    breadcrumbs() {
      const matched = this.$route.matched.filter((r) => r.meta && r.meta.title);
      const crumbs = [{ label: this.isAdmin ? '管理端' : '机构端', path: this.isAdmin ? '/admin/dashboard' : '/shelter/workbench' }];
      matched.forEach((r) => crumbs.push({ label: r.meta.title, path: r.path }));
      return crumbs;
    }
  },
  watch: {
    $route() {
      this.refreshUnread();
    }
  },
  mounted() {
    this.refreshUnread();
    this.timer = setInterval(this.refreshUnread, 30000);
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    toggleSidebar() {
      this.$store.commit('app/TOGGLE_SIDEBAR');
    },
    isActive(path) {
      return this.$route.path === path || this.$route.path.startsWith(path + '/');
    },
    goNotifications() {
      this.$router.push(this.isAdmin ? '/admin/settings?tab=logs' : '/shelter/chat');
    },
    onCommand(cmd) {
      if (cmd === 'logout') {
        this.$store.dispatch('user/logout').then(() => this.$router.replace('/login'));
      }
    },
    refreshUnread() {
      if (!this.$store.state.user.token) return;
      unreadCount().then((n) => { this.unread = n || 0; }).catch(() => {});
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.admin-layout {
  display: flex;
  min-height: 100vh;
  background: $bg-page;
}

.sidebar {
  width: $admin-sidebar-w;
  background: $bg-dark-nav;
  color: #C9CDD4;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  transition: width $duration-panel $ease-out-quart;
  overflow: hidden;

  &.collapsed {
    width: 64px;
  }

  &__brand {
    height: $admin-topbar-h;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  &__logo {
    width: 28px;
    height: 28px;
    flex-shrink: 0;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
  }

  &__nav {
    flex: 1;
    overflow-y: auto;
    padding: 12px 0;
  }

  &__group-title {
    font-size: 12px;
    color: #86909C;
    padding: 12px 20px 6px;
    white-space: nowrap;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 20px;
    height: 44px;
    color: #C9CDD4;
    font-size: 14px;
    border-left: 3px solid transparent;
    white-space: nowrap;
    transition: background-color $duration-hover-in $ease-out-quart, color $duration-hover-in $ease-out-quart;

    i {
      font-size: 16px;
      width: 18px;
      text-align: center;
    }

    &:hover {
      background: rgba(255, 255, 255, 0.06);
      color: #fff;
    }

    &.active {
      background: rgba(255, 122, 69, 0.14);
      border-left-color: $color-primary;
      color: #FFD9C9;
    }
  }
}

.admin-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: $admin-topbar-h;
  background: #fff;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;

  &__collapse {
    border: none;
    background: transparent;
    font-size: 18px;
    color: $text-regular;
    cursor: pointer;
    width: 40px;
    height: 40px;
    border-radius: $radius-base;

    &:hover {
      background: $bg-page;
    }
  }

  &__breadcrumb {
    flex: 1;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__icon-btn {
    width: 40px;
    height: 40px;
    border: none;
    background: transparent;
    border-radius: $radius-circle;
    font-size: 17px;
    color: $text-regular;
    cursor: pointer;

    &:hover {
      background: $bg-page;
    }
  }

  &__user {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: $radius-base;

    &:hover {
      background: $bg-page;
    }
  }

  &__avatar {
    width: 30px;
    height: 30px;
    border-radius: $radius-circle;
    object-fit: cover;
  }

  &__username {
    font-size: 14px;
    color: $text-primary;
  }
}

.admin-main {
  flex: 1;
  padding: 24px;
  min-width: 0;
}
</style>
