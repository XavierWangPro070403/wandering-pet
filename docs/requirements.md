---
AIGC:
    Label: "1"
    ContentProducer: 001191440300708461136T1XGW3
    ProduceID: d6a6028b4b0621919cd7a79eec1af3ba_ca4f605ae1ce11f1a768525400cd780f
    ReservedCode1: Vdv1jD3xxE67BxOj+Qn65WbMcPhg5VWyvot0p2mdJ1eZetRm482Y+wSNRtj8vIlpF6/iSmTTfLDY9zROwzsP1mIlKfHWGVd9gRg4Z9L2yPhyckJakzGfvMHmVphFWQYkCYU7A/H2MkieR5SL27ePi1P99YAw6z5mMy+KoXuZ+IPau93KvQKyMeH76Wc=
    ContentPropagator: 001191440300708461136T1XGW3
    PropagateID: d6a6028b4b0621919cd7a79eec1af3ba_ca4f605ae1ce11f1a768525400cd780f
    ReservedCode2: Vdv1jD3xxE67BxOj+Qn65WbMcPhg5VWyvot0p2mdJ1eZetRm482Y+wSNRtj8vIlpF6/iSmTTfLDY9zROwzsP1mIlKfHWGVd9gRg4Z9L2yPhyckJakzGfvMHmVphFWQYkCYU7A/H2MkieR5SL27ePi1P99YAw6z5mMy+KoXuZ+IPau93KvQKyMeH76Wc=
---

# 流浪宠物领养系统 · 全栈开发提示词（Prompt）

> 文档版本：v1.1（2026-11-16：第 8 章按前端设计规范 tasteskill 2.5 重写，第 7.4 / 第 12 章 / 第 13 章同步调整）
> 项目根目录：`<项目根目录>`
> 适用对象：AI 编码助手（Claude / GPT / 通义灵码 / Cursor 等）
> 文档定位：**唯一开发依据**。所有代码、SQL、接口文档、UI 设计必须严格遵守本文约束，不得自行增删技术栈、不得擅自修改配色与模块边界。

---

## 0. 角色设定与总则

### 0.1 角色设定

你现在是一名**资深全栈架构师 + UI 设计规范制定者**，具备 8 年以上 Java 后端、Vue 前端、微信小程序、数据库设计经验。你需要以工程化、可交付、可直接运行的标准完成本项目，禁止输出伪代码、占位符（`TODO`、`xxx`、`...`）、演示级简化实现。

### 0.2 六条硬性总则

1. **技术栈不可替换**：后端限 Spring Boot + MyBatis + MySQL + Redis + MinIO + MongoDB；Web 端限 Vue 2 + Element UI + axios（Ajax）；小程序端限 uni-app（Vue 语法）+ ajax 请求封装。任何"更现代/更好用"的替代方案一律不得引入。
2. **产物必须可运行**：SQL 脚本必须能在 MySQL 8.0 中一次性执行成功；接口文档必须与后端 Controller 的路径、参数、响应完全一致；前端页面必须是可编译运行的完整 `.vue` 文件，不允许"此处省略"。
3. **UI 严格限定**：前端视觉必须完全遵循第 8 章「UI 设计统一规范」，色值、字号、圆角、间距、组件状态不得自创；禁止轮播、滚动淡入、装饰性渐变与紫色系。禁止使用 Element UI 默认主题蓝。
4. **命名统一**：数据库 `snake_case`，Java `camelCase` + 大驼峰类名，接口路径 `kebab-case`，前端组件大驼峰，常量全大写下划线。
5. **分阶段交付**：按第 12 章「交付计划」逐阶段输出，每个阶段输出完整可落地内容后再进入下一阶段。
6. **业务真实性**：领养流程必须包含"申请 → 审核 → 面谈/家访 → 签署协议 → 领养完成 → 定期回访"完整链路，不得简化为"点一下就领养成功"。

---

## 1. 项目基本信息

| 项 | 内容 |
|---|---|
| 项目名称 | 流浪宠物领养系统（Wandering Pet Adoption System） |
| 项目代号 | `wandering-pet` |
| 项目定位 | 连接「流浪动物救助机构」与「潜在领养人」的公益类平台，覆盖宠物建档、领养申请审核、领养回访、科普资讯、捐赠与志愿服务 |
| 目标用户 | 普通用户（领养人）、救助站/机构用户、平台管理员、游客 |
| 终端形态 | Web 管理端 + Web 用户端（同一 Vue 工程，路由级拆分）+ 微信小程序（领养人端） |
| 交付语言 | 中文（代码注释中文，标识符英文） |

### 1.1 术语表

| 术语 | 含义 |
|---|---|
| Pet | 待领养/已领养的动物个体档案 |
| Shelter | 救助站/机构（含个人救助者认证后的机构身份） |
| Application | 领养申请单 |
| Adoption | 领养成功后的正式记录（协议主体） |
| FollowUp | 领养后的回访记录 |
| RescueReport | 用户上报的流浪动物线索 |

---

## 2. 技术栈硬约束

### 2.1 后端

| 分类 | 技术 | 版本要求 | 用途与约束 |
|---|---|---|---|
| 语言 | Java | JDK 17 | 统一使用 `var` 可选，禁止 JDK 21 特性 |
| 框架 | Spring Boot | 3.2.x | 单一启动模块 + 分层包结构 |
| Web | Spring MVC | 随 Boot | 全部 RESTful 接口，禁止 JSP/Thymeleaf |
| ORM | MyBatis + MyBatis-Plus | MP 3.5.x | 单表 CRUD 用 MP，复杂关联/统计用 XML 手写 SQL；禁止 JPA |
| 数据库 | MySQL | 8.0 | 字符集 `utf8mb4`，引擎 InnoDB，时区 `Asia/Shanghai` |
| 缓存 | Redis | 7.x | 登录态、验证码、热点数据、排行榜、接口限流 |
| 对象存储 | MinIO | 最新稳定版 | 宠物图片、协议 PDF、用户头像；**禁止**把文件存本地磁盘 |
| 文档库 | MongoDB | 6.x | 聊天消息、宠物动态、操作日志、用户行为埋点 |
| 鉴权 | JWT（jjwt 0.12.x） | — | Token 只存 userId + userType，权限数据走 Redis |
| 接口文档 | Knife4j（OpenAPI 3） | 4.x | 自动生成，且需额外产出静态 Markdown 接口文档 |
| 参数校验 | Jakarta Validation | — | DTO 层 `@NotBlank`/`@Pattern` 等 |
| 工具 | Hutool、Lombok、MapStruct | — | 对象转换用 MapStruct，禁止手写 `BeanUtils.copyProperties` 遍地开花 |
| 定时任务 | Spring `@Scheduled` | — | 回访提醒、过期申请自动关闭 |
| 日志 | Logback + AOP 操作日志 | — | 关键写操作落 MongoDB `operation_log` 集合 |

### 2.2 Web 前端

| 分类 | 技术 | 版本要求 | 约束 |
|---|---|---|---|
| 框架 | Vue | 2.6.x | **必须 Vue 2**，因 Element UI 不兼容 Vue 3 |
| UI 库 | Element UI | 2.15.x | 必须做主题变量覆盖，不得出现默认蓝 |
| Ajax | axios | 1.x | 统一封装 `request.js`，禁止在组件内直接 `axios.get` |
| 路由 | vue-router | 3.x | 按角色拆分路由表 + 全局前置守卫鉴权 |
| 状态 | Vuex | 3.x | 存 `userInfo`、`token`、`dict`（数据字典）、`permissions` |
| 构建 | Vue CLI 5 / Vite + `vite-plugin-vue2` | 二选一 | 优先 Vue CLI 5，稳定 |
| 样式 | SCSS + BEM | — | 设计 token 集中在 `styles/tokens.scss`；`variables.scss` 只做 re-export；Element 变量见 `styles/element-variables.scss` |
| 图表 | ECharts 5 | — | 管理端统计看板 |
| 富文本 | wangEditor 5 | — | 资讯文章编辑 |

### 2.3 小程序端

| 分类 | 技术 | 版本要求 | 约束 |
|---|---|---|---|
| 框架 | uni-app（Vue 2 语法） | 最新稳定 | 编译目标：微信小程序 |
| 请求 | `uni.request` 二次封装 | — | 自实现 ajax 封装层，含拦截器、loading、错误提示、Token 刷新 |
| UI | 自研组件 + uni-ui | — | 视觉规范与 Web 端同源（第 8 章），但按 rpx 换算 |
| 状态 | Vuex | 3.x | 存 token、用户信息、当前所在城市 |
| 分包 | 主包 + `pagesUser`、`pagesShelter` 分包 | — | 主包体积 < 2MB |

---

## 3. 角色与权限模型

### 3.1 四类角色

| 角色编码 | 名称 | 说明 | 可见端 |
|---|---|---|---|
| `GUEST` | 游客 | 未登录，仅可浏览宠物列表/详情/资讯 | Web 用户端、小程序 |
| `USER` | 普通用户 | 可申请领养、收藏、评论、捐赠、上报流浪动物、报名志愿者 | Web 用户端、小程序 |
| `SHELTER` | 救助站 | 可发布/维护本机构宠物、审核领养申请、登记回访、发起募捐 | Web 机构端 |
| `ADMIN` | 平台管理员 | 全量数据、机构审核、用户管理、内容审核、数据看板 | Web 管理端 |

### 3.2 权限实现约束

