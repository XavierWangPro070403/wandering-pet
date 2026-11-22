# 流浪宠物领养系统 · Web 端接口文档

> 适用端：Web 用户端（门户）、救助站机构端、平台管理端。BaseURL、鉴权、统一响应、错误码等通用约定见 [README.md](./README.md)。
> 接口编号连续（1.1 ~ 21.1），共 **136** 个接口，覆盖全部 Controller 端点。

---

## 1. 认证

### 1.1 图形验证码

- **接口地址**：`GET /api/v1/auth/captcha`
- **权限要求**：无（游客）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uuid": "3f8a2c1e9b6d4a70",
    "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtLK"
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`imageBase64` 为 PNG Base64（含 `data:image/png;base64,` 前缀），可直接置于 `<img src>`；验证码 5 分钟有效、一次性（登录校验后即删除）。本接口限流 10 次/分钟/IP。
- **错误码**：429 请求过于频繁

### 1.2 后台登录（管理员/救助站）

- **接口地址**：`POST /api/v1/auth/admin-login`
- **权限要求**：无（游客）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| username | string | 是 | - | 登录账号（admin / shelter1 / shelter2） |
| password | string | 是 | - | 登录密码（演示环境统一 123456） |
| captchaUuid | string | 是 | - | 1.1 返回的验证码 uuid |
| captchaCode | string | 是 | - | 用户输入的验证码（不区分大小写） |

- **响应示例**（管理员账号）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJUeXBlIjoiQURNSU4ifQ.9Xb0kQ",
    "id": "1",
    "userType": "ADMIN",
    "nickname": "平台管理员",
    "avatar": "http://localhost:8080/imgs/avatar/a01.jpg",
    "phone": "138****0000",
    "permissions": ["admin:dashboard", "admin:shelter", "admin:user", "pet:manage-all", "adoption:manage-all", "article:manage", "comment:audit", "rescue:manage", "shelter:audit", "user:manage", "sys-user:manage", "dict:manage", "role:manage", "oplog:view", "statistics:view", "notification:push", "file:upload", "donation:record:view"]
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **响应示例**（救助站账号 shelter1）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjIsInVzZXJUeXBlIjoiU0hFTFRFUiJ9.Lm3kQp",
    "id": "2",
    "userType": "SHELTER",
    "nickname": "陈远山",
    "avatar": "http://localhost:8080/imgs/shelter/s01.jpg",
    "phone": "138****0003",
    "shelterId": "1",
    "shelterName": "暖阳流浪动物救助中心",
    "permissions": ["shelter:workbench", "shelter:pet", "pet:create", "pet:update", "pet:delete", "pet:status", "health-record:manage", "shelter:adoption", "adoption:review", "adoption:sign", "adoption:complete", "followup:manage", "shelter:donation", "donation:manage", "donation:record:view", "shelter:volunteer", "volunteer:audit", "shelter:update", "chat:use", "statistics:view", "file:upload"]
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：限流 5 次/分钟/IP；ADMIN 登录无 `shelterId/shelterName/realNameStatus`（null 字段不输出）。
- **错误码**：1001 用户名或密码错误；1002 账号已被禁用；1003 验证码错误或已过期；429 请求过于频繁

### 1.3 Web 用户端登录

- **接口地址**：`POST /api/v1/auth/portal-login`
- **权限要求**：无（游客）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| account | string | 是 | - | 账号（用户名或手机号） |
| password | string | 是 | - | 密码 |
| captchaUuid | string | 是 | - | 验证码 uuid |
| captchaCode | string | 是 | - | 验证码 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJUeXBlIjoiVVNFUiJ9.Kq2mVx",
    "id": "1",
    "userType": "USER",
    "nickname": "江小领",
    "avatar": "http://localhost:8080/imgs/avatar/a02.jpg",
    "phone": "138****0001",
    "city": "杭州市",
    "realNameStatus": 1,
    "permissions": []
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：1001 用户名或密码错误；1002 账号已被禁用；1003 验证码错误或已过期；429 请求过于频繁

### 1.4 Web 用户端注册

- **接口地址**：`POST /api/v1/auth/register`
- **权限要求**：无（游客）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| username | string | 是 | - | 账号，4-32 位，仅字母/数字/下划线 |
| password | string | 是 | - | 密码，6-32 位 |
| nickname | string | 是 | - | 昵称，≤32 位 |
| captchaUuid | string | 是 | - | 验证码 uuid |
| captchaCode | string | 是 | - | 验证码 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjUsInVzZXJUeXBlIjoiVVNFUiJ9.Wn8rTs",
    "id": "5",
    "userType": "USER",
    "nickname": "喵星人爱好者",
    "avatar": "",
    "realNameStatus": 0,
    "permissions": []
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：1004 用户名已被注册；1003 验证码错误或已过期；400 参数错误；429 请求过于频繁

### 1.5 小程序微信登录

- **接口地址**：`POST /api/v1/auth/wx-login`
- **权限要求**：无（游客）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| code | string | 是 | - | wx.login 返回的临时凭证 code |
| nickname | string | 否 | - | 微信昵称，首次登录时传入 |
| avatar | string | 否 | - | 微信头像 URL，首次登录时传入 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJUeXBlIjoiVVNFUiJ9.Kq2mVx",
    "id": "1",
    "userType": "USER",
    "nickname": "江小领",
    "avatar": "http://localhost:8080/imgs/avatar/a02.jpg",
    "phone": "138****0001",
    "city": "杭州市",
    "realNameStatus": 1,
    "permissions": []
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：dev 模式（未配置 appid）下任意 code 可登录，openid = `dev_ + code`，同一 code 对应同一账号；生产模式调用微信 code2session。限流 5 次/分钟/IP。
- **错误码**：5002 第三方服务调用失败（生产模式微信侧失败）；429 请求过于频繁

### 1.6 小程序绑定手机号

- **接口地址**：`POST /api/v1/auth/bind-phone`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| phone | string | 是 | - | 手机号，格式 `^1[3-9]\d{9}$` |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：400 参数错误；401 请先登录

### 1.7 找回密码

- **接口地址**：`POST /api/v1/auth/reset-password`
- **权限要求**：无（游客）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| account | string | 是 | - | 账号（用户名或手机号） |
| newPassword | string | 是 | - | 新密码，6-32 位 |
| captchaUuid | string | 是 | - | 验证码 uuid |
| captchaCode | string | 是 | - | 验证码 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：1003 验证码错误或已过期；1006 用户不存在；400 参数错误

### 1.8 退出登录

- **接口地址**：`POST /api/v1/auth/logout`
- **权限要求**：登录用户（三端均可）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：删除 Redis 登录态，Token 立即失效。
- **错误码**：401 请先登录

### 1.9 当前登录人信息

- **接口地址**：`GET /api/v1/auth/me`
- **权限要求**：登录用户（三端均可）
- **请求参数**：无
- **响应示例**（同 1.2/1.3 的 `data` 结构，按当前角色返回对应字段，此处以 USER 为例）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJUeXBlIjoiVVNFUiJ9.Kq2mVx",
    "id": "1",
    "userType": "USER",
    "nickname": "江小领",
    "avatar": "http://localhost:8080/imgs/avatar/a02.jpg",
    "phone": "138****0001",
    "city": "杭州市",
    "realNameStatus": 1,
    "permissions": []
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；1006 用户不存在

## 2. 用户中心（C 端）

### 2.1 我的资料

- **接口地址**：`GET /api/v1/users/me`
- **权限要求**：登录用户（USER）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "username": "demo",
    "nickname": "江小领",
    "avatar": "http://localhost:8080/imgs/avatar/a02.jpg",
    "phone": "138****0001",
    "gender": 1,
    "city": "杭州市",
    "realName": "江**",
    "realNameStatus": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 2.2 更新我的资料

- **接口地址**：`PUT /api/v1/users/me`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| nickname | string | 否 | - | 昵称，≤32 位 |
| avatar | string | 否 | - | 头像 URL（先经 21.1 上传 bizType=avatar） |
| gender | int | 否 | - | 0-未知 1-男 2-女 |
| city | string | 否 | - | 城市，≤32 位 |
| phone | string | 否 | - | 手机号，格式 `^1[3-9]\d{9}$` |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：400 参数错误；401 请先登录

### 2.3 实名认证

- **接口地址**：`POST /api/v1/users/me/real-name`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| realName | string | 是 | - | 真实姓名，≤32 位 |
| idCardNo | string | 是 | - | 18 位身份证号（`^\d{17}[\dXx]$`），加密存储、永不返回 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：提交成功后 `realNameStatus` 置 1；领养申请要求已实名。
- **错误码**：400 参数错误；401 请先登录

### 2.4 我的数字摘要

- **接口地址**：`GET /api/v1/users/me/summary`
- **权限要求**：登录用户（USER）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "applicationCount": 3,
    "activeApplicationCount": 1,
    "favoriteCount": 12,
    "reportCount": 2,
    "unreadCount": 5
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 2.5 我的地址列表

