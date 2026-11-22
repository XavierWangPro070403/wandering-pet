package com.wanderingpet.module.shop.controller;

import com.wanderingpet.common.annotation.RateLimit;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.shop.dto.OrderCreateDTO;
import com.wanderingpet.module.shop.service.ShopService;
import com.wanderingpet.module.shop.vo.CouponVO;
import com.wanderingpet.module.shop.vo.OrderVO;
import com.wanderingpet.module.shop.vo.PointsVO;
import com.wanderingpet.module.shop.vo.ProductVO;
import com.wanderingpet.module.shop.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物用品商城（门户）
 */
@Tag(name = "宠物商城", description = "商品浏览、下单、支付（演示）、优惠券与积分")
@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    // ---------- 商品 ----------

    @Operation(summary = "商品分页（免登录浏览）")
    @GetMapping("/products")
    public R<PageResult<ProductVO>> products(@RequestParam(required = false) String category,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "12") Integer pageSize) {
        return R.ok(shopService.pageProducts(category, keyword, pageNum, pageSize));
    }

    @Operation(summary = "商品详情（免登录）")
    @GetMapping("/products/{id}")
    public R<ProductVO> productDetail(@PathVariable Long id) {
        return R.ok(shopService.productDetail(id));
    }

    // ---------- 优惠券 / 积分 ----------

    @Operation(summary = "领券中心（发放中的优惠券）")
    @GetMapping("/coupons")
    public R<List<CouponVO>> coupons() {
        return R.ok(shopService.listCoupons());
    }

    @Operation(summary = "积分兑换优惠券（1元消费=1积分）")
    @RateLimit(maxRequests = 10, windowSeconds = 60)
    @PostMapping("/coupons/{id}/exchange")
    public R<Void> exchange(@PathVariable Long id) {
        shopService.exchangeCoupon(id);
        return R.ok();
    }

    @Operation(summary = "我的优惠券")
    @GetMapping("/coupons/mine")
    public R<List<UserCouponVO>> myCoupons(@RequestParam(required = false) String status) {
        return R.ok(shopService.myCoupons(status));
    }

    @Operation(summary = "我的积分（余额 + 最近流水）")
    @GetMapping("/points")
    public R<PointsVO> points() {
        return R.ok(shopService.myPoints());
    }

    // ---------- 订单 ----------

    @Operation(summary = "提交订单（下单即扣库存；可选优惠券抵扣）")
    @PostMapping("/orders")
    public R<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return R.ok(shopService.createOrder(dto));
    }

    @Operation(summary = "我的订单分页（status 可筛：PENDING_PAYMENT/PAID/SHIPPED/CANCELLED）")
    @GetMapping("/orders")
    public R<PageResult<OrderVO>> myOrders(@RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(shopService.myOrders(status, pageNum, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/orders/{id}")
    public R<OrderVO> orderDetail(@PathVariable Long id) {
        return R.ok(shopService.orderDetail(id));
    }

    @Operation(summary = "模拟支付（演示环境；PAID 后按 1元=1积分 发放积分）")
    @PostMapping("/orders/{id}/pay")
    public R<OrderVO> pay(@PathVariable Long id) {
        return R.ok(shopService.pay(id));
    }

    @Operation(summary = "取消待支付订单（恢复库存、退回优惠券）")
    @PostMapping("/orders/{id}/cancel")
    public R<OrderVO> cancel(@PathVariable Long id) {
        return R.ok(shopService.cancel(id));
    }
}
