<template>
  <view class="page">
    <!-- 统计卡 -->
    <view class="stats card" v-if="workbench">
      <view class="stats__row">
        <view class="stats__item">
          <text class="stats__num num">{{ workbench.petTotal }}</text>
          <text class="stats__label">在册宠物</text>
        </view>
        <view class="stats__item">
          <text class="stats__num num">{{ workbench.availableCount }}</text>
          <text class="stats__label">可领养</text>
        </view>
      </view>
      <view class="stats__row">
        <view class="stats__item">
          <text class="stats__num num" style="color:#8A5A00;">{{ workbench.pendingApplication }}</text>
          <text class="stats__label">待处理申请</text>
        </view>
        <view class="stats__item">
          <text class="stats__num num" style="color:#8A5A00;">{{ workbench.followUpPending }}</text>
          <text class="stats__label">待回访</text>
        </view>
      </view>
      <view class="stats__row">
        <view class="stats__item">
          <text class="stats__num num" style="color:#0F6F63;">{{ workbench.monthAdopted }}</text>
          <text class="stats__label">本月领养</text>
        </view>
        <view class="stats__item">
          <text class="stats__num num">{{ workbench.unreadMessage }}</text>
          <text class="stats__label">未读消息</text>
        </view>
      </view>
    </view>

    <view v-if="!workbench" class="card" style="margin-top:24rpx;">
      <view class="skeleton" style="height:200rpx;"></view>
    </view>

    <!-- 快捷操作 -->
    <view class="card">
      <text class="card-title">快捷处理</text>
      <button class="btn-primary" style="margin-top:20rpx;" @click="go('/pagesShelter/applications/applications')">处理领养申请</button>
      <text class="tip">宠物档案维护、回访登记等完整功能请在 Web 机构工作台操作。</text>
    </view>

    <!-- 最新申请预览 -->
    <view class="card">
      <text class="card-title">最新申请</text>
      <view class="app" v-for="app in apps" :key="app.id">
        <view class="app__row">
          <text class="app__name">{{ app.petName }} · {{ app.applicantName }}</text>
          <text class="tag" :class="'tag--' + tone(app.status)">{{ label(app.status) }}</text>
        </view>
        <text class="app__time num">{{ app.createTime }}</text>
      </view>
      <view v-if="!apps.length" class="empty"><text class="empty__title">暂无申请</text></view>
    </view>
  </view>
</template>

<script>
import { shelterWorkbench, shelterApplications } from '../../common/api.js';

export default {
  data() {
    return {
      workbench: null,
      apps: []
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
      try {
        this.workbench = await shelterWorkbench();
      } catch (e) { /* */ }
      try {
        const data = await shelterApplications({ pageNum: 1, pageSize: 5 });
        this.apps = (data && data.list) || [];
      } catch (e) { /* */ }
    },
    go(url) {
      uni.navigateTo({ url });
    },
    tone: (s) => ({
      SUBMITTED: 'warning', REVIEWING: 'primary', INTERVIEW: 'primary', HOME_VISIT: 'primary',
      APPROVED: 'success', SIGNED: 'success', ADOPTED: 'success', REJECTED: 'danger'
    }[s] || 'neutral'),
    label: (s) => ({
      SUBMITTED: '已提交', REVIEWING: '审核中', INTERVIEW: '待面谈', HOME_VISIT: '待家访',
      APPROVED: '审核通过', SIGNED: '已签约', ADOPTED: '领养完成', REJECTED: '已驳回'
    }[s] || s)
  }
};
</script>

<style scoped>
.stats {
  margin-top: 24rpx;
}

.stats__row {
  display: flex;
  padding: 16rpx 0;
}

.stats__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats__num {
  font-size: 44rpx;
  font-weight: 600;
  color: #1F2733;
}

.stats__label {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #667080;
}

.tip {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #A9B0BC;
  line-height: 36rpx;
}

.app {
  padding: 20rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.app:last-child {
  border-bottom: none;
}

.app__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app__name {
  font-size: 28rpx;
  color: #1F2733;
  font-weight: 500;
}

.app__time {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #A9B0BC;
}
</style>