- **接口地址**：`GET /api/v1/users/me/addresses`
- **权限要求**：登录用户（USER）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "userId": "1",
      "receiver": "江小领",
      "phone": "13800000001",
      "province": "浙江省",
      "city": "杭州市",
      "district": "西湖区",
      "detail": "西湖区文三路 138 号",
      "isDefault": 1
    },
    {
      "id": "2",
      "userId": "1",
      "receiver": "江大领",
      "phone": "13900000002",
      "province": "浙江省",
      "city": "杭州市",
      "district": "滨江区",
      "detail": "滨江区江南大道 88 号",
      "isDefault": 0
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 2.6 新增地址

- **接口地址**：`POST /api/v1/users/me/addresses`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| receiver | string | 是 | - | 收件人姓名，≤32 位 |
| phone | string | 是 | - | 联系电话，`^1[3-9]\d{9}$` |
| province | string | 是 | - | 省份 |
| city | string | 是 | - | 城市 |
| district | string | 是 | - | 区县 |
| detail | string | 是 | - | 详细地址（区+街道），≤255 位 |
| isDefault | int | 否 | 0 | 1-设为默认地址 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "3",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`data` 为新地址 ID（字符串）。设为默认时自动取消原默认。
- **错误码**：400 参数错误；401 请先登录

### 2.7 更新地址

- **接口地址**：`PUT /api/v1/users/me/addresses/{id}`
- **权限要求**：登录用户（USER）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 地址 ID |
| Body | 同 2.6 | object | 是 | 字段含义一致（receiver/phone/province/city/district/detail/isDefault） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在（非本人地址）；400 参数错误；401 请先登录

### 2.8 删除地址

- **接口地址**：`DELETE /api/v1/users/me/addresses/{id}`
- **权限要求**：登录用户（USER）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 地址 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401 请先登录

## 3. 救助站（机构）

### 3.1 已入驻救助站列表

- **接口地址**：`GET /api/v1/shelters`
- **权限要求**：无（游客）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| city | string | 否 | - | 按城市筛选 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "name": "暖阳流浪动物救助中心",
      "logo": "http://localhost:8080/imgs/shelter/s01.jpg",
      "city": "杭州市",
      "district": "西湖区",
      "address": "西湖区转塘街道龙坞工业园 3 号",
      "intro": "成立于 2018 年，专注流浪犬猫的救助、医疗与领养匹配，现有在册志愿者 120 余人。",
      "contactName": "陈远山",
      "contactPhone": "13800000003",
      "petCount": 26
    },
    {
      "id": "2",
      "name": "城市伴侣动物救助联盟",
      "logo": "http://localhost:8080/imgs/shelter/s02.jpg",
      "city": "杭州市",
      "district": "滨江区",
      "address": "滨江区江陵路 88 号",
      "intro": "由多家民间救助团体联合发起，倡导领养代替购买。",
      "contactName": "罗晓萌",
      "contactPhone": "13800000004",
      "petCount": 18
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 3.2 救助站详情

- **接口地址**：`GET /api/v1/shelters/{id}`
- **权限要求**：无（游客）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 救助站 ID |

- **响应示例**：结构同 3.1 列表项（`ShelterVO`）。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "name": "暖阳流浪动物救助中心",
    "logo": "http://localhost:8080/imgs/shelter/s01.jpg",
    "city": "杭州市",
    "district": "西湖区",
    "address": "西湖区转塘街道龙坞工业园 3 号",
    "intro": "成立于 2018 年，专注流浪犬猫的救助、医疗与领养匹配。",
    "contactName": "陈远山",
    "contactPhone": "13800000003",
    "petCount": 26
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在

### 3.3 申请入驻（C 端用户）

- **接口地址**：`POST /api/v1/shelters/apply`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| name | string | 是 | - | 机构名称，≤64 位 |
| licenseNo | string | 是 | - | 统一社会信用代码/登记证号，≤64 位 |
| contactName | string | 是 | - | 联系人姓名，≤32 位 |
| contactPhone | string | 是 | - | 联系电话 |
| province | string | 是 | - | 省份 |
| city | string | 是 | - | 城市 |
| district | string | 否 | - | 区县 |
| address | string | 是 | - | 机构地址（区+街道） |
| intro | string | 否 | - | 机构简介，≤1000 字 |
| logo | string | 否 | - | LOGO URL（先上传） |
| accountUsername | string | 否 | - | 申请账号用户名，默认联系人手机号；审核通过后生成后台账号，初始密码 123456 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "3",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`data` 为救助站 ID；初始审核状态 0-待审核，待管理员审核（19.6）。
- **错误码**：400 参数错误；401 请先登录；403 没有操作权限（已有待审核/已通过的机构）

### 3.4 我的机构信息（SHELTER）

- **接口地址**：`GET /api/v1/shelters/me`
- **权限要求**：救助站（登录 SHELTER 账号）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "name": "暖阳流浪动物救助中心",
    "logo": "http://localhost:8080/imgs/shelter/s01.jpg",
    "city": "杭州市",
    "district": "西湖区",
    "address": "西湖区转塘街道龙坞工业园 3 号",
    "intro": "成立于 2018 年，专注流浪犬猫的救助、医疗与领养匹配。",
    "contactName": "陈远山",
    "contactPhone": "13800000003",
    "petCount": 26
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限（非 SHELTER）

### 3.5 更新我的机构信息（SHELTER）

- **接口地址**：`PUT /api/v1/shelters/me`
- **权限要求**：救助站（登录 SHELTER 账号）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| logo | string | 否 | - | LOGO URL |
| intro | string | 否 | - | 简介，≤1000 字 |
| contactName | string | 否 | - | 联系人姓名 |
| contactPhone | string | 否 | - | 联系电话 |
| address | string | 否 | - | 机构地址（区+街道） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：400 参数错误；401 请先登录；403 没有操作权限

## 4. 宠物（门户）

### 4.1 宠物分页列表

- **接口地址**：`GET /api/v1/pets`
- **权限要求**：无（游客；登录后返回 `favorited` 收藏状态）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| categoryId | long | 否 | - | 物种分类 ID |
| breedId | long | 否 | - | 品种 ID |
| gender | string | 否 | - | MALE / FEMALE / UNKNOWN |
| size | string | 否 | - | SMALL / MEDIUM / LARGE |
| ageMin | int | 否 | - | 最小月龄 |
| ageMax | int | 否 | - | 最大月龄 |
| city | string | 否 | - | 城市（来自救助站） |
| shelterId | long | 否 | - | 救助站 ID |
| status | string | 否 | AVAILABLE | 门户默认仅返回 AVAILABLE；可显式传其他状态 |
| keyword | string | 否 | - | 关键词（名称/性格描述） |
| orderBy | string | 否 | publish_time | publish_time / view_count / favorite_count / age_month |
| orderDirection | string | 否 | desc | asc / desc |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "petNo": "pet2026030100001",
        "name": "煤球",
        "categoryId": "1",
        "categoryName": "猫",
        "breedId": "3",
        "breedName": "中华田园猫",
        "gender": "MALE",
        "ageMonth": 14,
        "weight": 4.20,
        "size": "MEDIUM",
        "color": "黑色",
        "sterilized": 1,
        "vaccinated": 1,
        "dewormed": 1,
        "healthStatus": "HEALTHY",
        "characterDesc": "性格粘人，会主动蹭手求抚摸，能与其他猫和平相处。",
        "coverImage": "http://localhost:8080/imgs/pet/p01.jpg",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "city": "杭州市",
        "status": "AVAILABLE",
        "viewCount": 356,
        "favoriteCount": 48,
        "publishTime": "2026-05-12 10:30:00",
        "favorited": false
      },
      {
        "id": "2",
        "petNo": "pet2026030100002",
        "name": "豆包",
        "categoryId": "2",
        "categoryName": "狗",
        "breedId": "8",
        "breedName": "柯基",
        "gender": "FEMALE",
        "ageMonth": 10,
        "weight": 9.50,
        "size": "MEDIUM",
        "color": "黄白",
        "sterilized": 1,
        "vaccinated": 1,
        "dewormed": 1,
        "healthStatus": "HEALTHY",
        "characterDesc": "活泼好动，亲人不护食，已学会定点排便。",
        "coverImage": "http://localhost:8080/imgs/pet/p02.jpg",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "city": "杭州市",
        "status": "AVAILABLE",
        "viewCount": 512,
        "favoriteCount": 73,
        "publishTime": "2026-05-10 15:20:00",
        "favorited": false
      }
    ],
    "total": 57,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 6
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码（参数非法走 400）

### 4.2 宠物详情

- **接口地址**：`GET /api/v1/pets/{id}`
- **权限要求**：无（游客；登录后返回 `favorited`）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 宠物 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "petNo": "pet2026030100001",
    "name": "煤球",
    "categoryId": "1",
    "categoryName": "猫",
    "breedId": "3",
    "breedName": "中华田园猫",
    "gender": "MALE",
    "ageMonth": 14,
    "weight": 4.20,
    "size": "MEDIUM",
    "color": "黑色",
    "sterilized": 1,
    "vaccinated": 1,
    "dewormed": 1,
    "healthStatus": "HEALTHY",
    "characterDesc": "性格粘人，会主动蹭手求抚摸，能与其他猫和平相处。",
    "adoptionRequirements": "有独立居室、全员同意、签订领养协议并接受回访。",
    "story": "煤球原是小区车库的流浪猫，2026 年 3 月被志愿者救助，已完成绝育与免疫。",
    "coverImage": "http://localhost:8080/imgs/pet/p01.jpg",
    "images": [
      { "id": "1", "url": "http://localhost:8080/imgs/pet/p01.jpg", "objectName": "pet/2026/05/p01.jpg", "sort": 1, "type": "COVER" },
      { "id": "2", "url": "http://localhost:8080/imgs/pet/p01-2.jpg", "objectName": "pet/2026/05/p01-2.jpg", "sort": 2, "type": "GALLERY" }
    ],
    "healthRecords": [
      {
        "id": "1",
        "petId": "1",
        "recordType": "STERILIZE",
        "recordDate": "2026-03-20",
        "title": "绝育手术",
        "content": "手术顺利，术后恢复良好，已拆线。",
        "vetName": "王医生",
        "attachmentUrl": ""
      }
    ],
    "shelter": {
      "id": "1",
      "name": "暖阳流浪动物救助中心",
      "logo": "http://localhost:8080/imgs/shelter/s01.jpg",
      "city": "杭州市",
      "district": "西湖区",
      "address": "西湖区转塘街道龙坞工业园 3 号",
      "contactPhone": "13800000003"
    },
    "status": "AVAILABLE",
    "viewCount": 357,
    "favoriteCount": 48,
    "publishTime": "2026-05-12 10:30:00",
    "favorited": false
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在

### 4.3 相似推荐

- **接口地址**：`GET /api/v1/pets/{id}/similar`
- **权限要求**：无（游客）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 宠物 ID |
| Query | limit | int | 否 | 4 | 返回数量 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "7",
      "petNo": "pet2026030100007",
      "name": "奥利奥",
      "categoryId": "1",
      "categoryName": "猫",
      "breedId": "3",
      "breedName": "中华田园猫",
      "gender": "FEMALE",
      "ageMonth": 8,
      "weight": 3.10,
      "size": "SMALL",
      "color": "黑白",
      "sterilized": 0,
      "vaccinated": 1,
      "dewormed": 1,
      "healthStatus": "HEALTHY",
      "characterDesc": "好奇心强，爱玩逗猫棒。",
      "coverImage": "http://localhost:8080/imgs/pet/p07.jpg",
      "shelterId": "1",
      "shelterName": "暖阳流浪动物救助中心",
      "city": "杭州市",
      "status": "AVAILABLE",
      "viewCount": 98,
      "favoriteCount": 11,
      "publishTime": "2026-05-08 09:00:00",
      "favorited": false
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在

### 4.4 记录一次浏览

- **接口地址**：`POST /api/v1/pets/{id}/view`
- **权限要求**：无（游客）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 宠物 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：进入详情页时调用一次，累加 `viewCount`。
- **错误码**：2001 宠物不存在

### 4.5 宠物动态（MongoDB）

- **接口地址**：`GET /api/v1/pets/{id}/dynamics`
- **权限要求**：无（游客）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 宠物 ID |
| Query | limit | int | 否 | - | 返回数量上限 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "6832a1f4c9e77a2b3d4e5f01",
      "petId": "1",
      "content": "煤球今天完成了第 3 次驱虫，精神状态很好，已经开始粘人了。",
      "images": ["http://localhost:8080/imgs/pet/p01-d1.jpg"],
      "likeCount": 23,
      "createTime": "2026-05-20 16:40:00"
    },
    {
      "id": "6832a1f4c9e77a2b3d4e5f02",
      "petId": "1",
      "content": "周末开放日，煤球和志愿者们拍了合照。",
      "images": ["http://localhost:8080/imgs/pet/p01-d2.jpg", "http://localhost:8080/imgs/pet/p01-d3.jpg"],
      "likeCount": 41,
      "createTime": "2026-05-18 11:05:00"
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 4.6 物种分类列表

- **接口地址**：`GET /api/v1/pet-categories`
- **权限要求**：无（游客）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": "1", "name": "猫", "icon": "http://localhost:8080/imgs/category/cat.png", "sort": 1 },
    { "id": "2", "name": "狗", "icon": "http://localhost:8080/imgs/category/dog.png", "sort": 2 },
    { "id": "3", "name": "兔", "icon": "http://localhost:8080/imgs/category/rabbit.png", "sort": 3 },
    { "id": "4", "name": "其他", "icon": "http://localhost:8080/imgs/category/other.png", "sort": 4 }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 4.7 品种列表

- **接口地址**：`GET /api/v1/pet-breeds`
- **权限要求**：无（游客）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| categoryId | long | 否 | - | 物种 ID，可空（空则返回全部品种） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": "3", "categoryId": "1", "name": "中华田园猫" },
    { "id": "4", "categoryId": "1", "name": "英国短毛猫" },
    { "id": "5", "categoryId": "1", "name": "美国短毛猫" },
    { "id": "8", "categoryId": "2", "name": "柯基" }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

## 5. 收藏

### 5.1 收藏

- **接口地址**：`POST /api/v1/favorites`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| targetType | string | 是 | - | 收藏对象类型：PET / ARTICLE |
| targetId | long | 是 | - | 收藏对象 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：重复收藏幂等处理；收藏宠物会同步累加宠物 `favoriteCount`。
- **错误码**：2001 宠物不存在（targetType=PET 且 ID 无效）；400 参数错误；401 请先登录

### 5.2 取消收藏

- **接口地址**：`DELETE /api/v1/favorites/{targetType}/{targetId}`
- **权限要求**：登录用户（USER）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | targetType | string | 是 | PET / ARTICLE |
| 路径 | targetId | long | 是 | 收藏对象 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 5.3 我的宠物收藏

- **接口地址**：`GET /api/v1/favorites/my`
- **权限要求**：登录用户（USER）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "targetId": "2",
        "name": "豆包",
        "coverImage": "http://localhost:8080/imgs/pet/p02.jpg",
        "categoryName": "狗",
        "breedName": "柯基",
        "ageMonth": 10,
        "status": "AVAILABLE",
        "shelterName": "暖阳流浪动物救助中心",
        "city": "杭州市",
        "createTime": "2026-05-15 20:12:00"
      }
    ],
    "total": 12,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 5.4 是否已收藏

- **接口地址**：`GET /api/v1/favorites/status`
- **权限要求**：登录用户（USER）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| targetType | string | 是 | - | PET / ARTICLE |
| targetId | long | 是 | - | 收藏对象 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": true,
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

## 6. 领养申请（申请人）

### 6.1 提交领养申请

- **接口地址**：`POST /api/v1/adoption-applications`
- **权限要求**：登录用户（USER，建议已实名）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| petId | long | 是 | - | 宠物 ID（状态须为 AVAILABLE） |
| applicantName | string | 是 | - | 申请人姓名，≤32 位 |
| applicantPhone | string | 是 | - | 联系电话 |
| applicantAge | int | 是 | - | 年龄，18-100 |
| occupation | string | 否 | - | 职业，≤32 位 |
| city | string | 是 | - | 现居住城市 |
| housingType | string | 是 | - | 住房类型：OWN-自有住房 / RENT-租房 |
| hasPetExperience | int | 是 | - | 是否有养宠经验 0-否 1-是 |
| familyAgree | int | 是 | - | 家人是否同意 0-否 1-是 |
| hasChildren | int | 是 | - | 家中是否有儿童 0-否 1-是 |
| hasAllergy | int | 是 | - | 是否有过敏史 0-否 1-是 |
| reason | string | 是 | - | 领养原因，≤500 字 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "101",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`data` 为申请 ID；初始状态 SUBMITTED，宠物状态联动为 PENDING（锁定），提交 7 天未受理自动过期。
- **错误码**：2002 该宠物当前不可申请领养；2003 该宠物已被领养；3001 您已有该宠物的进行中申请；400 参数错误；401 请先登录

