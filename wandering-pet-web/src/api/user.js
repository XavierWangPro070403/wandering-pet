import request from '@/utils/request';

/** 我的资料 */
export const myProfile = () => request.get('/users/me');

/** 更新资料 */
export const updateProfile = (data) => request.put('/users/me', data);

/** 领养资料（申请领养时自动填充） */
export const getAdopterProfile = () => request.get('/users/me/adopter-profile');

/** 保存领养资料（补全） */
export const updateAdopterProfile = (data) => request.put('/users/me/adopter-profile', data);

/** 实名认证 */
export const realName = (data) => request.post('/users/me/real-name', data);

/** 数字摘要 */
export const mySummary = () => request.get('/users/me/summary');

/** 地址列表 */
export const addresses = () => request.get('/users/me/addresses');

/** 新增地址 */
export const saveAddress = (data) => request.post('/users/me/addresses', data);

/** 更新地址 */
export const updateAddress = (id, data) => request.put(`/users/me/addresses/${id}`, data);

/** 删除地址 */
export const deleteAddress = (id) => request.delete(`/users/me/addresses/${id}`);
