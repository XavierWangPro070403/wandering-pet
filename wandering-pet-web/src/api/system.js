import request from '@/utils/request';

/** 字典全量 */
export const getDictAll = () => request.get('/dicts');

// ---------- 管理端：系统设置 ----------

export const adminDicts = () => request.get('/admin/dicts');
export const adminDictItems = (dictType) => request.get('/admin/dict-items', { params: { dictType } });
export const createDictItem = (data) => request.post('/admin/dict-items', data);
export const updateDictItem = (id, data) => request.put(`/admin/dict-items/${id}`, data);
export const deleteDictItem = (id) => request.delete(`/admin/dict-items/${id}`);

export const adminRoles = () => request.get('/admin/roles');
export const permissionTree = () => request.get('/admin/permissions');
export const assignRolePerms = (roleId, permIds) => request.put(`/admin/roles/${roleId}/permissions`, { permIds });

export const adminSysUsers = (params) => request.get('/admin/sys-users', { params });
export const createSysUser = (data) => request.post('/admin/sys-users', data);
export const toggleSysUser = (id, enable) => request.post(`/admin/sys-users/${id}/status`, { enable });
export const resetSysUserPassword = (id, newPassword) =>
  request.post(`/admin/sys-users/${id}/reset-password`, { newPassword });

export const adminOperationLogs = (params) => request.get('/admin/operation-logs', { params });

// ---------- 管理端：机构审核 ----------

export const adminShelters = (params) => request.get('/admin/shelters', { params });
export const auditShelter = (id, data) => request.post(`/admin/shelters/${id}/audit`, data);

// ---------- 管理端：用户 ----------

export const adminUsers = (params) => request.get('/admin/users', { params });
export const toggleUser = (id, enable) => request.post(`/admin/users/${id}/status`, { enable });
