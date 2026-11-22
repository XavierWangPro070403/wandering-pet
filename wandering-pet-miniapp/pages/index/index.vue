<template>
  <view class="page">
    <!-- 启动加载屏：首页数据就绪且动画播足时长后淡出 -->
    <pet-boot-splash :ready="!loading" />
    <!-- 首屏：一张真实照片 + 一句主张 + 一个主行动 + 3 个快捷入口（§8.12，无轮播） -->
    <view class="hero">
      <image class="hero__img" src="http://localhost:8080/imgs/pet/p10.jpg" mode="aspectFill" />
      <view class="hero__scrim"></view>
      <view class="hero__body">
        <text class="hero__badge">🐾 杭州 · 成都 2 家救助站</text>
        <text class="hero__title">给流浪的它</text>
        <text class="hero__title hero__title--accent">一个家</text>
        <text class="hero__sub">待领养的猫狗已完成免疫与体检</text>
        <button class="hero__btn" @click="goAdopt">查看待领养</button>
      </view>
    </view>

    <!-- 快捷入口：极简剪影图标 -->
    <view class="quick card">
      <view class="quick__item tap" @click="go('/pagesUser/article-list/article-list')">
        <view class="quick__icon quick__icon--cat">
          <view class="q-ear q-ear--l"></view>
          <view class="q-ear q-ear--r"></view>
          <view class="q-face"></view>
          <view class="q-eye q-eye--l"></view>
          <view class="q-eye q-eye--r"></view>
        </view>
        <text class="quick__label">资讯科普</text>
        <text class="quick__desc">科学养宠小课堂</text>
      </view>
      <view class="quick__item tap" @click="go('/pagesUser/report/report')">
        <view class="quick__icon quick__icon--dog">
          <view class="q-ear q-ear--l"></view>
          <view class="q-ear q-ear--r"></view>
          <view class="q-face"></view>
          <view class="q-eye q-eye--l"></view>
          <view class="q-eye q-eye--r"></view>
          <view class="q-nose"></view>
        </view>
        <text class="quick__label">流浪上报</text>
        <text class="quick__desc">随手救它一命</text>
      </view>
      <view class="quick__item tap" @click="go('/pagesUser/donation/donation')">
        <view class="quick__icon quick__icon--heart">
          <view class="q-heart-l"></view>
          <view class="q-heart-r"></view>
          <view class="q-heart-body"></view>
        </view>
        <text class="quick__label">爱心捐赠</text>
        <text class="quick__desc">给毛孩子加餐</text>
      </view>
    </view>

    <!-- 平台真实数据 -->
    <view class="stats card">
      <view class="stats__item">
        <text class="stats__value num">{{ summary.availablePets || '—' }}</text>
        <text class="stats__label">在册待领养</text>
      </view>
      <view class="stats__item">
        <text class="stats__value num">{{ summary.adoptedPets || '—' }}</text>
        <text class="stats__label">累计领养</text>
      </view>
      <view class="stats__item">
        <text class="stats__value num">{{ summary.shelterCount || '—' }}</text>
        <text class="stats__label">入驻救助站</text>
      </view>
    </view>

    <!-- 推荐待领养 2 卡 -->
    <view class="section-head">
      <text class="section-head__title">🐾 推荐待领养</text>
      <text class="section-head__more" @click="goAdopt">全部</text>
    </view>
    <view v-if="loading">
      <pet-loading text="正在寻找小伙伴…" />
    </view>
    <view v-else>
      <view
        v-for="pet in pets"
        :key="pet.id"
        class="pet-card card"
        @click="go('/pagesUser/pet-detail/pet-detail?id=' + pet.id)">
        <image class="pet-card__img" :src="pet.coverImage" mode="widthFix" />
        <view class="pet-card__body">
          <view class="pet-card__row">
            <text class="pet-card__name">{{ pet.name }}</text>
            <text class="tag" :class="'tag--' + toneOf(pet.status)">{{ labelOf(pet.status) }}</text>
          </view>
          <text class="pet-card__meta">{{ pet.breedName || pet.categoryName }} · {{ genderOf(pet.gender) }} · {{ ageOf(pet.ageMonth) }} · {{ pet.city }}</text>
          <text class="pet-card__desc">{{ pet.characterDesc }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { pagePets } from '../../common/api.js';
import { portalSummaryUrl } from '../../common/config.js';

export default {
  data() {
    return {
      loading: true,
      pets: [],
      summary: {}
    };
  },
  onShow() {
    this.load();
  },
  onPullDownRefresh() {
    this.load().then(() => uni.stopPullDownRefresh());
  },
  methods: {
    async load() {
      this.loading = true;
      try {
        const [pets, summary] = await Promise.all([
          pagePets({ pageNum: 1, pageSize: 2 }),
          this.loadSummary()
        ]);
        this.pets = (pets && pets.list) || [];
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.loading = false;
      }
    },
    async loadSummary() {
      try {
        const res = await new Promise((resolve, reject) => {
          uni.request({
            url: portalSummaryUrl,
            method: 'GET',
            timeout: 10000,
            success: (res) => (res.data && res.data.code === 200 ? resolve(res.data.data) : reject()),
            fail: reject
          });
        });
        this.summary = res || {};
      } catch (e) {
        this.summary = {};
      }
    },
    go(url) {
      uni.navigateTo({ url });
    },
    goAdopt() {
      uni.switchTab({ url: '/pages/adopt/adopt' });
    },
    toneOf: (s) => ({ AVAILABLE: 'success', PENDING: 'primary', ADOPTED: 'success', MEDICAL: 'warning', OFFLINE: 'danger' }[s] || 'neutral'),
    labelOf: (s) => ({ AVAILABLE: '可领养', PENDING: '审核中', ADOPTED: '已领养', MEDICAL: '治疗中', OFFLINE: '已下架' }[s] || s),
    genderOf: (g) => ({ MALE: '公', FEMALE: '母' }[g] || '未知'),
    ageOf: (m) => {
      if (!m && m !== 0) return '';
      if (m < 12) return m + ' 个月';
      const y = Math.floor(m / 12);
      return m % 12 === 0 ? y + ' 岁' : y + ' 岁 ' + (m % 12) + ' 个月';
    }
  }
};
</script>

<style scoped>
/* ---------- 首屏 ---------- */
.hero {
  position: relative;
  height: 620rpx;
  overflow: hidden;
  border-radius: 0 0 40rpx 40rpx;
  background: #1F2733;
}

.hero__img {
  position: absolute;
  width: 100%;
  height: 100%;
}

.hero__scrim {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(31, 39, 51, 0.08) 0%, rgba(31, 39, 51, 0.30) 45%, rgba(31, 39, 51, 0.74) 100%);
}