### 6.2 我的申请分页

- **接口地址**：`GET /api/v1/adoption-applications/my`
- **权限要求**：登录用户（USER）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | 申请状态筛选（SUBMITTED/REVIEWING/...） |
| petId | long | 否 | - | 宠物 ID |
| keyword | string | 否 | - | 申请编号/宠物名/申请人姓名模糊 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "101",
        "applyNo": "apply2026060100101",
        "petId": "1",
        "petName": "煤球",
        "petCover": "http://localhost:8080/imgs/pet/p01.jpg",
        "userId": "1",
        "userNickname": "江小领",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "applicantName": "江小领",
        "applicantPhone": "138****0001",
        "city": "杭州市",
        "status": "INTERVIEW",
        "interviewTime": "2026-06-05 14:00:00",
        "homeVisitTime": "",
        "auditRemark": "",
        "createTime": "2026-06-01 09:30:00"
      }
    ],
    "total": 3,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 6.3 我的领养记录（含回访）

- **接口地址**：`GET /api/v1/adoption-applications/my/records`
- **权限要求**：登录用户（USER）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "adoptionNo": "ado2026062000001",
      "applicationId": "98",
      "petId": "2",
      "petName": "豆包",
      "petCover": "http://localhost:8080/imgs/pet/p02.jpg",
      "userId": "1",
      "userNickname": "江小领",
      "shelterId": "1",
      "shelterName": "暖阳流浪动物救助中心",
      "signDate": "2026-06-20",
      "agreementUrl": "http://localhost:9000/wandering-pet-private/agreement/2026/06/ado2026062000001.pdf?X-Amz-Expires=1800",
      "status": "ADOPTED",
      "returnReason": "",
      "returnTime": "",
      "createTime": "2026-06-20 10:00:00",
      "followUps": [
        {
          "id": "1",
          "recordId": "1",
          "applicationId": "98",
          "petId": "2",
          "petName": "豆包",
          "planDate": "2026-06-27",
          "actualDate": "",
          "round": 1,
          "status": "PENDING"
        },
        {
          "id": "2",
          "recordId": "1",
          "applicationId": "98",
          "petId": "2",
          "petName": "豆包",
          "planDate": "2026-07-20",
          "actualDate": "2026-07-21 09:15:00",
          "round": 2,
          "weight": 10.20,
          "healthStatus": "良好，已接种第二针疫苗",
          "vaccineStatus": "已接种 3 针",
          "lifeDesc": "豆包适应得很好，每天早晚遛弯，和邻居家的狗狗成了朋友。",
          "images": ["http://localhost:8080/imgs/followup/f01.jpg", "http://localhost:8080/imgs/followup/f02.jpg", "http://localhost:8080/imgs/followup/f03.jpg"],
          "status": "DONE"
        }
      ]
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 6.4 申请详情（含流水）

- **接口地址**：`GET /api/v1/adoption-applications/{id}`
- **权限要求**：登录用户（USER，仅本人申请）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "101",
    "applyNo": "apply2026060100101",
    "petId": "1",
    "petName": "煤球",
    "petCover": "http://localhost:8080/imgs/pet/p01.jpg",
    "userId": "1",
    "userNickname": "江小领",
    "shelterId": "1",
    "shelterName": "暖阳流浪动物救助中心",
    "applicantName": "江小领",
    "applicantPhone": "138****0001",
    "applicantAge": 28,
    "occupation": "软件工程师",
    "city": "杭州市",
    "housingType": "OWN",
    "hasPetExperience": 1,
    "familyAgree": 1,
    "hasChildren": 0,
    "hasAllergy": 0,
    "reason": "家中空间充足，希望领养一只猫陪伴，已做好科学喂养的准备。",
    "status": "INTERVIEW",
    "interviewTime": "2026-06-05 14:00:00",
    "auditRemark": "",
    "createTime": "2026-06-01 09:30:00",
    "flowLogs": [
      {
        "id": "1",
        "fromStatus": "",
        "toStatus": "SUBMITTED",
        "operatorId": "1",
        "operatorType": "USER",
        "remark": "提交申请",
        "createTime": "2026-06-01 09:30:00"
      },
      {
        "id": "2",
        "fromStatus": "SUBMITTED",
        "toStatus": "REVIEWING",
        "operatorId": "2",
        "operatorType": "SHELTER",
        "remark": "受理申请",
        "createTime": "2026-06-02 10:10:00"
      },
      {
        "id": "3",
        "fromStatus": "REVIEWING",
        "toStatus": "INTERVIEW",
        "operatorId": "2",
        "operatorType": "SHELTER",
        "remark": "排期面谈",
        "createTime": "2026-06-03 15:30:00"
      }
    ]
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：3003 领养申请不存在；401 请先登录；403 没有操作权限（非本人申请）

### 6.5 状态流水

- **接口地址**：`GET /api/v1/adoption-applications/{id}/flow-logs`
- **权限要求**：登录用户（USER，仅本人申请）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "fromStatus": "",
      "toStatus": "SUBMITTED",
      "operatorId": "1",
      "operatorType": "USER",
      "remark": "提交申请",
      "createTime": "2026-06-01 09:30:00"
    },
    {
      "id": "2",
      "fromStatus": "SUBMITTED",
      "toStatus": "REVIEWING",
      "operatorId": "2",
      "operatorType": "SHELTER",
      "remark": "受理申请",
      "createTime": "2026-06-02 10:10:00"
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：3003 领养申请不存在；401 请先登录

### 6.6 撤回申请

- **接口地址**：`POST /api/v1/adoption-applications/{id}/cancel`
- **权限要求**：登录用户（USER，仅本人申请）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：仅 SUBMITTED / REVIEWING 状态可撤回；撤回后宠物状态解锁回 AVAILABLE。
- **错误码**：3002 当前状态不允许该操作；3003 领养申请不存在；401 请先登录；403 没有操作权限

### 6.7 领养记录详情

- **接口地址**：`GET /api/v1/adoption-applications/records/{recordId}`
- **权限要求**：登录用户（USER，仅本人记录）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | recordId | long | 是 | 领养记录 ID |

- **响应示例**：结构同 6.3 列表项（`AdoptionRecordVO`，含 `followUps`）。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "adoptionNo": "ado2026062000001",
    "applicationId": "98",
    "petId": "2",
    "petName": "豆包",
    "petCover": "http://localhost:8080/imgs/pet/p02.jpg",
    "userId": "1",
    "userNickname": "江小领",
    "shelterId": "1",
    "shelterName": "暖阳流浪动物救助中心",
    "signDate": "2026-06-20",
    "agreementUrl": "http://localhost:9000/wandering-pet-private/agreement/2026/06/ado2026062000001.pdf?X-Amz-Expires=1800",
    "status": "ADOPTED",
    "createTime": "2026-06-20 10:00:00",
    "followUps": [
      {
        "id": "1",
        "recordId": "1",
        "applicationId": "98",
        "petId": "2",
        "petName": "豆包",
        "planDate": "2026-06-27",
        "actualDate": "",
        "round": 1,
        "status": "PENDING"
      }
    ]
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401 请先登录；403 没有操作权限

## 7. 领养审核（机构端）

> 本章接口前缀 `/api/v1/shelter/**`，仅 SHELTER 角色可访问；数据范围自动限定为本机构。

### 7.1 本机构申请分页

- **接口地址**：`GET /api/v1/shelter/applications`
- **权限要求**：救助站（权限点 shelter:adoption）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | 申请状态筛选 |
| petId | long | 否 | - | 宠物 ID |
| keyword | string | 否 | - | 申请编号/宠物名/申请人姓名模糊 |
| shelterId | long | 否 | 当前机构 | 机构端固定为当前机构，传入无效 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "101",
        "applyNo": "apply2026060100101",
        "petId": "1",
        "petName": "煤球",
        "petCover": "http://localhost:8080/imgs/pet/p01.jpg",
        "userId": "1",
        "userNickname": "江小领",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "applicantName": "江小领",
        "applicantPhone": "138****0001",
        "city": "杭州市",
        "status": "SUBMITTED",
        "interviewTime": "",
        "homeVisitTime": "",
        "auditRemark": "",
        "createTime": "2026-06-01 09:30:00"
      }
    ],
    "total": 8,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限（非 SHELTER 或无权限点）

### 7.2 受理申请

- **接口地址**：`POST /api/v1/shelter/applications/{id}/accept`
- **权限要求**：救助站（权限点 adoption:review）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID（状态须为 SUBMITTED） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：状态流转 SUBMITTED → REVIEWING，并向申请人发送站内通知。
- **错误码**：3002 当前状态不允许该操作；3003 领养申请不存在；401/403

### 7.3 排期面谈

- **接口地址**：`POST /api/v1/shelter/applications/{id}/interview`
- **权限要求**：救助站（权限点 adoption:review）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |
| Body | interviewTime | string | 否 | 面谈时间 yyyy-MM-dd HH:mm:ss |
| Body | remark | string | 否 | 备注，≤255 字 |

- **请求示例**：

```json
{
  "interviewTime": "2026-06-05 14:00:00",
  "remark": "请携带身份证到店面谈"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：3002 当前状态不允许该操作（须为 REVIEWING）；3003 领养申请不存在；401/403

### 7.4 排期家访

- **接口地址**：`POST /api/v1/shelter/applications/{id}/home-visit`
- **权限要求**：救助站（权限点 adoption:review）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |
| Body | homeVisitTime | string | 否 | 家访时间 yyyy-MM-dd HH:mm:ss |
| Body | remark | string | 否 | 家访情况记录，≤255 字 |

- **请求示例**：

```json
{
  "homeVisitTime": "2026-06-12 10:00:00"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：3002 当前状态不允许该操作（须为 INTERVIEW）；3003 领养申请不存在；401/403

### 7.5 审核通过

- **接口地址**：`POST /api/v1/shelter/applications/{id}/approve`
- **权限要求**：救助站（权限点 adoption:review）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |
| Body | remark | string | 否 | 备注，≤255 字（Body 可整体省略） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：3002 当前状态不允许该操作（须为 HOME_VISIT）；3003 领养申请不存在；401/403

### 7.6 驳回申请

- **接口地址**：`POST /api/v1/shelter/applications/{id}/reject`
- **权限要求**：救助站（权限点 adoption:review）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |
| Body | remark | string | 否 | 驳回原因，≤255 字 |

- **请求示例**：

```json
{
  "remark": "居住环境不适合饲养大型犬"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：3002 当前状态不允许该操作（须为 REVIEWING/INTERVIEW/HOME_VISIT）；3003 领养申请不存在；401/403

### 7.7 签约

- **接口地址**：`POST /api/v1/shelter/applications/{id}/sign`
- **权限要求**：救助站（权限点 adoption:sign）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |
| Body | signDate | string | 否 | 签署日期 yyyy-MM-dd，默认今天 |
| Body | agreementObjectName | string | 否 | 协议 PDF 的 MinIO 对象名（先经 21.1 上传 bizType=agreement，可后补） |

- **请求示例**：

```json
{
  "signDate": "2026-06-20",
  "agreementObjectName": "agreement/2026/06/ado2026062000001.pdf"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：3002 当前状态不允许该操作（须为 APPROVED）；3003 领养申请不存在；401/403

### 7.8 完成领养

- **接口地址**：`POST /api/v1/shelter/applications/{id}/complete`
- **权限要求**：救助站（权限点 adoption:complete）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：状态流转 SIGNED → ADOPTED，生成领养记录并自动创建第 7/30/90/180 天共 4 轮回访任务；宠物状态置 ADOPTED。
- **错误码**：3002 当前状态不允许该操作（须为 SIGNED）；3003 领养申请不存在；401/403

### 7.9 本机构领养记录分页

- **接口地址**：`GET /api/v1/shelter/adoption-records`
- **权限要求**：救助站（权限点 adoption:review）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | 记录状态：ADOPTED / RETURNED |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "adoptionNo": "ado2026062000001",
        "applicationId": "98",
        "petId": "2",
        "petName": "豆包",
        "petCover": "http://localhost:8080/imgs/pet/p02.jpg",
        "userId": "1",
        "userNickname": "江小领",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "signDate": "2026-06-20",
        "status": "ADOPTED",
        "createTime": "2026-06-20 10:00:00",
        "followUps": [
          { "id": "1", "recordId": "1", "applicationId": "98", "petId": "2", "petName": "豆包", "planDate": "2026-06-27", "actualDate": "", "round": 1, "status": "PENDING" }
        ]
      }
    ],
    "total": 6,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 7.10 退养登记

