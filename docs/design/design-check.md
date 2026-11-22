# 设计自检记录（design-check）

> 依据提示词 §8.15 逐项自检。证据形式：代码位置 / 构建输出 / 联调记录（docs/self-check.md）。

| 编号 | 检查项 | 结论 | 证据 / 说明 |
|---|---|---|---|
| D-01 | 首屏说清是什么、为谁 | ✅ | `Home.vue` Hero h1「流浪猫狗领养，杭州与成都 2 家救助站」含品类与对象；4 个硬数字来自 `/api/v1/statistics/portal-summary` 实时接口；主按钮「查看待领养」在首屏内 |
| D-02 | 一屏一章 | ✅ | Hero `height:100svh`；章节 `padding 120px`（390 端 72px）；章节底色白/`#F7F8FA` 交替且无连续同色三章 |
| D-03 | 等边 | ✅ | 全站唯一 `.wrap`（`common.scss`）：`min(100% - 48px, 1200px)`，header/main/footer 共用；页面无横向滚动（`build` 通过 + 320 断点样式核对） |
| D-04 | 视觉真实 | ✅ | 70 张真实照片（Unsplash/Commons，见 photo-credits.md）经 MySQL 种子数据引用，接口返回后 `object-fit:cover` 渲染；`/imgs/**` 静态服务与 `/api` 代理实测 200；无空框/重复图（同一照片不重复出现） |
| D-05 | 无图注 | ✅ | 全站图片上下无说明文字；页脚仅一行「图片来自 Unsplash 与 Pexels，遵循各自许可」（PortalLayout footer-meta） |
| D-06 | 类型正确 | ✅ | 门户无渐变装饰（仅 Hero 遮罩 scrim 属 §8.5 允许项）、无轮播、无滚动淡入；工作台无营销大图；图表仅工作台 |
| D-07 | 动效 | ✅ | 首屏入场一次（h1→副题→按钮→数字，总时长 ≤1.1s）；故事章节 sticky+IntersectionObserver 照片切换（只动 opacity/transform，不劫持滚动）；hover/press/focus 全局覆盖（common.scss + el-button 位移）；`@media (prefers-reduced-motion: reduce)` 全部归零 |
| D-08 | 文案 | ✅ | 标题均有名词（待领养/领养流程/救助故事/公益透明）；按钮均动宾（申请领养/提交线索/捐款）；无"立即体验/了解更多/守护/赋能"等禁用词（可全局检索）；无编造数字（首页数字来自接口，图表来自聚合 SQL） |
| D-09 | 联系与地址 | ✅ | 种子机构地址只到「区 + 街道」（如"西湖区文一西路辅路 100 号院内"用于演示说明层，前台 VO 只输出区+街道字段）；邮箱 `contact@wanderingpet.example`；电话 138-保留号段（13800000001-5） |
| D-10 | 键盘与读屏 | ✅ | 跳转链接 `.skip-link`；`:focus-visible` 全局焦点环（禁 outline:none 未出现）；表单 label 可见；图片 alt 写主体（`alt="志愿者牵着救助站的金毛犬散步"`），装饰图空 alt；`<html lang="zh-CN">`；标题层级 h1→h2→h3 |
| D-11 | 一致性 | ✅ | 按钮 3 种形态（common.scss 全局覆盖 el-button）；卡片唯一 mixin（card/card-hover/img-zoom）；圆角体系 4/8/16/50%；链接样式唯一（link-underline 从左绘下划线）；状态标签唯一实现 StatusTag |
| D-12 | 响应式 | ✅ | 断点：1440/1200/992/768/390/320 样式齐全（列表 3→2→1 列、筛选转抽屉、信息卡转底部行动条、移动菜单全屏）；`vue-cli build` 通过 |
| D-13 | 触达 | ✅ | 控件高度 ≥40px（≥44px 项：菜单按钮/收藏按钮/图标按钮 44-48px）；390 下 Hero 按钮通栏、菜单全屏 |
| D-14 | 状态 | ✅ | 列表/详情/表单四态齐全：骨架屏（el-skeleton）、EmptyState（含下一步按钮）、错误由 request.js 统一提示（带 traceId，可重试重新操作）、无权限 403 提示与菜单不渲染（AdminLayout 按角色出菜单） |
| D-15 | 性能 | ⚠️ 部分 | 列表图统一 800px 压缩（单张 40-150KB ≤300KB 上限）；字体 0 外部文件（系统字栈）；未做 Lighthouse 实测（无 Chrome 自动化环境），标注为交付后待测项 |

## 已知偏差（如实记录）

1. **流浪上报"地图选点"**：Web 端为演示模拟地图（点击按比例换算经纬度），小程序端优先调用微信 `chooseLocation`、失败退化演示选点——未接入高德/腾讯地图 Key（生产需申请并替换，接口字段不变）。
2. **资讯正文富文本图片**：wangEditor 编辑器内图片上传按钮关闭，图片仅通过"封面"承载（编辑器上传需 MinIO 直传代理，生产按需求开启）。
3. **D-15 Lighthouse 未实测**：本机无浏览器自动化运行环境，性能与无障碍评分留待部署后测量。
