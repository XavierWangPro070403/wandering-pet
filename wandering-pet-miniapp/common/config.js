/**
 * 小程序端配置
 * BASE_URL 修改为你的后端地址；微信开发者工具需勾选「不校验合法域名」
 */
export const BASE_URL = 'http://localhost:8080';

export const API_BASE = BASE_URL + '/api/v1';

export const portalSummaryUrl = API_BASE + '/statistics/portal-summary';
