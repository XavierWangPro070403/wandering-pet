import request from '@/utils/request';

// ---------- 机构端 ----------

/** 申请分页 */
export const shelterApplications = (params) => request.get('/shelter/applications', { params });

export const acceptApplication = (id) => request.post(`/shelter/applications/${id}/accept`);
export const interviewApplication = (id, data) => request.post(`/shelter/applications/${id}/interview`, data);
export const homeVisitApplication = (id, data) => request.post(`/shelter/applications/${id}/home-visit`, data);
export const approveApplication = (id, data) => request.post(`/shelter/applications/${id}/approve`, data);
export const rejectApplication = (id, data) => request.post(`/shelter/applications/${id}/reject`, data);
export const signApplication = (id, data) => request.post(`/shelter/applications/${id}/sign`, data);
export const completeApplication = (id) => request.post(`/shelter/applications/${id}/complete`);

/** 领养记录分页 */
export const shelterRecords = (params) => request.get('/shelter/adoption-records', { params });

/** 退养 */
export const returnRecord = (id, data) => request.post(`/shelter/adoption-records/${id}/return`, data);

// ---------- 回访 ----------

export const shelterFollowUps = (params) => request.get('/shelter/follow-ups', { params });
export const remindFollowUp = (id) => request.post(`/shelter/follow-ups/${id}/remind`);
export const archiveFollowUp = (id) => request.post(`/shelter/follow-ups/${id}/archive`);

// ---------- 线索处理 ----------

export const processReport = (id, data) => request.post(`/shelter/rescue-reports/${id}/process`, data);
export const resolveReport = (id, data) => request.post(`/shelter/rescue-reports/${id}/resolve`, data);

// ---------- 志愿者 ----------

export const shelterVolunteers = (params) => request.get('/shelter/volunteer-applications', { params });
export const auditVolunteer = (id, data) => request.post(`/shelter/volunteer-applications/${id}/audit`, data);

// ---------- 募捐 ----------

export const shelterProjects = () => request.get('/shelter/donation-projects');
export const createProject = (data) => request.post('/shelter/donation-projects', data);
export const updateProject = (id, data) => request.put(`/shelter/donation-projects/${id}`, data);
export const closeProject = (id) => request.post(`/shelter/donation-projects/${id}/close`);
export const shelterDonationRecords = (params) => request.get('/shelter/donation-projects/records', { params });

// ---------- 工作台统计 ----------

export const shelterWorkbench = () => request.get('/statistics/shelter-workbench');
