-- =====================================================================================
-- 商城模块建表 + 种子数据（宠物用品 / 订单 / 优惠券 / 积分）
-- 在 init.sql 之后执行；`user` 表的 points 列已在 init.sql 中定义
-- =====================================================================================
USE `wandering_pet`;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `shop_order`;
DROP TABLE IF EXISTS `user_coupon`;
DROP TABLE IF EXISTS `points_log`;
DROP TABLE IF EXISTS `coupon`;
DROP TABLE IF EXISTS `product`;

-- ----------------------------
-- 商品表（宠物用品）
-- ----------------------------
CREATE TABLE `product` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_no`       VARCHAR(32)     NOT NULL COMMENT '商品编号',
  `name`             VARCHAR(64)     NOT NULL COMMENT '商品名称',
  `category`         VARCHAR(16)     NOT NULL DEFAULT 'OTHER' COMMENT '分类 FOOD-粮食 TOY-玩具 CLEAN-清洁 CARE-护理 OTHER-其他',
  `price`            DECIMAL(10,2)   NOT NULL COMMENT '单价（元）',
  `stock`            INT             NOT NULL DEFAULT 0 COMMENT '库存',
  `cover_image`      VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '封面图',
  `description`      VARCHAR(500)    NOT NULL DEFAULT '' COMMENT '商品描述',
  `status`           VARCHAR(16)     NOT NULL DEFAULT 'ON_SHELF' COMMENT '状态 ON_SHELF-在售 OFF_SHELF-下架',
  `sales_count`      INT             NOT NULL DEFAULT 0 COMMENT '累计销量',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`          TINYINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_product_no` (`product_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物用品商品表';

-- ----------------------------
-- 订单表
-- ----------------------------
CREATE TABLE `shop_order` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`         VARCHAR(32)     NOT NULL COMMENT '订单编号',
  `user_id`          BIGINT UNSIGNED NOT NULL COMMENT '买家用户 ID',
  `total_amount`     DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '商品总额',
  `discount_amount`  DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '优惠券抵扣',
  `pay_amount`       DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '实付金额',
  `coupon_id`        BIGINT UNSIGNED NULL COMMENT '使用的用户优惠券 ID',
  `points_earned`    INT             NOT NULL DEFAULT 0 COMMENT '本单获得积分（1元=1积分）',
  `status`           VARCHAR(16)     NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '状态 PENDING_PAYMENT-待支付 PAID-已支付 SHIPPED-已发货 CANCELLED-已取消',
  `receiver_name`    VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '收货人（快照）',
  `receiver_phone`   VARCHAR(20)     NOT NULL DEFAULT '' COMMENT '联系电话（快照）',
  `receiver_address` VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '收货地址（快照）',
  `pay_time`         DATETIME        NULL COMMENT '支付时间',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`          TINYINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_order_no` (`order_no`), KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物用品订单表';

-- ----------------------------
-- 订单明细表
-- ----------------------------
CREATE TABLE `order_item` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT UNSIGNED NOT NULL COMMENT '订单 ID',
  `product_id`    BIGINT UNSIGNED NOT NULL COMMENT '商品 ID',
  `product_name`  VARCHAR(64)     NOT NULL COMMENT '商品名称（快照）',
  `product_cover` VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '商品封面（快照）',
  `price`         DECIMAL(10,2)   NOT NULL COMMENT '成交单价',
  `quantity`      INT             NOT NULL DEFAULT 1 COMMENT '数量',
  `subtotal`      DECIMAL(10,2)   NOT NULL COMMENT '小计',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ----------------------------
-- 优惠券模板
-- ----------------------------
CREATE TABLE `coupon` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(64)     NOT NULL COMMENT '券名称',
  `discount`       DECIMAL(10,2)   NOT NULL COMMENT '面额（元）',
  `min_spend`      DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '使用门槛（元），0 无门槛',
  `points_cost`    INT             NOT NULL DEFAULT 0 COMMENT '兑换所需积分',
  `total_count`    INT             NOT NULL DEFAULT 0 COMMENT '发放总量',
  `received_count` INT             NOT NULL DEFAULT 0 COMMENT '已领取数',
  `status`         VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE-发放中 OFF-停发',
  `expire_days`    INT             NOT NULL DEFAULT 30 COMMENT '领取后有效天数',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT         NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板';

-- ----------------------------
-- 用户优惠券
-- ----------------------------
CREATE TABLE `user_coupon` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '持有用户',
  `coupon_id`     BIGINT UNSIGNED NOT NULL COMMENT '券模板 ID',
  `coupon_name`   VARCHAR(64)     NOT NULL COMMENT '券名称（快照）',
  `discount`      DECIMAL(10,2)   NOT NULL COMMENT '面额（快照）',
  `min_spend`     DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '门槛（快照）',
  `status`        VARCHAR(16)     NOT NULL DEFAULT 'UNUSED' COMMENT '状态 UNUSED-未使用 USED-已使用',
  `expire_time`   DATETIME        NOT NULL COMMENT '过期时间',
  `used_time`     DATETIME        NULL COMMENT '核销时间',
  `used_order_id` BIGINT UNSIGNED NULL COMMENT '核销订单',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

-- ----------------------------
-- 积分流水
-- ----------------------------
CREATE TABLE `points_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户',
  `point_change` INT             NOT NULL COMMENT '积分变动（正获得/负消耗）',
  `balance`     INT             NOT NULL COMMENT '变动后余额',
  `type`        VARCHAR(16)     NOT NULL COMMENT '类型 EARN-消费获得 EXCHANGE-兑换优惠券',
  `remark`      VARCHAR(100)    NOT NULL DEFAULT '' COMMENT '说明',
  `order_id`    BIGINT UNSIGNED NULL COMMENT '关联订单',
  `coupon_id`   BIGINT UNSIGNED NULL COMMENT '关联优惠券',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水';

