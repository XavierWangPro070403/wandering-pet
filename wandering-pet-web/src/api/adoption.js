import request from '@/utils/request';

/** 提交领养申请 */
export const submitApplication = (data) => request.post('/adoption-applications', data);

/** 我的申请分页 */
export const myApplications = (params) => request.get('/adoption-applications/my', { params });

/** 申请详情 */
export const applicationDetail = (id) => request.get(`/adoption-applications/${id}`);

/** 状态流水 */
export const flowLogs = (id) => request.get(`/adoption-applications/${id}/flow-logs`);

/** 撤回 */
export const cancelApplication = (id) => request.post(`/adoption-applications/${id}/cancel`);

/** 我的领养记录（含回访） */
export const myRecords = () => request.get('/adoption-applications/my/records');

/** 领养记录详情 */
export const recordDetail = (id) => request.get(`/adoption-applications/records/${id}`);
