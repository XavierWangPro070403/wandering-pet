# 贡献指南

感谢你对「流浪宠物领养系统」的关注。为了让改动更容易被合并，请先读一下这几条。

## 一、本地跑起来

```bash
# 1. 数据库（MySQL 8.0）
mysql -uroot -p --default-character-set=utf8mb4 < docs/sql/init.sql
mysql -uroot -p --default-character-set=utf8mb4 < docs/sql/init-data.sql
mysql -uroot -p --default-character-set=utf8mb4 < docs/sql/shop-init.sql

# 2. 后端（8080）
cd wandering-pet-server
mvn clean package -DskipTests
java -jar target/wandering-pet-server.jar

# 3. Web（8081）
cd wandering-pet-web
npm install
npm run serve
```

小程序用 HBuilderX 导入 `wandering-pet-miniapp`，运行到微信开发者工具即可；微信 appid 未配置时自动进入开发模式（任意 code 可登录）。

## 二、提 Issue

- Bug 请用 **Bug 报告** 模板，附上复现步骤、期望结果、实际结果，以及后端控制台/浏览器 Console 的报错；
- 功能建议请用 **功能建议** 模板，说明使用场景，而不只是"想要某个页面"。

## 三、提 Pull Request

1. 从 `main` 切出分支：`feat/xxx`、`fix/xxx`、`docs/xxx`；
2. 一个 PR 只做一件事，避免顺带重构无关文件；
3. 提交信息建议使用约定式提交：

   ```
   feat(adoption): 支持按机构筛选申请列表
   fix(chat): 修复撤回后会话预览未刷新的问题
   docs(readme): 补充小程序运行说明
   ```

4. 提交前自查：

   - [ ] `wandering-pet-server` 能 `mvn -B -DskipTests package` 通过
   - [ ] `wandering-pet-web` 能 `npm run build` 通过
   - [ ] 小程序改动能通过 HBuilderX 编译，且没有控制台报错
   - [ ] 没有提交 `node_modules`、`target`、`dist`、`unpackage`、日志或任何密钥
   - [ ] 新增接口已同步更新 `docs/api/` 下的文档
   - [ ] 涉及数据库变更时，同步更新 `docs/sql/` 下的 SQL（脚本需可重复执行）

## 四、代码风格

- Java：4 空格缩进，遵循现有分层（`controller → service → mapper`），统一返回 `R<T>`，业务异常用 `BizException.of(ResultCode.XXX)`；
- Vue：2 空格缩进，组件样式使用 `scoped` + `@import '~@/styles/variables.scss'`，颜色/字号一律取 `styles/tokens.scss` 中的变量；
- 尽量不新增第三方依赖，确需引入请在 PR 描述里说明理由。

## 五、许可

贡献的代码默认以 [MIT License](LICENSE) 授权。
