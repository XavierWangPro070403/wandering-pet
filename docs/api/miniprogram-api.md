# 流浪宠物领养系统 · 微信小程序端接口文档

> 适用端：微信小程序（`wandering-pet-miniapp`）。BaseURL、鉴权、统一响应、错误码等通用约定见 [README.md](./README.md)；Web 端完整接口清单见 [web-api.md](./web-api.md)。
> 小程序以 **USER** 角色访问（登录方式为微信登录）；机构工作人员可使用 SHELTER 账号登录小程序机构工作台（第 12 章）。
> 接口编号连续（1.1 ~ 12.10），共 **55** 个接口，均为服务端已有端点，与 web-api.md 中的接口一一对应。

---

## 1. 登录与账号绑定

### 1.1 小程序微信登录

- **接口地址**：`POST /api/v1/auth/wx-login`
- **权限要求**：无（游客）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| code | string | 是 | - | wx.login 返回的临时凭证 code（一次性，5 分钟有效） |
| nickname | string | 否 | - | 微信昵称，首次登录时传入用于初始化资料 |
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

- **说明**：
  - **dev 模式**（服务端未配置 `wandering.wx.appid`）：任意 code 均可登录，openid 解析为 `dev_ + code`（如 code=`test123` → `dev_test123`），同一 code 重复登录返回同一账号，便于本地联调；
  - **生产模式**：服务端携带 appid/secret 调用微信 `code2session` 换取 openid，失败返回 5002；
  - 返回 `realNameStatus=0` 时，提交领养申请前需引导用户在小程序内完成实名；
  - 本接口限流 5 次/分钟/IP。
- **错误码**：1002 账号已被禁用；5002 第三方服务调用失败（生产模式微信侧失败）；429 请求过于频繁

### 1.2 小程序绑定手机号

- **接口地址**：`POST /api/v1/auth/bind-phone`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| phone | string | 是 | - | 手机号，格式 `^1[3-9]\d{9}$` |

- **请求示例**：