- **接口地址**：`POST /api/v1/shelter/adoption-records/{id}/return`
- **权限要求**：救助站（权限点 adoption:complete）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 领养记录 ID |
| Body | reason | string | 是 | 退养原因，≤255 字 |

- **请求示例**：

```json
{
  "reason": "领养人家中出现过敏反应，协商后退养"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：记录状态 ADOPTED → RETURNED，宠物状态回 AVAILABLE 重新开放申请。
- **错误码**：3002 当前状态不允许该操作；404 资源不存在；400 参数错误；401/403

## 8. 回访

### 8.1 填写回访表（领养人）

- **接口地址**：`POST /api/v1/follow-ups/{id}`
- **权限要求**：登录用户（USER，仅本人回访任务）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 回访任务 ID |
| Body | weight | number | 否 | 体重 kg |
| Body | healthStatus | string | 否 | 健康状况，≤64 字 |
| Body | vaccineStatus | string | 否 | 疫苗情况，≤32 字 |
| Body | lifeDesc | string | 否 | 生活近况，≤500 字 |
| Body | images | string[] | 否 | 生活照 URL 列表（建议 3 张，先经 21.1 上传） |

- **请求示例**：

```json
{
  "weight": 10.2,
  "healthStatus": "良好，食欲正常",
  "vaccineStatus": "已接种 3 针",
  "lifeDesc": "豆包适应得很好，每天早晚遛弯，和邻居家的狗狗成了朋友。",
  "images": ["http://localhost:8080/imgs/followup/f01.jpg", "http://localhost:8080/imgs/followup/f02.jpg", "http://localhost:8080/imgs/followup/f03.jpg"]
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：填写后任务状态 PENDING/OVERDUE → DONE，机构端收到通知。
- **错误码**：3005 回访任务不存在；401 请先登录；403 没有操作权限（非本人任务）

### 8.2 我的回访任务（领养人）

- **接口地址**：`GET /api/v1/follow-ups/my`
- **权限要求**：登录用户（USER）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | PENDING / DONE / OVERDUE |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "recordId": "1",
        "applicationId": "98",
        "petId": "2",
        "petName": "豆包",
        "planDate": "2026-06-27",
        "actualDate": "",
        "round": 1,
        "status": "PENDING"
      },
      {
        "id": "2",
        "recordId": "1",
        "applicationId": "98",
        "petId": "2",
        "petName": "豆包",
        "planDate": "2026-07-20",
        "actualDate": "2026-07-21 09:15:00",
        "round": 2,
        "weight": 10.20,
        "healthStatus": "良好，已接种第二针疫苗",
        "vaccineStatus": "已接种 3 针",
        "lifeDesc": "豆包适应得很好，每天早晚遛弯。",
        "images": ["http://localhost:8080/imgs/followup/f01.jpg"],
        "status": "DONE"
      }
    ],
    "total": 4,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 8.3 回访任务分页（机构端）

- **接口地址**：`GET /api/v1/shelter/follow-ups`
- **权限要求**：救助站（权限点 shelter:followup）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | PENDING / DONE / OVERDUE |
| round | int | 否 | - | 回访轮次 1-4 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "3",
        "recordId": "1",
        "applicationId": "98",
        "petId": "2",
        "petName": "豆包",
        "userId": "1",
        "userNickname": "江小领",
        "userPhone": "138****0001",
        "planDate": "2026-06-27",
        "actualDate": "",
        "round": 1,
        "status": "OVERDUE"
      }
    ],
    "total": 9,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 8.4 回访催办（机构端）

- **接口地址**：`POST /api/v1/shelter/follow-ups/{id}/remind`
- **权限要求**：救助站（权限点 followup:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 回访任务 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：向领养人发送催办站内通知。
- **错误码**：3005 回访任务不存在；401/403

### 8.5 回访归档（机构端）

- **接口地址**：`POST /api/v1/shelter/follow-ups/{id}/archive`
- **权限要求**：救助站（权限点 followup:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 回访任务 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：3005 回访任务不存在；401/403

## 9. 资讯与评论（门户）

### 9.1 文章分页（已发布）

- **接口地址**：`GET /api/v1/articles`
- **权限要求**：无（游客）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| category | string | 否 | - | RAISE-饲养指导 / KNOWLEDGE-科普百科 / NEWS-平台动态 / STORY-救助故事 |
| keyword | string | 否 | - | 标题关键词 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "title": "领养前必读：如何判断自己是否适合养猫",
        "cover": "http://localhost:8080/imgs/article/a01.jpg",
        "category": "RAISE",
        "summary": "从时间、经济、居住环境三个维度评估自己的养宠条件。",
        "author": "平台运营组",
        "viewCount": 1024,
        "likeCount": 89,
        "isTop": 1,
        "status": 1,
        "publishTime": "2026-05-01 09:00:00",
        "createTime": "2026-04-28 16:20:00"
      },
      {
        "id": "2",
        "title": "流浪猫的常见疾病与救治常识",
        "cover": "http://localhost:8080/imgs/article/a02.jpg",
        "category": "KNOWLEDGE",
        "summary": "猫瘟、猫鼻支、皮肤病的识别与应急处理。",
        "author": "平台运营组",
        "viewCount": 768,
        "likeCount": 45,
        "isTop": 0,
        "status": 1,
        "publishTime": "2026-04-25 10:00:00",
        "createTime": "2026-04-22 14:10:00"
      }
    ],
    "total": 15,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 9.2 文章详情（含评论）

- **接口地址**：`GET /api/v1/articles/{id}`
- **权限要求**：无（游客；登录后返回 `liked`）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 文章 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "title": "领养前必读：如何判断自己是否适合养猫",
    "cover": "http://localhost:8080/imgs/article/a01.jpg",
    "category": "RAISE",
    "summary": "从时间、经济、居住环境三个维度评估自己的养宠条件。",
    "content": "<p>领养一只猫意味着未来 10-20 年的承诺……</p><p>首先是时间成本……</p>",
    "author": "平台运营组",
    "viewCount": 1025,
    "likeCount": 89,
    "isTop": 1,
    "publishTime": "2026-05-01 09:00:00",
    "liked": false,
    "comments": [
      {
        "id": "5",
        "targetType": "ARTICLE",
        "targetId": "1",
        "parentId": "0",
        "userId": "1",
        "userNickname": "江小领",
        "userAvatar": "http://localhost:8080/imgs/avatar/a02.jpg",
        "content": "写得很实用，已经转给准备领养的朋友了！",
        "likeCount": 3,
        "status": 1,
        "createTime": "2026-05-02 18:30:00"
      },
      {
        "id": "6",
        "targetType": "ARTICLE",
        "targetId": "1",
        "parentId": "5",
        "userId": "3",
        "userNickname": "铲屎官阿花",
        "userAvatar": "http://localhost:8080/imgs/avatar/a03.jpg",
        "content": "同感，经济基础真的很重要。",
        "likeCount": 1,
        "status": 1,
        "createTime": "2026-05-03 09:12:00"
      }
    ]
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`content` 为白名单过滤后的富文本 HTML；`comments` 仅返回审核通过的评论。
- **错误码**：404 资源不存在

### 9.3 点赞

- **接口地址**：`POST /api/v1/articles/{id}/like`
- **权限要求**：登录用户（USER）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 文章 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401 请先登录

### 9.4 发表评论（先审后显）

- **接口地址**：`POST /api/v1/comments`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| targetType | string | 是 | - | 评论对象类型：ARTICLE |
| targetId | long | 是 | - | 评论对象 ID |
| parentId | long | 否 | 0 | 父评论 ID（0-一级评论） |
| content | string | 是 | - | 评论内容，≤500 字 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "7",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`data` 为评论 ID；评论先审后显，审核通过前不出现在详情页。
- **错误码**：404 资源不存在；400 参数错误；401 请先登录

## 10. 资讯与评论（管理端）

### 10.1 文章分页（全状态）

- **接口地址**：`GET /api/v1/admin/articles`
- **权限要求**：管理员（权限点 article:manage）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| category | string | 否 | - | RAISE / KNOWLEDGE / NEWS / STORY |
| status | int | 否 | - | 0-草稿 1-已发布 2-已下架 |
| keyword | string | 否 | - | 标题关键词 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "title": "领养前必读：如何判断自己是否适合养猫",
        "cover": "http://localhost:8080/imgs/article/a01.jpg",
        "category": "RAISE",
        "summary": "从时间、经济、居住环境三个维度评估自己的养宠条件。",
        "author": "平台运营组",
        "viewCount": 1024,
        "likeCount": 89,
        "isTop": 1,
        "status": 1,
        "publishTime": "2026-05-01 09:00:00",
        "createTime": "2026-04-28 16:20:00"
      },
      {
        "id": "8",
        "title": "2026 年世界兽医日公益活动回顾",
        "cover": "http://localhost:8080/imgs/article/a08.jpg",
        "category": "NEWS",
        "summary": "平台联合 6 家救助站开展免费体检活动。",
        "author": "平台运营组",
        "viewCount": 0,
        "likeCount": 0,
        "isTop": 0,
        "status": 0,
        "createTime": "2026-06-10 11:00:00"
      }
    ],
    "total": 18,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 10.2 新增文章

- **接口地址**：`POST /api/v1/admin/articles`
- **权限要求**：管理员（权限点 article:manage）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| title | string | 是 | - | 标题，≤128 字 |
| cover | string | 否 | - | 封面 URL（先经 21.1 上传 bizType=article） |
| category | string | 是 | - | RAISE / KNOWLEDGE / NEWS / STORY |
| summary | string | 否 | - | 摘要，≤255 字 |
| content | string | 是 | - | 富文本正文（HTML，服务端白名单过滤） |
| isTop | int | 否 | 0 | 是否置顶 0-否 1-是 |
| status | int | 否 | 0 | 0-存草稿 1-发布 |

- **请求示例**：

```json
{
  "title": "夏季宠物防暑降温指南",
  "cover": "http://localhost:8080/imgs/article/a09.jpg",
  "category": "KNOWLEDGE",
  "summary": "高温天气下的喂水、遛弯时间与室内降温建议。",
  "content": "<p>夏季气温升高……</p>",
  "isTop": 0,
  "status": 1
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "19",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：400 参数错误；401/403

### 10.3 更新文章

- **接口地址**：`PUT /api/v1/admin/articles/{id}`
- **权限要求**：管理员（权限点 article:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 文章 ID |
| Body | 同 10.2 | object | 是 | 字段含义一致 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；400 参数错误；401/403

### 10.4 删除文章

- **接口地址**：`DELETE /api/v1/admin/articles/{id}`
- **权限要求**：管理员（权限点 article:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 文章 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 10.5 置顶/取消置顶

- **接口地址**：`POST /api/v1/admin/articles/{id}/top`
- **权限要求**：管理员（权限点 article:manage）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| top | boolean | 是 | - | true-置顶 false-取消置顶（字符串 "true" 亦可） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 10.6 上下架

- **接口地址**：`POST /api/v1/admin/articles/{id}/status`
- **权限要求**：管理员（权限点 article:manage）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| status | int | 是 | - | 1-上架发布 2-下架 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 10.7 评论分页（审核）

- **接口地址**：`GET /api/v1/admin/comments`
- **权限要求**：管理员（权限点 comment:audit）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | int | 否 | - | 0-待审核 1-已通过 2-已屏蔽 |
| targetType | string | 否 | - | 对象类型，如 ARTICLE |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "9",
        "targetType": "ARTICLE",
        "targetId": "1",
        "parentId": "0",
        "userId": "4",
        "userNickname": "铲屎官阿花",
        "userAvatar": "http://localhost:8080/imgs/avatar/a03.jpg",
        "content": "请问领养流程大概需要多久？",
        "likeCount": 0,
        "status": 0,
        "createTime": "2026-06-12 20:45:00"
      }
    ],
    "total": 22,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 3
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 10.8 评论审核（通过/屏蔽）

- **接口地址**：`POST /api/v1/admin/comments/{id}/audit`
- **权限要求**：管理员（权限点 comment:audit）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 评论 ID |
| Body | pass | boolean | 是 | true-通过 false-屏蔽 |

- **请求示例**：

```json
{
  "pass": true
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 10.9 删除评论

- **接口地址**：`DELETE /api/v1/admin/comments/{id}`
- **权限要求**：管理员（权限点 comment:audit）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 评论 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

## 11. 消息中心

### 11.1 我的消息分页

- **接口地址**：`GET /api/v1/notifications`
- **权限要求**：登录用户（三端均可，USER/SHELTER/ADMIN）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| onlyUnread | boolean | 否 | - | true-仅看未读 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "31",
        "title": "申请已受理",
        "content": "您对宠物「煤球」的领养申请已被暖阳流浪动物救助中心受理，请保持电话畅通。",
        "bizType": "APPLICATION",
        "bizId": "101",
        "isRead": 0,
        "createTime": "2026-06-02 10:10:00"
      },
      {
        "id": "30",
        "title": "回访提醒",
        "content": "您领养的「豆包」第 1 次回访将于 2026-06-27 到期，请及时填写回访表。",
        "bizType": "FOLLOW_UP",
        "bizId": "1",
        "isRead": 1,
        "createTime": "2026-06-26 09:00:00"
      }
    ],
    "total": 15,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`bizType` 取值：APPLICATION-领养 / FOLLOW_UP-回访 / DONATION-捐赠 / SHELTER-机构 / REPORT-线索 / SYSTEM-系统。