.hero__body {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  /* 与页面主体 32rpx 内边距对齐，避免文字与下方卡片错位 */
  padding: 40rpx 32rpx 44rpx;
}

.hero__badge {
  display: inline-block;
  padding: 8rpx 22rpx;
  margin-bottom: 22rpx;
  border-radius: 999rpx;
  /* 实底品牌色块替代半透明描边，与按钮同色系，视觉更规整 */
  background: rgba(255, 122, 69, 0.92);
  font-size: 22rpx;
  color: #FFFFFF;
  letter-spacing: 1rpx;
}

.hero__title--accent {
  color: #FFC9A8;
}

.hero__title {
  display: block;
  font-size: 56rpx;
  line-height: 76rpx;
  font-weight: 600;
  color: #FFFFFF;
  letter-spacing: 4rpx;
  text-shadow: 0 4rpx 16rpx rgba(31, 39, 51, 0.35);
}

.hero__sub {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  line-height: 38rpx;
  color: rgba(255, 255, 255, 0.92);
  letter-spacing: 1rpx;
}

.hero__btn {
  display: inline-block;
  margin-top: 30rpx;
  width: auto;
  height: 84rpx;
  line-height: 84rpx;
  padding: 0 52rpx;
  border: none;
  border-radius: 42rpx;
  background: linear-gradient(135deg, #FF9A62, #FF7A45);
  box-shadow: 0 12rpx 28rpx rgba(255, 122, 69, 0.35);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.hero__btn::after {
  border: none;
}

.hero__btn:active {
  background: #F2672B;
}

/* ---------- 快捷入口 ---------- */
/* ---------- 快捷入口：极简剪影图标 ---------- */
.quick {
  display: flex;
  padding: 30rpx 12rpx;
}

.quick__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.quick__icon {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #FFF1EB;
}

.quick__item:active .quick__icon {
  transform: scale(0.92);
}

/* 剪影统一品牌橙 #FF7A45，细节留白 */
.q-face {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 56rpx;
  height: 52rpx;
  margin: -26rpx 0 0 -28rpx;
  border-radius: 50%;
  background: #FF7A45;
}

/* 猫：尖耳 */
.quick__icon--cat .q-ear {
  position: absolute;
  top: 14rpx;
  width: 22rpx;
  height: 22rpx;
  background: #FF7A45;
  border-radius: 5rpx;
}

.quick__icon--cat .q-ear--l { left: 22rpx; transform: rotate(20deg); }
.quick__icon--cat .q-ear--r { right: 22rpx; transform: rotate(-20deg); }

/* 狗：圆垂耳 + 白鼻点 */
.quick__icon--dog .q-ear {
  position: absolute;
  top: 16rpx;
  width: 22rpx;
  height: 34rpx;
  border-radius: 11rpx;
  background: #FF7A45;
}

.quick__icon--dog .q-ear--l { left: 14rpx; }
.quick__icon--dog .q-ear--r { right: 14rpx; }

.quick__icon--dog .q-nose {
  position: absolute;
  left: 50%;
  top: 52rpx;
  width: 12rpx;
  height: 10rpx;
  margin-left: -6rpx;
  border-radius: 50%;
  background: #FFF1EB;
}

.q-eye {
  position: absolute;
  top: 42rpx;
  width: 9rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #FFF1EB;
}

.q-eye--l { left: 36rpx; }
.q-eye--r { right: 36rpx; }

/* 爱心：双圆 + 旋转方块 */
.q-heart-body {
  position: absolute;
  left: 50%;
  top: 46rpx;
  width: 34rpx;
  height: 34rpx;
  margin-left: -17rpx;
  transform: rotate(45deg);
  border-radius: 8rpx;
  background: #FF7A45;
}

.q-heart-l,
.q-heart-r {
  position: absolute;
  top: 30rpx;
  width: 26rpx;
  height: 26rpx;
  border-radius: 50%;
  background: #FF7A45;
}

.q-heart-l { left: 25rpx; }
.q-heart-r { right: 25rpx; }

.quick__label {
  margin-top: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
}

.quick__desc {
  font-size: 22rpx;
  color: #A9B0BC;
}

/* ---------- 平台数据 ---------- *//* ---------- 平台数据 ---------- */
.stats {
  display: flex;
  padding: 28rpx 0;
}

.stats__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats__item + .stats__item {
  border-left: 2rpx solid #F0F1F4;
}

.stats__value {
  font-size: 40rpx;
  font-weight: 700;
  color: #FF7A45;
  font-variant-numeric: tabular-nums;
}

.stats__label {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #667080;
}

/* ---------- 宠物卡 ---------- */
.pet-card {
  padding: 0;
  overflow: hidden;
}

.pet-card__img {
  width: 100%;
  /* widthFix：完整展示宠物全貌不裁切，卡片高度随图片比例自适应 */
  height: auto;
  display: block;
  background: #F2F3F5;
}

.pet-card__body {
  padding: 28rpx;
}

.pet-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pet-card__name {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F2733;
}

.pet-card__meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #667080;
}

.pet-card__desc {
  margin-top: 10rpx;
  font-size: 26rpx;
  line-height: 40rpx;
  color: #4E5969;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
