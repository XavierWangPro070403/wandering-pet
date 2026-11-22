# 流浪宠物领养系统接口文档 · 通用约定

> 适用范围：`wandering-pet-server`（Spring Boot 3）全部 HTTP 接口。
> Web 端接口清单见 [web-api.md](./web-api.md)，小程序端接口清单见 [miniprogram-api.md](./miniprogram-api.md)。

---

## 1. BaseURL 与环境

| 环境 | BaseURL | 说明 |
| --- | --- | --- |
| 本地开发 | `http://localhost:8080` | `application.yml` 默认端口 8080，profile=dev |
| 生产 | 以部署域名为准 | 通过环境变量覆盖数据源等配置 |

- 所有业务接口均以 `/api/v1` 为前缀。
- 在线调试：启动后访问 Swagger（knife4j 中文界面）：`http://localhost:8080/doc.html` 或 `http://localhost:8080/swagger-ui.html`；OpenAPI JSON：`/v3/api-docs`。
- 静态图片直链：`http://localhost:8080/imgs/**`（本地演示图片目录，已在白名单内）。

## 2. 鉴权方式（JWT Bearer Token）

### 2.1 Token 获取与携带

- 登录成功后从 `data.token` 取得 JWT，有效期 **120 分钟**（`wandering.jwt.expire-minutes`），服务端同时在 Redis 中保存登录态（`auth:token:{USER_TYPE}:{userId}`），双重校验：Token 被篡改或 Redis 登录态被删除（改密/禁用/强制下线）即失效。
- 除白名单接口外，所有请求必须携带请求头：

```
Authorization: Bearer <token>
```

- 未携带或失效时返回业务码 `401`（`data` 为空）：`{"code":401,"message":"请先登录",...}`。

### 2.2 四种登录方式的区别

| 登录方式 | 接口 | 适用端 | 账号类型 | 验证码 |
| --- | --- | --- | --- | --- |
| 后台登录 | `POST /api/v1/auth/admin-login` | Web 管理后台 | ADMIN（管理员）/ SHELTER（救助站机构） | 需要（图形验证码） |
| 门户登录 | `POST /api/v1/auth/portal-login` | Web 用户端 | USER（C 端用户） | 需要（图形验证码） |
| 微信登录 | `POST /api/v1/auth/wx-login` | 微信小程序 | USER（C 端用户） | 不需要 |
| 手机号验证码登录 | `POST /api/v1/auth/sms-login` | Web 用户端 + 微信小程序 | USER（C 端用户，未注册自动创建） | 需要（短信验证码） |

- **手机号短信验证码登录**（双端同一套接口）：
  1. `POST /api/v1/auth/sms/send`，body `{"phone":"13800000001","scene":"login"}` → 下发 6 位验证码（Redis 存 5 分钟；同一手机号同一场景 60 秒内限发 1 条、单日上限 10 条、按 IP 每分钟 5 次）；
  2. `POST /api/v1/auth/sms-login`，body `{"phone":"13800000001","code":"123456"}` → 返回与门户登录相同的 `LoginVO`。手机号未注册时自动创建 USER，昵称「用户 + 后四位」。
- **绑定/换绑手机号**：已登录用户先以 `scene=bind` 取码，再调 `POST /api/v1/auth/sms-bind`，body `{"phone":"...","code":"..."}`；该手机号已被其他账号占用返回 `1010`。
- **短信开发模式**：`wandering.sms` 的 `access-key-id` / `access-key-secret` / `sign-name` / `template-code` 任一为空时不真实下发，响应 `data.devMode=true` 且 `data.code` 回传验证码（Web 弹窗与小程序登录页会自动填入），便于本地联调；配好阿里云凭证、签名与模板后自动切换为真实发送。

- **注册**：Web 用户端通过 `POST /api/v1/auth/register` 创建 USER 账号；SHELTER 账号由 C 端用户提交入驻申请、管理员审核通过后自动创建；ADMIN 账号由管理员在“系统设置-后台账号”中创建。
- **小程序 dev 模式**：`application.yml` 中 `wandering.wx.appid` 为空时进入开发模式，`wx-login` 对任意 `code` 都能登录，openid 解析为 `dev_ + code`（例如 code=`test123` 的 openid 为 `dev_test123`），同一 code 重复登录返回同一账号；生产环境配置 appid/secret 后走微信 `code2session`。
- **演示账号**（密码统一为 `123456`，见 `docs/sql/init-data.sql`）：

