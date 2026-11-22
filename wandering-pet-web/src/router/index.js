import Vue from 'vue';
import VueRouter from 'vue-router';
import store from '@/store';
import { getToken, getUser } from '@/utils/auth';
import portalRoutes from './modules/portal';
import adminRoutes from './modules/admin';
import shelterRoutes from './modules/shelter';

Vue.use(VueRouter);

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/PortalLayout.vue'),
    children: portalRoutes
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { admin: true },
    children: adminRoutes
  },
  {
    path: '/shelter',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { admin: true },
    children: shelterRoutes
  },
  { path: '*', redirect: '/' }
];

const router = new VueRouter({
  mode: 'history',
  routes,
  scrollBehavior() {
    return { x: 0, y: 0 };
  }
});

/**
 * Vue Router 3.6 起：守卫里用 next('/xxx') 重定向，会让「原导航」的 push/replace Promise
 * 以 NavigationFailure 拒绝（redirected / cancelled / aborted / duplicated）。
 * 这些是正常的导航结果而不是异常，不处理就会在控制台报
 * "Redirected when going from "/" to "/me" via a navigation guard."
 * 这里统一吞掉这几类失败，其它真实错误继续抛出。
 */
function isBenignNavigationFailure(err) {
  if (!err) return true;
  if (err.name === 'NavigationDuplicated') return true;
  if (typeof err.type === 'number') return true; // NavigationFailure：1=aborted 2=cancelled 3=duplicated 4=redirected
  return /Redirected when going|Navigation cancelled|Navigation aborted|redundant navigation/i.test(err.message || '');
}

function withFailureHandling(promise) {
  return promise && typeof promise.catch === 'function'
    ? promise.catch((err) => (isBenignNavigationFailure(err) ? err : Promise.reject(err)))
    : promise;
}

const originalPush = VueRouter.prototype.push;
const originalReplace = VueRouter.prototype.replace;

VueRouter.prototype.push = function push(location, onComplete, onAbort) {
  if (onComplete || onAbort) return originalPush.call(this, location, onComplete, onAbort);
  return withFailureHandling(originalPush.call(this, location));
};

VueRouter.prototype.replace = function replace(location, onComplete, onAbort) {
  if (onComplete || onAbort) return originalReplace.call(this, location, onComplete, onAbort);
  return withFailureHandling(originalReplace.call(this, location));
};

// 全局前置守卫：token → 用户信息 → 角色校验 → 标题
router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} · 流浪宠物领养` : '流浪宠物领养系统';

  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth);
  const roles = to.meta.roles;
  const token = getToken();

  if (!requiresAuth) {
    next();
    return;
  }
  if (!token) {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`);
    return;
  }
  // 拉取/刷新用户信息（store 有数据且非刷新场景可跳过）
  let user = store.state.user.userInfo;
  try {
    if (!user || to.meta.refreshUser) {
      user = await store.dispatch('user/getInfo');
    }
  } catch (e) {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`);
    return;
  }
  if (roles && roles.length && !roles.includes(user.userType)) {
    // 角色不符：回各自首页（next(location) 属于重定向，失败由上面的 withFailureHandling 兜底）
    const home = user.userType === 'ADMIN' ? '/admin/dashboard'
      : user.userType === 'SHELTER' ? '/shelter/workbench' : '/';
    if (to.path === home) {
      next(false);
      return;
    }
    next(home);
    return;
  }
  next();
});

export default router;