```json
{
  "phone": "13800000001"
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

- **说明**：绑定后登录态/资料中的手机号以脱敏形式展示（如 `138****0001`）；手机号 AES 加密存储。
- **错误码**：400 参数错误（手机号格式不正确）；401 请先登录

## 2. 宠物浏览

### 2.1 宠物分页列表

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

- **说明**：小程序端默认仅返回 AVAILABLE 状态宠物。
- **错误码**：无业务码

### 2.2 宠物详情

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

### 2.3 相似推荐

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

### 2.4 记录一次浏览

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

### 2.5 宠物动态（MongoDB）

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
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 2.6 物种分类列表

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

### 2.7 品种列表

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
    { "id": "5", "categoryId": "1", "name": "美国短毛猫" }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

## 3. 收藏

### 3.1 收藏

- **接口地址**：`POST /api/v1/favorites`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| targetType | string | 是 | - | PET / ARTICLE |
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

- **错误码**：2001 宠物不存在；400 参数错误；401 请先登录

### 3.2 取消收藏

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

### 3.3 我的宠物收藏

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

### 3.4 是否已收藏

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

## 4. 领养申请

### 4.1 提交领养申请

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
| housingType | string | 是 | - | OWN-自有住房 / RENT-租房 |
| hasPetExperience | int | 是 | - | 是否有养宠经验 0-否 1-是 |
| familyAgree | int | 是 | - | 家人是否同意 0-否 1-是 |
| hasChildren | int | 是 | - | 家中是否有儿童 0-否 1-是 |
| hasAllergy | int | 是 | - | 是否有过敏史 0-否 1-是 |
| reason | string | 是 | - | 领养原因，≤500 字 |

- **请求示例**：

```json
{
  "petId": "1",
  "applicantName": "江小领",
  "applicantPhone": "13800000001",
  "applicantAge": 28,
  "occupation": "软件工程师",
  "city": "杭州市",
  "housingType": "OWN",
  "hasPetExperience": 1,
  "familyAgree": 1,
  "hasChildren": 0,
  "hasAllergy": 0,
  "reason": "家中空间充足，希望领养一只猫陪伴，已做好科学喂养的准备。"
}
```

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

### 4.2 我的申请分页

- **接口地址**：`GET /api/v1/adoption-applications/my`
- **权限要求**：登录用户（USER）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | 申请状态筛选 |
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

### 4.3 申请详情（含进度流水）

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

### 4.4 状态流水

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

### 4.5 撤回申请

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

### 4.6 我的领养记录（含回访）

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
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：401 请先登录

### 4.7 领养记录详情

- **接口地址**：`GET /api/v1/adoption-applications/records/{recordId}`
- **权限要求**：登录用户（USER，仅本人记录）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | recordId | long | 是 | 领养记录 ID |

- **响应示例**：结构同 4.6 列表项（`AdoptionRecordVO`，含 `followUps`）。

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

## 5. 回访填写

### 5.1 填写回访表

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
| Body | images | string[] | 否 | 生活照 URL 列表（建议 3 张） |

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

- **说明**：填写后任务状态 PENDING/OVERDUE → DONE；回访计划为领养完成后第 7/30/90/180 天共 4 轮。
- **错误码**：3005 回访任务不存在；401 请先登录；403 没有操作权限（非本人任务）

### 5.2 我的回访任务

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

## 6. 资讯

### 6.1 文章分页（已发布）

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

### 6.2 文章详情（含评论）

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
      }
    ]
  },
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：404 资源不存在

### 6.3 点赞

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

### 6.4 发表评论（先审后显）

- **接口地址**：`POST /api/v1/comments`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| targetType | string | 是 | - | 评论对象类型：ARTICLE |
| targetId | long | 是 | - | 文章 ID |
| parentId | long | 否 | 0 | 父评论 ID（0-一级评论） |
| content | string | 是 | - | 评论内容，≤500 字 |

- **请求示例**：

```json
{
  "targetType": "ARTICLE",
  "targetId": "1",
  "parentId": "0",
  "content": "写得很实用，已经转给准备领养的朋友了！"
}
```

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

- **说明**：`data` 为评论 ID；评论先审后显。
- **错误码**：404 资源不存在；400 参数错误；401 请先登录

## 7. 消息通知

### 7.1 我的消息分页

- **接口地址**：`GET /api/v1/notifications`
- **权限要求**：登录用户（USER）
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

- **说明**：`bizType` 用于小程序内跳转：APPLICATION→申请详情、FOLLOW_UP→回访任务、DONATION→捐赠项目、SHELTER→机构主页、REPORT→我的上报、SYSTEM→无跳转。
- **错误码**：401 请先登录

### 7.2 标记已读

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

### 7.3 全部已读

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

### 7.4 未读数

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

- **说明**：用于小程序底部 TabBar/首页角标轮询。
- **错误码**：401 请先登录

## 8. 在线咨询（聊天）

### 8.1 创建/获取与机构的会话

- **接口地址**：`POST /api/v1/chat/sessions`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| shelterId | long | 是 | - | 目标机构 ID（宠物详情/机构主页「在线咨询」按钮） |

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

- **错误码**：404 资源不存在；401 请先登录；403 没有操作权限（仅 USER 可发起）

### 8.2 我的会话列表

- **接口地址**：`GET /api/v1/chat/sessions`
- **权限要求**：登录用户（USER / SHELTER，按视角返回）
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

### 8.3 会话消息分页（进入即已读）

- **接口地址**：`GET /api/v1/chat/sessions/{id}/messages`
- **权限要求**：登录用户（会话双方）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 会话 ID |
| Query | pageNum | int | 否 | 1 | 页码 |
| Query | pageSize | int | 否 | 20 | 每页条数（上限 100） |

- **响应示例**（MyBatis-Plus Page 结构，按时间倒序，进入即把对方消息置为已读）：

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
        "contentType": "IMAGE",
        "content": "",
        "objectUrl": "http://localhost:8080/imgs/chat/c01.jpg",
        "isRead": 1,
        "createTime": "2026-06-02 11:16:00"
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

- **说明**：`senderType` 判断消息在左（对方）还是在右（自己）；`contentType=IMAGE` 时渲染 `objectUrl`。
- **错误码**：404 资源不存在；401 请先登录

### 8.4 发送消息

- **接口地址**：`POST /api/v1/chat/sessions/{id}/messages`
- **权限要求**：登录用户（会话双方）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | - | 会话 ID |
| Body | contentType | string | 否 | TEXT | TEXT / IMAGE |
| Body | content | string | 否 | - | 文本内容或图片说明，≤1000 字 |
| Body | objectUrl | string | 否 | - | 图片 URL（contentType=IMAGE 时必传） |

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

### 8.5 标记会话已读

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

### 8.6 未读总数

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

## 9. 募捐捐赠

### 9.1 进行中项目列表

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
    }
  ],
  "traceId": "a1b2c3d4",
  "timestamp": 1737052800123
}
```

- **错误码**：无业务码

### 9.2 项目详情

- **接口地址**：`GET /api/v1/donation-projects/{id}`
- **权限要求**：无（游客）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 项目 ID |

- **响应示例**：

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

### 9.3 捐赠（模拟支付）

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

- **说明**：模拟支付即时到账；捐赠成功后机构端收到 DONATION 通知。
- **错误码**：404 资源不存在（项目不存在或已关闭）；400 参数错误；401 请先登录

### 9.4 捐赠明细（公开）

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