- **错误码**：401 请先登录

### 11.2 标记已读

- **接口地址**：`POST /api/v1/notifications/{id}/read`
- **权限要求**：登录用户（仅本人消息）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 消息 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401 请先登录

### 11.3 全部已读

- **接口地址**：`POST /api/v1/notifications/read-all`
- **权限要求**：登录用户
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 11.4 未读数

- **接口地址**：`GET /api/v1/notifications/unread-count`
- **权限要求**：登录用户
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "5",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 11.5 批量推送（管理员）

- **接口地址**：`POST /api/v1/admin/notifications/broadcast`
- **权限要求**：管理员（权限点 notification:push）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| userType | string | 是 | - | 接收方类型：USER / SHELTER / ADMIN |
| title | string | 是 | - | 标题，≤64 字 |
| content | string | 是 | - | 内容，≤1000 字 |

- **请求示例**：

```json
{
  "userType": "USER",
  "title": "平台活动通知",
  "content": "6 月 28 日线下领养日活动开始报名，欢迎参与！"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "已推送 23 人",
  "data": "23",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：向该类型的全部在册用户逐一发送 SYSTEM 消息，`data` 为推送人数。
- **错误码**：400 参数错误；401/403

## 12. 数据统计

### 12.1 门户数字统计

- **接口地址**：`GET /api/v1/statistics/portal-summary`
- **权限要求**：无（游客）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "availablePets": 57,
    "adoptedPets": 132,
    "shelterCount": 6,
    "cityCount": 4
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 12.2 机构工作台统计

- **接口地址**：`GET /api/v1/statistics/shelter-workbench`
- **权限要求**：救助站（登录 SHELTER 账号）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "petTotal": 26,
    "availableCount": 18,
    "pendingApplication": 5,
    "followUpPending": 7,
    "monthAdopted": 3,
    "unreadMessage": 4
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限（非 SHELTER）

### 12.3 管理端看板

- **接口地址**：`GET /api/v1/statistics/admin-dashboard`
- **权限要求**：管理员（权限点 statistics:view）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totals": [
      { "name": "availablePets", "value": 57, "extra": "" },
      { "name": "adoptedPets", "value": 132, "extra": "" },
      { "name": "petTotal", "value": 213, "extra": "" },
      { "name": "shelterCount", "value": 6, "extra": "" },
      { "name": "userCount", "value": 358, "extra": "" },
      { "name": "applicationCount", "value": 245, "extra": "" },
      { "name": "articleCount", "value": 18, "extra": "" },
      { "name": "donationAmount", "value": 186500, "extra": "" }
    ],
    "adoptionTrend": [
      { "name": "2026-01", "value": 11, "extra": "" },
      { "name": "2026-02", "value": 14, "extra": "" },
      { "name": "2026-03", "value": 18, "extra": "" },
      { "name": "2026-04", "value": 21, "extra": "" },
      { "name": "2026-05", "value": 25, "extra": "" },
      { "name": "2026-06", "value": 19, "extra": "" }
    ],
    "categoryDistribution": [
      { "name": "猫", "value": 96, "extra": "" },
      { "name": "狗", "value": 84, "extra": "" },
      { "name": "兔", "value": 18, "extra": "" },
      { "name": "其他", "value": 15, "extra": "" }
    ],
    "cityDistribution": [
      { "name": "杭州市", "value": 44, "extra": "" },
      { "name": "上海市", "value": 32, "extra": "" },
      { "name": "南京市", "value": 27, "extra": "" },
      { "name": "苏州市", "value": 19, "extra": "" }
    ],
    "funnel": [
      { "name": "提交", "value": 245, "extra": "" },
      { "name": "审核", "value": 182, "extra": "" },
      { "name": "面谈/家访", "value": 121, "extra": "" },
      { "name": "通过", "value": 89, "extra": "" },
      { "name": "完成", "value": 132, "extra": "" }
    ]
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

## 13. 在线咨询

> 用户与机构双向会话，消息存 MongoDB。发送方由登录态判断（USER 发给机构 / SHELTER 回复用户），无权限点要求，但须为对应角色。

### 13.1 创建/获取与机构的会话

- **接口地址**：`POST /api/v1/chat/sessions`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| shelterId | long | 是 | - | 目标机构 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "userId": "1",
    "userNickname": "江小领",
    "userAvatar": "http://localhost:8080/imgs/avatar/a02.jpg",
    "shelterId": "1",
    "shelterName": "暖阳流浪动物救助中心",
    "shelterLogo": "http://localhost:8080/imgs/shelter/s01.jpg",
    "lastMessage": "您好，煤球目前还在可领养状态，欢迎到店看看。",
    "lastTime": "2026-06-02 11:20:00",
    "unread": 0
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：同一用户与同一机构的会话唯一，已存在则直接返回。
- **错误码**：404 资源不存在（机构不存在）；401 请先登录；403 没有操作权限（仅 USER 可发起）

### 13.2 我的会话列表

- **接口地址**：`GET /api/v1/chat/sessions`
- **权限要求**：登录用户（USER / SHELTER，按视角返回）
- **请求参数**：无
- **响应示例**（用户视角）：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "userId": "1",
      "userNickname": "江小领",
      "userAvatar": "http://localhost:8080/imgs/avatar/a02.jpg",
      "shelterId": "1",
      "shelterName": "暖阳流浪动物救助中心",
      "shelterLogo": "http://localhost:8080/imgs/shelter/s01.jpg",
      "lastMessage": "您好，煤球目前还在可领养状态，欢迎到店看看。",
      "lastTime": "2026-06-02 11:20:00",
      "unread": 0
    },
    {
      "id": "2",
      "userId": "1",
      "userNickname": "江小领",
      "userAvatar": "http://localhost:8080/imgs/avatar/a02.jpg",
      "shelterId": "2",
      "shelterName": "城市伴侣动物救助联盟",
      "shelterLogo": "http://localhost:8080/imgs/shelter/s02.jpg",
      "lastMessage": "请问周末可以去看猫吗？",
      "lastTime": "2026-06-01 15:42:00",
      "unread": 2
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 13.3 会话消息分页（进入即已读）

- **接口地址**：`GET /api/v1/chat/sessions/{id}/messages`
- **权限要求**：登录用户（会话双方）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 会话 ID |
| Query | pageNum | int | 否 | 1 | 页码 |
| Query | pageSize | int | 否 | 20 | 每页条数（上限 100） |

- **响应示例**（返回 MyBatis-Plus Page 结构，按时间倒序）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": "6832a1f4c9e77a2b3d4e5f10",
        "sessionId": "1",
        "senderId": "2",
        "senderType": "SHELTER",
        "contentType": "TEXT",
        "content": "您好，煤球目前还在可领养状态，欢迎到店看看。",
        "objectUrl": "",
        "isRead": 1,
        "createTime": "2026-06-02 11:20:00"
      },
      {
        "id": "6832a1f4c9e77a2b3d4e5f0f",
        "sessionId": "1",
        "senderId": "1",
        "senderType": "USER",
        "contentType": "TEXT",
        "content": "你好，我想了解一下煤球的健康情况。",
        "objectUrl": "",
        "isRead": 1,
        "createTime": "2026-06-02 11:15:00"
      }
    ],
    "total": 12,
    "size": 20,
    "current": 1,
    "pages": 1,
    "orders": [],
    "optimizeCountSql": true,
    "searchCount": true
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：调用即把对方发来的消息置为已读；`id/sessionId` 为 MongoDB ObjectId 字符串。
- **错误码**：404 资源不存在（会话不存在或非双方）；401 请先登录

### 13.4 发送消息

- **接口地址**：`POST /api/v1/chat/sessions/{id}/messages`
- **权限要求**：登录用户（会话双方）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 会话 ID |
| Body | contentType | string | 否 | TEXT | TEXT / IMAGE |
| Body | content | string | 否 | - | 文本内容或图片说明，≤1000 字 |
| Body | objectUrl | string | 否 | - | 图片 URL（contentType=IMAGE 时必传，先经 21.1 上传） |

- **请求示例**：

```json
{
  "contentType": "TEXT",
  "content": "好的，周六上午过去，麻烦留一下煤球。"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；400 参数错误；401 请先登录

### 13.5 标记会话已读

- **接口地址**：`POST /api/v1/chat/sessions/{id}/read`
- **权限要求**：登录用户（会话双方）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 会话 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401 请先登录

### 13.6 未读总数

- **接口地址**：`GET /api/v1/chat/unread-total`
- **权限要求**：登录用户（USER / SHELTER）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "2",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

## 14. 募捐捐赠（门户）

### 14.1 进行中项目列表

- **接口地址**：`GET /api/v1/donation-projects`
- **权限要求**：无（游客）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "shelterId": "1",
      "shelterName": "暖阳流浪动物救助中心",
      "shelterCity": "杭州市",
      "title": "夏季犬舍降温改造计划",
      "cover": "http://localhost:8080/imgs/donation/d01.jpg",
      "targetAmount": 20000.00,
      "raisedAmount": 12680.50,
      "donorCount": 96,
      "startDate": "2026-05-01",
      "endDate": "2026-07-31",
      "status": "ONGOING",
      "progress": 63,
      "content": "杭州夏季高温，犬舍需加装遮阳棚与水冷风扇……",
      "createTime": "2026-04-28 10:00:00"
    },
    {
      "id": "2",
      "shelterId": "2",
      "shelterName": "城市伴侣动物救助联盟",
      "shelterCity": "杭州市",
      "title": "流浪猫 TNR 绝育基金",
      "cover": "http://localhost:8080/imgs/donation/d02.jpg",
      "targetAmount": 15000.00,
      "raisedAmount": 9320.00,
      "donorCount": 74,
      "startDate": "2026-05-10",
      "endDate": "2026-08-10",
      "status": "ONGOING",
      "progress": 62,
      "content": "为社区流浪猫提供免费绝育手术……",
      "createTime": "2026-05-06 09:30:00"
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 14.2 项目详情

- **接口地址**：`GET /api/v1/donation-projects/{id}`
- **权限要求**：无（游客）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 项目 ID |

- **响应示例**：结构同 14.1 列表项。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1",
    "shelterId": "1",
    "shelterName": "暖阳流浪动物救助中心",
    "shelterCity": "杭州市",
    "title": "夏季犬舍降温改造计划",
    "cover": "http://localhost:8080/imgs/donation/d01.jpg",
    "targetAmount": 20000.00,
    "raisedAmount": 12680.50,
    "donorCount": 96,
    "startDate": "2026-05-01",
    "endDate": "2026-07-31",
    "status": "ONGOING",
    "progress": 63,
    "content": "杭州夏季高温，犬舍需加装遮阳棚与水冷风扇，预计改造 12 间犬舍……",
    "createTime": "2026-04-28 10:00:00"
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在

### 14.3 捐赠（模拟支付）

- **接口地址**：`POST /api/v1/donation-projects/{id}/donate`
- **权限要求**：登录用户（USER）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 项目 ID |
| Body | amount | number | 是 | - | 捐赠金额（元），≥0.01，最多 2 位小数 |
| Body | isAnonymous | int | 否 | 0 | 1-匿名展示 |
| Body | message | string | 否 | - | 留言，≤255 字 |

- **请求示例**：

```json
{
  "amount": 100.00,
  "isAnonymous": 0,
  "message": "希望毛孩子们清凉过夏天！"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：模拟支付，即时到账并累加 `raisedAmount`；向机构发送 DONATION 通知。
- **错误码**：404 资源不存在（项目不存在或已关闭）；400 参数错误；401 请先登录

### 14.4 捐赠明细（公开）

- **接口地址**：`GET /api/v1/donation-projects/{id}/records`
- **权限要求**：无（游客）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 项目 ID |
| Query | limit | int | 否 | - | 返回条数上限 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "201",
      "projectId": "1",
      "donorName": "江**",
      "amount": 100.00,
      "isAnonymous": 0,
      "message": "希望毛孩子们清凉过夏天！",
      "payTime": "2026-06-01 12:30:00"
    },
    {
      "id": "202",
      "projectId": "1",
      "donorName": "匿名爱心人士",
      "amount": 50.00,
      "isAnonymous": 1,
      "message": "",
      "payTime": "2026-06-01 13:05:00"
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：捐赠人姓名脱敏展示；匿名捐赠显示为「匿名爱心人士」。
- **错误码**：无业务码

### 14.5 捐赠榜

- **接口地址**：`GET /api/v1/donation-projects/{id}/rank`
- **权限要求**：无（游客）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 项目 ID |
| Query | topN | int | 否 | - | 榜单条数 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "rank": 1, "donorName": "匿名爱心人士", "totalAmount": 2000.00, "donationCount": 5 },
    { "rank": 2, "donorName": "王**", "totalAmount": 1500.00, "donationCount": 3 },
    { "rank": 3, "donorName": "李**", "totalAmount": 800.00, "donationCount": 2 }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

## 15. 募捐管理（机构/管理端）

### 15.1 本机构项目列表

- **接口地址**：`GET /api/v1/shelter/donation-projects`
- **权限要求**：救助站（权限点 shelter:donation）
- **请求参数**：无
- **响应示例**：结构同 14.1（含全部状态项目，如 ONGOING/CLOSED）。

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "shelterId": "1",
      "shelterName": "暖阳流浪动物救助中心",
      "shelterCity": "杭州市",
      "title": "夏季犬舍降温改造计划",
      "cover": "http://localhost:8080/imgs/donation/d01.jpg",
      "targetAmount": 20000.00,
      "raisedAmount": 12680.50,
      "donorCount": 96,
      "startDate": "2026-05-01",
      "endDate": "2026-07-31",
      "status": "ONGOING",
      "progress": 63,
      "content": "杭州夏季高温，犬舍需加装遮阳棚与水冷风扇……",
      "createTime": "2026-04-28 10:00:00"
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 15.2 新建募捐项目

- **接口地址**：`POST /api/v1/shelter/donation-projects`
- **权限要求**：救助站（权限点 donation:manage）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| title | string | 是 | - | 项目标题，≤128 字 |
| cover | string | 否 | - | 封面 URL（先经 21.1 上传 bizType=donation） |
| targetAmount | number | 是 | - | 目标金额（元），≥1 |
| startDate | string | 否 | - | 开始日期 yyyy-MM-dd |
| endDate | string | 否 | - | 结束日期 yyyy-MM-dd |
| content | string | 否 | - | 项目说明，≤5000 字 |

- **请求示例**：

```json
{
  "title": "冬季猫舍保暖物资募集",
  "cover": "http://localhost:8080/imgs/donation/d03.jpg",
  "targetAmount": 8000.00,
  "startDate": "2026-11-01",
  "endDate": "2026-12-31",
  "content": "为猫舍采购保温棉帘、恒温垫与宠物电热毯……"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "3",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：400 参数错误；401/403

### 15.3 更新募捐项目

- **接口地址**：`PUT /api/v1/shelter/donation-projects/{id}`
- **权限要求**：救助站（权限点 donation:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 项目 ID |
| Body | 同 15.2 | object | 是 | 字段含义一致 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在（非本机构项目）；400 参数错误；401/403

### 15.4 关闭募捐项目

- **接口地址**：`POST /api/v1/shelter/donation-projects/{id}/close`
- **权限要求**：救助站（权限点 donation:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 项目 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 15.5 本机构捐赠明细

- **接口地址**：`GET /api/v1/shelter/donation-projects/records`
- **权限要求**：救助站（权限点 donation:record:view）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| projectId | long | 否 | - | 按项目筛选 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "201",
        "projectId": "1",
        "donorName": "江**",
        "amount": 100.00,
        "isAnonymous": 0,
        "message": "希望毛孩子们清凉过夏天！",
        "payTime": "2026-06-01 12:30:00"
      }
    ],
    "total": 96,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 15.6 管理端捐赠明细分页

