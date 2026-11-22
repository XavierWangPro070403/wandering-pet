import request from '@/utils/request';

/** 上报线索 */
export const createReport = (data) => request.post('/rescue-reports', data);

/** 我的上报 */
export const myReports = (params) => request.get('/rescue-reports/my', { params });

// ---------- 管理端 ----------

export const adminReports = (params) => request.get('/admin/rescue-reports', { params });
export const assignReport = (id, data) => request.post(`/admin/rescue-reports/${id}/assign`, data);
export const closeReport = (id) => request.post(`/admin/rescue-reports/${id}/close`);