-- ----------------------------
-- 种子：12 个商品（封面复用演示图库）
-- ----------------------------
INSERT INTO `product` (`product_no`, `name`, `category`, `price`, `stock`, `cover_image`, `description`, `sales_count`) VALUES
('P20261121001', '全价幼猫粮 1.5kg', 'FOOD', 89.00, 120, 'http://localhost:8080/imgs/pet/p01.jpg', '高蛋白易消化，添加牛磺酸，助力幼猫健康成长。', 156),
('P20261121002', '成犬通用粮 5kg', 'FOOD', 199.00, 60, 'http://localhost:8080/imgs/pet/p04.jpg', '精选鸡肉配方，均衡营养，适合中大型成犬。', 89),
('P20261121003', '豆腐猫砂 6L×4', 'CLEAN', 59.90, 200, 'http://localhost:8080/imgs/pet/p07.jpg', '植物原料结团快，可冲厕所，低尘除臭。', 320),
('P20261121004', '逗猫棒三件套', 'TOY', 29.90, 150, 'http://localhost:8080/imgs/pet/p10.jpg', '铃铛羽毛+毛毛虫+激光笔，解放双手陪玩神器。', 210),
('P20261121005', '狗狗洁齿骨 220g', 'CARE', 45.00, 180, 'http://localhost:8080/imgs/pet/p13.jpg', '磨牙洁齿减少牙结石，鸡肉味狗狗爱不释口。', 98),
('P20261121006', '宠物自动饮水机 2L', 'CARE', 129.00, 45, 'http://localhost:8080/imgs/pet/p16.jpg', '循环活水四重过滤，静音水泵鼓励多喝水。', 67),
('P20261121007', '猫抓板大号瓦楞纸', 'TOY', 25.00, 300, 'http://localhost:8080/imgs/pet/p19.jpg', '加厚密度耐抓耐磨，附带猫薄荷。', 402),
('P20261121008', '宠物香波 500ml', 'CLEAN', 39.90, 90, 'http://localhost:8080/imgs/pet/p22.jpg', '温和低敏配方，适用于猫狗，蓬松留香。', 145),
('P20261121009', '四季通用宠物窝 M', 'OTHER', 158.00, 55, 'http://localhost:8080/imgs/pet/p02.jpg', '加厚保暖可拆洗，防滑底布，猫狗通用。', 54),
('P20261121010', '反光牵引绳 1.5m', 'OTHER', 35.00, 220, 'http://localhost:8080/imgs/pet/p03.jpg', '反光织带夜间出行更安全，缓冲扣减冲击。', 233),
('P20261121011', '益生菌调理粉 60g', 'CARE', 68.00, 110, 'http://localhost:8080/imgs/pet/p05.jpg', '六种活性菌株，缓解软便腹泻，调理肠胃。', 76),
('P20261121012', '冻干零食混合装 100g', 'FOOD', 49.90, 88, 'http://localhost:8080/imgs/pet/p06.jpg', '单一原料冻干，无添加，训练奖励首选。', 188);

-- ----------------------------
-- 种子：3 张可兑换优惠券
-- ----------------------------
INSERT INTO `coupon` (`name`, `discount`, `min_spend`, `points_cost`, `total_count`, `received_count`, `status`, `expire_days`) VALUES
('新人无门槛券', 10.00, 0, 300, 500, 0, 'ACTIVE', 30),
('满100减25券', 25.00, 100, 800, 300, 0, 'ACTIVE', 30),
('满200减60券', 60.00, 200, 1800, 100, 0, 'ACTIVE', 30);

SET FOREIGN_KEY_CHECKS = 1;
