import request from '@/utils/request';

/** 我的消息分页 */
export const myNotifications = (params) => request.get('/notifications', { params });

/** 标记已读 */
export const readNotification = (id) => request.post(`/notifications/${id}/read`);

/** 全部已读 */
export const readAllNotifications = () => request.post('/notifications/read-all');

/** 未读数 */
export const unreadCount = () => request.get('/notifications/unread-count');

/** 批量推送（管理员） */
export const broadcast = (data) => request.post('/admin/notifications/broadcast', data);