- **接口地址**：`GET /api/v1/admin/donation-records`
- **权限要求**：管理员（权限点 donation:record:view）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| projectId | long | 否 | - | 按项目筛选 |

- **响应示例**：结构同 15.5（全平台数据）。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "188",
        "projectId": "2",
        "donorName": "匿名爱心人士",
        "amount": 500.00,
        "isAnonymous": 1,
        "message": "支持 TNR 行动",
        "payTime": "2026-05-20 19:10:00"
      }
    ],
    "total": 170,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 17
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

## 16. 志愿者

### 16.1 报名志愿者

- **接口地址**：`POST /api/v1/volunteer-applications`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| shelterId | long | 是 | - | 报名的救助站 ID |
| realName | string | 是 | - | 真实姓名，≤32 位 |
| phone | string | 是 | - | 联系电话 |
| skills | string | 否 | - | 个人技能，≤255 字（如医疗、摄影、运输） |
| availableTime | string | 否 | - | 可服务时间，≤255 字 |

- **请求示例**：

```json
{
  "shelterId": "1",
  "realName": "江小领",
  "phone": "13800000001",
  "skills": "宠物护理、周末运输",
  "availableTime": "每周六、周日全天"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "5",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`data` 为报名 ID；初始状态 PENDING，待机构审核（16.4）。
- **错误码**：400 参数错误；401 请先登录；403 没有操作权限（存在待审核/已通过的报名）

### 16.2 我的报名

- **接口地址**：`GET /api/v1/volunteer-applications/my`
- **权限要求**：登录用户（USER）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "5",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "userId": "1",
        "userNickname": "江小领",
        "realName": "江小领",
        "phone": "13800000001",
        "skills": "宠物护理、周末运输",
        "availableTime": "每周六、周日全天",
        "status": "APPROVED",
        "auditRemark": "欢迎加入！",
        "auditTime": "2026-05-30 09:20:00",
        "createTime": "2026-05-28 14:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 16.3 报名审核分页（机构端）

- **接口地址**：`GET /api/v1/shelter/volunteer-applications`
- **权限要求**：救助站（权限点 shelter:volunteer）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | PENDING / APPROVED / REJECTED |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "6",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "userId": "3",
        "userNickname": "铲屎官阿花",
        "realName": "周晓花",
        "phone": "13900000002",
        "skills": "医疗协助",
        "availableTime": "工作日晚间",
        "status": "PENDING",
        "createTime": "2026-06-10 10:15:00"
      }
    ],
    "total": 4,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 16.4 志愿者审核（机构端）

- **接口地址**：`POST /api/v1/shelter/volunteer-applications/{id}/audit`
- **权限要求**：救助站（权限点 volunteer:audit）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 报名 ID |
| Body | pass | boolean | 是 | true-通过 false-不通过 |
| Body | remark | string | 否 | 审核备注，≤255 字 |

- **请求示例**：

```json
{
  "pass": true,
  "remark": "欢迎加入志愿者团队"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；400 参数错误；401/403

## 17. 流浪动物线索

### 17.1 上报线索

- **接口地址**：`POST /api/v1/rescue-reports`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| categoryId | long | 否 | - | 物种分类 ID（可空，空则视为“其他”） |
| description | string | 是 | - | 动物特征与所处环境描述，≤500 字 |
| images | string[] | 否 | - | 现场图片 URL，最多 9 张（先经 21.1 上传 bizType=report） |
| province | string | 是 | - | 省份 |
| city | string | 是 | - | 城市 |
| district | string | 否 | - | 区县 |
| address | string | 是 | - | 位置（区+街道/地标），≤255 字 |
| longitude | number | 否 | - | 经度 |
| latitude | number | 否 | - | 纬度 |
| urgency | int | 否 | 1 | 1-一般 2-紧急 3-危急 |

- **请求示例**：

```json
{
  "categoryId": "1",
  "description": "小区东门绿化带发现一只瘦弱的橘猫，左后腿疑似受伤，行动缓慢。",
  "images": ["http://localhost:8080/imgs/report/r01.jpg", "http://localhost:8080/imgs/report/r02.jpg"],
  "province": "浙江省",
  "city": "杭州市",
  "district": "西湖区",
  "address": "西湖区文新街道桂花园小区东门",
  "longitude": 120.075,
  "latitude": 30.286,
  "urgency": 2
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "12",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`data` 为线索 ID；初始状态 PENDING，编号 `reportNo` 以 `rsr` 开头。
- **错误码**：400 参数错误；401 请先登录

### 17.2 我的上报

- **接口地址**：`GET /api/v1/rescue-reports/my`
- **权限要求**：登录用户（USER）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | PENDING / ASSIGNED / PROCESSING / RESOLVED / CLOSED |
| urgency | int | 否 | - | 1 / 2 / 3 |
| city | string | 否 | - | 城市 |
| keyword | string | 否 | - | 描述关键词 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "12",
        "reportNo": "rsr2026060100012",
        "userId": "1",
        "userNickname": "江小领",
        "categoryId": "1",
        "categoryName": "猫",
        "description": "小区东门绿化带发现一只瘦弱的橘猫，左后腿疑似受伤，行动缓慢。",
        "images": ["http://localhost:8080/imgs/report/r01.jpg"],
        "province": "浙江省",
        "city": "杭州市",
        "district": "西湖区",
        "address": "西湖区文新街道桂花园小区东门",
        "urgency": 2,
        "status": "PROCESSING",
        "handlerId": "2",
        "handlerName": "暖阳流浪动物救助中心",
        "handleRemark": "已派志愿者前往处置",
        "handleTime": "2026-06-01 16:30:00",
        "createTime": "2026-06-01 14:20:00"
      }
    ],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 17.3 管理端线索分页

- **接口地址**：`GET /api/v1/admin/rescue-reports`
- **权限要求**：管理员（权限点 rescue:manage）
- **请求参数**（Query）：同 17.2（status/urgency/city/keyword/pageNum/pageSize）。
- **响应示例**：结构同 17.2（全平台数据）。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "15",
        "reportNo": "rsr2026060500015",
        "userId": "4",
        "userNickname": "橘猫守护者",
        "categoryId": "2",
        "categoryName": "狗",
        "description": "高架桥下有一只被遗弃的泰迪，装有项圈，疑为走失宠物。",
        "images": [],
        "province": "浙江省",
        "city": "杭州市",
        "district": "滨江区",
        "address": "滨江区西兴大桥桥底",
        "urgency": 3,
        "status": "PENDING",
        "createTime": "2026-06-05 08:10:00"
      }
    ],
    "total": 18,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 17.4 线索分派（管理员）

- **接口地址**：`POST /api/v1/admin/rescue-reports/{id}/assign`
- **权限要求**：管理员（权限点 rescue:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 线索 ID |
| Body | handlerId | long | 是 | 处理方救助站账号 ID（sys_user.id） |
| Body | remark | string | 否 | 备注，≤255 字 |

- **请求示例**：

```json
{
  "handlerId": "2",
  "remark": "请尽快前往处置，伤情较重"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：状态 PENDING → ASSIGNED，并向被分派的机构发送 REPORT 通知。
- **错误码**：404 资源不存在；400 参数错误；401/403

### 17.5 线索关闭（管理员）

- **接口地址**：`POST /api/v1/admin/rescue-reports/{id}/close`
- **权限要求**：管理员（权限点 rescue:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 线索 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 17.6 开始处理（救助站）

- **接口地址**：`POST /api/v1/shelter/rescue-reports/{id}/process`
- **权限要求**：救助站（登录 SHELTER 账号，仅本机构被分派的线索）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 线索 ID |
| Body | remark | string | 否 | 处理备注，≤255 字（Body 可整体省略） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：状态 ASSIGNED → PROCESSING。
- **错误码**：404 资源不存在；401/403

### 17.7 标记解决（救助站）

- **接口地址**：`POST /api/v1/shelter/rescue-reports/{id}/resolve`
- **权限要求**：救助站（登录 SHELTER 账号，仅本机构被分派的线索）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 线索 ID |
| Body | remark | string | 否 | 处理结果说明，≤255 字（Body 可整体省略） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：状态 PROCESSING → RESOLVED，并向上报人发送通知；管理员可再执行 17.5 关闭。
- **错误码**：404 资源不存在；401/403

## 18. 宠物管理（机构端）

> 本章接口前缀 `/api/v1/shelter/pets`，仅 SHELTER 角色可访问，数据范围限定本机构。

### 18.1 本机构宠物分页（全状态）

- **接口地址**：`GET /api/v1/shelter/pets`
- **权限要求**：救助站（权限点 shelter:pet）
- **请求参数**（Query）：同 4.1（pageNum/pageSize/categoryId/breedId/gender/size/ageMin/ageMax/city/shelterId/status/keyword/orderBy/orderDirection）；机构端默认返回全部状态。
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "petNo": "pet2026030100001",
        "name": "煤球",
        "categoryId": "1",
        "categoryName": "猫",
        "breedName": "中华田园猫",
        "gender": "MALE",
        "ageMonth": 14,
        "coverImage": "http://localhost:8080/imgs/pet/p01.jpg",
        "status": "PENDING",
        "viewCount": 357,
        "favoriteCount": 48,
        "activeApplicationCount": 2,
        "updateTime": "2026-06-02 10:10:00"
      },
      {
        "id": "3",
        "petNo": "pet2026030100003",
        "name": "雪球",
        "categoryId": "1",
        "categoryName": "猫",
        "breedName": "英国短毛猫",
        "gender": "FEMALE",
        "ageMonth": 24,
        "coverImage": "http://localhost:8080/imgs/pet/p03.jpg",
        "status": "AVAILABLE",
        "viewCount": 156,
        "favoriteCount": 20,
        "activeApplicationCount": 0,
        "updateTime": "2026-05-20 09:00:00"
      }
    ],
    "total": 26,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 3
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 18.2 发布宠物档案