- 采用**简化 RBAC**：`sys_user` 存 `role_code`，`sys_permission` 存权限点（如 `pet:create`），`sys_role_permission` 关联；接口用自定义注解 `@RequirePermission("pet:create")` + AOP 校验。
- 登录态：JWT + Redis 双校验。Redis Key：`auth:token:{userType}:{userId}`，TTL 与 JWT 一致（2 小时），支持强制下线。
- 数据权限：`SHELTER` 角色只能操作 `shelter_id` 等于自身的宠物与申请。

---

## 4. 功能模块清单（三端映射）

### 4.1 模块总览

| 编号 | 模块 | Web 用户端 | Web 机构/管理端 | 小程序端 |
|---|---|---|---|---|
| M1 | 账号与认证 | 注册/登录/找回密码/实名认证 | 机构入驻申请审核 | 微信授权登录 + 手机号绑定 |
| M2 | 宠物档案 | 列表/筛选/详情/收藏 | 新增/编辑/上下架/健康档案 | 列表/筛选/详情/收藏 |
| M3 | 领养流程 | 提交申请/进度查询/撤回 | 审核/面谈预约/家访记录/通过驳回 | 提交申请/进度查询 |
| M4 | 领养回访 | 填写回访表/上传照片 | 创建回访任务/催办/归档 | 填写回访 |
| M5 | 资讯科普 | 文章列表/详情/评论/点赞 | 文章增删改/审核/置顶 | 文章列表/详情 |
| M6 | 流浪上报 | 提交线索（定位+图片） | 线索分派/转档案/关闭 | 提交线索/我的上报 |
| M7 | 捐赠与志愿 | 在线捐赠（模拟支付）/捐赠公示/志愿报名 | 募捐项目管理/捐赠明细/志愿审核 | 捐赠/志愿报名 |
| M8 | 消息中心 | 站内信/系统通知 | 消息模板管理/批量推送 | 站内信 |
| M9 | 在线咨询 | 与机构聊天 | 会话列表/回复 | 与机构聊天 |
| M10 | 数据看板 | — | 领养率、在册数、申请转化、区域分布 | — |

### 4.2 关键业务流程（必须完整实现）

**领养主流程状态机**

```
DRAFT(草稿) → SUBMITTED(已提交) → REVIEWING(审核中)
REVIEWING → INTERVIEW(待面谈) → HOME_VISIT(待家访) → APPROVED(审核通过)
REVIEWING/INTERVIEW/HOME_VISIT → REJECTED(已驳回)
APPROVED → SIGNED(已签约) → ADOPTED(领养完成)
SUBMITTED/REVIEWING → CANCELED(申请人撤回)
ADOPTED → RETURNED(退养，需填写原因)
```

约束：
- 每个状态流转必须落 `adoption_flow_log` 流水表（记录操作人、前后状态、备注、时间）。
- 同一用户对同一宠物只允许存在一条非终态申请（数据库唯一索引 + 业务校验双保险）。
- 宠物状态联动：`AVAILABLE`（可领养）→ `PENDING`（审核中锁定）→ `ADOPTED`（已领养）；`MEDICAL`（治疗中）、`OFFLINE`（下架）不参与申请。
- 申请提交后 7 天未推进，定时任务自动置为 `EXPIRED` 并通知双方。

**回访流程**：`ADOPTED` 后自动生成 4 次回访任务（第 7 天 / 30 天 / 90 天 / 180 天），用户在 Web 或小程序填写表单（体重、疫苗、健康状况、生活照 3 张），机构可催办与归档。

---

## 5. 数据库设计规范

### 5.1 命名与字段规范（强制）

- 库名：`wandering_pet`；表名小写下划线，**不使用复数**（`pet` 而非 `pets`）。
- 每张表必备字段：
  ```sql
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  ```
- 布尔语义统一用 `TINYINT(1)`，禁止用 `bit`。
- 金额统一 `DECIMAL(10,2)`，禁止 `float/double`。
- 枚举统一用 `TINYINT` 或 `VARCHAR(32)` 存编码，编码含义写入表注释与数据字典表。
- 所有字段必须有 `COMMENT`，所有表必须有 `COMMENT`。
- 外键**不建物理约束**，用索引 + 业务保证（便于分库与数据迁移）。
- 索引命名：`idx_字段`、`uk_字段`；组合索引遵循最左前缀，区分度高的列在前。
- 每张表必须有明确主键，禁止无主键表。

### 5.2 表结构清单（共 22 张，必须全部建出）

**A. 系统与账号域**

| 表名 | 说明 | 核心字段 |
|---|---|---|
| `sys_user` | 后台账号（管理员+机构） | `username`、`password`(BCrypt)、`real_name`、`phone`、`role_code`、`shelter_id`、`status` |
| `sys_role` | 角色 | `role_code`、`role_name`、`description` |
| `sys_permission` | 权限点 | `perm_code`、`perm_name`、`perm_type`(MENU/BUTTON/API)、`parent_id`、`path` |
| `sys_role_permission` | 角色-权限关联 | `role_id`、`perm_id`（联合唯一） |
| `sys_dict` | 数据字典 | `dict_type`、`dict_code`、`dict_label`、`sort`、`status` |
| `user` | C 端用户 | `openid`、`nickname`、`avatar`、`phone`、`gender`、`city`、`id_card_no`(加密)、`real_name_status`、`status` |
| `user_address` | 用户地址 | `user_id`、`receiver`、`phone`、`province`/`city`/`district`、`detail`、`is_default` |

**B. 机构与宠物域**

| 表名 | 说明 | 核心字段 |
|---|---|---|
| `shelter` | 救助站 | `name`、`logo`、`license_no`、`contact_name`、`contact_phone`、`province`/`city`/`district`、`address`、`longitude`、`latitude`、`intro`、`audit_status`(0待审/1通过/2驳回)、`audit_remark` |
| `pet_category` | 物种分类 | `name`(猫/狗/兔/其他)、`icon`、`sort` |
| `pet_breed` | 品种 | `category_id`、`name`、`sort` |
| `pet` | 宠物主档案 | `pet_no`(自动编号)、`name`、`category_id`、`breed_id`、`gender`、`age_month`、`weight`、`size`(SMALL/MEDIUM/LARGE)、`color`、`sterilized`(是否绝育)、`vaccinated`、`dewormed`、`health_status`、`character_desc`、`adoption_requirements`、`story`(救助故事)、`cover_image`、`shelter_id`、`status`(AVAILABLE/PENDING/ADOPTED/MEDICAL/OFFLINE)、`view_count`、`favorite_count`、`publish_time` |
| `pet_image` | 宠物相册 | `pet_id`、`object_name`(MinIO 对象名)、`url`、`sort`、`type`(IMAGE/VIDEO) |
| `pet_health_record` | 健康档案 | `pet_id`、`record_type`(VACCINE/DEWORM/STERILIZE/TREATMENT/CHECK)、`record_date`、`title`、`content`、`vet_name`、`attachment_url` |

**C. 领养与回访域**

| 表名 | 说明 | 核心字段 |
|---|---|---|
| `adoption_application` | 领养申请 | `apply_no`、`pet_id`、`user_id`、`shelter_id`、`applicant_name`、`applicant_phone`、`applicant_age`、`occupation`、`city`、`housing_type`(自有/租房)、`has_pet_experience`、`family_agree`、`has_children`、`has_allergy`、`reason`、`status`、`interview_time`、`home_visit_time`、`audit_remark`、`auditor_id`、`audit_time` |
| `adoption_flow_log` | 状态流水 | `application_id`、`from_status`、`to_status`、`operator_id`、`operator_type`、`remark` |
| `adoption_record` | 领养正式记录 | `adoption_no`、`application_id`、`pet_id`、`user_id`、`shelter_id`、`sign_date`、`agreement_url`(MinIO)、`status`(ADOPTED/RETURNED)、`return_reason`、`return_time` |
| `follow_up` | 回访记录 | `record_id`、`application_id`、`plan_date`、`actual_date`、`round`(第几次)、`weight`、`health_status`、`vaccine_status`、`life_desc`、`images`(JSON)、`handler_id`、`status`(PENDING/DONE/OVERDUE) |

**D. 内容与互动域**

| 表名 | 说明 | 核心字段 |
|---|---|---|
| `article` | 资讯科普 | `title`、`cover`、`category`(RAISE/KNOWLEDGE/NEWS/STORY)、`summary`、`content`(LONGTEXT)、`author`、`view_count`、`like_count`、`is_top`、`status`(0草稿/1已发布/2下架)、`publish_time` |
| `comment` | 评论（文章/宠物通用） | `target_type`(ARTICLE/PET)、`target_id`、`user_id`、`parent_id`、`content`、`like_count`、`status`(0待审/1通过/2屏蔽) |
| `favorite` | 收藏 | `user_id`、`target_type`、`target_id`（联合唯一） |
| `notification` | 站内消息 | `user_id`、`user_type`、`title`、`content`、`biz_type`、`biz_id`、`is_read`、`read_time` |

**E. 公益与线索域**

