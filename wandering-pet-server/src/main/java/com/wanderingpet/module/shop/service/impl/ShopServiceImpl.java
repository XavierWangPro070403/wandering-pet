package com.wanderingpet.module.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.SeqUtil;
import com.wanderingpet.module.shop.dto.OrderCreateDTO;
import com.wanderingpet.module.shop.dto.ProductSaveDTO;
import com.wanderingpet.module.shop.entity.Coupon;
import com.wanderingpet.module.shop.entity.OrderItem;
import com.wanderingpet.module.shop.entity.PointsLog;
import com.wanderingpet.module.shop.entity.Product;
import com.wanderingpet.module.shop.entity.ShopOrder;
import com.wanderingpet.module.shop.entity.UserCoupon;
import com.wanderingpet.module.shop.mapper.CouponMapper;
import com.wanderingpet.module.shop.mapper.OrderItemMapper;
import com.wanderingpet.module.shop.mapper.PointsLogMapper;
import com.wanderingpet.module.shop.mapper.ProductMapper;
import com.wanderingpet.module.shop.mapper.ShopOrderMapper;
import com.wanderingpet.module.shop.mapper.UserCouponMapper;
import com.wanderingpet.module.shop.service.ShopService;
import com.wanderingpet.module.shop.vo.CouponVO;
import com.wanderingpet.module.shop.vo.OrderVO;
import com.wanderingpet.module.shop.vo.PointsVO;
import com.wanderingpet.module.shop.vo.ProductVO;
import com.wanderingpet.module.shop.vo.UserCouponVO;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.entity.UserAddress;
import com.wanderingpet.module.user.mapper.UserAddressMapper;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 宠物用品商城实现
 *
 * <p>价格链路：商品总额 → 优惠券抵扣（不超过总额）→ 实付金额；
 * 支付成功按实付金额发放积分（1元=1积分，向下取整）；
 * 待支付取消时恢复库存并退回优惠券。</p>
 */
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ProductMapper productMapper;
    private final ShopOrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final PointsLogMapper pointsLogMapper;
    private final UserAddressMapper userAddressMapper;
    private final UserMapper userMapper;
    private final SeqUtil seqUtil;

    private LoginUser current() {
        LoginUser user = UserContext.current();
        if (user == null) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    // ================================================== 商品（门户）

    @Override
    public PageResult<ProductVO> pageProducts(String category, String keyword, Integer pageNum, Integer pageSize) {
        Page<Product> page = productMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 50)),
                new LambdaQueryWrapper<Product>()
                        // 在售与下架都返回：前端对下架/售罄商品灰显并标注「已售罄」
                        .in(Product::getStatus, "ON_SHELF", "OFF_SHELF")
                        .eq(category != null && !category.isBlank(), Product::getCategory, category)
                        .like(keyword != null && !keyword.isBlank(), Product::getName, keyword)
                        .orderByDesc(Product::getSalesCount));
        List<ProductVO> list = page.getRecords().stream().map(this::toProductVO).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public ProductVO productDetail(Long id) {
        Product product = productMapper.selectById(id);
        // 下架/售罄商品允许查看详情（前端灰显并禁用购买），仅删除的不可见
        if (product == null || product.getDeleted() != null && product.getDeleted() == 1) {
            throw BizException.of(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }
        return toProductVO(product);
    }

    private ProductVO toProductVO(Product p) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(p, vo);
        return vo;
    }

    // ================================================== 优惠券

    @Override
    public List<CouponVO> listCoupons() {
        LoginUser user = current();
        User u = userMapper.selectById(user.getUserId());
        int points = u == null || u.getPoints() == null ? 0 : u.getPoints();
        List<Coupon> coupons = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, "ACTIVE")
                .orderByAsc(Coupon::getPointsCost));
        return coupons.stream().map(c -> {
            CouponVO vo = new CouponVO();
            BeanUtils.copyProperties(c, vo);
            vo.setCanExchange(points >= c.getPointsCost()
                    && c.getReceivedCount() < c.getTotalCount());
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeCoupon(Long couponId) {
        LoginUser user = current();
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || !"ACTIVE".equals(coupon.getStatus())) {
            throw BizException.of(ResultCode.NOT_FOUND, "优惠券不存在或已停发");
        }
        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw BizException.of(ResultCode.BAD_REQUEST, "该优惠券已抢完");
        }
        User u = userMapper.selectById(user.getUserId());
        int balance = u.getPoints() == null ? 0 : u.getPoints();
        if (balance < coupon.getPointsCost()) {
            throw BizException.of(ResultCode.BAD_REQUEST, "积分不足，还差 " + (coupon.getPointsCost() - balance) + " 分");
        }
        // 扣积分 + 流水
        u.setPoints(balance - coupon.getPointsCost());
        userMapper.updateById(u);
        savePointsLog(u.getId(), -coupon.getPointsCost(), u.getPoints(), "EXCHANGE",
                "兑换「" + coupon.getName() + "」", null, coupon.getId());
        // 发券（快照）
        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        couponMapper.updateById(coupon);
        UserCoupon uc = new UserCoupon();
        uc.setUserId(u.getId());
        uc.setCouponId(coupon.getId());
        uc.setCouponName(coupon.getName());
        uc.setDiscount(coupon.getDiscount());
        uc.setMinSpend(coupon.getMinSpend());
        uc.setStatus("UNUSED");
        uc.setExpireTime(LocalDateTime.now().plusDays(coupon.getExpireDays()));
        userCouponMapper.insert(uc);
    }

    @Override
    public List<UserCouponVO> myCoupons(String status) {
        LoginUser user = current();
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, user.getUserId())
                .eq(status != null && !status.isBlank(), UserCoupon::getStatus, status)
                .orderByAsc(UserCoupon::getExpireTime);
        return userCouponMapper.selectList(wrapper).stream().map(uc -> {
            UserCouponVO vo = new UserCouponVO();
            BeanUtils.copyProperties(uc, vo);
            return vo;
        }).toList();
    }

    // ================================================== 积分

    @Override
    public PointsVO myPoints() {
        LoginUser user = current();
        User u = userMapper.selectById(user.getUserId());
        PointsVO vo = new PointsVO();
        vo.setBalance(u.getPoints() == null ? 0 : u.getPoints());
        List<PointsLog> logs = pointsLogMapper.selectList(new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, user.getUserId())
                .orderByDesc(PointsLog::getCreateTime)
                .last("LIMIT 20"));
        vo.setLogs(logs.stream().map(l -> {
            PointsVO.LogItem item = new PointsVO.LogItem();
            item.setId(l.getId());
            item.setChange(l.getPointChange());
            item.setBalance(l.getBalance());
            item.setType(l.getType());
            item.setRemark(l.getRemark());
            item.setCreateTime(l.getCreateTime() == null ? null : l.getCreateTime().toString().replace('T', ' '));
            return item;
        }).toList());
        return vo;
    }

    /** 记积分流水（balance 为变动后余额） */
    private void savePointsLog(Long userId, int change, int balance, String type, String remark, Long orderId, Long couponId) {
        PointsLog logRow = new PointsLog();
        logRow.setUserId(userId);
        logRow.setPointChange(change);
        logRow.setBalance(balance);
        logRow.setType(type);
        logRow.setRemark(remark);
        logRow.setOrderId(orderId);
        logRow.setCouponId(couponId);
        pointsLogMapper.insert(logRow);
    }

    // ================================================== 订单

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateDTO dto) {
        LoginUser user = current();
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || !"ON_SHELF".equals(product.getStatus())) {
            throw BizException.of(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }
        // 收货地址校验（必须本人地址）
        UserAddress address = userAddressMapper.selectById(dto.getAddressId());
        if (address == null || !Objects.equals(address.getUserId(), user.getUserId())) {
            throw BizException.of(ResultCode.BAD_REQUEST, "请选择正确的收货地址");
        }
        BigDecimal total = product.getPrice()
                .multiply(BigDecimal.valueOf(dto.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        ShopOrder order = new ShopOrder();
        order.setOrderNo(seqUtil.next(BizConstant.SEQ_SHOP_ORDER, "ORD"));
        order.setUserId(user.getUserId());
        order.setTotalAmount(total);
        order.setStatus("PENDING_PAYMENT");
        order.setReceiverName(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(String.join(" ", address.getProvince(), address.getCity(),
                address.getDistrict(), address.getDetail()).trim());

        // 优惠券抵扣：属于本人 + 未使用 + 未过期 + 满足门槛
        if (dto.getCouponId() != null) {
            UserCoupon uc = userCouponMapper.selectById(dto.getCouponId());
            if (uc == null || !Objects.equals(uc.getUserId(), user.getUserId())) {
                throw BizException.of(ResultCode.BAD_REQUEST, "优惠券不存在");
            }
            if (!"UNUSED".equals(uc.getStatus())) {
                throw BizException.of(ResultCode.BAD_REQUEST, "优惠券已使用");
            }
            if (uc.getExpireTime().isBefore(LocalDateTime.now())) {
                throw BizException.of(ResultCode.BAD_REQUEST, "优惠券已过期");
            }
            if (total.compareTo(uc.getMinSpend()) < 0) {
                throw BizException.of(ResultCode.BAD_REQUEST,
                        "该券需满 " + uc.getMinSpend() + " 元可用");
            }
            BigDecimal discount = uc.getDiscount().min(total);
            order.setCouponId(uc.getId());
            order.setDiscountAmount(discount);
            uc.setStatus("USED");
            uc.setUsedTime(LocalDateTime.now());
            userCouponMapper.updateById(uc);
        }
        order.setPayAmount(total.subtract(
                order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount()));
        orderMapper.insert(order);

        // 明细（单商品）
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductCover(product.getCoverImage());
        item.setPrice(product.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setSubtotal(total);
        orderItemMapper.insert(item);

        // 扣库存（防超卖：带条件更新）+ 销量
        int rows = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, product.getId())
                .ge(Product::getStock, dto.getQuantity())
                .setSql("stock = stock - " + dto.getQuantity()));
        if (rows == 0) {
            throw BizException.of(ResultCode.BAD_REQUEST, "手慢了，该商品已售罄");
        }
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, product.getId())
                .setSql("sales_count = sales_count + " + dto.getQuantity()));

        // 回填券的核销订单号
        if (order.getCouponId() != null) {
            userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                    .eq(UserCoupon::getId, order.getCouponId())
                    .set(UserCoupon::getUsedOrderId, order.getId()));
        }
        return toOrderVO(order, List.of(item));
    }

    @Override
    public PageResult<OrderVO> myOrders(String status, Integer pageNum, Integer pageSize) {
        LoginUser user = current();
        Page<ShopOrder> page = orderMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 50)),
                new LambdaQueryWrapper<ShopOrder>()
                        .eq(ShopOrder::getUserId, user.getUserId())
                        .eq(status != null && !status.isBlank(), ShopOrder::getStatus, status)
                        .orderByDesc(ShopOrder::getCreateTime));
        List<OrderVO> list = page.getRecords().stream().map(o -> toOrderVO(o,
                orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId())))).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public OrderVO orderDetail(Long orderId) {
        LoginUser user = current();
        ShopOrder order = requireOwnedOrder(orderId, user);
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        return toOrderVO(order, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO pay(Long orderId) {
        LoginUser user = current();
        ShopOrder order = requireOwnedOrder(orderId, user);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw BizException.of(ResultCode.BAD_REQUEST, "订单状态不支持支付");
        }
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        // 发放积分：1元=1积分（向下取整）
        int earned = order.getPayAmount().setScale(0, RoundingMode.DOWN).intValue();
        order.setPointsEarned(earned);
        orderMapper.updateById(order);

        User u = userMapper.selectById(order.getUserId());
        int balance = (u.getPoints() == null ? 0 : u.getPoints()) + earned;
        u.setPoints(balance);
        userMapper.updateById(u);
        savePointsLog(u.getId(), earned, balance, "EARN",
                "订单 " + order.getOrderNo() + " 支付获得", order.getId(), null);
        return toOrderVO(order, orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(Long orderId) {
        LoginUser user = current();
        ShopOrder order = requireOwnedOrder(orderId, user);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw BizException.of(ResultCode.BAD_REQUEST, "仅待支付订单可以取消");
        }
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
        // 恢复库存与销量
        OrderItem item = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId).last("LIMIT 1"));
        if (item != null) {
            productMapper.update(null, new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, item.getProductId())
                    .setSql("stock = stock + " + item.getQuantity() + ", sales_count = GREATEST(sales_count - " + item.getQuantity() + ", 0)"));
        }
        // 退回优惠券
        if (order.getCouponId() != null) {
            userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                    .eq(UserCoupon::getId, order.getCouponId())
                    .set(UserCoupon::getStatus, "UNUSED")
                    .set(UserCoupon::getUsedTime, null)
                    .set(UserCoupon::getUsedOrderId, null));
        }
        return toOrderVO(order, item == null ? List.of() : List.of(item));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long orderId) {
        ShopOrder order = orderMapper.selectById(orderId);
        if (order == null || !"PAID".equals(order.getStatus())) {
            throw BizException.of(ResultCode.BAD_REQUEST, "仅已支付订单可以发货");
        }
        order.setStatus("SHIPPED");
        orderMapper.updateById(order);
    }

    private ShopOrder requireOwnedOrder(Long orderId, LoginUser user) {
        ShopOrder order = orderMapper.selectById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), user.getUserId())) {
            throw BizException.of(ResultCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private OrderVO toOrderVO(ShopOrder order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setCreateTime(order.getCreateTime() == null ? null : order.getCreateTime().toString().replace('T', ' '));
        vo.setPayTime(order.getPayTime() == null ? null : order.getPayTime().toString().replace('T', ' '));
        if (items != null) {
            vo.setItems(items.stream().map(i -> {
                OrderVO.Item item = new OrderVO.Item();
                BeanUtils.copyProperties(i, item);
                return item;
            }).toList());
        }
        return vo;
    }

    // ================================================== 管理端商品

    @Override
    public PageResult<ProductVO> adminPageProducts(String keyword, Integer pageNum, Integer pageSize) {
        Page<Product> page = productMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 50)),
                new LambdaQueryWrapper<Product>()
                        .like(keyword != null && !keyword.isBlank(), Product::getName, keyword)
                        .orderByDesc(Product::getCreateTime));
        List<ProductVO> list = page.getRecords().stream().map(this::toProductVO).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public Long saveProduct(ProductSaveDTO dto) {
        Product product = new Product();
        applyProduct(product, dto);
        product.setProductNo(seqUtil.next(BizConstant.SEQ_SHOP_ORDER, "P"));
        product.setSalesCount(0);
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    public void updateProduct(Long id, ProductSaveDTO dto) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "商品不存在");
        }
        applyProduct(product, dto);
        productMapper.updateById(product);
    }

    private void applyProduct(Product product, ProductSaveDTO dto) {
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCoverImage(dto.getCoverImage() == null ? "" : dto.getCoverImage());
        product.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        product.setStatus("OFF_SHELF".equals(dto.getStatus()) ? "OFF_SHELF" : "ON_SHELF");
    }

    @Override
    public void changeProductStatus(Long id, String status) {
        if (!"ON_SHELF".equals(status) && !"OFF_SHELF".equals(status)) {
            throw BizException.of(ResultCode.BAD_REQUEST, "状态不合法");
        }
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .set(Product::getStatus, status));
    }
}
