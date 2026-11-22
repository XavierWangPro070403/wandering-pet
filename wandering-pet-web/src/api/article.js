import request from '@/utils/request';

/** 文章分页（已发布） */
export const pageArticles = (params) => request.get('/articles', { params });

/** 文章详情（含评论） */
export const articleDetail = (id) => request.get(`/articles/${id}`);

/** 点赞 */
export const likeArticle = (id) => request.post(`/articles/${id}/like`);

/** 发表评论 */
export const createComment = (data) => request.post('/comments', data);

// ---------- 管理端 ----------

export const adminArticles = (params) => request.get('/admin/articles', { params });
export const saveArticle = (data) => request.post('/admin/articles', data);
export const updateArticle = (id, data) => request.put(`/admin/articles/${id}`, data);
export const deleteArticle = (id) => request.delete(`/admin/articles/${id}`);
export const topArticle = (id, top) => request.post(`/admin/articles/${id}/top`, { top });
export const changeArticleStatus = (id, status) => request.post(`/admin/articles/${id}/status`, { status });

export const adminComments = (params) => request.get('/admin/comments', { params });
export const auditComment = (id, pass) => request.post(`/admin/comments/${id}/audit`, { pass });
export const deleteComment = (id) => request.delete(`/admin/comments/${id}`);
