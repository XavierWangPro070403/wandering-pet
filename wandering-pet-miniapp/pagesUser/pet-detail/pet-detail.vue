<template>
  <view class="page">
    <!-- 沉浸式大图：widthFix 完整展示宠物全貌，点击可查看原图 -->
    <view class="hero">
      <image class="hero__img" :src="pet.coverImage" mode="widthFix" @click="previewHero" />
      <view class="hero__hint" v-if="pet.coverImage">
        <text>🐾 点击图片可查看原图</text>
      </view>
    </view>
    <view class="body">
      <view class="head card">
        <view class="head__row">
          <text class="head__name">{{ pet.name }}</text>
          <text class="tag" :class="'tag--' + tone(pet.status)">{{ label(pet.status) }}</text>
        </view>
        <text class="head__no num">档案编号 {{ pet.petNo }}</text>
        <view class="head__facts">
          <view class="fact"><text class="fact__label">品种</text><text class="fact__value">{{ pet.categoryName }}{{ pet.breedName ? ' · ' + pet.breedName : '' }}</text></view>
          <view class="fact"><text class="fact__label">性别</text><text class="fact__value">{{ gender(pet.gender) }}</text></view>
          <view class="fact"><text class="fact__label">月龄</text><text class="fact__value num">{{ age(pet.ageMonth) }}</text></view>
          <view class="fact"><text class="fact__label">体重</text><text class="fact__value num">{{ pet.weight ? pet.weight + 'kg' : '—' }}</text></view>
          <view class="fact"><text class="fact__label">体型</text><text class="fact__value">{{ sizeLabel(pet.size) }}</text></view>
          <view class="fact"><text class="fact__label">毛色</text><text class="fact__value">{{ pet.color || '—' }}</text></view>
        </view>
        <view class="head__flags">
          <text class="flag" :class="{ ok: pet.sterilized === 1 }">{{ pet.sterilized === 1 ? '已绝育' : '未绝育' }}</text>
          <text class="flag" :class="{ ok: pet.vaccinated === 1 }">{{ pet.vaccinated === 1 ? '已疫苗' : '未齐疫苗' }}</text>
          <text class="flag" :class="{ ok: pet.dewormed === 1 }">{{ pet.dewormed === 1 ? '已驱虫' : '未驱虫' }}</text>
          <text class="flag" :class="{ ok: pet.healthStatus === 'HEALTHY' }">{{ healthText(pet.healthStatus) }}</text>
        </view>
      </view>

      <!-- 相册 -->
      <view class="album card" v-if="album.length">
        <text class="card-title">相册</text>
        <scroll-view scroll-x class="album__scroll">
          <view class="album__row">
            <image
              v-for="(img, i) in album"
              :key="i"
              class="album__img"
              :class="{ current: i === current }"
              :src="img.url"
              mode="aspectFill"
              @click="current = i" />
          </view>
        </scroll-view>
      </view>

      <!-- 健康档案 -->
      <view class="card" v-if="pet.healthRecords && pet.healthRecords.length">
        <text class="card-title">健康档案</text>
        <view class="record" v-for="r in pet.healthRecords" :key="r.id">
          <view class="record__row">
            <text class="record__title">{{ r.title }}</text>
            <text class="record__date num">{{ r.recordDate }}</text>
          </view>
          <text class="record__content">{{ r.content }}</text>
        </view>
      </view>

      <!-- 性格与领养要求 -->
      <view class="card">
        <text class="card-title">性格与领养要求</text>
        <text class="para">{{ pet.characterDesc || '暂无性格记录' }}</text>
        <text class="para para--label">领养要求</text>
        <text class="para">{{ pet.adoptionRequirements || '以救助站面谈说明为准' }}</text>
      </view>

      <!-- 救助故事 -->
      <view class="card" v-if="pet.story">
        <text class="card-title">救助故事</text>
        <text class="para">{{ pet.story }}</text>
      </view>

      <view class="card">
        <text class="card-title">接收机构</text>
        <text class="para">{{ shelterName }} · {{ city }}</text>
      </view>
    </view>

    <!-- 底部行动条：收藏 + 申请领养 -->
    <view class="action-bar">
      <view class="action-bar__fav" :class="{ on: pet.favorited }" @click="toggleFavorite">
        <text>{{ pet.favorited ? '★' : '☆' }}</text>
        <text class="action-bar__fav-label">收藏</text>
      </view>
      <button class="btn-primary action-bar__btn" :disabled="!canApply" @click="goApply">
        {{ canApply ? '申请领养' : disabledText }}
      </button>
      <button v-if="canApply" class="action-bar__chat" @click="goChat">咨询</button>
    </view>
  </view>
</template>

<script>
import { petDetail, recordView, favorite, unfavorite, getOrCreateSession } from '../../common/api.js';

