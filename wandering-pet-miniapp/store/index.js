import Vue from 'vue';
import Vuex from 'vuex';
import { getMe } from '../common/api.js';

Vue.use(Vuex);

const store = new Vuex.Store({
  state: {
    token: uni.getStorageSync('wp_token') || '',
    userInfo: null
  },
  getters: {
    isLoggedIn: (state) => !!state.token,
    isUser: (state) => !!(state.userInfo && state.userInfo.userType === 'USER'),
    isShelter: (state) => !!(state.userInfo && state.userInfo.userType === 'SHELTER'),
    nickname: (state) => (state.userInfo && state.userInfo.nickname) || '未登录'
  },
  mutations: {
    setToken(state, token) {
      state.token = token;
      uni.setStorageSync('wp_token', token || '');
    },
    setUserInfo(state, info) {
      state.userInfo = info;
      if (info) {
        uni.setStorageSync('wp_user', JSON.stringify(info));
      }
    },
    clear(state) {
      state.token = '';
      state.userInfo = null;
      uni.removeStorageSync('wp_token');
      uni.removeStorageSync('wp_user');
    }
  },
  actions: {
    /** 登录成功后保存 */
    async loginSuccess({ commit }, loginVO) {
      commit('setToken', loginVO.token);
      commit('setUserInfo', loginVO);
    },
    /** 拉取个人信息（启动/下拉刷新时调用） */
    async loadMe({ commit, state }) {
      if (!state.token) return null;
      try {
        const me = await getMe();
        // 服务端若下发了新 token（续签场景）则同步，避免手里留着旧 token 导致后续 401
        if (me && me.token) commit('setToken', me.token);
        commit('setUserInfo', me);
        return me;
      } catch (e) {
        return null;
      }
    },
    async logout({ commit }) {
      commit('clear');
    }
  }
});

export default store;
