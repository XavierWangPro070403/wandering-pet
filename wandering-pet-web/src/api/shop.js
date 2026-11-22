import request from '@/utils/request';

/** 商品分页（免登录） */
export const shopProducts = (params) => request.get('/shop/products', { params });

/** 商品详情（免登录） */
export const shopProductDetail = (id) => request.get(`/shop/products/${id}`);

/** 领券中心 */
export const shopCoupons = () => request.get('/shop/coupons');

/** 积分兑换优惠券 */
export const shopExchangeCoupon = (id) => request.post(`/shop/coupons/${id}/exchange`);

/** 我的优惠券 */
export const shopMyCoupons = (params) => request.get('/shop/coupons/mine', { params });

/** 我的积分（余额 + 流水） */
export const shopPoints = () => request.get('/shop/points');

/** 提交订单 */
export const shopCreateOrder = (data) => request.post('/shop/orders', data);

/** 我的订单分页 */
export const shopOrders = (params) => request.get('/shop/orders', { params });

/** 订单详情 */
export const shopOrderDetail = (id) => request.get(`/shop/orders/${id}`);

/** 模拟支付 */
export const shopPayOrder = (id) => request.post(`/shop/orders/${id}/pay`);

/** 取消订单 */
export const shopCancelOrder = (id) => request.post(`/shop/orders/${id}/cancel`);