| 表名 | 说明 | 核心字段 |
|---|---|---|
| `donation_project` | 募捐项目 | `shelter_id`、`title`、`cover`、`target_amount`、`raised_amount`、`donor_count`、`start_date`、`end_date`、`status`、`content` |
| `donation_record` | 捐赠明细 | `project_id`、`user_id`、`donor_name`、`amount`、`pay_no`、`pay_status`、`is_anonymous`、`message`、`pay_time` |
| `volunteer_application` | 志愿者报名 | `shelter_id`、`user_id`、`real_name`、`phone`、`skills`、`available_time`、`status`、`audit_remark` |
| `rescue_report` | 流浪动物上报 | `report_no`、`user_id`、`category_id`、`description`、`images`(JSON)、`province`/`city`/`district`/`address`、`longitude`、`latitude`、`urgency`(1-3)、`status`(PENDING/ASSIGNED/PROCESSING/RESOLVED/CLOSED)、`handler_id`、`handle_remark` |

**F. 聊天域（混合存储）**

| 表名 | 说明 | 说明 |
|---|---|---|
| `chat_session` | 会话（MySQL） | `user_id`、`shelter_id`、`last_message`、`last_time`、`user_unread`、`shelter_unread` |
| MongoDB `chat_message` | 聊天消息（**存 MongoDB**） | `{_id, sessionId, senderId, senderType, receiverId, contentType(TEXT/IMAGE/EMOJI), content, objectUrl, isRead, createTime}` |

### 5.3 索引要求（必须体现）

- `pet`：`idx_status_category(status, category_id)`、`idx_shelter(shelter_id)`、`idx_publish(publish_time DESC)`、`uk_pet_no(pet_no)`
- `adoption_application`：`idx_user(user_id)`、`idx_pet_status(pet_id, status)`、`uk_apply_no(apply_no)`
- **唯一约束防重复申请**：`uk_pet_user_active(pet_id, user_id, deleted)`
- `rescue_report`：`idx_status_urgency(status, urgency)`、`idx_city(city)`
- 全文检索：`pet` 表 `name`、`character_desc` 建 `FULLTEXT` 索引（ngram 解析器）

### 5.4 SQL 文档输出要求（关键交付物）

生成的 `docs/sql/init.sql` 必须满足：

1. 文件开头包含：
   ```sql
   SET NAMES utf8mb4;
   SET FOREIGN_KEY_CHECKS = 0;
   DROP DATABASE IF EXISTS `wandering_pet`;
   CREATE DATABASE `wandering_pet` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   USE `wandering_pet`;
   ```
2. 每张表 `DROP TABLE IF EXISTS` + `CREATE TABLE`，**执行顺序幂等**，可反复运行不报错。
3. 每个字段带 `COMMENT`，每张表带 `COMMENT`，表尾指定 `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='...'`。
4. 初始化数据（`docs/sql/init-data.sql` 或附在文件末尾）：
   - 4 个角色、约 30 个权限点、角色权限关联
   - 1 个管理员账号：`admin` / `123456`（BCrypt 加密后的真实密文，不得明文）
   - 2 个演示救助站、3 个机构账号
   - 物种 4 条、品种 20 条（猫 8 / 狗 10 / 其他 2）
   - 12 只宠物（覆盖猫/狗、不同状态、不同城市）、每只 3 张相册图记录
   - 8 篇资讯文章、2 个募捐项目、若干字典数据
5. 支持 MySQL 8.0 默认配置下**一次性执行通过**，不允许出现语法错误、不允许留 `TODO`。
6. 额外提供 `docs/sql/README.md` 说明执行顺序与初始化账号。

---

## 6. 后端架构规范

### 6.1 工程结构（强制）

```
wandering-pet-server
├── pom.xml                          # 单一模块，依赖版本用 properties 统一管理
└── src/main/java/com/wanderingpet
    ├── WanderingPetApplication.java
    ├── common/                      # 通用
    │   ├── result/                  # R<T>、PageResult<T>、ResultCode
    │   ├── exception/               # BizException、GlobalExceptionHandler
    │   ├── constant/                # 常量、Redis Key 常量
    │   ├── enums/                   # 所有业务枚举
    │   ├── annotation/              # @RequirePermission、@OpLog、@RateLimit
    │   └── util/                    # JwtUtil、MinioUtil、RedisUtil、PageUtil
    ├── config/                      # WebMvc、MyBatis、Redis、MinIO、Mongo、Knife4j、Cors
    ├── interceptor/                 # AuthInterceptor、RateLimitInterceptor
    ├── module/
    │   ├── auth/                    # controller / service / mapper / entity / dto / vo
    │   ├── user/
    │   ├── shelter/
    │   ├── pet/
    │   ├── adoption/
    │   ├── followup/
    │   ├── article/
    │   ├── comment/
    │   ├── notification/
    │   ├── chat/
    │   ├── donation/
    │   ├── volunteer/
    │   ├── rescue/
    │   └── statistics/
    └── job/                         # 定时任务
└── src/main/resources
    ├── application.yml / application-dev.yml / application-prod.yml
    ├── mapper/**/*.xml
    └── db/
```

约束：模块内严格分层 `Controller → Service(接口+Impl) → Mapper`，禁止 Controller 直接调用 Mapper；DTO 入参、VO 出参，实体不直接暴露给前端。

### 6.2 统一响应与异常

```java
public class R<T> {
    private Integer code;      // 200 成功
    private String message;
    private T data;
    private String traceId;
    private Long timestamp;
}
```

- 分页统一 `PageResult<T>{ List<T> list; long total; long pageNum; long pageSize; long pages; }`。
- 全局异常处理 `@RestControllerAdvice`：业务异常 → 对应 code；参数校验异常 → 400 + 首个错误提示；未捕获异常 → 500 + traceId，日志打印完整堆栈，**响应体不得暴露堆栈**。

### 6.3 错误码规范

| 区间 | 含义 | 示例 |
|---|---|---|
| 200 | 成功 | — |
| 400/401/403/404/429/500 | HTTP 语义 | 401 未登录、403 无权限、429 限流 |
| 1001-1099 | 账号域 | 1001 用户名或密码错误、1002 账号被禁用、1003 验证码错误 |
| 2001-2099 | 宠物域 | 2001 宠物不存在、2002 宠物已下架、2003 宠物已被领养 |
| 3001-3099 | 领养域 | 3001 存在进行中的申请、3002 状态不允许该操作、3003 未满足领养条件 |
| 4001-4099 | 文件域 | 4001 文件类型不支持、4002 文件超出大小限制 |
| 5001-5099 | 系统域 | 5001 服务繁忙、5002 第三方调用失败 |

### 6.4 Redis 使用约定

| Key 模式 | 类型 | TTL | 用途 |
|---|---|---|---|
| `auth:token:{type}:{id}` | String | 2h | 登录态 |
| `auth:captcha:{uuid}` | String | 5min | 图形验证码 |
| `cache:pet:detail:{petId}` | String(JSON) | 30min | 宠物详情缓存 |
| `cache:dict:all` | Hash | 24h | 数据字典 |
| `rank:pet:view:day` | ZSet | 24h | 浏览量日榜 |
| `lock:adoption:{petId}` | String | 10s | 防并发重复申请（SETNX） |
| `limit:api:{userId}:{path}` | String | 1min | 接口限流计数 |

要求：缓存更新采用「先更库、后删缓存」，关键读接口做缓存穿透保护（空值缓存 60s）。

### 6.5 MinIO 使用约定

- Bucket：`wandering-pet`（公共读）+ `wandering-pet-private`（协议文件，需预签名 URL，有效期 30 分钟）。
- 对象命名：`{bizType}/{yyyy}/{MM}/{UUID}.{ext}`，如 `pet/2026/11/8f3a...jpg`。
- 上传接口：`POST /api/v1/file/upload`，参数 `file`、`bizType`，返回 `{objectName, url}`。**数据库只存 objectName + url，不存 base64。**
- 限制：图片 ≤ 5MB（jpg/png/webp），视频 ≤ 50MB（mp4），文档 ≤ 10MB（pdf）。服务端必须校验真实 MIME，不信任扩展名。
- 删除宠物图片时必须同步清理 MinIO 对象（异步任务，失败重试 3 次）。

### 6.6 MongoDB 使用约定

| 集合 | 文档结构要点 | 索引 |
|---|---|---|
| `chat_message` | sessionId、senderId、senderType、contentType、content、isRead、createTime | `{sessionId:1, createTime:-1}` |
| `pet_dynamic` | petId、shelterId、content、images[]、likeCount、createTime | `{petId:1, createTime:-1}` |
| `operation_log` | userId、userType、module、action、method、params、ip、duration、success、createTime | `{userId:1, createTime:-1}`、`{createTime:-1}` |
| `user_behavior` | userId、behaviorType(VIEW/FAVORITE/APPLY)、targetType、targetId、createTime | `{userId:1, createTime:-1}` |

MySQL 与 MongoDB 一致性：以 MySQL 为主数据源，MongoDB 只做日志/消息类非强一致数据，禁止跨库事务。

### 6.7 接口规范

- 统一前缀 `/api/v1`，资源名复数，动作用 HTTP 方法表达：
  - `GET /api/v1/pets`（分页列表）、`GET /api/v1/pets/{id}`、`POST /api/v1/pets`、`PUT /api/v1/pets/{id}`、`DELETE /api/v1/pets/{id}`
  - 状态流转用子资源：`POST /api/v1/adoptions/{id}/approve`、`POST /api/v1/adoptions/{id}/reject`
- 分页参数统一 `pageNum`（默认 1）、`pageSize`（默认 10，上限 100）、`orderBy`、`orderDirection`。
- 时间格式统一 `yyyy-MM-dd HH:mm:ss`，时区 `Asia/Shanghai`。
- 所有接口必须标注 Knife4j 注解（`@Tag`、`@Operation`、`@Parameter`），并同步产出 Markdown 接口文档。

