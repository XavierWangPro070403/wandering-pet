# wandering-pet 前端开发简报（代理专用）

> 本文件是给页面开发代理的执行规范。所有页面必须严格遵守，不得另造视觉体系。
> 参考范例（必须先读）：`src/views/portal/Home.vue`、`src/views/portal/PetList.vue`、`src/views/portal/PetDetail.vue`、`src/layout/PortalLayout.vue`、`src/layout/AdminLayout.vue`、`src/components/*.vue`

## 技术约束
- Vue 2.6 选项式 API + Element UI 2.15 + SCSS。禁止 Vue3 语法、setup、TS。
- 样式必须 `@import '~@/styles/variables.scss';` 后只使用 token 变量（`$color-primary`、`$text-primary`、`$radius-card`、`$shadow-1`、`$space-*` 等，见 `src/styles/tokens.scss`）。禁止写死色值/字号/圆角。
- 容器一律 `.wrap`（PortalLayout 内）或工作台内容区直接写页面；工作台页面外层不需要 `.wrap`（AdminLayout 已有 24px 内边距）。
- API 一律走 `@/api/*` 已封装好的函数（函数清单见下）；禁止直接 axios。

## UI 硬规则（违反即返工）
1. 主色 #FF7A45；主按钮=橙底+深墨字（已全局覆盖，不要在页面里改按钮样式）；禁紫色/渐变/轮播/滚动淡入/深色大底。
2. 卡片只有一种形态：白底 16px 圆角 1px `$border-color` 边 `$shadow-1`；可点卡片 hover 上浮 2px（用 mixin `@include card; @include card-hover;`）。
3. 状态一律"颜色+文字"：用 `<StatusTag :status="..."/>`。
4. 每个可点元素有 hover 反馈；焦点环已全局处理，不要写 `outline: none`。
5. 文案：标题有名词、按钮=动词+对象（"申请领养/查看档案/提交线索"），禁止"立即体验/了解更多"；每屏正文 40–80 字；无感叹号。
6. 空态用 `<EmptyState>`；加载用 `el-skeleton`；错误给重试按钮。
7. 图片比例：卡片 4:3（`aspect-ratio: 4/3; object-fit: cover`），画廊主图 4:3，头像 1:1。所有图片必须能真实渲染（后端图片 URL 形如 `http://localhost:8080/imgs/pet/p01.jpg`，直接用接口返回的 URL）。
8. 工作台页（shelter/admin）：页面头 = h1(30/40) + 一句说明(14px) + 主行动按钮右对齐；筛选条一行 ≤4 控件 + 重置；表格用 el-table（全局已覆盖样式：行高 52、表头浅灰、无竖线）；状态列用 StatusTag；详情优先 480 抽屉；表单 label 顶部对齐；分页右对齐。
9. 时间格式 `yyyy-MM-dd HH:mm:ss`；金额 `¥ x.xx` 用 `money()`；数字加 `class="num"`（tabular-nums）。

## 已有全局资产（直接用，不要重复造）
- 组件：`StatusTag(status[,label])`、`PetCard(pet[,showStatus])`、`EmptyState(title,desc,actionText,@action)`、`ImageUploader(v-model=[{objectName,url}], bizType, limit)`、`LoginDialog(ref.open())`
- 工具：`@/utils/format`：`money/ageText/genderText/sizeText/petStatusText/applicationStatusText/followUpStatusText/reportStatusText/volunteerStatusText/articleCategoryText/maskPhone/parseTime`
- `@/utils/request` 已处理 401/403/错误提示；页面只需 `.then/.catch`。
- 指令：`v-permission="'pet:create'"`（按钮级权限）。
- store：`this.$store.state.user.userInfo/token`；字典 `this.$store.dispatch('dict/loadDicts')` + `this.$store.getters['dict/label']('pet_status', pet.status)`（可选）。
- 登录用户类型：`userInfo.userType`（ADMIN/SHELTER/USER）；机构 ID `userInfo.shelterId`。

## API 函数清单（src/api/*.js，全部已就绪）
- `pet.js`: pagePets(params), petDetail(id), similarPets(id,limit), recordView(id), petDynamics(id,limit), categories(), breeds(categoryId), favorite(type,id), unfavorite(type,id), myFavorites(params), favoriteStatus(type,id)
- `adoption.js`: submitApplication, myApplications(params), applicationDetail(id), flowLogs(id), cancelApplication(id), myRecords(), recordDetail(id)
- `shelter.js`: shelters(city), shelterDetail(id), applyShelter, myShelter, updateShelter, shelterPets(params), createPet, updatePet(id,data), deletePet(id), changePetStatus({status,ids}), healthRecords(petId), createHealthRecord(petId,data), updateHealthRecord(id,data), deleteHealthRecord(id), createDynamic(petId,data)
- `shelterBiz.js`: shelterApplications(params), acceptApplication(id), interviewApplication(id,{interviewTime,remark}), homeVisitApplication(id,{homeVisitTime,remark}), approveApplication(id,{remark}), rejectApplication(id,{remark}), signApplication(id,{signDate,agreementObjectName}), completeApplication(id), shelterRecords(params), returnRecord(id,{reason}), shelterFollowUps(params), remindFollowUp(id), archiveFollowUp(id), processReport(id,data), resolveReport(id,data), shelterVolunteers(params), auditVolunteer(id,{pass,remark}), shelterProjects(), createProject, updateProject(id,data), closeProject(id), shelterDonationRecords(params), shelterWorkbench()
- `article.js`: pageArticles(params), articleDetail(id), likeArticle(id), createComment({targetType,targetId,content}), adminArticles, saveArticle, updateArticle, deleteArticle, topArticle(id,top), changeArticleStatus(id,status), adminComments(params), auditComment(id,pass), deleteComment(id)
- `donation.js`: ongoingProjects(), projectDetail(id), donate(id,{amount,isAnonymous,message}), projectRecords(id,limit), projectRank(id,topN), adminDonationRecords(params)
- `rescue.js`: createReport(data), myReports(params), adminReports(params), assignReport(id,{handlerId,remark}), closeReport(id)
- `volunteer.js`: applyVolunteer(data), myVolunteerApplications(params)
- `user.js`: myProfile, updateProfile, realName, mySummary, addresses, saveAddress, updateAddress, deleteAddress
- `notification.js`: myNotifications(params), readNotification(id), readAllNotifications(), unreadCount()
- `chat.js`: getOrCreateSession(shelterId), mySessions(), sessionMessages(id,{pageNum,pageSize}), sendMessage(id,{contentType,content,objectUrl}), readSession(id), chatUnreadTotal()
- `statistics.js`: portalSummary(), adminDashboard()
- `system.js`: getDictAll, adminDictItems, createDictItem, updateDictItem, deleteDictItem, adminRoles, permissionTree, assignRolePerms(roleId,permIds), adminSysUsers, createSysUser, toggleSysUser, resetSysUserPassword, adminOperationLogs, adminShelters, auditShelter(id,{pass,remark}), adminUsers, toggleUser
- `file.js`: uploadFile(file, bizType)

## 后端返回约定
- 分页返回 `{ list, total, pageNum, pageSize, pages }`（request.js 已解包，直接拿 data）。
- ID 均为字符串（Long→String）；时间字符串；`favorited/liked` 布尔。
- 写操作成功后 `this.$message.success('...')` 并刷新列表。