export default {
  data() {
    return {
      pet: {},
      current: 0
    };
  },
  computed: {
    id() {
      return this.$root.$mp.query.id;
    },
    album() {
      return (this.pet.images || []).filter((img) => img.url && img.url !== this.pet.coverImage).slice(0, 6);
    },
    shelterName() {
      return (this.pet.shelter && this.pet.shelter.name) || '救助站';
    },
    city() {
      const s = this.pet.shelter || {};
      return (s.city || '') + (s.district ? ' · ' + s.district : '');
    },
    canApply() {
      return this.pet.status === 'AVAILABLE';
    },
    disabledText() {
      return {
        PENDING: '审核中', ADOPTED: '已被领养', MEDICAL: '治疗中', OFFLINE: '已下架'
      }[this.pet.status] || '不可申请';
    },
    isLoggedIn() {
      return this.$store.getters.isLoggedIn;
    }
  },
  onLoad() {
    this.load();
  },
  methods: {
    async load() {
      try {
        this.pet = await petDetail(this.id);
        recordView(this.id).catch(() => {});
      } catch (e) {
        uni.showToast({ title: '宠物档案不存在', icon: 'none' });
        setTimeout(() => uni.navigateBack(), 800);
      }
    },
    /** 点击主图查看原图（全屏 1:1 比例，可左右滑动相册） */
    previewHero() {
      const urls = [this.pet.coverImage, ...this.album.map((img) => img.url)].filter(Boolean);
      if (!urls.length) return;
      uni.previewImage({ urls, current: this.pet.coverImage });
    },
    async toggleFavorite() {
      if (!this.isLoggedIn) {
        uni.navigateTo({ url: '/pagesUser/login/login' });
        return;
      }
      try {
        if (this.pet.favorited) {
          await unfavorite('PET', this.pet.id);
          this.pet.favorited = false;
          uni.showToast({ title: '已取消收藏', icon: 'none' });
        } else {
          await favorite('PET', this.pet.id);
          this.pet.favorited = true;
          uni.showToast({ title: '已收藏', icon: 'none' });
        }
      } catch (e) { /* */ }
    },
    async goChat() {
      if (!this.isLoggedIn) {
        uni.navigateTo({ url: '/pagesUser/login/login' });
        return;
      }
      try {
        const session = await getOrCreateSession(this.pet.shelterId);
        uni.navigateTo({ url: '/pagesUser/chat/chat?sessionId=' + session.id });
      } catch (e) { /* */ }
    },
    goApply() {
      if (!this.isLoggedIn) {
        uni.navigateTo({ url: '/pagesUser/login/login' });
        return;
      }
      uni.navigateTo({ url: '/pagesUser/apply/apply?petId=' + this.pet.id });
    },
    tone: (s) => ({ AVAILABLE: 'success', PENDING: 'primary', ADOPTED: 'success', MEDICAL: 'warning', OFFLINE: 'danger' }[s] || 'neutral'),
    label: (s) => ({ AVAILABLE: '可领养', PENDING: '审核中', ADOPTED: '已领养', MEDICAL: '治疗中', OFFLINE: '已下架' }[s] || s),
    gender: (g) => ({ MALE: '公', FEMALE: '母', UNKNOWN: '未知' }[g] || '未知'),
    age(m) {
      if (!m && m !== 0) return '';
      if (m < 12) return m + ' 个月';
      const y = Math.floor(m / 12);
      return m % 12 === 0 ? y + ' 岁' : y + ' 岁 ' + (m % 12) + ' 个月';
    },
    sizeLabel: (s) => ({ SMALL: '小型', MEDIUM: '中型', LARGE: '大型' }[s] || '—'),
    healthText: (s) => ({ HEALTHY: '健康', GENERAL: '健康一般', ILL: '治疗中' }[s] || s)
  }
};
</script>

<style scoped>
.hero {
  position: relative;
  background: #F2F3F5;
}

.hero__img {
  width: 100%;
  /* widthFix：高度随原图比例自适应，完整展示宠物全貌不裁切 */
  height: auto;
  display: block;
}

.hero__hint {
  position: absolute;
  right: 20rpx;
  bottom: 16rpx;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(31, 39, 51, 0.45);
  font-size: 22rpx;
  color: #FFFFFF;
}

.body {
  padding-top: 24rpx;
}

.head__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.head__name {
  font-size: 40rpx;
  font-weight: 600;
  color: #1F2733;
}

.head__no {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #A9B0BC;
}

.head__facts {
  margin-top: 24rpx;
  display: flex;
  flex-wrap: wrap;
}

.fact {
  width: 33.33%;
  display: flex;
  flex-direction: column;
  padding: 8rpx 0;
}

.fact__label {
  font-size: 22rpx;
  color: #667080;
}

.fact__value {
  margin-top: 4rpx;
  font-size: 28rpx;
  color: #1F2733;
}

.head__flags {
  margin-top: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.flag {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  background: #F2F3F5;
  color: #5A6472;
}

.flag.ok {
  background: #E6F7F4;
  color: #0F6F63;
}

.album__scroll {
  margin-top: 16rpx;
  white-space: nowrap;
}

.album__row {
  display: inline-flex;
  gap: 16rpx;
}

.album__img {
  width: 200rpx;
  height: 150rpx;
  border-radius: 16rpx;
  border: 4rpx solid transparent;
}

.album__img.current {
  border-color: #FF7A45;
}

.record {
  padding: 20rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.record:last-child {
  border-bottom: none;
}

.record__row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.record__title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F2733;
}

.record__date {
  font-size: 22rpx;
  color: #A9B0BC;
}

.record__content {
  display: block;
  margin-top: 6rpx;
  font-size: 26rpx;
  color: #4E5969;
}

.para {
  display: block;
  margin-top: 12rpx;
  font-size: 28rpx;
  line-height: 46rpx;
  color: #4E5969;
}

.para--label {
  margin-top: 24rpx;
  font-weight: 600;
  color: #1F2733;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 99;
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  border-top: 2rpx solid #E5E6EB;
}

.action-bar__fav {
  width: 110rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 34rpx;
  color: #667080;
}

.action-bar__fav.on {
  color: #B23A0E;
}

.action-bar__fav-label {
  font-size: 20rpx;
}

.action-bar__btn {
  flex: 1;
}

.action-bar__chat {
  width: 140rpx;
  height: 88rpx;
  line-height: 84rpx;
  border-radius: 44rpx;
  background: #FFFFFF;
  border: 2rpx solid #FF7A45;
  color: #B23A0E;
  font-size: 28rpx;
  padding: 0;
  margin: 0;
}

.action-bar__chat::after {
  border: none;
}
</style>
