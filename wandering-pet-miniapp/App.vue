<script>
/** 这些页面允许游客浏览，登录态失效时不要打断用户（只清状态 + 提示） */
const PUBLIC_TAB_ROUTES = ['pages/index/index', 'pages/adopt/adopt', 'pages/message/message', 'pages/mine/mine'];

export default {
  onLaunch() {
    // 登录态失效统一由请求封装发事件，这里同步清理 Vuex 并决定是否跳登录页
    uni.$on('auth:expired', this.handleAuthExpired);

    // 启动时静默恢复登录态（token 过期由请求封装统一处理 401）
    const token = uni.getStorageSync('wp_token');
    if (token) {
      this.$store.commit('setToken', token);
      this.$store.dispatch('loadMe');
    }
  },
  methods: {
    /**
     * 登录态失效：
     * - 同步清空 Vuex（store 里若残留 token，登录页会误显示成"绑定手机号"页面）
     * - 已在登录页则不重复跳；在 tab 页只提示、不打断
     */
    handleAuthExpired() {
      this.$store.commit('clear');
      const pages = getCurrentPages();
      const route = pages.length ? pages[pages.length - 1].route : '';
      if (route === 'pagesUser/login/login' || PUBLIC_TAB_ROUTES.indexOf(route) !== -1) return;
      uni.navigateTo({ url: '/pagesUser/login/login' });
    }
  }
};
</script>

<style>
/* 全局样式：与 Web 端设计规范同源（§8.12） */
page {
  background-color: #F7F8FA;
  color: #4E5969;
  font-size: 28rpx;
  line-height: 44rpx;
  font-family: -apple-system, "PingFang SC", "Noto Sans SC", "Microsoft YaHei", sans-serif;
}

/* 页面容器 */
.page {
  min-height: 100vh;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

/* 卡片：白底 24rpx 圆角，左右边距 32rpx */
.card {
  background: #FFFFFF;
  border-radius: 24rpx;
  margin: 0 32rpx 24rpx;
  padding: 24rpx;
  box-sizing: border-box;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2733;
  line-height: 44rpx;
}

/* 主按钮：88rpx 高、44rpx 圆角、主色实心 + 深墨字 */
.btn-primary {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  background: #FF7A45;
  color: #1F2733;
  font-size: 32rpx;
  font-weight: 500;
  text-align: center;
  border: none;
  margin: 0;
  padding: 0;
}

.btn-primary::after {
  border: none;
}

.btn-primary:active {
  transform: scale(0.98);
  background: #F2672B;
}

.btn-primary[disabled] {
  background: #F2F3F5;
  color: #A9B0BC;
}

/* 次按钮 */
.btn-plain {
  height: 88rpx;
  line-height: 84rpx;
  border-radius: 44rpx;
  background: #FFFFFF;
  color: #B23A0E;
  font-size: 32rpx;
  border: 2rpx solid #FF7A45;
  text-align: center;
}

.btn-plain::after {
  border: none;
}

.btn-plain:active {
  background: #FFF1EB;
}

/* 状态标签：颜色 + 文字（§8.2） */
.tag {
  display: inline-flex;
  align-items: center;
  height: 40rpx;
  padding: 0 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  line-height: 40rpx;
}

.tag::before {
  content: "";
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: currentColor;
  margin-right: 10rpx;
}

.tag--success { color: #0F6F63; background: #E6F7F4; }
.tag--primary { color: #B23A0E; background: #FFF1EB; }
.tag--warning { color: #8A5A00; background: #FFF7E6; }
.tag--danger { color: #B3122B; background: #FFECEF; }
.tag--neutral { color: #5A6472; background: #F2F3F5; }

/* 数字 tabular */
.num {
  font-variant-numeric: tabular-nums;
}

/* ---------- 版式：区块标题 / 分区 / 列表 ---------- */
.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 40rpx 32rpx 20rpx;
}

.section-head__title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F2733;
  line-height: 48rpx;
}

.section-head__desc {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #667080;
}

.section-head__more {
  font-size: 26rpx;
  color: #B23A0E;
  flex-shrink: 0;
}

/* 分组卡片：白底 + 分割线，替代一屏多个独立卡片 */
.group {
  margin: 0 32rpx 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.group__label {
  display: block;
  margin: 32rpx 32rpx 12rpx;
  font-size: 24rpx;
  color: #667080;
  letter-spacing: 1rpx;
}

/* 表单行：输入框 + 验证码按钮 */
.field-label {
  display: block;
  margin: 24rpx 0 12rpx;
  font-size: 24rpx;
  color: #667080;
}

.form-input {
  height: 88rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #1F2733;
}

/* 点击反馈：整卡可点时统一 0.99 缩放 */
.tap:active {
  transform: scale(0.99);
}

/* 骨架屏 */
.skeleton {
  background: linear-gradient(90deg, #F2F3F5 25%, #EDEFF2 37%, #F2F3F5 63%);
  background-size: 400% 100%;
  animation: wp-skeleton 1.4s ease infinite;
  border-radius: 8rpx;
}

@keyframes wp-skeleton {
  0% { background-position: 100% 50%; }
  100% { background-position: 0 50%; }
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 96rpx 0;
}

.empty__title {
  display: block;
  font-size: 28rpx;
  color: #4E5969;
  margin-bottom: 8rpx;
}

.empty__desc {
  display: block;
  font-size: 24rpx;
  color: #A9B0BC;
}
</style>