- **错误码**：无业务码

### 9.5 捐赠榜

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

## 10. 流浪动物上报

### 10.1 上报线索

- **接口地址**：`POST /api/v1/rescue-reports`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| categoryId | long | 否 | - | 物种分类 ID（可空，空则视为“其他”） |
| description | string | 是 | - | 动物特征与所处环境描述，≤500 字 |
| images | string[] | 否 | - | 现场图片 URL，最多 9 张 |
| province | string | 是 | - | 省份 |
| city | string | 是 | - | 城市 |
| district | string | 否 | - | 区县 |
| address | string | 是 | - | 位置（区+街道/地标），≤255 字 |
| longitude | number | 否 | - | 经度（wx.getLocation 获取） |
| latitude | number | 否 | - | 纬度 |
| urgency | int | 否 | 1 | 1-一般 2-紧急 3-危急 |

- **请求示例**：

```json
{
  "categoryId": "1",
  "description": "小区东门绿化带发现一只瘦弱的橘猫，左后腿疑似受伤，行动缓慢。",
  "images": ["http://localhost:8080/imgs/report/r01.jpg"],
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

- **说明**：`data` 为线索 ID；初始状态 PENDING，管理员分派救助站处理后状态依次变为 ASSIGNED/PROCESSING/RESOLVED。
- **错误码**：400 参数错误；401 请先登录

### 10.2 我的上报

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

## 11. 志愿者

### 11.1 报名志愿者

- **接口地址**：`POST /api/v1/volunteer-applications`
- **权限要求**：登录用户（USER）
- **请求参数**（Body，application/json）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| shelterId | long | 是 | - | 报名的救助站 ID |
| realName | string | 是 | - | 真实姓名，≤32 位 |
| phone | string | 是 | - | 联系电话 |
| skills | string | 否 | - | 个人技能，≤255 字 |
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

- **说明**：`data` 为报名 ID；初始状态 PENDING。
- **错误码**：400 参数错误；401 请先登录；403 没有操作权限（存在待审核/已通过的报名）

### 11.2 我的报名

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

## 12. 机构工作台（SHELTER 账号）

> 机构工作人员可使用 SHELTER 账号在小程序端登录工作台（对应后台登录 `POST /api/v1/auth/admin-login`），以下接口前缀 `/api/v1/shelter/**`，仅 SHELTER 角色可访问，数据范围限定本机构。

### 12.1 机构工作台统计

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

### 12.2 本机构申请分页

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

- **错误码**：401 请先登录；403 没有操作权限

### 12.3 受理申请

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

- **说明**：SUBMITTED → REVIEWING，并向申请人发送站内通知。
- **错误码**：3002 当前状态不允许该操作；3003 领养申请不存在；401/403

### 12.4 排期面谈

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

### 12.5 排期家访

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

### 12.6 审核通过

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

### 12.7 驳回申请

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

### 12.8 签约

- **接口地址**：`POST /api/v1/shelter/applications/{id}/sign`
- **权限要求**：救助站（权限点 adoption:sign）
- **请求参数**：

| 位置 | 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| 路径 | id | long | 是 | 申请 ID |
| Body | signDate | string | 否 | 签署日期 yyyy-MM-dd，默认今天 |
| Body | agreementObjectName | string | 否 | 协议 PDF 的 MinIO 对象名（可后补上传） |

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

### 12.9 完成领养

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

- **说明**：SIGNED → ADOPTED，生成领养记录并自动创建第 7/30/90/180 天共 4 轮回访任务。
- **错误码**：3002 当前状态不允许该操作（须为 SIGNED）；3003 领养申请不存在；401/403

### 12.10 本机构领养记录分页

- **接口地址**：`GET /api/v1/shelter/adoption-records`
- **权限要求**：救助站（权限点 adoption:review）
- **请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | ADOPTED / RETURNED |

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

---

## 附：接口数量统计

| 章节 | 模块 | 接口数 |
| --- | --- | --- |
| 1 | 登录与账号绑定 | 2 |
| 2 | 宠物浏览 | 7 |
| 3 | 收藏 | 4 |
| 4 | 领养申请 | 7 |
| 5 | 回访填写 | 2 |
| 6 | 资讯 | 4 |
| 7 | 消息通知 | 4 |
| 8 | 在线咨询（聊天） | 6 |
| 9 | 募捐捐赠 | 5 |
| 10 | 流浪动物上报 | 2 |
| 11 | 志愿者 | 2 |
| 12 | 机构工作台 | 10 |
| **合计** | | **55** |

> 说明：小程序与 Web 端共用同一套服务端接口，本章仅收录小程序实际调用的子集；退养登记、线索分派、系统设置等仅 Web 端使用的接口未列入，完整清单见 [web-api.md](./web-api.md)。

