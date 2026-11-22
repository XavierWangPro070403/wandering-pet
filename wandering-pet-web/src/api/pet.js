import request from '@/utils/request';

/** 宠物分页列表（门户） */
export const pagePets = (params) => request.get('/pets', { params });

/** 宠物详情 */
export const petDetail = (id) => request.get(`/pets/${id}`);

/** 相似推荐 */
export const similarPets = (id, limit = 4) => request.get(`/pets/${id}/similar`, { params: { limit } });

/** 记录一次浏览 */
export const recordView = (id) => request.post(`/pets/${id}/view`);

/** 宠物动态 */
export const petDynamics = (id, limit = 10) => request.get(`/pets/${id}/dynamics`, { params: { limit } });

/** 物种分类 */
export const categories = () => request.get('/pet-categories');

/** 品种 */
export const breeds = (categoryId) => request.get('/pet-breeds', { params: { categoryId } });

/** 收藏 */
export const favorite = (targetType, targetId) => request.post('/favorites', { targetType, targetId });

/** 取消收藏 */
export const unfavorite = (targetType, targetId) => request.delete(`/favorites/${targetType}/${targetId}`);

/** 我的宠物收藏 */
export const myFavorites = (params) => request.get('/favorites/my', { params });

/** 收藏状态 */
export const favoriteStatus = (targetType, targetId) =>
  request.get('/favorites/status', { params: { targetType, targetId } });