| 账号 | 密码 | 角色 | 说明 |
| --- | --- | --- | --- |
| `admin` | 123456 | ADMIN | 平台管理员，拥有全部权限点 |
| `shelter1` | 123456 | SHELTER | “暖阳流浪动物救助中心”机构账号（shelterId=1） |
| `shelter2` | 123456 | SHELTER | “城市伴侣动物救助联盟”机构账号（shelterId=2） |
| `demo` | 123456 | USER | Web 用户端演示账号（昵称“江小领”，已实名）；小程序端使用任意 code（如 `test123`）经 wx-login 也可登录对应 dev 账号 |

### 2.3 接口级权限控制（三层）

1. **白名单（免登录）**：`application.yml` 的 `wandering.auth.ignore-urls`，命中即放行（仍会尝试解析 Token，支持“游客浏览 + 登录后附加收藏状态”的可选登录场景）：

   ```
   /api/v1/auth/**
   /api/v1/pet-categories
   /api/v1/pet-breeds
   /api/v1/dicts
   /api/v1/pets/**
   /api/v1/pet-health-records/**
   /api/v1/articles/**
   /api/v1/comments
   /api/v1/shelters/**
   /api/v1/donation-projects/**
   /api/v1/volunteer-stations/**
   /api/v1/statistics/portal-summary
   /api/v1/file/preview/**
   /v3/api-docs/**  /doc.html  /swagger-ui/**  /webjars/**
   /favicon.ico  /imgs/**  /error
   ```

2. **路径前缀角色控制**：`/api/v1/admin/**` 仅 ADMIN；`/api/v1/shelter/**` 仅 SHELTER。越权返回 `403`。
3. **权限点控制**：标注 `@RequirePermission("权限编码")` 的接口校验登录时加载的权限集合（存于 `sys_permission`，按角色缓存于 Redis），无权限返回 `403`。

   | 角色 | 权限点 |
   | --- | --- |
   | ADMIN | 全部权限点（含 `user:manage`、`sys-user:manage`、`shelter:audit`、`pet:manage-all`、`adoption:manage-all`、`article:manage`、`comment:audit`、`rescue:manage`、`dict:manage`、`role:manage`、`oplog:view`、`statistics:view`、`notification:push`、`donation:record:view`、`file:upload` 等） |
   | SHELTER | `shelter:pet`、`pet:create`、`pet:update`、`pet:delete`、`pet:status`、`health-record:manage`、`shelter:adoption`、`adoption:review`、`adoption:sign`、`adoption:complete`、`followup:manage`、`shelter:donation`、`donation:manage`、`donation:record:view`、`shelter:volunteer`、`volunteer:audit`、`shelter:update`、`chat:use`、`statistics:view`、`file:upload` 及机构端菜单 |
   | USER | 无权限点（C 端接口以“登录用户”为门槛） |

### 2.4 限流

- 登录/注册类接口 **5 次/分钟/IP**（Redis 滑动窗口计数）：`POST /api/v1/auth/admin-login`、`POST /api/v1/auth/portal-login`、`POST /api/v1/auth/register`、`POST /api/v1/auth/wx-login`。超限返回 `429`。
- 图形验证码接口另有 `@RateLimit` 限制 **10 次/分钟**。

## 3. 统一响应结构

所有接口（含错误）HTTP 状态码恒为 200，业务状态由 `code` 表达：

