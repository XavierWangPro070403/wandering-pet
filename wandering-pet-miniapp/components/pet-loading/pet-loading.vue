<template>
  <view class="pet-loading">
    <!-- 动物园三小只：依次弹跳 -->
    <view class="pet-loading__zoo">
      <text class="pet-loading__pet pet-loading__pet--1">🐶</text>
      <text class="pet-loading__pet pet-loading__pet--2">🐱</text>
      <text class="pet-loading__pet pet-loading__pet--3">🐰</text>
    </view>
    <!-- 走路的小爪印 -->
    <view class="pet-loading__paws">
      <text class="pet-loading__paw pet-loading__paw--1">🐾</text>
      <text class="pet-loading__paw pet-loading__paw--2">🐾</text>
      <text class="pet-loading__paw pet-loading__paw--3">🐾</text>
    </view>
    <text class="pet-loading__text">{{ text }}</text>
  </view>
</template>

<script>
export default {
  name: 'pet-loading',
  props: {
    /** 提示文案，可按页面场景传入 */
    text: { type: String, default: '正在加载…' }
  }
};
</script>

<style scoped>
.pet-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140rpx 0;
}

.pet-loading__zoo {
  display: flex;
  align-items: flex-end;
  gap: 40rpx;
}

.pet-loading__pet {
  font-size: 76rpx;
  line-height: 1;
  animation: pet-loading-bounce 0.9s ease-in-out infinite;
}

.pet-loading__pet--2 {
  animation-delay: 0.15s;
}

.pet-loading__pet--3 {
  animation-delay: 0.3s;
}

.pet-loading__paws {
  display: flex;
  align-items: center;
  gap: 26rpx;
  margin-top: 26rpx;
}

.pet-loading__paw {
  font-size: 30rpx;
  line-height: 1;
  opacity: 0.2;
  animation: pet-loading-step 1.2s ease-in-out infinite;
}

.pet-loading__paw--2 {
  animation-delay: 0.2s;
}

.pet-loading__paw--3 {
  animation-delay: 0.4s;
}

.pet-loading__text {
  margin-top: 26rpx;
  font-size: 26rpx;
  color: #A9B0BC;
}

@keyframes pet-loading-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-18rpx); }
}

@keyframes pet-loading-step {
  0%, 100% { opacity: 0.2; transform: translateY(0) scale(0.9); }
  50% { opacity: 1; transform: translateY(-8rpx) scale(1.1); }
}
</style>
