<template>
  <view class="page">
    <!-- 项目卡 -->
    <view v-for="p in projects" :key="p.id" class="project card">
      <image class="project__cover" :src="p.cover" mode="aspectFill" />
      <view class="project__body">
        <text class="project__title">{{ p.title }}</text>
        <text class="project__shelter">{{ p.shelterName }} · {{ p.shelterCity }}</text>
        <view class="project__bar"><view class="project__bar-inner" :style="{ width: Math.min(p.progress, 100) + '%' }"></view></view>
        <view class="project__row">
          <text class="project__raised num">已筹 {{ money(p.raisedAmount) }}</text>
          <text class="project__target num">目标 {{ money(p.targetAmount) }} · {{ p.donorCount }} 人</text>
        </view>
        <view class="project__actions">
          <text class="project__link" @click="toggle(p.id)">{{ expandedId === p.id ? '收起详情' : '项目详情' }}</text>
          <button class="project__donate" @click="openDonate(p)">捐款</button>
        </view>
      </view>
      <!-- 展开详情：说明 + 明细 + 捐赠榜 -->
      <view class="project__detail" v-if="expandedId === p.id">
        <text class="project__content">{{ p.content }}</text>
        <text class="project__sub">最近捐赠</text>
        <view class="record" v-for="r in (records[p.id] || [])" :key="r.id">
          <view class="record__row">
            <text class="record__name">{{ r.donorName }}</text>
            <text class="record__amount num">{{ money(r.amount) }}</text>
          </view>
          <text v-if="r.message" class="record__msg">“{{ r.message }}”</text>
          <text class="record__time num">{{ r.payTime }}</text>
        </view>
        <text class="project__sub">捐赠榜</text>
        <view class="rank" v-for="d in (ranks[p.id] || [])" :key="d.rank">
          <text class="rank__no num">{{ d.rank }}</text>
          <text class="rank__name">{{ d.donorName }}</text>
          <text class="rank__amount num">{{ money(d.totalAmount) }}</text>
        </view>
      </view>
    </view>

    <view v-if="!projects.length" class="empty" style="padding-top:160rpx;">
      <text class="empty__title">暂无进行中的募捐项目</text>
      <text class="empty__desc">新项目发起后会出现在这里</text>
    </view>

    <!-- 捐赠弹窗 -->
    <view class="mask" v-if="donating" @click="donating = false">
      <view class="dialog" @click.stop>
        <text class="dialog__title">捐赠「{{ donating.title }}」</text>
        <view class="dialog__amounts">
          <view v-for="a in [50, 100, 200, 500]" :key="a" class="dialog__chip" :class="{ active: amount === a }" @click="amount = a">
            <text class="num">¥{{ a }}</text>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">自定义金额</text>
          <input class="form-input num" type="digit" v-model="amountInput" placeholder="输入金额（元）" />
        </view>
        <view class="form-item">
          <text class="form-label">留言（选填）</text>
          <input class="form-input" v-model="message" maxlength="255" placeholder="给救助站带句话" />
        </view>
        <view class="dialog__anon" @click="anonymous = !anonymous">
          <view class="check" :class="{ on: anonymous }"><text v-if="anonymous">✓</text></view>
          <text>匿名展示（捐赠榜显示“匿名爱心人士”）</text>
        </view>
        <button class="btn-primary dialog__btn" :disabled="paying" @click="pay">
          {{ paying ? '支付中...' : '确认捐赠（模拟支付）' }}
        </button>
        <text class="dialog__tip">演示环境为模拟支付，不产生真实扣款；明细与公示数据均真实入库。</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ongoingProjects, donate } from '../../common/api.js';
import http from '../../common/request.js';