- **接口地址**：`POST /api/v1/shelter/pets`
- **权限要求**：救助站（权限点 pet:create）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| name | string | 是 | - | 宠物名字，≤32 字 |
| categoryId | long | 是 | - | 物种分类 ID |
| breedId | long | 否 | - | 品种 ID |
| gender | string | 是 | - | MALE / FEMALE / UNKNOWN |
| ageMonth | int | 是 | - | 月龄 |
| weight | number | 否 | - | 体重 kg |
| size | string | 是 | - | SMALL / MEDIUM / LARGE |
| color | string | 否 | - | 毛色，≤32 字 |
| sterilized | int | 否 | 0 | 是否绝育 0-否 1-是 |
| vaccinated | int | 否 | 0 | 是否免疫 0-否 1-是 |
| dewormed | int | 否 | 0 | 是否驱虫 0-否 1-是 |
| healthStatus | string | 是 | - | HEALTHY / GENERAL / ILL |
| characterDesc | string | 否 | - | 性格描述，≤255 字 |
| adoptionRequirements | string | 否 | - | 领养要求，≤512 字 |
| story | string | 否 | - | 救助故事 |
| coverImage | string | 是 | - | 封面图 URL（先经 21.1 上传 bizType=pet） |
| images | array | 否 | - | 相册（不含封面）：`[{"objectName":"pet/xxx.jpg","url":"http://...","sort":2}]` |
| status | string | 否 | AVAILABLE | AVAILABLE-直接发布 / OFFLINE-存为下架 |

- **请求示例**：

