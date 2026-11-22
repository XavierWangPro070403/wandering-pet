package com.wanderingpet.module.shop.service;

import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.shop.dto.OrderCreateDTO;
import com.wanderingpet.module.shop.dto.ProductSaveDTO;
import com.wanderingpet.module.shop.vo.CouponVO;
import com.wanderingpet.module.shop.vo.OrderVO;
import com.wanderingpet.module.shop.vo.PointsVO;
import com.wanderingpet.module.shop.vo.ProductVO;
import com.wanderingpet.module.shop.vo.UserCouponVO;

import java.util.List;

/**
 * 宠物用品商城服务（商品 / 订单 / 优惠券 / 积分）
 */
public interface ShopService {

    // ---------- 商品（门户） ----------

    PageResult<ProductVO> pageProducts(String category, String keyword, Integer pageNum, Integer pageSize);

    ProductVO productDetail(Long id);

    // ---------- 优惠券 ----------

    /** 领券中心：发放中的券 + 当前用户可兑换状态 */
    List<CouponVO> listCoupons();

    /** 积分兑换优惠券（1 元消费 = 1 积分） */
    void exchangeCoupon(Long couponId);

    /** 我的优惠券 */
    List<UserCouponVO> myCoupons(String status);

    // ---------- 积分 ----------

    /** 积分余额 + 最近流水 */
    PointsVO myPoints();

    // ---------- 订单 ----------

    OrderVO createOrder(OrderCreateDTO dto);

    PageResult<OrderVO> myOrders(String status, Integer pageNum, Integer pageSize);

    OrderVO orderDetail(Long orderId);

    /** 模拟支付：PAID + 发放积分（1元=1积分）；接微信支付后替换为支付回调 */
    OrderVO pay(Long orderId);

    /** 取消待支付订单：恢复库存、退回优惠券 */
    OrderVO cancel(Long orderId);

    /** 管理端发货 */
    void ship(Long orderId);

    // ---------- 管理端商品 ----------

    PageResult<ProductVO> adminPageProducts(String keyword, Integer pageNum, Integer pageSize);

    Long saveProduct(ProductSaveDTO dto);

    void updateProduct(Long id, ProductSaveDTO dto);

    void changeProductStatus(Long id, String status);
}
