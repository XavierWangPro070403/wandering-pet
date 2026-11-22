import store from '@/store';

/**
 * 按钮级权限指令：v-permission="'pet:create'"，无权限则移除元素
 */
const permission = {
  inserted(el, binding) {
    const { value } = binding;
    if (!value) return;
    const perms = (store.state.user.permissions || []);
    const isAdmin = store.state.user.userInfo && store.state.user.userInfo.userType === 'ADMIN';
    if (!isAdmin && !perms.includes(value)) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
};

export default {
  install(Vue) {
    Vue.directive('permission', permission);
  }
};
