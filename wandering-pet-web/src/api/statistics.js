import request from '@/utils/request';

/** 门户数字统计 */
export const portalSummary = () => request.get('/statistics/portal-summary');

/** 管理端看板 */
export const adminDashboard = () => request.get('/statistics/admin-dashboard');
