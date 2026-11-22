/**
 * 聊天 WebSocket 管理器（实时接收）
 * - 发送仍走 HTTP 接口（校验/持久化/未读数在服务端），本通道只负责实时接收推送
 * - 断线自动重连（退避封顶 30s），25s 心跳保活
 * - 登录态变化（登录/登出）请调用 resetChatSocket() 重建连接
 */
import { getToken } from './auth';

const listeners = new Set();
let socket = null;
let retryTimer = null;
let hbTimer = null;
let retryCount = 0;

function connect() {
  if (socket) return;
  const token = getToken();
  if (!token) return;
  const wsProtocol = location.protocol === 'https:' ? 'wss' : 'ws';
  socket = new WebSocket(`${wsProtocol}://${location.host}/ws/chat?token=${encodeURIComponent(token)}`);
  socket.onopen = () => {
    retryCount = 0;
    startHeartbeat();
  };
  socket.onmessage = (e) => {
    let data;
    try {
      data = JSON.parse(e.data);
    } catch (err) {
      return;
    }
    if (data.type === 'PONG') return;
    listeners.forEach((fn) => {
      try {
        fn(data);
      } catch (err) {
        /* 单个监听器异常不影响其他 */
      }
    });
  };
  socket.onclose = () => {
    socket = null;
    stopHeartbeat();
    if (getToken()) scheduleReconnect();
  };
  socket.onerror = () => {
    try {
      if (socket) socket.close();
    } catch (err) {
      /* 已关闭 */
    }
  };
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
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'PING' }));
      }
    } catch (err) {
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
  if (socket) {
    const s = socket;
    socket = null;
    try {
      s.close();
    } catch (err) {
      /* 已关闭 */
    }
  }
  connect();
}
