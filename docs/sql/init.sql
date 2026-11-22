-- =====================================================================================
-- 流浪宠物领养系统（wandering-pet）数据库初始化脚本
-- 适用：MySQL 8.0+，一次执行成功，可反复运行（幂等）
-- 执行顺序：1. init.sql（本文件）  2. init-data.sql（演示数据）
-- 说明：提示词 5.2 节标注“22 张表”，但其表格实际列出 26 张，本脚本以 5.2 节清单为准建满 26 张。
-- =====================================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

DROP DATABASE IF EXISTS `wandering_pet`;
CREATE DATABASE `wandering_pet` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `wandering_pet`;

-- =====================================================================================
-- A. 系统与账号域
-- =====================================================================================

-- ----------------------------
-- 1. 后台账号表（管理员 + 机构账号）
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(64)     NOT NULL COMMENT '登录账号（全局唯一）',
  `password`    VARCHAR(100)    NOT NULL COMMENT '登录密码（BCrypt 密文，强度 10）',
  `real_name`   VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '真实姓名',
  `phone`       VARCHAR(20)     NOT NULL DEFAULT '' COMMENT '联系电话',
  `avatar`      VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '头像 URL',
  `role_code`   VARCHAR(16)     NOT NULL COMMENT '角色编码（ADMIN-平台管理员 / SHELTER-救助站）',
  `shelter_id`  BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '所属救助站 ID（SHELTER 角色必填，ADMIN 为空）',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
  `last_login_time` DATETIME    NULL DEFAULT NULL COMMENT '最近登录时间',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_shelter` (`shelter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台账号表（管理员+救助站机构账号）';

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_code`   VARCHAR(16)     NOT NULL COMMENT '角色编码（GUEST/USER/SHELTER/ADMIN）',
  `role_name`   VARCHAR(32)     NOT NULL COMMENT '角色名称',
  `description` VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '角色说明',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 3. 权限点表
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `perm_code`   VARCHAR(64)     NOT NULL COMMENT '权限编码（如 pet:create）',
  `perm_name`   VARCHAR(64)     NOT NULL COMMENT '权限名称',
  `perm_type`   VARCHAR(8)      NOT NULL DEFAULT 'API' COMMENT '权限类型 MENU-菜单 BUTTON-按钮 API-接口',
  `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级权限 ID（0-顶级）',
  `path`        VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '菜单路由路径（MENU 类型使用）',
  `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点表';

-- ----------------------------
-- 4. 角色-权限关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id`     BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',
  `perm_id`     BIGINT UNSIGNED NOT NULL COMMENT '权限 ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `perm_id`),
  KEY `idx_perm` (`perm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- ----------------------------
-- 5. 数据字典表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type`   VARCHAR(32)     NOT NULL COMMENT '字典类型（如 pet_size）',
  `dict_code`   VARCHAR(32)     NOT NULL COMMENT '字典编码（如 SMALL）',
  `dict_label`  VARCHAR(64)     NOT NULL COMMENT '字典标签（如 小型犬）',
  `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-停用',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type_code` (`dict_type`, `dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- ----------------------------
-- 6. C 端用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`         VARCHAR(64)     NULL DEFAULT NULL COMMENT '登录账号（Web 用户端注册，全局唯一，可为空）',
  `password`         VARCHAR(100)    NULL DEFAULT NULL COMMENT '登录密码（BCrypt 密文；微信授权注册用户为空）',
  `openid`           VARCHAR(64)     NULL DEFAULT NULL COMMENT '微信 openid（小程序授权登录，全局唯一）',
  `nickname`         VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '用户昵称',
  `avatar`           VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '用户头像 URL',
  `phone`            VARCHAR(255)    NULL DEFAULT NULL COMMENT '手机号（AES 加密存储，展示层脱敏；手机号验证码登录按密文等值查询）',
  `gender`           TINYINT         NOT NULL DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `city`             VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '所在城市',
  `id_card_no`       VARCHAR(255)    NULL DEFAULT NULL COMMENT '身份证号（AES 加密存储）',
  `real_name`        VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '实名姓名（实名认证填写）',
  `real_name_status` TINYINT         NOT NULL DEFAULT 0 COMMENT '实名认证状态 0-未认证 1-已认证',
  `adopter_name`     VARCHAR(32)     NULL DEFAULT NULL COMMENT '领养申请人姓名（申请领养时资料补全，多宠物申请免重复填写）',
  `adopter_age`      TINYINT         NULL DEFAULT NULL COMMENT '领养申请人年龄',
  `occupation`       VARCHAR(32)     NULL DEFAULT NULL COMMENT '职业',
  `housing_type`     VARCHAR(8)      NULL DEFAULT NULL COMMENT '住房类型 OWN-自有 RENT-租赁',
  `has_pet_experience` TINYINT       NULL DEFAULT NULL COMMENT '有养宠经验 0-否 1-是',
  `family_agree`     TINYINT         NULL DEFAULT NULL COMMENT '家人同意饲养 0-否 1-是',
  `has_children`     TINYINT         NULL DEFAULT NULL COMMENT '家中有儿童 0-否 1-是',
  `has_allergy`      TINYINT         NULL DEFAULT NULL COMMENT '有过敏史 0-否 1-是',
  `points`           INT             NOT NULL DEFAULT 0 COMMENT '积分余额（1元消费=1积分）',
  `status`           TINYINT         NOT NULL DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
  `last_login_time`  DATETIME        NULL DEFAULT NULL COMMENT '最近登录时间',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C 端用户表（领养人）';

-- ----------------------------
-- 7. 用户地址表
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
  `receiver`    VARCHAR(32)     NOT NULL COMMENT '收件人姓名',
  `phone`       VARCHAR(20)     NOT NULL COMMENT '联系电话',
  `province`    VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '省份',
  `city`        VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '城市',
  `district`    VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '区县',
  `detail`      VARCHAR(255)    NOT NULL COMMENT '详细地址（区+街道，不含门牌号）',
  `is_default`  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否默认地址 0-否 1-是',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- =====================================================================================
-- B. 机构与宠物域
-- =====================================================================================

-- ----------------------------
-- 8. 救助站（机构）表
-- ----------------------------
DROP TABLE IF EXISTS `shelter`;
CREATE TABLE `shelter` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`         VARCHAR(64)     NOT NULL COMMENT '救助站名称',
  `logo`         VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '机构 LOGO URL',
  `license_no`   VARCHAR(64)     NOT NULL COMMENT '统一社会信用代码/登记证号',
  `contact_name` VARCHAR(32)     NOT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20)    NOT NULL COMMENT '联系电话',
  `province`     VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '省份',
  `city`         VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '城市',
  `district`     VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '区县',
  `address`      VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '机构地址（仅到区+街道，不展示门牌号）',
  `longitude`    DECIMAL(10,6)   NULL DEFAULT NULL COMMENT '经度（仅用于地图选点与后台审核）',
  `latitude`     DECIMAL(10,6)   NULL DEFAULT NULL COMMENT '纬度（仅用于地图选点与后台审核）',
  `intro`        VARCHAR(1000)   NOT NULL DEFAULT '' COMMENT '机构简介',
  `audit_status` TINYINT         NOT NULL DEFAULT 0 COMMENT '入驻审核状态 0-待审核 1-已通过 2-已驳回',
  `audit_remark` VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '审核备注（驳回原因等）',
  `audit_time`   DATETIME        NULL DEFAULT NULL COMMENT '审核时间',
  `audit_by`     BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '审核人（管理员 sys_user ID）',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='救助站（机构）表';

-- ----------------------------
-- 9. 宠物物种分类表
-- ----------------------------
DROP TABLE IF EXISTS `pet_category`;
CREATE TABLE `pet_category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(32)     NOT NULL COMMENT '物种名称（猫/狗/兔/其他）',
  `icon`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '分类图标 URL',
  `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物物种分类表';

-- ----------------------------
-- 10. 宠物品种表
-- ----------------------------
DROP TABLE IF EXISTS `pet_breed`;
CREATE TABLE `pet_breed` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '所属物种 ID',
  `name`        VARCHAR(64)     NOT NULL COMMENT '品种名称',
  `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物品种表';

-- ----------------------------
-- 11. 宠物主档案表
-- ----------------------------
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
  `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pet_no`                VARCHAR(32)     NOT NULL COMMENT '宠物档案编号（PET+日期+序号，自动生成）',
  `name`                  VARCHAR(32)     NOT NULL COMMENT '宠物名字',
  `category_id`           BIGINT UNSIGNED NOT NULL COMMENT '物种分类 ID',
  `breed_id`              BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '品种 ID',
  `gender`                VARCHAR(8)      NOT NULL DEFAULT 'UNKNOWN' COMMENT '性别 MALE-公 FEMALE-母 UNKNOWN-未知',
  `age_month`             INT             NOT NULL DEFAULT 0 COMMENT '月龄（如 14 表示 14 个月）',
  `weight`                DECIMAL(5,2)    NULL DEFAULT NULL COMMENT '体重（kg）',
  `size`                  VARCHAR(8)      NOT NULL DEFAULT 'MEDIUM' COMMENT '体型 SMALL-小型 MEDIUM-中型 LARGE-大型',
  `color`                 VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '毛色',
  `sterilized`            TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否绝育 0-否 1-是',
  `vaccinated`            TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否接种疫苗 0-否 1-是',
  `dewormed`              TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否驱虫 0-否 1-是',
  `health_status`         VARCHAR(32)     NOT NULL DEFAULT 'HEALTHY' COMMENT '健康状况 HEALTHY-健康 GENERAL-一般 ILL-治疗中',
  `character_desc`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '性格描述',
  `adoption_requirements` VARCHAR(512)    NOT NULL DEFAULT '' COMMENT '领养要求',
  `story`                 TEXT            NULL COMMENT '救助故事',
  `cover_image`           VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '封面图 URL',
  `shelter_id`            BIGINT UNSIGNED NOT NULL COMMENT '所属救助站 ID',
  `status`                VARCHAR(16)     NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态 AVAILABLE-可领养 PENDING-审核中锁定 ADOPTED-已领养 MEDICAL-治疗中 OFFLINE-已下架',
  `view_count`            INT             NOT NULL DEFAULT 0 COMMENT '浏览量',
  `favorite_count`        INT             NOT NULL DEFAULT 0 COMMENT '收藏数',
  `publish_time`          DATETIME        NULL DEFAULT NULL COMMENT '发布时间',
  `create_time`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`               TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_no` (`pet_no`),
  KEY `idx_status_category` (`status`, `category_id`),
  KEY `idx_shelter` (`shelter_id`),
  KEY `idx_publish` (`publish_time` DESC),
  FULLTEXT KEY `ft_pet_search` (`name`, `character_desc`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物主档案表';

-- ----------------------------
-- 12. 宠物相册表
-- ----------------------------
DROP TABLE IF EXISTS `pet_image`;
CREATE TABLE `pet_image` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pet_id`      BIGINT UNSIGNED NOT NULL COMMENT '宠物 ID',
  `object_name` VARCHAR(255)    NOT NULL DEFAULT '' COMMENT 'MinIO 对象名（如 pet/2026/11/8f3a.jpg）',
  `url`         VARCHAR(255)    NOT NULL COMMENT '图片访问 URL',
  `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `type`        VARCHAR(8)      NOT NULL DEFAULT 'IMAGE' COMMENT '类型 IMAGE-图片 VIDEO-视频',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_pet` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物相册表';

-- ----------------------------
-- 13. 宠物健康档案表
-- ----------------------------
DROP TABLE IF EXISTS `pet_health_record`;
CREATE TABLE `pet_health_record` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pet_id`         BIGINT UNSIGNED NOT NULL COMMENT '宠物 ID',
  `record_type`    VARCHAR(16)     NOT NULL COMMENT '记录类型 VACCINE-疫苗 DEWORM-驱虫 STERILIZE-绝育 TREATMENT-治疗 CHECK-体检',
  `record_date`    DATE            NOT NULL COMMENT '记录日期',
  `title`          VARCHAR(64)     NOT NULL COMMENT '记录标题',
  `content`        VARCHAR(1000)   NOT NULL DEFAULT '' COMMENT '记录内容',
  `vet_name`       VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '兽医/医院名称',
  `attachment_url` VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '附件 URL（诊断单/化验单）',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_pet_date` (`pet_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物健康档案表';

-- =====================================================================================
-- C. 领养与回访域
-- =====================================================================================

-- ----------------------------
-- 14. 领养申请表
-- ----------------------------
DROP TABLE IF EXISTS `adoption_application`;
CREATE TABLE `adoption_application` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `apply_no`           VARCHAR(32)     NOT NULL COMMENT '申请编号（APP+日期+序号）',
  `pet_id`             BIGINT UNSIGNED NOT NULL COMMENT '宠物 ID',
  `user_id`            BIGINT UNSIGNED NOT NULL COMMENT '申请人用户 ID',
  `shelter_id`         BIGINT UNSIGNED NOT NULL COMMENT '受理救助站 ID',
  `applicant_name`     VARCHAR(32)     NOT NULL COMMENT '申请人姓名',
  `applicant_phone`    VARCHAR(20)     NOT NULL COMMENT '联系电话',
  `applicant_age`      INT             NOT NULL DEFAULT 0 COMMENT '申请人年龄',
  `occupation`         VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '职业',
  `city`               VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '现居住城市',
  `housing_type`       VARCHAR(8)      NOT NULL DEFAULT 'RENT' COMMENT '住房类型 OWN-自有住房 RENT-租房',
  `has_pet_experience` TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否有养宠经验 0-否 1-是',
  `family_agree`       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '家人是否同意 0-否 1-是',
  `has_children`       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '家中是否有儿童 0-否 1-是',
  `has_allergy`        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否有过敏史 0-否 1-是',
  `reason`             VARCHAR(500)    NOT NULL COMMENT '领养原因',
  `status`             VARCHAR(16)     NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态 DRAFT-草稿 SUBMITTED-已提交 REVIEWING-审核中 INTERVIEW-待面谈 HOME_VISIT-待家访 APPROVED-审核通过 SIGNED-已签约 ADOPTED-领养完成 REJECTED-已驳回 CANCELED-已撤回 EXPIRED-已过期 RETURNED-已退养',
  `interview_time`     DATETIME        NULL DEFAULT NULL COMMENT '面谈预约时间',
  `home_visit_time`    DATETIME        NULL DEFAULT NULL COMMENT '家访预约时间',
  `audit_remark`       VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '审核备注（驳回原因/通过说明）',
  `auditor_id`         BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '审核人（救助站账号 ID）',
  `audit_time`         DATETIME        NULL DEFAULT NULL COMMENT '最近审核时间',
  `active_flag`        TINYINT         GENERATED ALWAYS AS (IF(`status` IN ('DRAFT','SUBMITTED','REVIEWING','INTERVIEW','HOME_VISIT','APPROVED','SIGNED'), 1, NULL)) STORED COMMENT '非终态标记（非终态为 1、终态为 NULL，用于唯一索引防重复申请）',
  `create_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  UNIQUE KEY `uk_pet_user_active` (`pet_id`, `user_id`, `active_flag`),
  KEY `idx_user` (`user_id`),
  KEY `idx_pet_status` (`pet_id`, `status`),
  KEY `idx_shelter_status` (`shelter_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养申请表（uk_pet_user_active 通过生成列实现“同一用户同一宠物仅一条非终态申请”）';

-- ----------------------------
-- 15. 领养状态流水表
-- ----------------------------
DROP TABLE IF EXISTS `adoption_flow_log`;
CREATE TABLE `adoption_flow_log` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `application_id` BIGINT UNSIGNED NOT NULL COMMENT '申请 ID',
  `from_status`    VARCHAR(16)     NOT NULL DEFAULT '' COMMENT '流转前状态（首次提交为空）',
  `to_status`      VARCHAR(16)     NOT NULL COMMENT '流转后状态',
  `operator_id`    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作人 ID（系统任务为 0）',
  `operator_type`  VARCHAR(8)      NOT NULL DEFAULT 'SYSTEM' COMMENT '操作人类型 USER/SHELTER/ADMIN/SYSTEM',
  `remark`         VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '备注（驳回原因/面谈安排等）',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养申请状态流转流水表';

-- ----------------------------
-- 16. 领养正式记录表（协议主体）
-- ----------------------------
DROP TABLE IF EXISTS `adoption_record`;
CREATE TABLE `adoption_record` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `adoption_no`          VARCHAR(32)     NOT NULL COMMENT '领养编号（ADO+日期+序号）',
  `application_id`       BIGINT UNSIGNED NOT NULL COMMENT '关联申请 ID',
  `pet_id`               BIGINT UNSIGNED NOT NULL COMMENT '宠物 ID',
  `user_id`              BIGINT UNSIGNED NOT NULL COMMENT '领养人用户 ID',
  `shelter_id`           BIGINT UNSIGNED NOT NULL COMMENT '救助站 ID',
  `sign_date`            DATE            NOT NULL COMMENT '协议签署日期',
  `agreement_object_name` VARCHAR(255)   NOT NULL DEFAULT '' COMMENT '协议 PDF MinIO 对象名（wandering-pet-private 私有桶）',
  `agreement_url`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '协议访问 URL（私有桶走预签名）',
  `status`               VARCHAR(16)     NOT NULL DEFAULT 'ADOPTED' COMMENT '状态 ADOPTED-领养完成 RETURNED-已退养',
  `return_reason`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '退养原因',
  `return_time`          DATETIME        NULL DEFAULT NULL COMMENT '退养时间',
  `create_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`              TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_adoption_no` (`adoption_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_pet` (`pet_id`),
  KEY `idx_shelter` (`shelter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养正式记录表';

-- ----------------------------
-- 17. 领养回访记录表
-- ----------------------------
DROP TABLE IF EXISTS `follow_up`;
CREATE TABLE `follow_up` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `record_id`      BIGINT UNSIGNED NOT NULL COMMENT '领养记录 ID',
  `application_id` BIGINT UNSIGNED NOT NULL COMMENT '申请 ID',
  `plan_date`      DATE            NOT NULL COMMENT '计划回访日期',
  `actual_date`    DATE            NULL DEFAULT NULL COMMENT '实际回访日期',
  `round`          INT             NOT NULL COMMENT '第几次回访（1-4）',
  `weight`         DECIMAL(5,2)    NULL DEFAULT NULL COMMENT '体重（kg）',
  `health_status`  VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '健康状况描述',
  `vaccine_status` VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '疫苗情况描述',
  `life_desc`      VARCHAR(500)    NOT NULL DEFAULT '' COMMENT '生活近况描述',
  `images`         JSON            NULL COMMENT '生活照（JSON 数组，3 张）',
  `handler_id`     BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '处理人（救助站账号 ID，归档时记录）',
  `status`         VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING-待回访 DONE-已完成 OVERDUE-已逾期',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_record_round` (`record_id`, `round`),
  KEY `idx_status_plan` (`status`, `plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养后回访记录表';

-- =====================================================================================
-- D. 内容与互动域
-- =====================================================================================

-- ----------------------------
-- 18. 资讯科普文章表
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(128)    NOT NULL COMMENT '文章标题',
  `cover`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '封面图 URL',
  `category`     VARCHAR(16)     NOT NULL COMMENT '分类 RAISE-饲养指导 KNOWLEDGE-科普百科 NEWS-平台动态 STORY-救助故事',
  `summary`      VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '摘要（列表页展示）',
  `content`      LONGTEXT        NULL COMMENT '正文（富文本 HTML，保存前经白名单过滤）',
  `author`       VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '作者（发布人姓名）',
  `view_count`   INT             NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count`   INT             NOT NULL DEFAULT 0 COMMENT '点赞数',
  `is_top`       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
  `status`       TINYINT         NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-已下架',
  `publish_time` DATETIME        NULL DEFAULT NULL COMMENT '发布时间',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_status` (`category`, `status`),
  KEY `idx_publish` (`publish_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯科普文章表';

-- ----------------------------
-- 19. 评论表（文章/宠物通用）
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `target_type` VARCHAR(8)      NOT NULL COMMENT '评论对象类型 ARTICLE-文章 PET-宠物',
  `target_id`   BIGINT UNSIGNED NOT NULL COMMENT '评论对象 ID',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '评论人用户 ID',
  `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父评论 ID（0-一级评论）',
  `content`     VARCHAR(500)    NOT NULL COMMENT '评论内容',
  `like_count`  INT             NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status`      TINYINT         NOT NULL DEFAULT 0 COMMENT '状态 0-待审核 1-已通过 2-已屏蔽',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`, `status`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表（文章/宠物通用）';

-- ----------------------------
-- 20. 收藏表
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
  `target_type` VARCHAR(8)      NOT NULL COMMENT '收藏对象类型 PET-宠物 ARTICLE-文章',
  `target_id`   BIGINT UNSIGNED NOT NULL COMMENT '收藏对象 ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ----------------------------
-- 21. 站内消息表
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '接收人 ID',
  `user_type`   VARCHAR(8)      NOT NULL COMMENT '接收人类型 USER-普通用户 SHELTER-救助站 ADMIN-管理员',
  `title`       VARCHAR(64)     NOT NULL COMMENT '消息标题',
  `content`     VARCHAR(1000)   NOT NULL COMMENT '消息内容',
  `biz_type`    VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '业务类型（APPLICATION/FOLLOW_UP/DONATION/SYSTEM 等）',
  `biz_id`      BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '业务 ID（点击跳转定位）',
  `is_read`     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
  `read_time`   DATETIME        NULL DEFAULT NULL COMMENT '已读时间',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`user_id`, `user_type`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- =====================================================================================
-- E. 公益与线索域
-- =====================================================================================

-- ----------------------------
-- 22. 募捐项目表
-- ----------------------------
DROP TABLE IF EXISTS `donation_project`;
CREATE TABLE `donation_project` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shelter_id`    BIGINT UNSIGNED NOT NULL COMMENT '发起救助站 ID',
  `title`         VARCHAR(128)    NOT NULL COMMENT '项目标题',
  `cover`         VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '项目封面 URL',
  `target_amount` DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT '目标金额（元）',
  `raised_amount` DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT '已筹金额（元）',
  `donor_count`   INT             NOT NULL DEFAULT 0 COMMENT '捐赠人数',
  `start_date`    DATE            NOT NULL COMMENT '开始日期',
  `end_date`      DATE            NOT NULL COMMENT '结束日期',
  `status`        VARCHAR(16)     NOT NULL DEFAULT 'ONGOING' COMMENT '状态 ONGOING-进行中 ENDED-已结束 CLOSED-已关闭',
  `content`       TEXT            NULL COMMENT '项目详情说明',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_shelter_status` (`shelter_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='募捐项目表';

-- ----------------------------
-- 23. 捐赠明细表
-- ----------------------------
DROP TABLE IF EXISTS `donation_record`;
CREATE TABLE `donation_record` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id`   BIGINT UNSIGNED NOT NULL COMMENT '募捐项目 ID',
  `user_id`      BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '捐赠人用户 ID（游客捐赠为空）',
  `donor_name`   VARCHAR(32)     NOT NULL DEFAULT '爱心人士' COMMENT '捐赠人展示名（匿名时显示“匿名爱心人士”）',
  `amount`       DECIMAL(10,2)   NOT NULL COMMENT '捐赠金额（元）',
  `pay_no`       VARCHAR(32)     NOT NULL COMMENT '支付单号（PAY+日期+序号，模拟支付）',
  `pay_status`   VARCHAR(8)      NOT NULL DEFAULT 'SUCCESS' COMMENT '支付状态 SUCCESS-成功 FAILED-失败',
  `is_anonymous` TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否匿名 0-实名展示 1-匿名',
  `message`      VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '捐赠留言',
  `pay_time`     DATETIME        NULL DEFAULT NULL COMMENT '支付时间',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  KEY `idx_project` (`project_id`, `pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='捐赠明细表（模拟支付）';

-- ----------------------------
-- 24. 志愿者报名表
-- ----------------------------
DROP TABLE IF EXISTS `volunteer_application`;
CREATE TABLE `volunteer_application` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shelter_id`     BIGINT UNSIGNED NOT NULL COMMENT '报名救助站 ID',
  `user_id`        BIGINT UNSIGNED NOT NULL COMMENT '报名用户 ID',
  `real_name`      VARCHAR(32)     NOT NULL COMMENT '真实姓名',
  `phone`          VARCHAR(20)     NOT NULL COMMENT '联系电话',
  `skills`         VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '个人技能（摄影/医疗/运输/文案等）',
  `available_time` VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '可参与时间（如周末全天）',
  `status`         VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING-待审核 APPROVED-已通过 REJECTED-未通过',
  `audit_remark`   VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '审核备注',
  `audit_time`     DATETIME        NULL DEFAULT NULL COMMENT '审核时间',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_shelter_status` (`shelter_id`, `status`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者报名表';

-- ----------------------------
-- 25. 流浪动物上报线索表
-- ----------------------------
DROP TABLE IF EXISTS `rescue_report`;
CREATE TABLE `rescue_report` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `report_no`     VARCHAR(32)     NOT NULL COMMENT '线索编号（RSR+日期+序号）',
  `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '上报人用户 ID',
  `category_id`   BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '动物物种 ID',
  `description`   VARCHAR(500)    NOT NULL COMMENT '情况描述（动物特征/状态/所处环境）',
  `images`        JSON            NULL COMMENT '现场图片（JSON 数组，最多 9 张）',
  `province`      VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '省份',
  `city`          VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '城市',
  `district`      VARCHAR(32)     NOT NULL DEFAULT '' COMMENT '区县',
  `address`       VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '位置描述（区+街道/地标）',
  `longitude`     DECIMAL(10,6)   NULL DEFAULT NULL COMMENT '经度',
  `latitude`      DECIMAL(10,6)   NULL DEFAULT NULL COMMENT '纬度',
  `urgency`       TINYINT         NOT NULL DEFAULT 2 COMMENT '紧急程度 1-一般 2-紧急 3-危急（受伤/被困）',
  `status`        VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING-待处理 ASSIGNED-已分派 PROCESSING-处理中 RESOLVED-已解决 CLOSED-已关闭',
  `handler_id`    BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '处理人（救助站账号 ID）',
  `handle_remark` VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '处理备注',
  `handle_time`   DATETIME        NULL DEFAULT NULL COMMENT '最近处理时间',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  KEY `idx_status_urgency` (`status`, `urgency`),
  KEY `idx_city` (`city`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流浪动物上报线索表';

-- =====================================================================================
-- F. 聊天域（会话存 MySQL，消息存 MongoDB chat_message 集合）
-- =====================================================================================

-- ----------------------------
-- 26. 聊天会话表
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键（即 MongoDB 消息中的 sessionId）',
  `user_id`        BIGINT UNSIGNED NOT NULL COMMENT '用户 ID（领养人）',
  `shelter_id`     BIGINT UNSIGNED NOT NULL COMMENT '救助站 ID',
  `last_message`   VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '最后一条消息摘要',
  `last_time`      DATETIME        NULL DEFAULT NULL COMMENT '最后消息时间',
  `user_unread`    INT             NOT NULL DEFAULT 0 COMMENT '用户侧未读数',
  `shelter_unread` INT             NOT NULL DEFAULT 0 COMMENT '机构侧未读数',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_shelter` (`user_id`, `shelter_id`),
  KEY `idx_shelter_last` (`shelter_id`, `last_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表（消息正文存 MongoDB chat_message）';

-- 商城模块（商品/订单/优惠券/积分）建表语句见 shop-init.sql（可与本文件一并执行）
SET FOREIGN_KEY_CHECKS = 1;
