<template>
  <view class="page">
    <!-- 状态机横向步骤 -->
    <view class="flow card">
      <text class="card-title">领养进度</text>
      <view class="flow__steps">
        <view class="flow__step" v-for="(s, i) in flowSteps" :key="s.key">
          <view class="flow__dot" :class="stepClasses[i]">
            <text v-if="doneIndex > i || doneIndex === i && isRejected" >✓</text>
          </view>
          <text class="flow__label" :class="{ active: doneIndex === i && !isRejected }">{{ s.label }}</text>
          <view v-if="i < flowSteps.length - 1" class="flow__line" :class="{ done: doneIndex > i }"></view>
        </view>
      </view>
      <view class="flow__state" :class="'state--' + tone(app.status)">
        <text class="tag" :class="'tag--' + tone(app.status)">{{ label(app.status) }}</text>
        <text class="flow__state-text">{{ statusTip }}</text>
      </view>
    </view>

    <!-- 申请摘要 -->
    <view class="card">
      <text class="card-title">申请信息</text>
      <view class="row"><text class="row__label">申请编号</text><text class="row__value num">{{ app.applyNo }}</text></view>
      <view class="row"><text class="row__label">宠物</text><text class="row__value">{{ app.petName }}</text></view>
      <view class="row"><text class="row__label">受理机构</text><text class="row__value">{{ app.shelterName }}</text></view>
      <view class="row" v-if="app.interviewTime"><text class="row__label">面谈时间</text><text class="row__value num">{{ app.interviewTime }}</text></view>
      <view class="row" v-if="app.homeVisitTime"><text class="row__label">家访时间</text><text class="row__value num">{{ app.homeVisitTime }}</text></view>
      <view class="row" v-if="app.auditRemark"><text class="row__label">审核备注</text><text class="row__value">{{ app.auditRemark }}</text></view>
    </view>

    <!-- 流水 -->
    <view class="card">
      <text class="card-title">状态记录</text>
      <view class="log" v-for="(log, i) in app.flowLogs" :key="log.id">
        <view class="log__dot"></view>
        <view class="log__body">
          <view class="log__row">
            <text class="log__action">{{ transitionText(log) }}</text>
            <text class="log__time num">{{ log.createTime }}</text>
          </view>
          <text v-if="log.remark" class="log__remark">{{ log.remark }}</text>
        </view>
      </view>
    </view>

    <!-- 操作 -->
    <view class="footer">
      <button v-if="canCancel" class="btn-plain footer__cancel" @click="cancel">撤回申请</button>
      <button class="btn-primary footer__chat" @click="contactShelter">联系机构</button>
    </view>
  </view>
</template>

<script>
import { applicationDetail, cancelApplication, getOrCreateSession } from '../../common/api.js';

