import request from '@/utils/request';

/** 进行中项目 */
export const ongoingProjects = () => request.get('/donation-projects');

/** 项目详情 */
export const projectDetail = (id) => request.get(`/donation-projects/${id}`);

/** 捐赠（模拟支付） */
export const donate = (id, data) => request.post(`/donation-projects/${id}/donate`, data);

/** 捐赠明细 */
export const projectRecords = (id, limit) => request.get(`/donation-projects/${id}/records`, { params: { limit } });

/** 捐赠榜 */
export const projectRank = (id, topN) => request.get(`/donation-projects/${id}/rank`, { params: { topN } });

// ---------- 管理端 ----------

export const adminDonationRecords = (params) => request.get('/admin/donation-records', { params });
