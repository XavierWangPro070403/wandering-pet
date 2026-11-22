# 数据库脚本说明（wandering_pet）

## 执行顺序

```bash
# 1. 建库建表（幂等，可反复执行）
mysql -uroot -p --default-character-set=utf8mb4 < init.sql

# 2. 演示数据（幂等，可反复执行；依赖 init.sql）
mysql -uroot -p --default-character-set=utf8mb4 < init-data.sql
```

要求 MySQL 8.0+（脚本使用了 ngram 全文索引与生成列唯一索引）。

## 初始化账号（密码统一为 `123456`，BCrypt 强度 10）

| 账号 | 密码 | 角色 | 说明 |
|---|---|---|---|
| `admin` | 123456 | ADMIN | Web 管理端登录（http://localhost:8081 → /login） |
| `shelter1` | 123456 | SHELTER | 毛窝窝流浪动物救助站（杭州） |
| `shelter2` | 123456 | SHELTER | 汪喵之家小动物保护中心（成都） |
| `shelter3` | 123456 | SHELTER | 阳光伴侣动物救助基地（北京，入驻待审核） |
| `demo` | 123456 | USER | Web 用户端登录（C 端用户表 `user`） |
| 微信用户 | — | USER | 小程序开发者工具中任意 code 登录（开发模式） |

## 表清单（26 张）

> 提示词 5.2 节标题为“22 张表”，但其表格逐行列出 26 张，本工程以 5.2 节清单为准。

- 系统与账号域（7）：`sys_user`、`sys_role`、`sys_permission`、`sys_role_permission`、`sys_dict`、`user`、`user_address`
- 机构与宠物域（6）：`shelter`、`pet_category`、`pet_breed`、`pet`、`pet_image`、`pet_health_record`
- 领养与回访域（4）：`adoption_application`、`adoption_flow_log`、`adoption_record`、`follow_up`
- 内容与互动域（4）：`article`、`comment`、`favorite`、`notification`
- 公益与线索域（4）：`donation_project`、`donation_record`、`volunteer_application`、`rescue_report`
- 聊天域（1）：`chat_session`（消息正文存 MongoDB `chat_message` 集合，索引由后端启动时自动创建）

## 设计说明

1. **防重复申请唯一约束**：MySQL 不支持部分索引，`adoption_application` 增加了生成列
   `active_flag`（非终态申请 = 1，终态 = NULL），并以
   `uk_pet_user_active(pet_id, user_id, active_flag)` 实现唯一约束——利用“唯一索引允许多个 NULL”
   的特性，保证“同一用户对同一宠物仅一条非终态申请”，且撤回/驳回后可再次申请。
2. **敏感字段加密**：`user.phone`、`user.id_card_no` 为 AES-128-CBC 加密后的 Base64 密文，
   密钥见后端 `application.yml`（演示环境固定密钥，生产环境应改用密钥管理服务）。
3. **图片素材**：演示数据中的图片 URL 指向后端静态目录
   `http://localhost:8080/imgs/**`（由 `wandering-pet-server/src/main/resources/static/imgs` 托管），
   通过上传接口新产生的文件存 MinIO（公共桶 `wandering-pet` / 私有桶 `wandering-pet-private`）。
4. **外键**：全部不建物理外键，用索引 + 业务层校验保证一致性（便于迁移）。
