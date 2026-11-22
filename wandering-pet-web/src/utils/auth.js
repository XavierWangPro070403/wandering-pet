/**
 * 登录态存取：token 与用户信息持久化（localStorage）
 */
const TOKEN_KEY = 'wp_token';
const USER_KEY = 'wp_user';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token || '');
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null');
  } catch (e) {
    return null;
  }
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user || {}));
}

export function removeUser() {
  localStorage.removeItem(USER_KEY);
}