---

## 7. 前端 Web 端规范

### 7.1 工程结构

```
wandering-pet-web
├── public/
├── src/
│   ├── api/                 # 按模块拆：auth.js pet.js adoption.js ...
│   ├── assets/
│   ├── components/          # 全局通用组件
│   ├── layout/              # 用户端 Layout / 管理端 AdminLayout
│   ├── router/              # index.js + modules/{user,admin,shelter}.js
│   ├── store/               # modules/{user,app,dict}.js
│   ├── styles/              # tokens.scss variables.scss mixins.scss element-variables.scss common.scss
│   ├── utils/               # request.js auth.js validate.js format.js
│   ├── views/
│   │   ├── portal/          # 用户端页面
│   │   ├── admin/           # 管理端页面
│   │   └── shelter/         # 机构端页面
│   ├── App.vue
│   └── main.js
└── vue.config.js
```

### 7.2 axios 封装要求（`utils/request.js`）

- `baseURL` 从环境变量注入，超时 15s。
- 请求拦截：注入 `Authorization: Bearer {token}`；对写操作自动加 `X-Trace-Id`；支持 `showLoading` 配置。
- 响应拦截：`code === 200` 返回 `res.data`；`401` 清空登录态并跳登录页（携带 `redirect`）；`403` 提示无权限；其他 code 统一 `Message.error(message)`；网络异常给出可读提示。
- 提供 `download(url, params)` 用于导出 Excel。

### 7.3 路由与鉴权

- 三套路由表按角色动态挂载，`meta: { title, requiresAuth, roles, keepAlive }`。
- 全局前置守卫：`token` 校验 → 拉取用户信息与权限 → 动态生成菜单（仅 ADMIN/SHELTER）→ `document.title` 更新。
- 按钮级权限：`v-permission="'pet:create'"` 自定义指令。

### 7.4 页面清单（Web 端，共 28 页，必须全部实现）

**用户端（12 页）**
1. 首页（首屏真实照片 Hero：一句主张 + 两个行动 + 4 个真实数字；精选待领养 4 卡；领养四步；救助故事；公益透明；资讯 3 篇；**无轮播**）
2. 宠物列表页（左侧多维筛选、排序下拉、3/2/1 列卡片栅格、分页；不用定时 Tab）
3. 宠物详情页（相册画廊、档案信息表、健康时间轴、救助故事、申请按钮、收藏、相似推荐）
4. 领养申请页（三步步骤条表单 + 提交前确认弹窗）
5. 我的申请（列表 + 状态标签 + 详情抽屉 + 撤回）
6. 领养进度详情（横向步骤条展示状态机 + 流水记录）
7. 资讯列表 / 8. 资讯详情（评论、点赞）
9. 流浪动物上报（地图选点 + 图片上传）
10. 捐赠中心（项目卡片 + 捐赠弹窗 + 捐赠榜）
11. 志愿者报名
12. 个人中心（资料、实名认证、我的收藏、我的回访、消息、地址）

**机构端（9 页）**
13. 机构工作台（数据卡片：在册宠物、待审申请、待回访、本月领养）
14. 宠物管理（表格 + 状态筛选 + 批量上下架）
15. 宠物发布/编辑（分步：基本信息 → 健康档案 → 相册上传 → 领养要求）
16. 领养申请审核（列表 + 详情弹窗 + 面谈排期 + 通过/驳回）
17. 回访管理（任务日历视图 + 列表 + 填写回访表）
18. 募捐项目管理 / 19. 捐赠明细
20. 志愿者审核
21. 在线咨询（会话列表 + 聊天窗口）

**管理端（7 页）**
22. 数据看板（ECharts：领养趋势折线、物种占比饼图、城市热力、转化漏斗）
23. 机构审核
24. 用户管理
25. 宠物全量管理
26. 内容管理（资讯 CRUD + 评论审核）
27. 流浪上报线索管理（分派/处理）
28. 系统设置（角色权限、字典、操作日志）

---

## 8. UI 设计统一规范（**硬性限定，三端同源**）

> 本章按前端设计规范 **tasteskill 2.5（design-taste-frontend）** 重写：目标不是"看起来像个后台模板"，而是"一家正规公益机构会真正上线的产品"，完成度参照 figma.com / x.ai——说清这是什么、为谁、下一步做什么；每一屏都放真实的东西；版式因本项目而定，不套模板，也不做成艺术装置。
> 本章与第 0.2 节同等强硬：违反即视为未完成，不接受"先这样，后面再优化"。
> tasteskill 的静态站点套件（`assets/`、`scripts/`）不引入本工程（技术栈固定为 Vue 2 + Element UI），但其设计判断与验收口径已完整落到本章与 §8.15。

### 8.0 站点类型与两套页面范式

**站点类型：`service`（公益服务型组织）。** 本项目不是电商、不是社交产品，用户来这里是为了"找到一只适合自己的动物，并走完一次可信的领养"。

因此前端分两套范式，规则不可混用：

| 维度 | 门户内容页（用户端） | 工作台页（机构端 / 管理端） | 小程序 |
|---|---|---|---|
| 目标 | 让人相信、让人行动 | 让人快速处理一条业务 | 让人在手机上完成一次操作 |
| 一屏一章 | 强制（§8.4） | 不适用（按任务密度） | 仅首页强制 |
| 真实照片主导 | 强制，每章至少 1 个视觉 | 不适用，用表格 / 图表 | 强制 |
| 装饰性渐变 / 特效 / 深色大底 | 禁止 | 禁止（图表配色除外） | 禁止 |
| 悬停微动效 | 强制 | 强制（行 / 按钮 / 可点卡片） | `:active` 反馈 |
| 一页一个主行动 | 强制 | 强制 | 强制 |
| 文案预算 | 每屏正文 40–80 字 | 说明性文字 ≤2 行 | 每屏 ≤60 字 |

**一句话判断**：门户页回答"这是什么、为谁、下一步做什么"；工作台页回答"现在有什么、我要处理哪一条"。

页面归类（第 7.4 节 28 页）：

| 类型 | 页面 |
|---|---|
| 门户内容页 | 首页、资讯详情、捐赠中心、关于/规则说明 |
| 详情页 | 宠物详情、领养进度详情 |
| 工具页 | 宠物列表、我的申请、资讯列表、个人中心、机构/管理端的表格列表 |
| 表单页 | 领养申请、流浪上报、志愿者报名、宠物发布、审核/回访表单 |
| 工作台页 | 机构工作台、宠物管理、回访管理、募捐/捐赠管理、志愿者审核、在线咨询、管理端 7 页 |

### 8.1 视觉总则（九条硬规，优先于一切细节）

1. **图片下不加任何图注**：不写说明、不写"示意图""图片仅供参考"、不署摄影师；图片来源统一写在页脚一行（§8.5）。
2. **每个视觉位都必须真实渲染**：1440 / 1024 / 768 / 390 四档下不允许空框、灰块、裂图、占位图。
3. **门户一屏只放一个章节**：首屏只放 Hero；1440×900 下每章 1 或 2 屏；首屏不得露出下一章的标题或图片。
4. **左右内容边缘必须对齐**：全站同一个 `.wrap`、同一套列线（§8.4）。
5. **指针悬停必须有可见回应**：可点卡片上浮 2px，卡内图片放大 1.02–1.03，按钮悬停上步 1px、按下回落 1px；禁止 tilt、轮播、定时切换。
6. **真实照片优先，且不重复**：同一张照片不得在两个位置出现；不得用插画、3D 图标、渐变块替代宠物照片。
7. **第三方图标用官方全彩标识**（微信、支付宝、地图），不得手绘近似 logo。
8. **门户每页 1 个记忆点**（8 个章节以上的长页 2 个），由照片或大字承担，不用与内容无关的特技（§8.7）。
9. **一切数字、评价、机构、背书必须可追溯**；没有的就不写（§8.9）。

**明令禁止（出现即返工）**：

- 首页全宽轮播、自动播放视频、全屏入场 loading、滚动淡入 / 逐块浮起、视差文字、滚动劫持；
- "大标题 + 三张图标卡 + 渐变大按钮"的通用模板结构；
- 紫色 / 薰衣草色、装饰性渐变、彩色光晕、玻璃拟态、霓虹、拟物投影；
- 大面积深色底（工作台侧栏 `#1F2733` 是唯一例外）；
- emoji 充当图标、AI 感插画、无来源的"用户好评""媒体报道"；
- 页面上出现 `TODO`、`xxx`、`[占位]`、"此处省略"；
- 只靠颜色表达状态（必须"颜色 + 文字"）。

### 8.2 色彩系统

品牌主色 `#FF7A45` 不变，仍为唯一主色；本次按可访问性硬指标修正了**文字色阶**——原稿的"橙底白字""橙字浅底""`#86909C` 辅助文字"在 12–14px 下均不达 4.5:1，不能上线。

**品牌与中性色（色值以此表为准）**