export default {
  data() {
    return {
      projects: [],
      expandedId: null,
      records: {},
      ranks: {},
      donating: null,
      amount: 100,
      amountInput: '',
      message: '',
      anonymous: false,
      paying: false
    };
  },
  onShow() {
    this.load();
  },
  methods: {
    async load() {
      try {
        this.projects = (await ongoingProjects()) || [];
      } catch (e) { /* */ }
    },
    async toggle(id) {
      if (this.expandedId === id) {
        this.expandedId = null;
        return;
      }
      this.expandedId = id;
      try {
        const [records, ranks] = await Promise.all([
          http.get('/donation-projects/' + id + '/records', { limit: 10 }, { showLoading: false }),
          http.get('/donation-projects/' + id + '/rank', { topN: 5 }, { showLoading: false })
        ]);
        this.records = Object.assign({}, this.records, { [id]: records || [] });
        this.ranks = Object.assign({}, this.ranks, { [id]: ranks || [] });
      } catch (e) { /* */ }
    },
    openDonate(p) {
      this.donating = p;
      this.amount = 100;
      this.amountInput = '';
      this.message = '';
      this.anonymous = false;
    },
    async pay() {
      const amount = this.amountInput ? Number(this.amountInput) : this.amount;
      if (!amount || amount <= 0) {
        uni.showToast({ title: '请输入捐赠金额', icon: 'none' });
        return;
      }
      this.paying = true;
      try {
        await donate(this.donating.id, {
          amount,
          isAnonymous: this.anonymous ? 1 : 0,
          message: this.message
        });
        this.donating = null;
        uni.showToast({ title: '捐赠成功（模拟支付）', icon: 'none' });
        this.load();
      } catch (e) { /* */ } finally {
        this.paying = false;
      }
    },
    money: (v) => {
      const n = Number(v || 0);
      return '¥ ' + n.toFixed(2);
    }
  }
};
</script>

<style scoped>
.project {
  padding: 0;
  overflow: hidden;
  margin-top: 24rpx;
}

.project__cover {
  width: 100%;
  height: 320rpx;
  display: block;
}

.project__body {
  padding: 24rpx;
}

.project__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2733;
}

.project__shelter {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #667080;
}

.project__bar {
  margin-top: 20rpx;
  height: 12rpx;
  border-radius: 6rpx;
  background: #F2F3F5;
  overflow: hidden;
}

.project__bar-inner {
  height: 100%;
  border-radius: 6rpx;
  background: #FF7A45;
}

.project__row {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;
}

.project__raised {
  font-size: 28rpx;
  color: #B23A0E;
  font-weight: 600;
}

.project__target {
  font-size: 22rpx;
  color: #A9B0BC;
}

.project__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
}

.project__link {
  font-size: 26rpx;
  color: #B23A0E;
}

.project__donate {
  width: 160rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
  background: #FF7A45;
  color: #1F2733;
  font-size: 26rpx;
  padding: 0;
  margin: 0;
  border: none;
}

.project__donate::after {
  border: none;
}

.project__detail {
  padding: 0 24rpx 24rpx;
  border-top: 2rpx solid #F7F8FA;
}

.project__content {
  display: block;
  padding-top: 20rpx;
  font-size: 26rpx;
  color: #4E5969;
  line-height: 42rpx;
}

.project__sub {
  display: block;
  margin-top: 24rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
}

.record {
  padding: 16rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.record__row {
  display: flex;
  justify-content: space-between;
}

.record__name {
  font-size: 26rpx;
  color: #1F2733;
}

.record__amount {
  font-size: 26rpx;
  color: #B23A0E;
}

.record__msg {
  display: block;
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #667080;
}

.record__time {
  display: block;
  margin-top: 4rpx;
  font-size: 20rpx;
  color: #A9B0BC;
}

.rank {
  display: flex;
  align-items: center;
  padding: 12rpx 0;
}

.rank__no {
  width: 48rpx;
  color: #A9B0BC;
  font-size: 26rpx;
}

.rank__name {
  flex: 1;
  font-size: 26rpx;
  color: #1F2733;
}

.rank__amount {
  font-size: 26rpx;
  color: #B23A0E;
}

.mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(31, 39, 51, 0.45);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.dialog {
  width: 100%;
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  padding: 40rpx 32rpx calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.dialog__title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F2733;
}

.dialog__amounts {
  display: flex;
  gap: 16rpx;
  margin: 28rpx 0;
}

.dialog__chip {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  background: #F7F8FA;
  color: #4E5969;
  font-size: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid transparent;
}

.dialog__chip.active {
  background: #FFF1EB;
  border-color: #FF7A45;
  color: #B23A0E;
  font-weight: 600;
}

.form-item {
  padding: 12rpx 0;
}

.form-label {
  display: block;
  font-size: 24rpx;
  color: #667080;
  margin-bottom: 10rpx;
}

.form-input {
  height: 80rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}

.dialog__anon {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 16rpx 0;
  font-size: 26rpx;
  color: #4E5969;
}

.check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 8rpx;
  border: 2rpx solid #A9B0BC;
  color: #FFFFFF;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check.on {
  background: #FF7A45;
  border-color: #FF7A45;
}

.dialog__btn {
  margin-top: 16rpx;
}

.dialog__tip {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  color: #A9B0BC;
  line-height: 34rpx;
  text-align: center;
}
</style>