```json
{
  "code": 200,
  "message": "success",
  "data": { },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 200 成功；400/401/403/404/429/500 及业务码（见第 5 节） |
| message | string | 提示信息；成功为 `success`，失败为具体中文提示 |
| data | object/array/null | 业务数据；无数据时整体省略（`non_null` 序列化） |
| traceId | string | 8 位链路追踪 ID，与响应头 `X-Trace-Id` 一致，排查问题请提供 |
| timestamp | long | 服务器毫秒时间戳 |

序列化约定：

- **ID 一律序列化为字符串**（`Long → String`，JacksonConfig 全局配置），示例中 `"id":"12"`。
- **时间格式统一** `yyyy-MM-dd HH:mm:ss`（时区 Asia/Shanghai）；少数字段（签约日期 `signDate`）为 `yyyy-MM-dd`。
- **null 字段不输出**（`default-property-inclusion: non_null`），示例中不会出现 `"xxx": null`。
- 枚举/状态以大写字符串返回（如 `AVAILABLE`、`SUBMITTED`），中文含义见数据字典 `GET /api/v1/dicts`。

## 4. 分页约定

分页统一通过 `PageResult<T>` 包装于 `data`：

```json
{
  "list": [ ],
  "total": 57,
  "pageNum": 1,
  "pageSize": 10,
  "pages": 6
}
```

- 请求参数：`pageNum`（默认 1）、`pageSize`（默认 10，部分接口上限 100），GET 查询参数或 DTO 字段。
- 例外：在线咨询“会话消息分页”直接返回 MyBatis-Plus `Page` 对象，分页字段为 `records / total / size / current / pages`（见 web-api 第 13 章）。

## 5. 错误码完整表

来源：`common/result/ResultCode.java`。分段规则：1001-1099 账号域、2001-2099 宠物域、3001-3099 领养域、4001-4099 文件域、5001-5099 系统域。

| code | message | 说明 |
| --- | --- | --- |
| 200 | success | 成功 |
| 400 | 请求参数错误 | 参数校验失败（@Valid），message 为具体字段提示 |
| 401 | 请先登录 | 未登录 / Token 失效 / 被强制下线 |
| 403 | 没有操作权限 | 角色或权限点不足；机构端越权访问他机构数据同样返回 |
| 404 | 资源不存在 | 请求的资源 ID 无效 |
| 429 | 请求过于频繁，请稍后再试 | 触发限流 |
| 500 | 系统繁忙，请稍后再试 | 未捕获异常 |
| 1001 | 用户名或密码错误 | 登录失败 |
| 1002 | 账号已被禁用 | status=0 |
| 1003 | 验证码错误或已过期 | 图形验证码一次性，校验后即删除 |
| 1004 | 用户名已被注册 | 注册账号重复 |
| 1005 | 原密码不正确 | 修改密码场景 |
| 1006 | 用户不存在 | 指定用户/账号不存在 |
| 1007 | 短信验证码错误或已过期 | 验证码错、超时或失败次数超过 5 次 |
| 1008 | 验证码已发送，请稍后再试 | 同一手机号 60 秒内重复获取 |
| 1009 | 该手机号今日验证码发送次数已达上限 | 单日 10 条上限 |
| 1010 | 该手机号已被其他账号绑定 | 绑定/换绑手机号时被占用 |
| 2001 | 宠物不存在 | 宠物 ID 无效 |
| 2002 | 该宠物当前不可申请领养 | 宠物状态非 AVAILABLE（PENDING/MEDICAL/OFFLINE 等） |
| 2003 | 该宠物已被领养 | 宠物状态 ADOPTED |
| 2004 | 物种分类不存在 | categoryId 无效 |
| 3001 | 您已有该宠物的进行中申请，请先等待处理或撤回 | 重复提交领养申请 |
| 3002 | 当前状态不允许该操作 | 领养申请状态机不匹配 |
| 3003 | 领养申请不存在 | 申请 ID 无效或非本人/本机构数据 |
| 3004 | 未满足领养条件，请完善申请信息 | 申请信息不完整 |
| 3005 | 回访任务不存在 | 回访 ID 无效或非本人数据 |
| 4001 | 文件类型不支持 | 扩展名/魔数不符或 bizType 非法 |
| 4002 | 文件超出大小限制 | 图片 5MB / PDF 10MB / 视频 50MB |
| 4003 | 文件上传失败，请稍后重试 | MinIO 异常 |
| 4004 | 上传文件不能为空 | 空文件 |
| 5001 | 服务繁忙，请稍后再试 | 服务过载 |
| 5002 | 第三方服务调用失败 | 微信 code2session 等第三方异常 |

## 6. 文件上传约定

- 统一入口：`POST /api/v1/file/upload`（multipart/form-data），需权限点 `file:upload`（ADMIN/SHELTER 可用；小程序端如需上传另行申请，本项目中 USER 角色不持有该权限点）。
- 参数：`file`（二进制）+ `bizType`（默认 `pet`）。
- 业务类型与限制：

| bizType | 允许格式 | 大小限制 | 存储桶 | 返回 url |
| --- | --- | --- | --- | --- |
| pet / avatar / article / donation / report / dynamic | jpg / jpeg / png / webp | ≤ 5MB | 公共桶 wandering-pet | 公共直链 |
| agreement | pdf | ≤ 10MB | 私有桶 wandering-pet-private | 30 分钟预签名 URL |
| 其他（未列出） | mp4 | ≤ 50MB | 公共桶 | 公共直链 |

- 服务端校验文件头魔数（不信任扩展名），扩展名与内容不符返回 4001。
- 响应 `data`：`{ "objectName": "pet/2026/xxx.jpg", "url": "http://localhost:8080/imgs/pet/xxx.jpg" }`；数据库仅存 `objectName`，展示用 `url`（私有桶 URL 有时效）。
- 请求体总大小上限 60MB（`spring.servlet.multipart.max-request-size`）。

## 7. 其他约定

- **CORS**：仅允许 `http://localhost:8081`、`http://127.0.0.1:8081`（可配置），预检请求直接放行。
- **操作日志**：关键写操作接口（标注 `@OpLog`）异步写入 MongoDB `operation_log` 集合，可在管理端“操作日志”查询。
- **数据字典**：三端下拉选项统一取 `GET /api/v1/dicts`（Redis 缓存 24h），常用字典类型：`pet_gender`、`pet_size`、`pet_status`、`pet_health`、`health_record_type`、`application_status`、`housing_type`、`article_category`、`report_urgency`、`report_status`、`follow_up_status`、`shelter_audit_status`、`volunteer_status`。
- **敏感信息**：手机号、姓名在响应中已脱敏（如 `138****0001`、`江**`）；身份证号不返回。
- **收藏/评论对象类型** `targetType` 取值：`PET`（宠物）、`ARTICLE`（文章）。
- **领养申请状态机**：`SUBMITTED → REVIEWING → INTERVIEW → HOME_VISIT → APPROVED → SIGNED → ADOPTED`；`REVIEWING/INTERVIEW/HOME_VISIT` 可驳回（REJECTED）；`SUBMITTED/REVIEWING` 可撤回（CANCELED）；提交后 7 天未受理自动过期（EXPIRED）；`ADOPTED` 可退养（RETURNED）。
- **回访计划**：领养完成后自动生成第 7/30/90/180 天共 4 轮回访任务；超计划日期 3 天未填写自动标记逾期（OVERDUE）。