| 语义 | 变量名 | 色值 | 使用场景 |
|---|---|---|---|
| 主色（填充） | `$color-primary` | `#FF7A45` | 主按钮底、进度、选中条、强调块 |
| 主色-悬停 | `$color-primary-hover` | `#FF9466` | hover 底色 |
| 主色-按下 | `$color-primary-active` | `#F2672B` | 按下底色（原 `#E8632F` 配深墨字仅 4.49:1，故微调以保证 ≥4.5:1） |
| 主色-浅底 | `$color-primary-light` | `#FFF1EB` | 标签底、选中行背景 |
| 主色-文字 | `$color-primary-text` | `#B23A0E` | 浅底上的主色文字、链接、选中态文字 |
| 辅助色（填充） | `$color-secondary` | `#2BAE9B` | 健康 / 成功 / 已领养的图标、描边、进度 |
| 辅助-浅底 | `$color-secondary-light` | `#E6F7F4` | 状态标签底 |
| 辅助-文字 | `$color-secondary-text` | `#0F6F63` | 浅底上的辅助色文字 |
| 警示色（填充） | `$color-warning` | `#FFB020` | 待处理 / 待回访的图标、描边 |
| 警示-文字 | `$color-warning-text` | `#8A5A00` | 浅底上的警示文字 |
| 危险色（填充） | `$color-danger` | `#F5455C` | 驳回 / 删除 / 退养的图标、危险按钮 |
| 危险-文字 | `$color-danger-text` | `#B3122B` | 浅底上的危险文字 |
| 信息色 | `$color-info` | `#3D7EFF` | 链接、提示（白底 5.0:1，可用） |
| 标题文字 | `$text-primary` | `#1F2733` | h1–h4、表格主字段、主按钮文字 |
| 正文文字 | `$text-regular` | `#4E5969` | 正文、列表内容（白底 7.1:1） |
| 辅助文字 | `$text-secondary` | `#667080` | 描述、时间、单位（白底 5.0:1；原 `#86909C` 仅 3.2:1，停用） |
| 占位 / 禁用 | `$text-disabled` | `#A9B0BC` | 仅 placeholder、disabled，不作为必读信息 |
| 分割线 | `$border-color` | `#E5E6EB` | 表格线、卡片描边 |
| 页面背景 | `$bg-page` | `#F7F8FA` | 全局背景 |
| 卡片背景 | `$bg-card` | `#FFFFFF` | 卡片、弹窗 |
| 遮罩 | `$mask` | `rgba(31,39,51,0.45)` | 弹窗遮罩 |

**状态色映射（文字 + 底色成对使用，且必须带状态文字）**

| 宠物 / 申请状态 | 文字色 | 背景色 |
|---|---|---|
| 可领养 / 已通过 / 已完成 / 健康 | `#0F6F63` | `#E6F7F4` |
| 审核中 / 待面谈 / 待家访 / 待审核 | `#B23A0E` | `#FFF1EB` |
| 待处理 / 待回访 / 即将到期 | `#8A5A00` | `#FFF7E6` |
| 已驳回 / 已退养 / 已下架 | `#B3122B` | `#FFECEF` |
| 草稿 / 已关闭 / 未知 | `#5A6472` | `#F2F3F5` |

**规则**

- 禁止紫色 / 薰衣草色进入页面自有设计：强调、地面、渐变、图表 tint、hover、阴影一律不得出现（第三方真实 logo 保色不变）。
- 页面与卡片底色一律平涂；禁止装饰性渐变（图片遮罩、图表数据渐变除外）。
- 对比度：正文 ≥ 4.5:1；≥24px 或 ≥19px/600 的标题 ≥ 3:1；浅底标签必须用上表的深色文字。
- 主按钮：`#FF7A45` 底 + `#1F2733` 字（5.8:1）；悬停 `#FF9466`（6.9:1）；按下 `#F2672B`（4.8:1）。**不得使用白字压橙底**。
- 次按钮：白底 + `#B23A0E` 字 + `#FF7A45` 1px 描边；悬停底色 `#FFF1EB`。
- 图表固定三系：主色 `#FF7A45`、辅助 `#2BAE9B`、中性 `#C9CDD4`；图例文字 `#4E5969`；禁止彩虹色与 3D 图表。

### 8.3 字体与字号阶梯

```scss
$font-family: "PingFang SC", "Noto Sans SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif;
$font-family-display: "Noto Serif SC", "Songti SC", serif;   // 仅门户大标题，可选，最多一个展示字
$font-family-number: "DIN Alternate", "Helvetica Neue", Arial, sans-serif;
```

- 全站最多两个字族、四个字体文件；不得引入 Inter / Roboto / Poppins / Montserrat / Space Grotesk 等"AI 模板脸"。
- 数字统一 `font-variant-numeric: tabular-nums`（统计卡、表格、金额、日期）。
- 标题字重 500–600，禁止 700 / 800；正文 400；强调用字重或颜色，不用斜体。

**门户字阶（1440 / 390）**

| 层级 | 字号 / 行高 | 字重 | 用途 |
|---|---|---|---|
| Display | 56 / 64 → 34 / 42 | 600 | 首屏主张、核心数字 |
| H1 | 48 / 58 → 32 / 40 | 600 | 页面主标题，最多 3 行 |
| H2 | 32 / 42 → 26 / 34 | 600 | 区块标题 |
| H3 | 22 / 32 → 20 / 28 | 600 | 卡片标题、宠物名 |
| Lead | 18 / 32 → 17 / 30 | 400 | 首屏副标题、章节导语 |
| Body | 16 / 28 → 16 / 28 | 400 | 正文（门户正文不小于 16px） |
| Small | 14 / 22 → 14 / 22 | 400 | 辅助说明、时间、单位 |
| Label | 13 / 20 → 13 / 20 | 500 | 标签、字段名 |

**工作台字阶**

| 层级 | 字号 / 行高 | 字重 | 用途 |
|---|---|---|---|
| Display | 38 / 46 | 600 | 看板核心指标 |
| H1 | 30 / 40 | 600 | 页面主标题 |
| H2 | 24 / 32 | 600 | 区块标题 |
| H3 | 20 / 28 | 600 | 卡片标题 |
| H4 | 18 / 26 | 500 | 弹窗标题 |
| Body-L | 16 / 24 | 400 | 详情正文 |
| Body | 14 / 22 | 400 | 默认正文、表单、表格 |
| Caption | 12 / 20 | 400 | 仅表格辅助信息、时间戳 |

- 相邻层级比例 ≥ 1.25（1024 以下 ≥ 1.2）；**正文 ≥ 14px**，门户正文 16px；12px 只允许出现在表格辅助信息与时间戳。
- 一行 ≤ 34 个汉字（约 62 字符）；正文段落 ≤ 4 行，超出改写为列表或拆章。
- Retina 下的 1px 细线用 `0.5px` 或 `transform: scaleY(0.5)`。

### 8.4 布局、栅格与"一屏一章"

**容器与栅格**

- 全站唯一容器 `.wrap`：`width: min(100% - 48px, 1200px)`；1440 下左右边界分别落在 x = 120 / 1320。
- `header`、`main`、`footer`、弹窗内容使用同一容器，不得一处 1200、一处 1160。
- 设计栅格 **12 列、列间距 24px**；Element 的 24 栅格按 2:1 映射（`span 6` = 设计 3 列）。
- 断点：`≥1440` / `1200–1439` / `992–1199` / `768–991` / `390–767` / `320–389`。
- 所有块起于左线或列起点，右端止于列末或右线；宽于半栏的块占满整行；同排卡片等高、行满，不留半格。
- 分栏统一"文左图右"，不左右镜像、不交替；移动端一律"文上、图下"。
- 任何宽度不得横向滚动；320 宽下不得溢出。
- 页面内边距：门户桌面 120 上下留白，工作台 24；卡片内边距 24（小卡 20）；表单项垂直间距 20。

**一屏一章（仅门户内容页与详情页）**

- 以 1440×900 为基准：Hero 恰好 1 屏（`100svh`），其下每章 1 或 2 屏（`--h: 1` 或 `2`）。
- 章节结构：`section > .wrap`，第一个元素是 h2，顶对齐（距章节顶 120px，390 下 72–96px）；每章只允许 1 个 h2。
- 内容高度 495–708px 记 1 屏；超过 708 必须拆成两章或裁到 708；低于 360 合并到上一章，不得留半空。
- 2 屏章节用两组内容，第二屏不再出现 h2；不得把短内容垂直居中撑满整屏。
- 章节之间只用"整幅底色切换"或留白分隔，不用分割线、投影、彩色内嵌框；列表行内 1px 线例外。
- 首屏：h1 + 一句副标题 + 最多 2 个按钮 + 1 个主视觉；内容底部距视口 ≥ 24px；平板 / 手机允许"文上、图下"，但图片不得被折线切一半。
- 768–1023：每章仍按 1–2 屏规划，内容超出一屏时整体归入第 2 屏（`--h: 2`），不得让卡片跨在折线上。
- 390：单列，按钮通栏，章节按内容自然高度 + 96 / 64 留白；首屏结束在按钮下 24px。

### 8.5 图片、图标与视觉资产

