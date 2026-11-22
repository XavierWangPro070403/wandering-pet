<template>
  <view v-if="visible" class="pet-boot" :class="{ 'pet-boot--hide': hiding }">
    <!-- 动物园三小只：依次弹跳 -->
    <view class="pet-boot__zoo">
      <text class="pet-boot__pet pet-boot__pet--1">🐶</text>
      <text class="pet-boot__pet pet-boot__pet--2">🐱</text>
      <text class="pet-boot__pet pet-boot__pet--3">🐰</text>
    </view>
    <text class="pet-boot__brand">流浪宠物领养</text>
    <view class="pet-boot__dots">
      <view class="pet-boot__dot pet-boot__dot--1"></view>
      <view class="pet-boot__dot pet-boot__dot--2"></view>
      <view class="pet-boot__dot pet-boot__dot--3"></view>
    </view>
  </view>
</template>

<script>
const MIN_SHOW_MS = 1200; // 最短展示时长，保证动画可见
const FADE_MS = 400;      // 淡出过渡时长

/**
 * 启动加载屏：全屏覆盖，等首页数据就绪且达到最短展示时长后淡出。
 * 用法：<pet-boot-splash :ready="!loading" />（放在首页模板根部）
 */
export default {
  name: 'pet-boot-splash',
  props: {
    /** 外部资源是否就绪（如首页 loading 结束）；默认 true，仅按最短时长展示 */
    ready: { type: Boolean, default: true }
  },
  data() {
    return {
      visible: true,
      hiding: false
    };
  },
  created() {
    this._start = Date.now();
  },
  mounted() {
    this._timer = setTimeout(() => this.tryHide(), MIN_SHOW_MS);
  },
  watch: {
    ready(val) {
      if (val) this.tryHide();
    }
  },
  beforeDestroy() {
    clearTimeout(this._timer);
    clearTimeout(this._fadeTimer);
    clearTimeout(this._delayTimer);
  },
  methods: {
    tryHide() {
      if (!this.ready || this.hiding || !this.visible) return;
      const wait = Math.max(0, MIN_SHOW_MS - (Date.now() - this._start));
      this._delayTimer = setTimeout(() => {
        this.hiding = true;
        this._fadeTimer = setTimeout(() => { this.visible = false; }, FADE_MS);
      }, wait);
    }
  }
};
</script>

<style scoped>
.pet-boot {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #FFF6EF 0%, #FFFFFF 100%);
  opacity: 1;
  transition: opacity 0.35s ease;
}

.pet-boot--hide {
  opacity: 0;
}

.pet-boot__zoo {
  display: flex;
  align-items: flex-end;
  gap: 40rpx;
}

.pet-boot__pet {
  font-size: 96rpx;
  line-height: 1;
  animation: pet-boot-bounce 0.9s ease-in-out infinite;
}

.pet-boot__pet--2 {
  animation-delay: 0.15s;
}

.pet-boot__pet--3 {
  animation-delay: 0.3s;
}

.pet-boot__brand {
  margin-top: 36rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #1F2733;
  letter-spacing: 4rpx;
}

.pet-boot__dots {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 20rpx;
}

.pet-boot__dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #FF7A45;
  animation: pet-boot-dot 1.2s ease-in-out infinite;
}

.pet-boot__dot--2 {
  animation-delay: 0.2s;
}

.pet-boot__dot--3 {
  animation-delay: 0.4s;
}

@keyframes pet-boot-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20rpx); }
}

@keyframes pet-boot-dot {
  0%, 100% { opacity: 0.25; transform: translateY(0); }
  50% { opacity: 1; transform: translateY(-8rpx); }
}
</style>
