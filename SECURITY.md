# 安全策略

## 支持范围

本项目为教学 / 演示性质的开源项目，仅对 `main` 分支的最新提交提供安全修复。

## 报告漏洞

请**不要**公开提 Issue。可以通过以下方式私下联系维护者：

- 在仓库的 **Security → Report a vulnerability** 中提交私密报告（推荐）；
- 或发送邮件到仓库资料页里的联系邮箱。

请在报告中说明：受影响的模块与版本、复现步骤、可能的影响范围。确认后会在修复发布的 Release Note 中致谢。

## 仓库中的"演示配置"

以下配置是为了让项目**开箱即可运行**而保留的演示值，**请勿直接用于生产**：

| 位置 | 项 | 生产建议 |
|---|---|---|
| `wandering-pet-server/src/main/resources/application.yml` | `wandering.jwt.secret` | 换成 ≥64 字符的随机串，用环境变量注入 |
| 同上 | `wandering.aes.key`（手机号/身份证加密） | 换成 16 字节随机值并妥善托管，**更换后旧数据将无法解密** |
| 同上 | `spring.datasource.password`（默认 `123456`） | 使用独立数据库账号，最小权限 |
| 同上 | `wandering.wx.appid/secret`、`wandering.sms.*` | 用环境变量注入，勿提交到仓库 |
| `wandering-pet-miniapp/manifest.json` | `mp-weixin.appid` | 换成自己的小程序 appid |

## 提交前务必确认

- [ ] 没有把 `.env`、`application-prod.yml`、日志、`*.token.txt` 等含密钥的文件加入暂存区
- [ ] 没有把真实的手机号、身份证、用户数据、数据库导出文件一起提交
- [ ] 生产环境的 AccessKey / Secret / 证书一律走环境变量或密钥管理服务

## 依赖安全

```bash
# 后端
cd wandering-pet-server && mvn org.owasp:dependency-check-maven:check
# 前端
cd wandering-pet-web && npm audit
```
