import request from '@/utils/request';

/** 创建/获取与机构的会话 */
export const getOrCreateSession = (shelterId) => request.post('/chat/sessions', { shelterId });

/** 我的会话列表 */
export const mySessions = () => request.get('/chat/sessions');

/** 会话消息分页 */
export const sessionMessages = (id, params) => request.get(`/chat/sessions/${id}/messages`, { params });

/** 发送消息 */
export const sendMessage = (id, data) => request.post(`/chat/sessions/${id}/messages`, data);

/** 标记会话已读 */
export const readSession = (id) => request.post(`/chat/sessions/${id}/read`);

/** 撤回消息（仅发送者，2 分钟内） */
export const recallMessage = (messageId) => request.post(`/chat/messages/${messageId}/recall`);

/** 删除消息（仅对自己隐藏） */
export const deleteMessage = (messageId) => request.delete(`/chat/messages/${messageId}`);

/** 未读总数 */
export const chatUnreadTotal = () => request.get('/chat/unread-total');
