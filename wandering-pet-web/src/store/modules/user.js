import { login as loginApi, portalLogin, register, smsLogin as smsLoginApi, logout as logoutApi, getMe } from '@/api/auth';
import { getToken, setToken, removeToken, getUser, setUser, removeUser } from '@/utils/auth';

/**
 * 用户模块：登录态、用户信息、权限
 */
const state = {
  token: getToken(),
  userInfo: getUser() || null,
  permissions: (getUser() && getUser().permissions) || []
};

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token;
    setToken(token);
  },
  SET_USER_INFO(state, info) {
    state.userInfo = info;
    state.permissions = (info && info.permissions) || [];
    setUser(info);
  },
  RESET(state) {
    state.token = '';
    state.userInfo = null;
    state.permissions = [];
    removeToken();
    removeUser();
  }
};

const actions = {
  /** 后台登录（管理员/救助站） */
  async login({ commit }, form) {
    const data = await loginApi(form);
    commit('SET_TOKEN', data.token);
    commit('SET_USER_INFO', data);
    return data;
  },

  /** Web 用户端登录 */
  async portalLogin({ commit }, form) {
    const data = await portalLogin(form);
    commit('SET_TOKEN', data.token);
    commit('SET_USER_INFO', data);
    return data;
  },

  /** Web 用户端注册 */
  async register({ commit }, form) {
    const data = await register(form);
    commit('SET_TOKEN', data.token);
    commit('SET_USER_INFO', data);
    return data;
  },

  /** 手机号 + 短信验证码登录（未注册自动创建） */
  async smsLogin({ commit }, form) {
    const data = await smsLoginApi(form);
    commit('SET_TOKEN', data.token);
    commit('SET_USER_INFO', data);
    return data;
  },

  /** 微信扫码登录成功（token 由轮询接口换出） */
  async qrLoginSuccess({ commit }, data) {
    commit('SET_TOKEN', data.token);
    commit('SET_USER_INFO', data);
    return data;
  },

  /** 拉取当前用户信息（守卫调用，保证过期数据被刷新） */
  async getInfo({ commit }) {
    const data = await getMe();
    commit('SET_TOKEN', state.token);
    commit('SET_USER_INFO', data);
    return data;
  },

  /** 退出登录 */
  async logout({ commit }) {
    try {
      await logoutApi();
    } catch (e) {
      // 忽略登出接口异常
    }
    commit('RESET');
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
};