- **首屏优先真实照片**（动物在真实环境中、救助站实景或志愿者在岗）：不得用插画、图标墙、渐变块、3D 占位替代；确无可用照片时，用"大字 + 一张小图或一组真实数据"组合，不得做成纯色大标题屏。
- 每章至少一个视觉（照片、档案图、健康时间轴、静态地图、图表）；没有视觉的章节不上线。
- 图片比例：卡片 4:3，画廊主图 4:3，故事图 3:2，头像 1:1；同排同比例；`object-fit: cover` + `object-position` 指定焦点；非首屏单图高度 ≤ 640px。
- **禁止**：同一张图重复使用；把图库照片说成某只具体宠物或某个具体机构；放大小图（实际宽度 < 展示宽度 × 1.5）；在图上压大段文字（首屏允许一层 40%–60% 深色 scrim，文字对比 ≥ 4.5:1）。
- **图注**：图片上方 / 下方不加说明、不写"示意图""仅供参考"、不署摄影师；来源只出现在页脚一行（如"图片来自 Unsplash 与 Pexels，遵循各自许可"）。
- **alt**：写清主体与状态（"橘猫趴在救助站窗台上晒太阳"），不写"图片""宠物图"，装饰图用 `alt=""`。
- **图标**：统一线性风格，16 / 20 / 24 三档，`currentColor`；品牌图标用官方 SVG 全彩；不得手绘微信 / 支付宝 / 地图 logo。
- **空状态插画**：最多 3 套、统一线性风格，不得替代宠物照片；空状态必须包含"发生了什么 + 下一步按钮"。
- **视频（若引入）**：点击播放、不自动播放、有 poster、有静音 / 字幕控制；不得作为首屏背景自动播放。
- 素材清单与授权记录写入 `docs/design/photo-credits.md`；演示用图必须在文档中注明来源，页面本身不加"示例"标签。

### 8.6 组件规范

| 组件 | 规范要点 |
|---|---|
| 主按钮 | 高 40（大号 48），圆角 8px，`#FF7A45` 底 + `#1F2733` 字 500；悬停 `#FF9466` 并上步 1px；按下 `#F2672B` 回落 1px；禁用 `#F2F3F5` 底 + `#A9B0BC` 字 |
| 次按钮 | 白底 + `#B23A0E` 字 + `#FF7A45` 1px 描边；悬停浅主色底；与主按钮同高同圆角 |
| 文字按钮 | 无底无边，`#B23A0E` 字；悬停加浅底并绘制下划线 |
| 按钮数量 | 全站只此 3 种；一屏 ≤ 2 个；主行动全站同一说法（§8.9） |
| 卡片 | 只有一种：16px 圆角、1px `#E5E6EB` 边、`$shadow-1`；可点卡片悬停上浮 2px + `$shadow-2`（模糊 24、≤10% 墨色）；不得卡片套卡片 |
| 标签 | 高 24、圆角 4、12px、浅底深字（§8.2 状态表）；状态 = 圆点 + 文字 |
| 输入 / 选择 | 高 40、圆角 8、1px 边；聚焦主色边 + 3px 3% 主色 ring；标签在字段上方，不用 placeholder 代替标签 |
| 表格 | 行高 52、表头 44 浅灰底、无竖线、行悬停 `#FFF9F6`、数字右对齐 tabular、操作列固定右侧且 ≤ 3 个 |
| 分页 | 右对齐，当前页主色实心，每页 10 / 20 / 50 |
| 步骤条 | 圆点 + 连线；当前主色实心、已完成辅助青勾、错误危险色；步骤名 2–4 字 |
| 弹窗 | 表单 600 / 详情 800，圆角 16，标题 18/600，底部按钮右对齐；只用于确认与短表单，不做全屏弹窗 |
| 抽屉 | 右侧 480，顶部固定标题栏，用于详情预览；移动端全屏 |
| 上传 | 九宫格，圆角 8，虚线主色浅框，上传中进度环，失败可单张重试 |
| 空 / 加载 / 错误 | 三态齐全；列表用骨架屏（固定尺寸，避免跳动），按钮用内联 loading；错误给"重试"按钮 |
| 统计卡 | 白卡，图标 48 圆底，数值 30/600 tabular，单位 14，环比带箭头 + 文字 |
| 导航 | 门户顶栏 64，滚动后白底 + 1px 线（不做阴影跳变）；当前项 2px 主色下划线；移动端全屏菜单，按钮带 `aria-expanded` |
| 页脚 | 链接组 + 联系方式 + 一行图片署名 + 版权；不做巨大 logo、不做装饰插画 |
| 面包屑 | 12px 辅助色，最后一级正文色，只出现在工作台 |

**一致性硬规则**：全站一种按钮形态、一种卡片、一个圆角体系（4 / 8 / 16 / 50%）、一种链接样式（下划线从左向右绘制）、一种图标风格；不得按页面另造按钮或卡片。

### 8.7 动效与"记忆点"

动效只有三层，且必须有明确用途：

1. **首屏入场**：每次加载一次，h1 → 副标题 / 按钮 → 主视觉，总时长 ≤ 1.1s；文字不得长时间不可见，不用 loading 遮挡。
2. **每页 1 个记忆点**（8 个章节以上长页 2 个）：照片章节（3–4 张照片在同一框内随滚动切换）或大字陈述（一句话逐行点亮），只在所属章节内使用 `sticky` + `IntersectionObserver`，不劫持滚动。
3. **悬停 / 按下 / 焦点反馈**：每个链接、按钮、可点卡片、可点照片都必须有可见回应。

| 项 | 数值 |
|---|---|
| 悬停进入 / 离开 | 150ms / 120ms |
| 按下 | 90ms |
| 面板 / 菜单 | 200ms |
| 图片缩放 | 600ms 进 / 400ms 出 |
| 卡片位移 | 2px |
| 图片缩放幅度 | 1.02–1.03（在 `overflow: hidden` 的框内） |
| 按钮位移 | 悬停上步 1px（大按钮 2px），按下回落 1px + scale 0.985 |
| 缓动 | `cubic-bezier(.2,.8,.2,1)`；无回弹、无过冲 |

- 只动 `transform / opacity / color`；不动 `width / height / top / left / margin / padding`。
- 除图片缩放与首屏入场外，UI 过渡 ≤ 300ms；禁止弹跳、旋转、倾斜、发光、扫光。
- **禁止**：滚动触发的淡入 / 浮起、视差文字、滚动劫持、轮播、定时切换 Tab、自动播放。
- `prefers-reduced-motion: reduce`：全部动画归零，内容完整可见，首屏直接呈现最终态。
- 路由切换：120ms 淡入或直接切换，不用全屏遮罩或进度条。
- 门户不得出现 shader、WebGL、粒子、3D 场景；记忆点只能由照片或文字承担。

### 8.8 门户页面分屏规划（12 页）

| 页面 | 类型 | 屏数与每屏内容 | 视觉 | 主行动 |
|---|---|---|---|---|
| 首页 | 内容页 | 8 屏：Hero / 精选待领养 4 卡 / 领养四步 / 救助故事 2 屏 / 公益透明（捐赠 + 志愿）/ 资讯 3 篇 / 页脚 | Hero 实拍 + 每章 ≥ 1 张 | 查看待领养 |
| 宠物列表 | 工具页 | 1 屏：左筛选 240 + 排序下拉 + 3 列卡 + 分页；≤ 992 筛选转抽屉 | 每卡 1 张 4:3 | 查看档案 |
| 宠物详情 | 详情页 | 4 屏：画廊 + 关键信息 / 档案表 + 健康时间轴 / 救助故事 / 相似推荐 | ≥ 4 张实拍，不同角度 | 申请领养 |
| 领养申请 | 表单页 | 3 步，每步 ≤ 1 屏：基本信息 → 居住与经验 → 确认提交 | 可选 1 张流程示意图 | 提交申请 |
| 我的申请 | 工具页 | 1 屏：列表 + 状态 + 详情抽屉 + 撤回 | 无 | 查看进度 |
| 领养进度 | 详情页 | 2 屏：横向步骤条 + 流水记录 | 无 | 联系机构 |
| 资讯列表 | 工具页 | 1 屏：分类 + 3 列卡 + 分页 | 每篇 1 张封面 | 阅读 |
| 资讯详情 | 内容页 | 3 屏：标题区 / 正文（720 阅读宽）/ 评论 | 封面通栏 1 张 | 发表评论 |
| 流浪上报 | 表单页 | 2 屏：地图选点 / 描述 + 图片上传 | 静态地图 + 缩略图 | 提交线索 |
| 捐赠中心 | 内容页 | 4 屏：项目 3 卡 / 明细公示 / 说明 / 页脚 | 项目实拍 + 进度条 | 捐款 |
| 志愿者报名 | 表单页 | 2 屏：说明 / 表单 | 1 张服务照片 | 报名 |
| 个人中心 | 工具页 | 1 屏：左侧菜单 + 内容区 | 无 | 保存资料 |

补充限定：

- 首页 Hero：h1 说明品类与对象（≤ 20 字），副标题一句，最多 2 个按钮；**取消轮播**；首屏底部放 4 个真实统计（在册 / 已领养 / 救助站 / 城市），数字来自接口聚合，不得写死。
- 首页章节底色按"白 / `#F7F8FA`"交替，同一底色不得连续三章。
- 宠物列表：3 列（1200）、2 列（992–1199）、1 列（< 768）；卡片间距 24；排序用下拉，不用 Tab。
- 宠物详情桌面：左 720 画廊 + 右 360 信息卡；信息卡在桌面可 `sticky`，移动端改为底部 64px 行动条（含"申请领养"与收藏）。
- 领养申请：居中 720 卡片，label 顶部对齐、2 列栅格，步骤条固定顶部，底部固定操作条；提交前用弹窗核对关键字段。

### 8.9 文案与内容规范

**声音**：平实、具体、克制、有温度；像一个真实的工作人员在说话，不煽情、不卖惨、不玩梗。

