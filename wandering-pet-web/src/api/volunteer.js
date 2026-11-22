import request from '@/utils/request';

/** 报名志愿者 */
export const applyVolunteer = (data) => request.post('/volunteer-applications', data);

/** 我的报名 */
export const myVolunteerApplications = (params) => request.get('/volunteer-applications/my', { params });
