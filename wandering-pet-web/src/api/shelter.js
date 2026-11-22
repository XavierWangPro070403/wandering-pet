import request from '@/utils/request';

/** 救助站列表（已入驻） */
export const shelters = (city) => request.get('/shelters', { params: { city } });

/** 救助站详情 */
export const shelterDetail = (id) => request.get(`/shelters/${id}`);

/** 申请入驻 */
export const applyShelter = (data) => request.post('/shelters/apply', data);

/** 我的机构信息 */
export const myShelter = () => request.get('/shelters/me');

/** 更新机构信息 */
export const updateShelter = (data) => request.put('/shelters/me', data);

// ---------- 机构端 ----------

/** 本机构宠物分页 */
export const shelterPets = (params) => request.get('/shelter/pets', { params });

/** 发布宠物 */
export const createPet = (data) => request.post('/shelter/pets', data);

/** 编辑宠物 */
export const updatePet = (id, data) => request.put(`/shelter/pets/${id}`, data);

/** 删除宠物 */
export const deletePet = (id) => request.delete(`/shelter/pets/${id}`);

/** 宠物状态变更（批量） */
export const changePetStatus = (data) => request.post('/shelter/pets/status', data);

/** 健康档案列表 */
export const healthRecords = (petId) => request.get(`/shelter/pets/${petId}/health-records`);

/** 新增健康档案 */
export const createHealthRecord = (petId, data) => request.post(`/shelter/pets/${petId}/health-records`, data);

/** 更新健康档案 */
export const updateHealthRecord = (id, data) => request.put(`/shelter/pets/health-records/${id}`, data);

/** 删除健康档案 */
export const deleteHealthRecord = (id) => request.delete(`/shelter/pets/health-records/${id}`);

/** 发布宠物动态 */
export const createDynamic = (petId, data) => request.post(`/shelter/pets/${petId}/dynamics`, data);
