<template>
  <view class="page">
    <view
      v-for="app in apps"
      :key="app.id"
      class="app-item card"
      @click="goProgress(app)">
      <image class="app-item__img" :src="app.petCover" mode="aspectFill" />
      <view class="app-item__body">
        <view class="app-item__row">
          <text class="app-item__name">{{ app.petName }}</text>
          <text class="tag" :class="'tag--' + tone(app.status)">{{ label(app.status) }}</text>
        </view>
        <text class="app-item__no num">{{ app.applyNo }}</text>
        <text class="app-item__time num">申请于 {{ app.createTime }}</text>
        <view class="app-item__actions">
          <text v-if="canCancel(app.status)" class="app-item__cancel" @click.stop="cancel(app)">撤回申请</text>
          <text class="app-item__progress">查看进度 ›</text>
        </view>
      </view>
    </view>

    <view v-if="loading && !apps.length">
      <pet-loading text="正在查看申请…" />
    </view>

    <view v-if="!loading && !apps.length" class="empty" style="padding-top:160rpx;">
      <text class="empty__title">还没有领养申请</text>
      <text class="empty__desc">去领养列表看看想带谁回家</text>
      <button class="btn-primary" style="width:320rpx;margin:32rpx auto 0;" @click="goAdopt">浏览待领养</button>
    </view>
  </view>
</template>

<script>
import { myApplications, cancelApplication } from '../../common/api.js';

export default {
  data() {
    return {
      apps: [],
      page: 1,
      finished: false,
      loading: true
    };
  },
  onShow() {
    this.load(true);
  },
  onPullDownRefresh() {
    this.load(true).then(() => uni.stopPullDownRefresh());
  },
  onReachBottom() {
    if (!this.finished) this.load(false);
  },
  methods: {
    async load(reset) {
      if (reset) {
        this.page = 1;
        this.finished = false;
        this.loading = true;
      }
      try {
        const data = await myApplications({ pageNum: this.page, pageSize: 10 });
        const list = (data && data.list) || [];
        this.apps = reset ? list : this.apps.concat(list);
        this.finished = this.apps.length >= (data.total || 0);
        this.page += 1;
      } catch (e) { /* */ } finally {
        this.loading = false;
      }
    },
    async cancel(app) {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({
          title: '撤回申请',
          content: '撤回后该宠物的审核将终止，确定撤回吗？',
          success: (res) => resolve(res.confirm)
        });
      });
      if (!confirmed) return;
      try {
        await cancelApplication(app.id);
        uni.showToast({ title: '已撤回', icon: 'none' });
        this.load(true);
      } catch (e) { /* */ }
    },
    goProgress(app) {
      uni.navigateTo({ url: '/pagesUser/application-progress/application-progress?id=' + app.id });
    },
    goAdopt() {
      uni.switchTab({ url: '/pages/adopt/adopt' });
    },
    canCancel: (s) => s === 'SUBMITTED' || s === 'REVIEWING',
    tone: (s) => ({
      SUBMITTED: 'warning', REVIEWING: 'primary', INTERVIEW: 'primary', HOME_VISIT: 'primary',
      APPROVED: 'success', SIGNED: 'success', ADOPTED: 'success', REJECTED: 'danger',
      CANCELED: 'neutral', EXPIRED: 'neutral', RETURNED: 'danger'
    }[s] || 'neutral'),
    label: (s) => ({
      DRAFT: '草稿', SUBMITTED: '已提交', REVIEWING: '审核中', INTERVIEW: '待面谈',
      HOME_VISIT: '待家访', APPROVED: '审核通过', SIGNED: '已签约', ADOPTED: '领养完成',
      REJECTED: '已驳回', CANCELED: '已撤回', EXPIRED: '已过期', RETURNED: '已退养'
    }[s] || s)
  }
};
</script>

<style scoped>
.app-item {
  display: flex;
  margin-top: 24rpx;
}

.app-item__img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  background: #F2F3F5;
}

.app-item__body {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.app-item__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.app-item__name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2733;
}

.app-item__no {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #A9B0BC;
}

.app-item__time {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #A9B0BC;
}

.app-item__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
}

.app-item__cancel {
  font-size: 24rpx;
  color: #B3122B;
}

.app-item__progress {
  font-size: 24rpx;
  color: #B23A0E;
}
</style>
