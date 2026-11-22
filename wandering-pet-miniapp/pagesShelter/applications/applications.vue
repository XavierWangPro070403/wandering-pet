<template>
  <view class="page">
    <!-- 状态筛选 -->
    <view class="filter">
      <view
        v-for="s in statusFilters"
        :key="s.value"
        class="filter__chip"
        :class="{ active: status === s.value }"
        @click="setStatus(s.value)">
        <text>{{ s.label }}</text>
      </view>
    </view>

    <view
      v-for="app in apps"
      :key="app.id"
      class="app card">
      <view class="app__head">
        <text class="app__name">{{ app.petName }} · {{ app.applicantName }}</text>
        <text class="tag" :class="'tag--' + tone(app.status)">{{ label(app.status) }}</text>
      </view>
      <text class="app__meta num">{{ app.applyNo }} · {{ app.city }} · {{ app.createTime }}</text>
      <view class="app__actions">
        <button
          v-if="app.status === 'SUBMITTED'"
          class="app__btn app__btn--primary"
          @click="accept(app)">受理</button>
        <button
          v-if="app.status === 'REVIEWING'"
          class="app__btn app__btn--primary"
          @click="interview(app)">安排面谈</button>
        <button
          v-if="['REVIEWING', 'INTERVIEW', 'HOME_VISIT'].includes(app.status)"
          class="app__btn app__btn--danger"
          @click="reject(app)">驳回</button>
        <button class="app__btn app__btn--plain" @click="callPhone">电话联系</button>
      </view>
    </view>

    <view v-if="!apps.length" class="empty">
      <text class="empty__title">暂无申请</text>
      <text class="empty__desc">完整审核流程请使用 Web 机构工作台</text>
    </view>
  </view>
</template>

<script>
import { shelterApplications, acceptApplication, interviewApplication, rejectApplication } from '../../common/api.js';

export default {
  data() {
    return {
      apps: [],
      status: '',
      statusFilters: [
        { value: '', label: '全部' },
        { value: 'SUBMITTED', label: '已提交' },
        { value: 'REVIEWING', label: '审核中' },
        { value: 'INTERVIEW', label: '待面谈' },
        { value: 'APPROVED', label: '审核通过' }
      ]
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
        const data = await shelterApplications({ pageNum: 1, pageSize: 20, status: this.status || undefined });
        this.apps = (data && data.list) || [];
      } catch (e) { /* */ }
    },
    setStatus(v) {
      this.status = v;
      this.load();
    },
    async accept(app) {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({ title: '受理申请', content: '受理后进入资料审核，确定受理吗？', success: (r) => resolve(r.confirm) });
      });
      if (!confirmed) return;
      try {
        await acceptApplication(app.id);
        uni.showToast({ title: '已受理', icon: 'none' });
        this.load();
      } catch (e) { /* */ }
    },
    interview(app) {
      uni.showModal({
        title: '安排面谈',
        editable: true,
        placeholderText: '面谈时间，如 2026-11-20 14:00',
        success: async (res) => {
          if (!res.confirm || !res.content) return;
          try {
            await interviewApplication(app.id, { interviewTime: res.content.trim().replace(/ /g, 'T') });
            uni.showToast({ title: '面谈已安排', icon: 'none' });
            this.load();
          } catch (e) { /* */ }
        }
      });
    },
    async reject(app) {
      uni.showModal({
        title: '驳回申请',
        editable: true,
        placeholderText: '请填写驳回原因（必填）',
        success: async (res) => {
          if (!res.confirm || !res.content) return;
          try {
            await rejectApplication(app.id, { remark: res.content.trim() });
            uni.showToast({ title: '已驳回', icon: 'none' });
            this.load();
          } catch (e) { /* */ }
        }
      });
    },
    callPhone() {
      uni.makePhoneCall({ phoneNumber: '13800000003' });
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
.filter {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 32rpx;
  flex-wrap: wrap;
}

.filter__chip {
  height: 56rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #FFFFFF;
  border: 2rpx solid #E5E6EB;
  font-size: 26rpx;
  color: #4E5969;
  display: flex;
  align-items: center;
}

.filter__chip.active {
  background: #FFF1EB;
  border-color: #FF7A45;
  color: #B23A0E;
  font-weight: 500;
}

.app__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app__name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2733;
}

.app__meta {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #A9B0BC;
}

.app__actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}

.app__btn {
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  padding: 0 28rpx;
  margin: 0;
  border: none;
}

.app__btn::after {
  border: none;
}

.app__btn--primary {
  background: #FF7A45;
  color: #1F2733;
}

.app__btn--danger {
  background: #FFECEF;
  color: #B3122B;
}

.app__btn--plain {
  background: #F7F8FA;
  color: #4E5969;
}
</style>