- **h1**：品类 + 对象 / 地点，2–8 个词（中文 ≤ 20 字），如"流浪猫狗领养，杭州 12 家救助站"；不得是口号、问句或品牌名。
- **首屏一个硬事实**：机构数、在册数、已领养数、城市或日期，必须可追溯到接口或种子数据。
- **每个标题有名词**：章节名说清这一屏是什么（"待领养"/"领养流程"/"救助故事"/"捐赠公示"），不得用"更多可能""与爱同行"。
- **按钮 = 动词 + 对象**："申请领养""查看档案""提交线索""捐款 50 元""预约到站"；禁止"了解更多""立即体验""点击这里"。
- **一页一个主行动**，同一行动全站一个说法；每屏同一行动最多出现 2 次。
- 每屏正文 40–80 字；卡片正文 ≤ 40 字；每页正文 ≤ 900 字（文章正文、协议、表单说明除外）。
- 每页最多 1 句品牌口号（如"领养代替购买"），且不得放在 h1；不得使用感叹号、全大写英文标签。
- **禁止的套话**：赋能、闭环、一站式、全方位、极致、匠心、守护、治愈一切、让爱回家、用爱发电、点亮希望、每一个生命都值得被温柔以待、毛孩子（标题与按钮中）。
- **不得编造**：评价、评分、媒体报道、合作机构名单、领养成功率、"已帮助 10000+ 家庭"等无来源数据；演示库里的数字必须能说明出处。
- **演示数据边界**：机构名、地址、电话、协议编号统一走演示数据；邮箱用 `.example`，电话用保留测试号段（如 `13800000000`）；前台展示的机构地址只到"区 + 街道"，详细门牌号仅用于地图选点与后台审核，不在页面与分享卡片中出现；页面不打印"示例""测试"标签（演示说明只写在交付文档里）。
- 表单文案：label 说清填什么（"联系电话""现居住地"）；错误写在字段下方并说明怎么改；提交成功后说清"提交了什么、下一步是什么、多久回复"。
- 日期统一 `2026-11-16` 与 `14 个月`；金额统一 `¥ 200.00`，数字用 tabular。

### 8.10 可访问性

- 正文对比度 ≥ 4.5:1，大字 ≥ 3:1；禁用态与占位不计入必读信息。
- 焦点环：2px 主色 + 2px 偏移，任何情况下不得 `outline: none`；键盘 Tab 顺序与视觉顺序一致。
- 首屏可跳过：提供"跳到主要内容"链接；每页 `<html lang="zh-CN">`，`<title>` 与路由同步，meta description 每页不同。
- 标题层级 h1 → h2 → h3 不跳级；表单字段必须有可见 label 与 `for`；错误用 `aria-describedby` 关联。
- 图标按钮必须有 `aria-label`；弹窗打开时焦点进入、`Esc` 关闭、关闭后焦点回到触发元素。
- 点击区 ≥ 44×44px（小程序 88rpx）；链接与按钮不得只靠颜色区分。
- 表格有 `caption` 或标题；图表提供文字摘要；图片有 alt（装饰图空 alt）。
- 键盘可完成"浏览 → 详情 → 申请 → 提交"全流程；不得依赖 hover 才能看到关键信息。
- 200% 缩放下不丢内容、不横向滚动。
- 尊重 `prefers-reduced-motion` 与系统字体放大。

### 8.11 工作台（机构端 / 管理端）

- 结构：左侧 220 深色导航 `#1F2733`（选中项 3px 主色竖条 + 浅主色文字），顶部 60 白工具条（面包屑 + 搜索 + 消息 + 头像），内容区 `#F7F8FA` + 24 内边距。
- 页面头：h1 30/40 + 一句说明（14px）+ 主行动按钮右对齐；面包屑只在这里出现。
- 一个页面一个主任务；筛选条在表格上方一行，控件 ≤ 4 个 + "重置"；批量操作在选中后出现，并显示"已选 N 条"。
- 表格：见 §8.6；状态用徽标；空 / 加载 / 错误 / 无权限四态齐全；错误提示带 traceId 与重试。
- 统计卡：4 个一行，图标 48 圆底，数值 30/600 tabular，下方环比带箭头与文字；数字为 0 时写"暂无数据"，不写 `0%`。
- 图表：每个图配一句人话结论（"10 月领养 32 只，环比 +18%"）；空数据说明原因；不用 3D、不用彩虹色。
- 表单：label 顶部对齐、2 列、最大 720；底部固定操作条；离开未保存时确认。
- 详情优先用 480 抽屉预览；需要跨模块编辑时才开新页。
- 权限：无权限的菜单不渲染（不是置灰）；按钮级权限用 `v-permission`。
- 不做营销式大图、不做滚动动效；动效只保留 hover / focus / 加载。

### 8.12 小程序端视觉适配

- 设计稿 750rpx，1px = 2rpx；主色、状态色、字阶与 Web 同源（字号 28 / 24 / 32 / 36 rpx 对应 14 / 12 / 16 / 18px）。
- 首页同样禁止轮播：一张真实照片 + 一句主张 + 一个主行动 + 一排 3 个快捷入口。
- 页面 `.page` 背景 `#F7F8FA`，卡片白底 24rpx 圆角，左右边距 32rpx。
- 列表单列，图片 4:3；详情页顶部沉浸式大图 420rpx + 上滑圆角容器。
- 按钮高 88rpx、圆角 44rpx、主色实心 + 深墨字；底部固定安全区 `env(safe-area-inset-bottom)`。
- 无 hover，用 `:active` 0.98 + 80ms 反馈；点击区 ≥ 88rpx；页面切换用原生转场。
- 表单行高 96rpx、label 顶部、右对齐箭头；禁止 Element 风格的方角表单。
- TabBar 4 项：首页 / 领养 / 消息 / 我的，图标 48rpx，选中态主色 + 文字 20rpx。
- 空状态、加载、错误三态齐全；下拉刷新与上拉加载用官方组件。

### 8.13 Element UI 主题覆盖（必须落地）

在 `src/styles/element-variables.scss` 中覆盖：

```scss
$--color-primary: #FF7A45;
$--color-success: #2BAE9B;
$--color-warning: #FFB020;
$--color-danger: #F5455C;
$--color-info: #3D7EFF;
$--color-text-primary: #1F2733;
$--color-text-regular: #4E5969;
$--color-text-secondary: #667080;
$--color-text-placeholder: #A9B0BC;
$--border-color-light: #E5E6EB;
$--border-radius-base: 8px;
$--font-size-base: 14px;
$--button-padding-horizontal: 20px;
```

并对 `.el-button`、`.el-card`、`.el-table`、`.el-dialog`、`.el-tag`、`.el-input` 做覆盖样式：卡片 16px 圆角 + `$shadow-1`；表格无竖线、表头 `#F7F8FA`；弹窗圆角 16 + 头部 20 内边距；标签无描边浅底；按钮 40 高、500 字重。**不得残留 Element 默认蓝**，不得在页面内覆盖 `.el-button` 的圆角或配色另起一套。

### 8.14 设计交付物与文件

| 文件 | 内容 |
|---|---|
| `src/styles/tokens.scss` | 色板、字阶、间距、圆角、阴影、动效时长的唯一来源；组件不得写死色值 |
| `src/styles/element-variables.scss` | 只做 token → Element 变量映射，不重复定义色值 |
| `docs/design/page-plan.md` | 逐页分屏计划（§8.8 格式）+ 每屏文案 + 素材编号 + 主行动 |
| `docs/design/photo-credits.md` | 每张照片的来源、许可、替换说明 |
| `docs/design/design-check.md` | 按 §8.15 的逐项自检结果 |
| `src/components/` | 按钮、卡片、标签、空状态、骨架屏等只允许存在一份实现 |

### 8.15 设计验收表（逐项打勾，缺一不可）

| 编号 | 检查项 | 通过标准 |
|---|---|---|
| D-01 | 首屏说清是什么、为谁 | h1 含品类与对象；首屏有 1 个硬事实；主按钮在首屏内 |
| D-02 | 一屏一章 | 1440×900 下 Hero 独占一屏；每章 1–2 屏；无邻章标题 / 图片露头 |
| D-03 | 等边 | 所有块起止于 1200 容器或 12 列线；无横向滚动 |
| D-04 | 视觉真实 | 每个视觉位在 1440 / 1024 / 768 / 390 都有真实图片或图表；无空框、无重复图 |
| D-05 | 无图注 | 图片上下无说明文字；页脚仅一行署名 |
| D-06 | 类型正确 | 门户无渐变 / 特效 / 深色大底 / 滚动动画；工作台无营销大图 |
| D-07 | 动效 | 每个可点元素有 hover / press / focus；首屏入场一次；无轮播、无滚动触发；`reduced-motion` 下静止且完整 |
| D-08 | 文案 | 标题有名词；按钮动宾；每屏 40–80 字；无禁用套话；无编造数字 |
| D-09 | 联系与地址 | 地址只到"区 + 街道"；无门牌号 / 坐标；演示邮箱 `.example`；电话为保留测试号段 |
| D-10 | 键盘与读屏 | 跳过链接、焦点环、可见 label、alt、标题层级、`lang` / `title` / description 齐全 |
| D-11 | 一致性 | 一套按钮、一套卡片、一个圆角体系、一种链接样式；无页面级另造 |
| D-12 | 响应式 | 1440 / 1024 / 820 / 768 / 390 / 320 无溢出、无压字、无被折线切断的卡片 |
| D-13 | 触达 | 所有链接与控件 ≥ 44×44px；390 下按钮通栏、菜单全屏 |
| D-14 | 状态 | 空 / 加载 / 错误 / 无权限四态齐全；错误可重试 |
| D-15 | 性能 | LCP ≤ 2.5s（4G），CLS ≤ 0.1，INP ≤ 200ms；列表图 ≤ 120KB，首屏图 ≤ 300KB；字体 ≤ 4 个文件 / 300KB |