export default {
  data() {
    return {
      app: {},
      flowSteps: [
        { key: 'SUBMITTED', label: '已提交' },
        { key: 'REVIEWING', label: '审核中' },
        { key: 'INTERVIEW', label: '面谈' },
        { key: 'HOME_VISIT', label: '家访' },
        { key: 'APPROVED', label: '审核通过' },
        { key: 'SIGNED', label: '签约' },
        { key: 'ADOPTED', label: '领养完成' }
      ],
      order: ['DRAFT', 'SUBMITTED', 'REVIEWING', 'INTERVIEW', 'HOME_VISIT', 'APPROVED', 'SIGNED', 'ADOPTED']
    };
  },
  computed: {
    id() {
      return this.$root.$mp.query.id;
    },
    doneIndex() {
      const idx = this.order.indexOf(this.app.status);
      return idx < 0 ? 0 : idx;
    },
    isRejected() {
      return ['REJECTED', 'CANCELED', 'EXPIRED', 'RETURNED'].includes(this.app.status);
    },
    // 小程序端 :class 不支持函数调用，这里先算出每步的样式类
    stepClasses() {
      return this.flowSteps.map((s) => this.dotClass(s.key));
    },
    canCancel() {
      return ['SUBMITTED', 'REVIEWING'].includes(this.app.status);
    },
    statusTip() {
      return {
        SUBMITTED: '救助站将在 3 个工作日内完成初筛',
        REVIEWING: '机构已受理，请保持电话畅通',
        INTERVIEW: '已通过初筛，请按时到场面谈',
        HOME_VISIT: '面谈通过，等待家访安排',
        APPROVED: '审核通过，请按机构通知办理签约',
        SIGNED: '协议已签署，等待完成领养',
        ADOPTED: '领养完成，回访计划已生成',
        REJECTED: '很遗憾本次申请未通过，可继续浏览其他宠物',
        CANCELED: '申请已撤回',
        EXPIRED: '申请超过 7 天未推进已自动关闭',
        RETURNED: '该宠物已退养回到待领养状态'
      }[this.app.status] || '';
    }
  },
  onLoad() {
    this.load();
  },
  methods: {
    async load() {
      try {
        this.app = await applicationDetail(this.id);
      } catch (e) {
        uni.navigateBack();
      }
    },
    dotClass(key) {
      const idx = this.order.indexOf(key);
      if (this.isRejected) return idx <= this.doneIndex ? 'done' : '';
      return idx < this.doneIndex ? 'done' : idx === this.doneIndex ? 'current' : '';
    },
    transitionText(log) {
      const labels = {
        SUBMITTED: '提交申请', REVIEWING: '进入审核', INTERVIEW: '安排面谈', HOME_VISIT: '安排家访',
        APPROVED: '审核通过', SIGNED: '签署协议', ADOPTED: '领养完成', REJECTED: '申请被驳回',
        CANCELED: '申请撤回', EXPIRED: '申请过期关闭', RETURNED: '退养登记'
      };
      return labels[log.toStatus] || log.toStatus;
    },
    async cancel() {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({ title: '撤回申请', content: '确定撤回本次申请吗？', success: (r) => resolve(r.confirm) });
      });
      if (!confirmed) return;
      try {
        await cancelApplication(this.id);
        uni.showToast({ title: '已撤回', icon: 'none' });
        this.load();
      } catch (e) { /* */ }
    },
    async contactShelter() {
      try {
        const session = await getOrCreateSession(this.app.shelterId);
        uni.navigateTo({ url: '/pagesUser/chat/chat?sessionId=' + session.id });
      } catch (e) { /* */ }
    },
    tone: (s) => ({
      SUBMITTED: 'warning', REVIEWING: 'primary', INTERVIEW: 'primary', HOME_VISIT: 'primary',
      APPROVED: 'success', SIGNED: 'success', ADOPTED: 'success', REJECTED: 'danger',
      CANCELED: 'neutral', EXPIRED: 'neutral', RETURNED: 'danger'
    }[s] || 'neutral'),
    label: (s) => ({
      SUBMITTED: '已提交', REVIEWING: '审核中', INTERVIEW: '待面谈', HOME_VISIT: '待家访',
      APPROVED: '审核通过', SIGNED: '已签约', ADOPTED: '领养完成', REJECTED: '已驳回',
      CANCELED: '已撤回', EXPIRED: '已过期', RETURNED: '已退养'
    }[s] || s)
  }
};
</script>

<style scoped>
.flow__steps {
  display: flex;
  align-items: flex-start;
  margin-top: 24rpx;
}

.flow__step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.flow__dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #F2F3F5;
  color: #FFFFFF;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.flow__dot.done {
  background: #2BAE9B;
}

.flow__dot.current {
  background: #FF7A45;
  box-shadow: 0 0 0 8rpx #FFF1EB;
}

.flow__label {
  margin-top: 12rpx;
  font-size: 20rpx;
  color: #A9B0BC;
  text-align: center;
}

.flow__label.active {
  color: #B23A0E;
  font-weight: 500;
}

.flow__line {
  position: absolute;
  top: 20rpx;
  left: calc(50% + 24rpx);
  right: calc(-50% + 24rpx);
  height: 4rpx;
  background: #F2F3F5;
}

.flow__line.done {
  background: #2BAE9B;
}

.flow__state {
  margin-top: 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  padding: 20rpx;
}

.flow__state-text {
  font-size: 26rpx;
  color: #4E5969;
}

.row {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.row:last-child {
  border-bottom: none;
}

.row__label {
  width: 160rpx;
  font-size: 26rpx;
  color: #667080;
  flex-shrink: 0;
}

.row__value {
  flex: 1;
  font-size: 26rpx;
  color: #1F2733;
}

.log {
  position: relative;
  padding: 20rpx 0 20rpx 40rpx;
  border-left: 4rpx solid #F2F3F5;
  margin-left: 12rpx;
}

.log__dot {
  position: absolute;
  left: -10rpx;
  top: 32rpx;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #FF7A45;
}

.log__row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.log__action {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F2733;
}

.log__time {
  font-size: 22rpx;
  color: #A9B0BC;
}

.log__remark {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #667080;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  gap: 20rpx;
  padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  border-top: 2rpx solid #E5E6EB;
}

.footer__cancel {
  width: 240rpx;
}

.footer__chat {
  flex: 1;
}
</style>
