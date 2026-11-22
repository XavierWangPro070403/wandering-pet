import { getDictAll } from '@/api/system';

/**
 * 数据字典模块：全量缓存（code → label 映射）
 */
const state = {
  dicts: null // { pet_status: [{code,label}] }
};

const mutations = {
  SET_DICTS(state, dicts) {
    state.dicts = dicts;
  }
};

const actions = {
  async loadDicts({ state, commit }) {
    if (state.dicts) return state.dicts;
    const data = await getDictAll();
    commit('SET_DICTS', data || {});
    return data;
  }
};

const getters = {
  /** dictLabel(state, 'pet_status', 'AVAILABLE') */
  label: (state) => (type, code) => {
    if (!state.dicts || !state.dicts[type]) return code;
    const item = state.dicts[type].find((d) => d.code === String(code));
    return item ? item.label : code;
  },
  options: (state) => (type) => (state.dicts && state.dicts[type]) || []
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
};
