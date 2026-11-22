/**
 * API 模块：与后端 Controller 一一对应
 */
import http from './request.js';

// ---------- 认证 ----------
export const wxLogin = (code, nickname, avatar) =>
  http.post('/auth/wx-login', { code, nickname, avatar });
export const bindWechat = (code) => http.post('/auth/bind-wechat', { code });
export const getMe = () => http.get('/auth/me', {}, { showLoading: false });

// ---------- 宠物 ----------
export const pagePets = (params) => http.get('/pets', params);
export const petDetail = (id) => http.get('/pets/' + id);
export const recordView = (id) => http.post('/pets/' + id + '/view', {}, { showLoading: false });
export const petCategories = () => http.get('/pet-categories', {}, { showLoading: false });
export const petBreeds = (categoryId) => http.get('/pet-breeds', { categoryId }, { showLoading: false });
export const favorite = (targetType, targetId) => http.post('/favorites', { targetType, targetId });
export const unfavorite = (targetType, targetId) => http.del('/favorites/' + targetType + '/' + targetId);
export const myFavorites = (params) => http.get('/favorites/my', params);

// ---------- 领养 ----------
export const submitApplication = (data) => http.post('/adoption-applications', data);
export const myApplications = (params) => http.get('/adoption-applications/my', params);
export const applicationDetail = (id) => http.get('/adoption-applications/' + id);
export const cancelApplication = (id) => http.post('/adoption-applications/' + id + '/cancel');
export const myRecords = () => http.get('/adoption-applications/my/records');

// ---------- 回访 ----------
export const myFollowUps = (params) => http.get('/follow-ups/my', params);
export const submitFollowUp = (id, data) => http.post('/follow-ups/' + id, data);

// ---------- 资讯 ----------
export const pageArticles = (params) => http.get('/articles', params);
export const articleDetail = (id) => http.get('/articles/' + id);
export const likeArticle = (id) => http.post('/articles/' + id + '/like', {}, { showLoading: false });
export const createComment = (data) => http.post('/comments', data);

// ---------- 消息 ----------
export const myNotifications = (params) => http.get('/notifications', params);
export const readNotification = (id) => http.post('/notifications/' + id + '/read', {}, { showLoading: false });
export const readAllNotifications = () => http.post('/notifications/read-all');
export const unreadCount = () => http.get('/notifications/unread-count', {}, { showLoading: false });

// ---------- 聊天 ----------
export const getOrCreateSession = (shelterId) => http.post('/chat/sessions', { shelterId });
export const mySessions = () => http.get('/chat/sessions', {}, { showLoading: false });
export const sessionMessages = (id, params) => http.get('/chat/sessions/' + id + '/messages', params, { showLoading: false });
export const sendMessage = (id, data) => http.post('/chat/sessions/' + id + '/messages', data, { showLoading: false });
export const readSession = (id) => http.post('/chat/sessions/' + id + '/read', {}, { showLoading: false });
export const chatUnreadTotal = () => http.get('/chat/unread-total', {}, { showLoading: false });
// 撤回（仅自己、2 分钟内）/ 删除（仅自己可见地隐藏）
export const recallMessage = (messageId) => http.post('/chat/messages/' + messageId + '/recall');
export const deleteMessage = (messageId) => http.del('/chat/messages/' + messageId);

// ---------- 捐赠 ----------
export const ongoingProjects = () => http.get('/donation-projects', {}, { showLoading: false });
export const donate = (id, data) => http.post('/donation-projects/' + id + '/donate', data);

// ---------- 线索 ----------
export const createReport = (data) => http.post('/rescue-reports', data);
export const myReports = (params) => http.get('/rescue-reports/my', params);

// ---------- 志愿者 ----------
export const applyVolunteer = (data) => http.post('/volunteer-applications', data);
export const shelters = (city) => http.get('/shelters', { city }, { showLoading: false });

// ---------- 用户 ----------
export const myProfile = () => http.get('/users/me', {}, { showLoading: false });
export const updateProfile = (data) => http.put('/users/me', data);
export const mySummary = () => http.get('/users/me/summary', {}, { showLoading: false });
export const getAdopterProfile = () => http.get('/users/me/adopter-profile', {}, { showLoading: false });
export const updateAdopterProfile = (data) => http.put('/users/me/adopter-profile', data);

// ---------- 机构端（分包） ----------
export const shelterWorkbench = () => http.get('/statistics/shelter-workbench');
export const shelterApplications = (params) => http.get('/shelter/applications', params);
export const acceptApplication = (id) => http.post('/shelter/applications/' + id + '/accept');
export const interviewApplication = (id, data) => http.post('/shelter/applications/' + id + '/interview', data);
export const rejectApplication = (id, data) => http.post('/shelter/applications/' + id + '/reject', data);

// ---------- AI 宠物顾问 ----------
export const askAi = (data) => http.post('/ai/chat', data, { showLoading: false });

// ---------- 宠物商城 ----------
export const shopProducts = (params) => http.get('/shop/products', params, { showLoading: false });
export const shopProductDetail = (id) => http.get('/shop/products/' + id, {}, { showLoading: false });
export const shopCoupons = () => http.get('/shop/coupons', {}, { showLoading: false });
export const shopExchangeCoupon = (id) => http.post('/shop/coupons/' + id + '/exchange');
export const shopMyCoupons = (params) => http.get('/shop/coupons/mine', params, { showLoading: false });
export const shopPoints = () => http.get('/shop/points', {}, { showLoading: false });
export const shopCreateOrder = (data) => http.post('/shop/orders', data);
export const shopOrders = (params) => http.get('/shop/orders', params, { showLoading: false });
export const shopPayOrder = (id) => http.post('/shop/orders/' + id + '/pay');
export const shopCancelOrder = (id) => http.post('/shop/orders/' + id + '/cancel');

// ---------- 短信验证码 / 手机号登录（Web 与小程序共用接口） ----------
export const sendSmsCode = (phone, scene = 'login') =>
  http.post('/auth/sms/send', { phone, scene }, { showLoading: false });
export const smsLogin = (phone, code) => http.post('/auth/sms-login', { phone, code });
export const smsBind = (phone, code) => http.post('/auth/sms-bind', { phone, code });
