# wandering-pet-miniapp（小程序端）

流浪宠物领养系统 · 微信小程序（领养人端），基于 **uni-app（Vue 2 语法）**。

## 导入 HBuilderX（3 步）

1. 打开 HBuilderX → 文件 → 导入 → 从本地目录导入 → 选择 `wandering-pet-miniapp` 文件夹；
2. 工具 → 设置 → 运行配置 → 微信开发者工具路径配置（填入微信开发者工具安装路径）；
3. 运行 → 运行到小程序模拟器 → 微信开发者工具。
   （微信开发者工具中需开启「设置 → 安全 → 服务端口」，并在「详情 → 本地设置」勾选**不校验合法域名**）

> 本目录为 HBuilderX 普通项目结构（`manifest.json`/`pages.json`/`App.vue`/`main.js` 直接位于根目录），
> 无需 npm install，HBuilderX 内置编译器即可编译。

## 配置后端地址

`common/config.js` 中 `BASE_URL` 默认为 `http://localhost:8080`，按需改为服务器地址。

## 演示账号 / 登录说明

- **手机号 + 短信验证码登录**（默认方式）：登录页输入 11 位手机号 → 获取验证码 → 登录；未注册自动创建账号。种子手机号 `13800000001`（江小领）、`13800000002`（汪汪队友）。
- **短信开发模式**：后端 `wandering.sms` 未配置阿里云凭证时，验证码不会真实下发，接口回传 `data.code`，登录页会自动填入，便于联调；填好 `access-key-id` / `access-key-secret` / `sign-name` / `template-code` 后自动真实发送。
- **微信一键登录**：后端 `wandering.wx.appid` 未配置时为开发模式 —— 任意 code 均可登录（自动创建 C 端用户）；配置真实 appid/secret 后自动切换为官方 `jscode2session` 流程。
- 已登录后可在登录页绑定 / 换绑手机号（同样走短信验证码，`scene=bind`）。
- 手机号、捐赠（模拟支付）均与 Web 端同源数据。

## 结构

```
wandering-pet-miniapp/
├── pages.json            # 主包 4 个 TabBar 页 + pagesUser/pagesShelter 分包
├── manifest.json         # uni-app 配置（mp-weixin，urlCheck 已关闭）
├── App.vue / main.js / uni.scss
├── common/
│   ├── config.js         # 后端地址
│   ├── request.js        # uni.request 二次封装（拦截/401 重登录/上传）
│   ├── api.js            # 与后端 Controller 一一对应的 API 模块
│   └── format.js         # 状态/年龄/金额格式化（与 Web 同源）
├── store/index.js        # Vuex：token、userInfo
├── static/               # TabBar 图标、默认头像
├── pages/                # 主包：首页/领养/消息/我的
├── pagesUser/            # 用户分包：宠物详情/申请/进度/资讯/上报/捐赠/志愿/聊天/登录
└── pagesShelter/         # 机构分包：工作台/申请审核（移动端轻量版）
```

## 视觉规范（与 Web 同源，§8.12）

- 主色 `#FF7A45`、页面底 `#F7F8FA`、卡片白底 24rpx 圆角、左右边距 32rpx；
- 按钮 88rpx 高 44rpx 圆角主色实心 + 深墨字；`:active` 0.98 反馈；
- TabBar 4 项：首页 / 领养 / 消息 / 我的；
- 状态标签"颜色+文字"、禁轮播、正文 28rpx、数字 tabular。
