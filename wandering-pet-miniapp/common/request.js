/**
 * 请求封装（ajax 二次封装，§2.3）：
 * - uni.request 统一包装，含拦截、loading、错误提示、401 重登录
 * - Token 注入与 401 处理
 * - 分页/时间等公共参数处理
 */

// 统一从 config.js 读取后端地址（改地址只需改 common/config.js）
import { API_BASE } from './config.js';

const BASE_URL = API_BASE;

/**
 * 查询参数清洗（GET/DELETE）：
 * uni.request 会把值为 null 的参数序列化成字符串 "null" 拼进 URL，
 * 后端 Long/Integer 类型参数（如 categoryId）会把 "null" 当文本解析，
 * 抛出类型转换异常（Failed to convert ... For input string: "null"）。
 * 这里统一过滤 null / undefined / ''，语义等同于"不传该查询条件"。
 */
function cleanParams(params) {
  if (!params || typeof params !== 'object') {
    return params;
  }
  const cleaned = {};
  Object.keys(params).forEach((key) => {
    const value = params[key];
    if (value !== null && value !== undefined && value !== '') {
      cleaned[key] = value;
    }
  });
  return cleaned;
}

/** HTTP 请求（Promise 化） */
function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    header = {},
    showLoading = true,
    showError = true
  } = options;
  const httpMethod = String(method).toUpperCase();

  if (showLoading) {
    uni.showLoading({ title: '加载中', mask: true });
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data: (httpMethod === 'GET' || httpMethod === 'DELETE') ? cleanParams(data) : data,
      timeout: 15000,
      header: Object.assign(
        {},
        header,
        getToken() ? { Authorization: 'Bearer ' + getToken() } : {}
      ),
      success: (res) => {
        if (showLoading) uni.hideLoading();
        const body = res.data || {};
        if (res.statusCode === 200 && body.code === 200) {
          return resolve(body.data);
        }
        if (body.code === 401) {
          handleUnauthorized(body.message);
          return reject(new Error(body.message || '请先登录'));
        }
        if (showError) {
          uni.showToast({ title: body.message || ('请求失败(' + body.code + ')'), icon: 'none' });
        }
        reject(new Error(body.message || '请求失败'));
      },
      fail: (err) => {
        if (showLoading) uni.hideLoading();
        if (showError) {
          uni.showToast({ title: '网络连接失败，请检查网络', icon: 'none' });
        }
        reject(err);
      }
    });
  });
}

function getToken() {
  return uni.getStorageSync('wp_token') || '';
}

function clearAuth() {
  uni.removeStorageSync('wp_token');
  uni.removeStorageSync('wp_user');
}

/** 401 去重：并发请求同时失败时只处理一次，避免连续压入多个登录页 */
let authExpiredLocked = false;

/**
 * 登录态失效统一处理：
 * 1. 清本地存储（Vuex 由 App.vue 的 auth:expired 监听同步清空，
 *    否则登录页会因为 store 里还有 token 而显示成"绑定手机号"）；
 * 2. 通过全局事件交给 App.vue 决定要不要跳登录页（tab 页不打扰用户）；
 * 3. 1.5s 内只处理一次。
 */
function handleUnauthorized(message) {
  clearAuth();
  if (authExpiredLocked) return;
  authExpiredLocked = true;
  uni.showToast({ title: message || '登录已失效，请重新登录', icon: 'none' });
  uni.$emit('auth:expired');
  setTimeout(() => {
    authExpiredLocked = false;
  }, 1500);
}


// ---------------- 语义化方法 ----------------
const get = (url, params, options = {}) =>
  request(Object.assign({ url, method: 'GET', data: params }, options));
const post = (url, data, options = {}) =>
  request(Object.assign({ url, method: 'POST', data }, options));
const put = (url, data, options = {}) =>
  request(Object.assign({ url, method: 'PUT', data }, options));
const del = (url, data, options = {}) =>
  request(Object.assign({ url, method: 'DELETE', data }, options));

/**
 * 上传文件（图片）：走后端统一上传接口（MinIO）
 *
 * 可靠性保障（三层）：
 * 1. onProgressUpdate 实时进度：上传中显示百分比，连接异常时进度会停在 0% 一眼可见；
 * 2. 平台 timeout（支持的端生效）；
 * 3. 自实现 30s 强制超时 + task.abort()：部分基础库/环境不支持 timeout 参数、
 *    或连接挂起不触发回调时，也能保证 Promise 必然 settle，页面不会卡死。
 */
function upload(filePath, bizType = 'pet') {
  uni.showLoading({ title: '上传中 0%', mask: true });
  return new Promise((resolve, reject) => {
    let settled = false;
    const finish = (fn, arg) => {
      if (settled) return;
      settled = true;
      clearTimeout(forceTimer);
      uni.hideLoading();
      fn(arg);
    };
    const forceReject = (msg) => finish(() => reject(new Error(msg)));

    const task = uni.uploadFile({
      url: BASE_URL + '/file/upload',
      filePath,
      name: 'file',
      formData: { bizType },
      timeout: 20000,
      header: getToken() ? { Authorization: 'Bearer ' + getToken() } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data);
          if (body.code === 200) return finish(resolve, body.data);
          if (body.code === 401) {
            handleUnauthorized(body.message);
            return forceReject(body.message || '请先登录');
          }
          uni.showToast({ title: body.message || '上传失败', icon: 'none' });
          forceReject(body.message || '上传失败');
        } catch (e) {
          forceReject('上传响应解析失败');
        }
      },
      fail: (err) => {
        // 真机连 localhost / 域名校验未放行 / 后端未启动 都会走到这里
        forceReject('网络连接失败，请检查网络与 BASE_URL 配置');
      }
    });

    // 进度百分比（showLoading 可重复调用更新文案）
    if (task && task.onProgressUpdate) {
      task.onProgressUpdate((p) => {
        if (settled) return;
        uni.showLoading({ title: '上传中 ' + p.progress + '%', mask: true });
      });
    }

    // 自实现强制超时：不依赖平台 timeout 支持，超时后 abort 并必然 settle
    const forceTimer = setTimeout(() => {
      if (!settled) {
        try {
          if (task && task.abort) task.abort();
        } catch (e) {
          /* 已结束 */
        }
        forceReject('上传超时，请检查网络或 BASE_URL 配置（真机需改为电脑局域网 IP）');
      }
    }, 30000);
  });
}

export default { request, get, post, put, del, upload, BASE_URL, getToken, clearAuth };