## 8. 接口清单索引

| 文档 | 章节 | 端 |
| --- | --- | --- |
| web-api.md | 第 1 章 认证 | 三端共用 |
| web-api.md | 第 2 章 用户中心（C 端） | 用户 |
| web-api.md | 第 3 章 救助站/机构 | 游客+用户+机构 |
| web-api.md | 第 4 章 宠物（门户） | 游客可浏览 |
| web-api.md | 第 5 章 收藏 | 用户 |
| web-api.md | 第 6 章 领养申请（申请人） | 用户 |
| web-api.md | 第 7 章 领养审核（机构端） | 救助站 |
| web-api.md | 第 8 章 回访 | 用户 + 救助站 |
| web-api.md | 第 9 章 资讯与评论（门户） | 游客可浏览 |
| web-api.md | 第 10 章 资讯与评论（管理端） | 管理员 |
| web-api.md | 第 11 章 消息中心 | 三端 |
| web-api.md | 第 12 章 数据统计 | 游客/机构/管理员 |
| web-api.md | 第 13 章 在线咨询 | 用户 + 救助站 |
| web-api.md | 第 14 章 募捐捐赠（门户） | 游客可浏览 |
| web-api.md | 第 15 章 募捐管理（机构/管理端） | 救助站/管理员 |
| web-api.md | 第 16 章 志愿者 | 用户 + 救助站 |
| web-api.md | 第 17 章 流浪动物线索 | 用户 + 管理员 + 救助站 |
| web-api.md | 第 18 章 宠物管理（机构端） | 救助站 |
| web-api.md | 第 19 章 管理端·用户/机构/宠物/领养全量 | 管理员 |
| web-api.md | 第 20 章 系统设置与数据字典 | 游客读字典 + 管理员 |
| web-api.md | 第 21 章 文件上传 | 管理员/救助站 |
| miniprogram-api.md | 小程序专用子集 | 小程序 |
