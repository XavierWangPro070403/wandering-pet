/**
 * 聊天 WebSocket 管理器（实时接收）
 * - 发送仍走 HTTP 接口（校验/持久化/未读数在服务端），本通道只负责实时接收推送
 * - 断线自动重连（退避封顶 30s），25s 心跳保活
 * - 登录态变化（登录/登出/换绑）请调用 resetChatSocket() 重建连接
 */
import { API_BASE } from './config.js';
import http from './request.js';

let task = null;
let retryTimer = null;
let hbTimer = null;
let retryCount = 0;
const listeners = new Set();

function connect() {
  if (task) return;
  const token = http.getToken();
  if (!token) return;
  const url = API_BASE.replace(/^http/, 'ws') + '/ws/chat?token=' + token;
  task = uni.connectSocket({ url, complete: () => {} });
  task.onOpen(() => {
    retryCount = 0;
    startHeartbeat();
  });
  task.onMessage((res) => {
    let data;
    try {
      data = JSON.parse(res.data);
    } catch (e) {
      return;
    }
    if (data.type === 'PONG') return;
    listeners.forEach((fn) => {
      try {
        fn(data);
      } catch (e) {
        /* 单个监听器异常不影响其他 */
      }
    });
  });
  task.onClose(() => {
    task = null;
    stopHeartbeat();
    if (http.getToken()) scheduleReconnect();
  });
  task.onError(() => {
    try {
      if (task) task.close({});
    } catch (e) {
      /* 已关闭 */
    }
  });
}

function scheduleReconnect() {
  if (retryTimer) return;
  retryCount += 1;
  const delay = Math.min(30000, 5000 * retryCount);
  retryTimer = setTimeout(() => {
    retryTimer = null;
    connect();
  }, delay);
}

function startHeartbeat() {
  stopHeartbeat();
  hbTimer = setInterval(() => {
    try {
      if (task) task.send({ data: JSON.stringify({ type: 'PING' }) });
    } catch (e) {
      /* 发送失败等待 onClose 重连 */
    }
  }, 25000);
}

function stopHeartbeat() {
  if (hbTimer) {
    clearInterval(hbTimer);
    hbTimer = null;
  }
}

/** 订阅实时推送，返回取消订阅函数；首次调用会自动建连 */
export function subscribeChatPush(fn) {
  listeners.add(fn);
  connect();
  return () => listeners.delete(fn);
}

/** 登录态变化时重建连接（登录 / 退出 / 换绑） */
export function resetChatSocket() {
  if (retryTimer) {
    clearTimeout(retryTimer);
    retryTimer = null;
  }
  retryCount = 0;
  if (task) {
    const t = task;
    task = null;
    try {
      t.close({});
    } catch (e) {
      /* 已关闭 */
    }
  }
  connect();
}