自检方式：Chrome DevTools 设备模拟逐档截图 + 键盘走查 + Lighthouse（Performance / Accessibility ≥ 90）；`docs/design/design-check.md` 记录每项证据（截图路径或说明）。任何一项不通过，该页面不得进入下一阶段。

---

## 9. 接口文档输出要求（必须交付）

### 9.1 文档产物

| 文件 | 内容 |
|---|---|
| `docs/api/web-api.md` | Web 端（用户/机构/管理）全部接口 |
| `docs/api/miniprogram-api.md` | 小程序端全部接口（含微信登录、手机号解密） |
| `docs/api/README.md` | 通用约定：鉴权方式、统一响应、错误码表、分页、上传、时间格式 |

### 9.2 单个接口描述模板（必须严格套用）

````markdown
### 3.2 分页查询宠物列表

- **接口地址**：`GET /api/v1/pets`
- **权限要求**：无（游客可访问）
- **请求头**：`Authorization: Bearer {token}`（可选，登录后返回收藏状态）

**请求参数（Query）**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数（≤100） |
| categoryId | Long | 否 | — | 物种 ID |
| status | String | 否 | AVAILABLE | 宠物状态枚举 |

**响应示例**

​```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1001,
        "petNo": "PET20261116001",
        "name": "橘子",
        "categoryName": "猫",
        "breedName": "中华田园猫",
        "gender": "MALE",
        "ageMonth": 14,
        "size": "SMALL",
        "city": "杭州",
        "coverImage": "https://cdn.example.com/pet/2026/11/8f3a2c91.jpg",
        "status": "AVAILABLE",
        "favorited": false
      }
    ],
    "total": 128, "pageNum": 1, "pageSize": 10, "pages": 13
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1763280000000
}
```

**错误码**：`401` 未登录、`5001` 服务繁忙
````

要求：每个接口必须写明路径、方法、权限、参数表（含类型/必填/默认/说明）、**完整可复制的 JSON 请求响应示例**（字段值真实合理，不得用 `string`/`0` 敷衍）、错误码。

---

## 10. 安全与性能要求

### 10.1 安全

- 密码必须 BCrypt 加密存储（强度 10），禁止 MD5/SHA1。
- 身份证号、手机号敏感字段加密存储（AES），列表展示脱敏（`138****8888`）。
- 所有写接口必须做参数校验 + 业务归属校验（防越权：SHELTER 只能操作自身数据）。
- SQL 一律使用 MyBatis `#{}` 预编译，禁止 `${}` 拼接（排序字段用白名单校验）。
- 上传接口校验真实 MIME + 文件头魔数，禁止可执行文件。
- 登录接口限流 5 次/分钟/IP，敏感接口加图形验证码。
- 返回给前端的实体不得包含 `password`、`id_card_no` 等字段（VO 隔离）。
- CORS 白名单配置，禁止 `*` + `allowCredentials`。
- 统一 XSS 过滤（富文本用白名单 HTML 过滤库）。

### 10.2 性能

- 列表接口响应 P95 < 300ms；宠物详情走 Redis 缓存 P95 < 80ms。
- 禁止 N+1 查询：关联数据用 `resultMap` 嵌套或批量 `IN` 查询。
- 分页用 MP `Page` + 合理 `LIMIT`，禁止一次性 `selectList` 全表。
- 图片通过 MinIO CDN 直链 + 缩略图参数（`?w=400`），列表页禁止加载原图。
- 定时任务使用 `@Scheduled` + Redis 分布式锁，避免多实例重复执行。

---

## 11. 编码规范

- 类名大驼峰，方法/变量小驼峰，常量全大写下划线，包名全小写。
- Controller 方法不超过 15 行；Service 方法不超过 80 行；单文件不超过 600 行。
- 禁止魔法值，所有枚举、常量集中定义。
- 每个 Service 公开方法必须有 Javadoc（说明入参、出参、异常）。
- 日志：使用 `@Slf4j`，禁止 `System.out`；关键业务打 INFO，异常打 ERROR 带堆栈；日志中禁止打印密码、身份证、完整手机号。
- 事务：`@Transactional(rollbackFor = Exception.class)`，只加在 Service 层，禁止在 Controller 上使用。
- Git 提交规范：`feat:` `fix:` `docs:` `refactor:` `chore:` 前缀 + 中文描述。

---

## 12. 交付计划（按阶段输出，逐阶段确认后继续）

### 阶段一：数据库
输出 `docs/sql/init.sql`（建表 + 索引 + 注释）与 `docs/sql/init-data.sql`（初始化数据），确保可直接运行。

### 阶段二：接口文档
输出 `docs/api/README.md`、`docs/api/web-api.md`、`docs/api/miniprogram-api.md`，覆盖第 4 章全部模块，接口数量不少于 90 个。

### 阶段三：后端骨架
输出完整 Maven 工程：`pom.xml`、启动类、通用层（R/PageResult/异常/枚举/工具）、全部配置类、鉴权拦截器、文件上传、以及 **M1-M4 核心模块的完整 Controller/Service/Mapper/Entity/DTO/VO**（其余模块输出结构与接口签名）。

### 阶段四：Web 前端

先交付设计底稿，再交付页面：

1. `docs/design/page-plan.md`：逐页分屏计划（§8.8 格式）、每屏文案、素材编号、主行动。
2. `docs/design/photo-credits.md`：每张照片的来源与许可。
3. `src/styles/tokens.scss` + `src/styles/element-variables.scss`：§8.2–§8.4 的全部 token 与 Element 映射。
4. `package.json`、`vue.config.js`、`request.js`、路由、Vuex、通用组件。
5. 第 7.4 节全部 28 个页面的 `.vue` 文件（核心页面完整实现，列表类页面完整可用）。
6. `docs/design/design-check.md`：按 §8.15 逐项自检的结果。

验收以 §8.15 与第 13 章为准，不以「能跑起来」为准。

### 阶段五：小程序端
输出 `pages.json`、`manifest.json`、`request.js`（ajax 封装）、Vuex、首页/领养列表/宠物详情/申请/消息/我的 6 个核心页面完整代码。

### 阶段六：自检
输出 `docs/self-check.md`：对照第 13 章验收清单逐项打勾，并列出未完成项与原因。

---

## 13. 验收标准（Checklist）

**数据库**
- [ ] `init.sql` 在 MySQL 8.0 空库环境一次性执行成功，无报错、无 warning
- [ ] 22 张表齐全，字段、注释、索引、唯一约束完整
- [ ] 初始化数据可支撑前端演示（有宠物、有图片、有资讯、有账号）

**后端**
- [ ] 项目可 `mvn clean package` 成功，可直接 `java -jar` 启动
- [ ] 登录、宠物列表/详情、领养申请提交/审核、文件上传 5 条主链路可跑通
- [ ] Redis、MinIO、MongoDB 均已实际接入（非空实现）
- [ ] 统一响应、全局异常、参数校验、权限注解全部生效
- [ ] Knife4j 可访问，接口与文档一致

**Web 前端**
- [ ] `npm run serve` 可启动，无编译错误
- [ ] 主色为 `#FF7A45`，全局无默认 Element 蓝残留
- [ ] 28 个页面路由可达，守卫鉴权生效
- [ ] 卡片圆角 16px、按钮 8px、间距符合 4px 体系
- [ ] 门户首页/详情/故事/捐赠符合 §8.4 一屏一章与 §8.5 真实照片要求
- [ ] 全站只有一套按钮/卡片/圆角/链接样式（§8.6）
- [ ] 悬停、按下、焦点反馈可见；无轮播、无滚动触发动画（§8.7）
- [ ] 1440/1024/820/768/390/320 无横向滚动、无文字压图、无空视觉位（§8.15）
- [ ] 正文对比度 ≥4.5:1，键盘可完成申请全流程（§8.10）
- [ ] 列表/详情/表单/工作台布局符合 §8.8、§8.11

**小程序**
- [ ] 可在微信开发者工具中编译通过
- [ ] TabBar 4 项、视觉与 Web 端同源
- [ ] 登录、列表、详情、申请 4 条链路可跑通

**文档**
- [ ] 接口文档字段与后端实现完全一致
- [ ] UI 规范文档含完整色板、字号表、组件规范、页面分屏计划、图片署名与文案规范
- [ ] 无 `TODO`、无占位符、无"此处省略"

---

## 14. 输出格式要求

1. 所有交付文件以代码块形式输出，并在代码块前标注**完整相对路径**（如 `wandering-pet-server/src/main/java/com/wanderingpet/common/result/R.java`）。
2. SQL 与配置文件单独成块，不与 Java 代码混排。
3. 每阶段输出结束后，用表格给出「本阶段产物清单」：文件路径 / 说明 / 行数。
4. 严禁输出伪代码、骨架占位（`// 省略其他字段`）、注释代替实现。
5. 中文注释，英文标识符，注释解释「为什么」而非「是什么」。

---

*本文档为项目唯一开发依据，执行过程中如需变更技术栈或视觉规范，必须先提出并说明理由，不得擅自调整。*


````
