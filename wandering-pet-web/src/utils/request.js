import axios from 'axios';
import { Message } from 'element-ui';
import store from '@/store';
import { getToken, removeToken, removeUser } from './auth';
import router from '@/router';

/**
 * axios 统一封装（§7.2）
 * - baseURL 环境变量注入，超时 15s
 * - 请求拦截：注入 Bearer Token、写操作带 X-Trace-Id、支持 showLoading
 * - 响应拦截：code===200 → data；401 清登录态跳登录；403 无权限；其他 code 统一提示
 */
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api/v1',
  timeout: 15000
});

let loadingCount = 0;
function showLoading() {
  if (loadingCount === 0) {
    store.commit('app/SET_LOADING', true);
  }
  loadingCount++;
}

function hideLoading() {
  loadingCount = Math.max(0, loadingCount - 1);
  if (loadingCount === 0) {
    store.commit('app/SET_LOADING', false);
  }
}

// 请求拦截
service.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    if (['post', 'put', 'delete'].includes((config.method || '').toLowerCase())) {
      config.headers['X-Trace-Id'] = genTraceId();
    }
    if (config.showLoading) {
      showLoading();
      config._loading = true;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截
service.interceptors.response.use(
  (response) => {
    if (response.config._loading) {
      hideLoading();
    }
    // 文件流直接返回
    if (response.config.responseType === 'blob') {
      return response;
    }
    const res = response.data;
    if (res.code === 200) {
      return res.data !== undefined ? res.data : res;
    }
    if (res.code === 401) {
      clearLoginAndRedirect();
      return Promise.reject(new Error(res.message || '请先登录'));
    }
    if (res.code === 403) {
      Message.error(res.message || '没有操作权限');
      return Promise.reject(new Error(res.message || '没有操作权限'));
    }
    Message.error(res.message || `请求失败（${res.code}）`);
    return Promise.reject(new Error(res.message || '请求失败'));
  },
  (error) => {
    if (error.config && error.config._loading) {
      hideLoading();
    }
    if (error.response) {
      const body = error.response.data || {};
      if (body.code === 401) {
        clearLoginAndRedirect();
      } else if (body.code === 403) {
        Message.error(body.message || '没有操作权限');
      } else {
        const trace = error.response.headers && error.response.headers['x-trace-id'];
        Message.error((body.message || '网络异常，请稍后再试') + (trace ? `（traceId: ${trace}）` : ''));
      }
    } else {
      Message.error('网络连接失败，请检查网络后重试');
    }
    return Promise.reject(error);
  }
);

function clearLoginAndRedirect() {
  removeToken();
  removeUser();
  store.commit('user/RESET');
  const current = router.currentRoute;
  const redirect = current && current.path !== '/login' ? `?redirect=${encodeURIComponent(current.fullPath)}` : '';
  Message.warning('请先登录');
  if (current && current.meta && current.meta.admin) {
    router.replace(`/login${redirect}`);
  }
}

function genTraceId() {
  return Math.random().toString(16).slice(2, 10);
}

/** 文件下载（导出 Excel 等） */
service.download = function download(url, params, filename) {
  return service.get(url, { params, responseType: 'blob' }).then((res) => {
    const blob = new Blob([res.data]);
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
  });
};

export default service;