```json
{
  "name": "雪球",
  "categoryId": "1",
  "breedId": "4",
  "gender": "FEMALE",
  "ageMonth": 24,
  "weight": 4.80,
  "size": "MEDIUM",
  "color": "白色",
  "sterilized": 1,
  "vaccinated": 1,
  "dewormed": 1,
  "healthStatus": "HEALTHY",
  "characterDesc": "安静温顺，喜欢晒太阳。",
  "adoptionRequirements": "限杭州本地领养，科学喂养，接受回访。",
  "story": "雪球曾是一位老人饲养的猫咪，老人住院后被送至救助站。",
  "coverImage": "http://localhost:8080/imgs/pet/p03.jpg",
  "images": [
    { "objectName": "pet/2026/06/p03-2.jpg", "url": "http://localhost:8080/imgs/pet/p03-2.jpg", "sort": 2 }
  ],
  "status": "AVAILABLE"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "27",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`data` 为宠物 ID；`petNo` 系统自动生成。
- **错误码**：2004 物种分类不存在；400 参数错误；401/403

### 18.3 编辑宠物档案

- **接口地址**：`PUT /api/v1/shelter/pets/{id}`
- **权限要求**：救助站（权限点 pet:update）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 宠物 ID |
| Body | 同 18.2 | object | 是 | 字段含义一致 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在（或非本机构）；400 参数错误；401/403

### 18.4 删除宠物档案

- **接口地址**：`DELETE /api/v1/shelter/pets/{id}`
- **权限要求**：救助站（权限点 pet:delete）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 宠物 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在；401/403

### 18.5 上下架/状态变更（支持批量）

- **接口地址**：`POST /api/v1/shelter/pets/status`
- **权限要求**：救助站（权限点 pet:status）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| status | string | 是 | - | 目标状态：AVAILABLE / MEDICAL / OFFLINE（PENDING/ADOPTED 由领养流程联动，不开放手工设置） |
| ids | long[] | 否 | - | 批量操作时传宠物 ID 数组；不传则只操作单只（配合单条接口使用） |

- **请求示例**：

```json
{
  "status": "OFFLINE",
  "ids": ["3", "4", "5"]
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在；400 参数错误；401/403

### 18.6 健康档案列表

- **接口地址**：`GET /api/v1/shelter/pets/{petId}/health-records`
- **权限要求**：救助站（权限点 shelter:pet）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | petId | long | 是 | 宠物 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "2",
      "petId": "1",
      "recordType": "VACCINE",
      "recordDate": "2026-05-10",
      "title": "猫三联第二针",
      "content": "接种妙三多第二针，无异常反应。",
      "vetName": "李医生",
      "attachmentUrl": ""
    },
    {
      "id": "1",
      "petId": "1",
      "recordType": "STERILIZE",
      "recordDate": "2026-03-20",
      "title": "绝育手术",
      "content": "手术顺利，术后恢复良好。",
      "vetName": "王医生",
      "attachmentUrl": ""
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在；401/403

### 18.7 新增健康档案

- **接口地址**：`POST /api/v1/shelter/pets/{petId}/health-records`
- **权限要求**：救助站（权限点 health-record:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | petId | long | 是 | 宠物 ID |
| Body | recordType | string | 是 | VACCINE-疫苗接种 / DEWORM-驱虫 / STERILIZE-绝育 / TREATMENT-治疗 / CHECK-体检 |
| Body | recordDate | string | 否 | 记录日期 yyyy-MM-dd |
| Body | title | string | 是 | 记录标题 |
| Body | content | string | 否 | 记录内容 |
| Body | vetName | string | 否 | 医生姓名 |
| Body | attachmentUrl | string | 否 | 附件 URL（如诊断单照片） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "3",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在；400 参数错误；401/403

### 18.8 更新健康档案

- **接口地址**：`PUT /api/v1/shelter/pets/health-records/{id}`
- **权限要求**：救助站（权限点 health-record:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 健康档案 ID |
| Body | 同 18.7 | object | 是 | 字段含义一致（petId 可省略，以路径为准的档案对应宠物不可改） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；400 参数错误；401/403

### 18.9 删除健康档案

- **接口地址**：`DELETE /api/v1/shelter/pets/health-records/{id}`
- **权限要求**：救助站（权限点 health-record:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 健康档案 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 18.10 发布宠物动态（MongoDB）

- **接口地址**：`POST /api/v1/shelter/pets/{petId}/dynamics`
- **权限要求**：救助站（权限点 pet:update）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | petId | long | 是 | 宠物 ID |
| Body | content | string | 否 | 动态文字 |
| Body | images | string[] | 否 | 动态图片 URL 列表（先经 21.1 上传 bizType=dynamic） |

- **请求示例**：

```json
{
  "content": "煤球今天完成了第 3 次驱虫，精神状态很好。",
  "images": ["http://localhost:8080/imgs/pet/p01-d1.jpg"]
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在；401/403

### 18.11 宠物动态列表（机构端）

- **接口地址**：`GET /api/v1/shelter/pets/{petId}/dynamics`
- **权限要求**：救助站（权限点 shelter:pet）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | petId | long | 是 | - | 宠物 ID |
| Query | limit | int | 否 | - | 返回数量上限 |

- **响应示例**：结构同 4.5（`DynamicVO` 列表）。

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "6832a1f4c9e77a2b3d4e5f01",
      "petId": "1",
      "content": "煤球今天完成了第 3 次驱虫，精神状态很好。",
      "images": ["http://localhost:8080/imgs/pet/p01-d1.jpg"],
      "likeCount": 23,
      "createTime": "2026-05-20 16:40:00"
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：2001 宠物不存在；401/403

## 19. 管理端·全量查询与审核

> 本章接口前缀 `/api/v1/admin/**`，仅 ADMIN 角色可访问。

### 19.1 宠物全量分页

- **接口地址**：`GET /api/v1/admin/pets`
- **权限要求**：管理员（权限点 pet:manage-all）
- **请求参数**（Query）：同 4.1 全部参数（含 status，可筛选全平台任意状态宠物）。
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "petNo": "pet2026030100001",
        "name": "煤球",
        "categoryName": "猫",
        "breedName": "中华田园猫",
        "gender": "MALE",
        "ageMonth": 14,
        "coverImage": "http://localhost:8080/imgs/pet/p01.jpg",
        "status": "PENDING",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "city": "杭州市",
        "viewCount": 357,
        "favoriteCount": 48,
        "publishTime": "2026-05-12 10:30:00",
        "createTime": "2026-05-12 10:30:00"
      },
      {
        "id": "9",
        "petNo": "pet2026030100009",
        "name": "大黄",
        "categoryName": "狗",
        "breedName": "中华田园犬",
        "gender": "MALE",
        "ageMonth": 36,
        "coverImage": "http://localhost:8080/imgs/pet/p09.jpg",
        "status": "ADOPTED",
        "shelterId": "2",
        "shelterName": "城市伴侣动物救助联盟",
        "city": "杭州市",
        "viewCount": 620,
        "favoriteCount": 88,
        "publishTime": "2026-04-02 09:00:00",
        "createTime": "2026-04-02 09:00:00"
      }
    ],
    "total": 213,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 22
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 19.2 领养申请全量分页

- **接口地址**：`GET /api/v1/admin/applications`
- **权限要求**：管理员（权限点 adoption:manage-all）
- **请求参数**（Query）：同 6.2（pageNum/pageSize/status/petId/keyword/shelterId）。
- **响应示例**：结构同 7.1 列表项（`ApplicationListVO`，全平台数据）。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "103",
        "applyNo": "apply2026060300103",
        "petId": "2",
        "petName": "豆包",
        "petCover": "http://localhost:8080/imgs/pet/p02.jpg",
        "userId": "3",
        "userNickname": "铲屎官阿花",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "applicantName": "周晓花",
        "applicantPhone": "139****0002",
        "city": "杭州市",
        "status": "REVIEWING",
        "auditRemark": "",
        "createTime": "2026-06-03 15:00:00"
      }
    ],
    "total": 245,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 25
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 19.3 C 端用户分页

- **接口地址**：`GET /api/v1/admin/users`
- **权限要求**：管理员（权限点 user:manage）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数（上限 100） |
| keyword | string | 否 | - | 用户名/昵称模糊 |
| status | int | 否 | - | 1-正常 0-禁用 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "username": "demo",
        "nickname": "江小领",
        "avatar": "http://localhost:8080/imgs/avatar/a02.jpg",
        "phone": "138****0001",
        "gender": 1,
        "city": "杭州市",
        "realNameStatus": 1,
        "status": 1,
        "lastLoginTime": "2026-06-15 08:30:00",
        "createTime": "2026-03-01 09:00:00"
      },
      {
        "id": "4",
        "username": "catlover",
        "nickname": "橘猫守护者",
        "avatar": "http://localhost:8080/imgs/avatar/a04.jpg",
        "phone": "137****5678",
        "gender": 2,
        "city": "上海市",
        "realNameStatus": 0,
        "status": 1,
        "lastLoginTime": "2026-06-14 22:10:00",
        "createTime": "2026-03-15 14:20:00"
      }
    ],
    "total": 358,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 36
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 19.4 启用/禁用 C 端用户

- **接口地址**：`POST /api/v1/admin/users/{id}/status`
- **权限要求**：管理员（权限点 user:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 用户 ID |
| Body | enable | boolean | 是 | true-启用 false-禁用（禁用即强制下线） |

- **请求示例**：

```json
{
  "enable": false
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：1006 用户不存在；401/403

### 19.5 机构分页列表

- **接口地址**：`GET /api/v1/admin/shelters`
- **权限要求**：管理员（权限点 shelter:audit）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| auditStatus | int | 否 | - | 0-待审核 1-已通过 2-已驳回 |
| keyword | string | 否 | - | 机构名称/联系人模糊 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "3",
        "name": "宠爱之家流浪动物保护协会",
        "logo": "http://localhost:8080/imgs/shelter/s03.jpg",
        "licenseNo": "52330100MJ880012XA",
        "contactName": "赵明远",
        "contactPhone": "13700000005",
        "province": "浙江省",
        "city": "杭州市",
        "district": "余杭区",
        "address": "余杭区仓前街道龙园路 9 号",
        "intro": "社区型救助组织，主打猫咪领养。",
        "auditStatus": 0,
        "accountUsername": "13700000005",
        "createTime": "2026-06-08 10:00:00"
      },
      {
        "id": "1",
        "name": "暖阳流浪动物救助中心",
        "logo": "http://localhost:8080/imgs/shelter/s01.jpg",
        "licenseNo": "52330100MJ880001AB",
        "contactName": "陈远山",
        "contactPhone": "13800000003",
        "province": "浙江省",
        "city": "杭州市",
        "district": "西湖区",
        "address": "西湖区转塘街道龙坞工业园 3 号",
        "intro": "成立于 2018 年，专注流浪犬猫的救助、医疗与领养匹配。",
        "auditStatus": 1,
        "auditTime": "2026-03-10 11:00:00",
        "accountUsername": "shelter1",
        "createTime": "2026-03-08 15:30:00"
      }
    ],
    "total": 8,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 19.6 入驻审核（通过/驳回）

- **接口地址**：`POST /api/v1/admin/shelters/{id}/audit`
- **权限要求**：管理员（权限点 shelter:audit）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 救助站 ID |
| Body | pass | boolean | 是 | true-通过 false-驳回 |
| Body | remark | string | 否 | 审核备注，≤255 字 |

- **请求示例**：

```json
{
  "pass": true,
  "remark": "资质齐全，同意入驻"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：审核通过后自动创建 SHELTER 后台账号（用户名取申请的 `accountUsername` 或联系人手机号，初始密码 123456），并向联系人发送 SHELTER 通知。
- **错误码**：404 资源不存在；400 参数错误；401/403

## 20. 系统设置与数据字典

### 20.1 字典全量（公开）

- **接口地址**：`GET /api/v1/dicts`
- **权限要求**：无（游客；Redis 缓存 24h）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pet_gender": [
      { "code": "MALE", "label": "公" },
      { "code": "FEMALE", "label": "母" },
      { "code": "UNKNOWN", "label": "未知" }
    ],
    "pet_size": [
      { "code": "SMALL", "label": "小型" },
      { "code": "MEDIUM", "label": "中型" },
      { "code": "LARGE", "label": "大型" }
    ],
    "pet_status": [
      { "code": "AVAILABLE", "label": "可领养" },
      { "code": "PENDING", "label": "审核中" },
      { "code": "ADOPTED", "label": "已领养" },
      { "code": "MEDICAL", "label": "治疗中" },
      { "code": "OFFLINE", "label": "已下架" }
    ],
    "pet_health": [
      { "code": "HEALTHY", "label": "健康" },
      { "code": "GENERAL", "label": "一般" },
      { "code": "ILL", "label": "治疗中" }
    ],
    "housing_type": [
      { "code": "OWN", "label": "自有住房" },
      { "code": "RENT", "label": "租房" }
    ],
    "article_category": [
      { "code": "RAISE", "label": "饲养指导" },
      { "code": "KNOWLEDGE", "label": "科普百科" },
      { "code": "NEWS", "label": "平台动态" },
      { "code": "STORY", "label": "救助故事" }
    ]
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 20.2 字典全量（管理）

- **接口地址**：`GET /api/v1/admin/dicts`
- **权限要求**：管理员（权限点 dict:manage）
- **请求参数**：无
- **响应示例**：结构同 20.1（按类型分组的 `DictItemVO` Map）。
- **错误码**：401 请先登录；403 没有操作权限

### 20.3 字典列表（管理）

- **接口地址**：`GET /api/v1/admin/dict-items`
- **权限要求**：管理员（权限点 dict:manage）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| dictType | string | 否 | - | 字典类型，如 pet_size；不传返回全部 |

- **响应示例**（返回 `SysDict` 实体列表）：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": "12", "dictType": "pet_size", "dictCode": "SMALL", "dictLabel": "小型", "sort": 1, "status": 1 },
    { "id": "13", "dictType": "pet_size", "dictCode": "MEDIUM", "dictLabel": "中型", "sort": 2, "status": 1 },
    { "id": "14", "dictType": "pet_size", "dictCode": "LARGE", "dictLabel": "大型", "sort": 3, "status": 1 }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 20.4 新增字典项

- **接口地址**：`POST /api/v1/admin/dict-items`
- **权限要求**：管理员（权限点 dict:manage）
- **请求参数**（Body，application/json，`SysDict` 实体）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| dictType | string | 是 | - | 字典类型 |
| dictCode | string | 是 | - | 字典编码 |
| dictLabel | string | 是 | - | 字典标签 |
| sort | int | 否 | 0 | 排序值 |
| status | int | 否 | 1 | 1-启用 0-停用 |

- **请求示例**：

```json
{
  "dictType": "pet_size",
  "dictCode": "GIANT",
  "dictLabel": "超大型",
  "sort": 4,
  "status": 1
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：400 参数错误；401/403

### 20.5 更新字典项

- **接口地址**：`PUT /api/v1/admin/dict-items/{id}`
- **权限要求**：管理员（权限点 dict:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 字典项 ID |
| Body | 同 20.4 | object | 是 | 字段含义一致 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 20.6 删除字典项

- **接口地址**：`DELETE /api/v1/admin/dict-items/{id}`
- **权限要求**：管理员（权限点 dict:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 字典项 ID |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 20.7 角色列表（含权限 ID）

- **接口地址**：`GET /api/v1/admin/roles`
- **权限要求**：管理员（权限点 role:manage）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "1",
      "roleCode": "ADMIN",
      "roleName": "平台管理员",
      "description": "拥有系统全部权限",
      "permIds": ["1", "2", "3", "4", "5", "6", "7", "15", "19", "24", "28", "30", "31", "32", "37", "38", "39", "40", "41", "42"]
    },
    {
      "id": "2",
      "roleCode": "SHELTER",
      "roleName": "救助站机构",
      "description": "机构端业务权限",
      "permIds": ["8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "20", "21", "22", "23", "25", "29", "33", "34", "35", "36", "41", "42"]
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 20.8 权限树

- **接口地址**：`GET /api/v1/admin/permissions`
- **权限要求**：管理员（权限点 role:manage）
- **请求参数**：无
- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": "1", "permCode": "admin:dashboard", "permName": "数据看板", "permType": "MENU", "parentId": "0", "path": "/admin/dashboard", "sort": 1, "children": [] },
    { "id": "2", "permCode": "admin:shelter", "permName": "机构审核", "permType": "MENU", "parentId": "0", "path": "/admin/shelters", "sort": 2, "children": [] },
    { "id": "15", "permCode": "pet:create", "permName": "发布宠物档案", "permType": "API", "parentId": "0", "path": "", "sort": 1, "children": [] },
    { "id": "19", "permCode": "pet:manage-all", "permName": "宠物全量管理", "permType": "API", "parentId": "0", "path": "", "sort": 5, "children": [] }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 20.9 分配角色权限

- **接口地址**：`PUT /api/v1/admin/roles/{id}/permissions`
- **权限要求**：管理员（权限点 role:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 角色 ID |
| Body | permIds | long[] | 是 | 权限点 ID 集合（全量覆盖） |

- **请求示例**：

```json
{
  "permIds": [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22, 23, 25, 29, 33, 34, 35, 36, 41, 42]
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；400 参数错误（permIds 为空）；401/403

### 20.10 后台账号分页

- **接口地址**：`GET /api/v1/admin/sys-users`
- **权限要求**：管理员（权限点 sys-user:manage）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| roleCode | string | 否 | - | ADMIN / SHELTER |
| keyword | string | 否 | - | 账号/姓名模糊 |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "username": "admin",
        "realName": "平台管理员",
        "phone": "13800000000",
        "avatar": "http://localhost:8080/imgs/avatar/a01.jpg",
        "roleCode": "ADMIN",
        "status": 1,
        "lastLoginTime": "2026-06-15 09:00:00",
        "createTime": "2026-03-01 09:00:00"
      },
      {
        "id": "2",
        "username": "shelter1",
        "realName": "陈远山",
        "phone": "13800000003",
        "avatar": "http://localhost:8080/imgs/shelter/s01.jpg",
        "roleCode": "SHELTER",
        "shelterId": "1",
        "shelterName": "暖阳流浪动物救助中心",
        "status": 1,
        "lastLoginTime": "2026-06-14 18:20:00",
        "createTime": "2026-03-08 15:30:00"
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

### 20.11 新增后台账号

- **接口地址**：`POST /api/v1/admin/sys-users`
- **权限要求**：管理员（权限点 sys-user:manage）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| username | string | 是 | - | 登录账号，4-32 位 |
| password | string | 是 | - | 初始密码，6-32 位 |
| realName | string | 是 | - | 姓名，≤32 位 |
| phone | string | 否 | - | 手机号 |
| roleCode | string | 是 | - | ADMIN / SHELTER |
| shelterId | long | 否 | - | SHELTER 角色必填（关联救助站） |

- **请求示例**：

```json
{
  "username": "shelter4",
  "password": "123456",
  "realName": "赵明远",
  "phone": "13700000005",
  "roleCode": "SHELTER",
  "shelterId": "3"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：400 参数错误；401/403

### 20.12 启用/禁用后台账号

- **接口地址**：`POST /api/v1/admin/sys-users/{id}/status`
- **权限要求**：管理员（权限点 sys-user:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 后台账号 ID |
| Body | enable | boolean | 是 | true-启用 false-禁用 |

- **请求示例**：

```json
{
  "enable": false
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；401/403

### 20.13 重置后台账号密码

- **接口地址**：`POST /api/v1/admin/sys-users/{id}/reset-password`
- **权限要求**：管理员（权限点 sys-user:manage）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 后台账号 ID |
| Body | newPassword | string | 是 | 新密码，6-32 位 |

- **请求示例**：

```json
{
  "newPassword": "wp@2026abc"
}
```

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在；400 参数错误；401/403

### 20.14 操作日志分页（MongoDB）

- **接口地址**：`GET /api/v1/admin/operation-logs`
- **权限要求**：管理员（权限点 oplog:view）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| userId | long | 否 | - | 操作人 ID |
| module | string | 否 | - | 业务模块：pet / adoption / shelter / article / comment / donation / volunteer / rescue / followup / user / sys-user / role / dict / notification / file |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "684a1f4c9e77a2b3d4e5f0ab",
        "userId": "2",
        "userType": "SHELTER",
        "module": "adoption",
        "action": "受理申请",
        "method": "com.wanderingpet.module.adoption.controller.ShelterAdoptionController#accept",
        "params": "[{\"id\":101}]",
        "ip": "127.0.0.1",
        "duration": 45,
        "success": true,
        "createTime": "2026-06-02 10:10:00"
      }
    ],
    "total": 86,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 9
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录；403 没有操作权限

## 21. 文件上传

### 21.1 上传文件

- **接口地址**：`POST /api/v1/file/upload`
- **权限要求**：登录后台账号（权限点 file:upload，ADMIN/SHELTER）
- **请求参数**（multipart/form-data）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| file | file | 是 | - | 二进制文件 |
| bizType | string | 否 | pet | pet / avatar / article / donation / report / agreement / dynamic（图片 5MB；agreement 仅 PDF 10MB；未列出的 bizType 仅支持 mp4 视频 50MB） |

- **响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "objectName": "pet/2026/06/1f8e2a4b9c.jpg",
    "url": "http://localhost:8080/imgs/pet/2026/06/1f8e2a4b9c.jpg"
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **说明**：`objectName` 为 MinIO 对象名（数据库存这个），`url` 为访问直链；`agreement` 类型返回 30 分钟预签名 URL。
- **错误码**：4001 文件类型不支持；4002 文件超出大小限制；4003 文件上传失败；4004 上传文件不能为空；401 请先登录；403 没有操作权限（USER 角色无 file:upload 权限点）

---

## 附：接口数量统计

| 章节 | 模块 | 接口数 |
| --- | --- | --- |
| 1 | 认证 | 9 |
| 2 | 用户中心（C 端） | 8 |
| 3 | 救助站（机构） | 5 |
| 4 | 宠物（门户） | 7 |
| 5 | 收藏 | 4 |
| 6 | 领养申请（申请人） | 7 |
| 7 | 领养审核（机构端） | 10 |
| 8 | 回访 | 5 |
| 9 | 资讯与评论（门户） | 4 |
| 10 | 资讯与评论（管理端） | 9 |
| 11 | 消息中心 | 5 |
| 12 | 数据统计 | 3 |
| 13 | 在线咨询 | 6 |
| 14 | 募捐捐赠（门户） | 5 |
| 15 | 募捐管理（机构/管理端） | 6 |
| 16 | 志愿者 | 4 |
| 17 | 流浪动物线索 | 7 |
| 18 | 宠物管理（机构端） | 11 |
| 19 | 管理端·全量查询与审核 | 6 |
| 20 | 系统设置与数据字典 | 14 |
| 21 | 文件上传 | 1 |
| **合计** | | **136** |
