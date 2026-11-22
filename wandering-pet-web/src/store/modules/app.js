/**
 * 应用状态：全局 loading、侧栏折叠
 */
const state = {
  loading: false,
  sidebarCollapsed: false
};

const mutations = {
  SET_LOADING(state, loading) {
    state.loading = loading;
  },
  TOGGLE_SIDEBAR(state) {
    state.sidebarCollapsed = !state.sidebarCollapsed;
  }
};

export default {
  namespaced: true,
  state,
  mutations
};
