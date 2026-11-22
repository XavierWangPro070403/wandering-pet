<template>
  <view class="page">
    <!-- 积分余额 -->
    <view class="points card">
      <text class="points__label">当前积分</text>
      <text class="points__value num">{{ balance }}</text>
      <text class="points__rule">1 元消费 = 1 积分 · 积分可兑换优惠券，下单自动抵扣</text>
    </view>

    <!-- 领券中心 -->
    <text class="group__label">领券中心</text>
    <view class="coupon card" v-for="c in coupons" :key="c.id">
      <view class="coupon__left" :class="{ off: !c.canExchange }">
        <text class="coupon__discount num">¥{{ c.discount }}</text>
        <text class="coupon__condition">{{ Number(c.minSpend) > 0 ? '满 ' + c.minSpend + ' 可用' : '无门槛' }}</text>
      </view>
      <view class="coupon__body">
        <text class="coupon__name">{{ c.name }}</text>
        <text class="coupon__meta">需 {{ c.pointsCost }} 积分 · 已领 {{ c.receivedCount }}/{{ c.totalCount }}</text>
      </view>
      <button class="coupon__btn" :class="{ off: !c.canExchange }" :disabled="c.receivedCount >= c.totalCount" @click="exchange(c)">
        {{ c.receivedCount >= c.totalCount ? '已抢完' : c.canExchange ? '积分兑换' : '积分不足' }}
      </button>
    </view>
    <view v-if="!coupons.length" class="empty"><text>暂无可领优惠券</text></view>

    <!-- 我的优惠券 -->
    <text class="group__label">我的优惠券</text>
    <view class="mine card" v-for="c in myCoupons" :key="c.id">
      <view class="mine__main">
        <text class="mine__name">{{ c.couponName }}</text>
        <text class="mine__meta">¥{{ c.discount }} {{ Number(c.minSpend) > 0 ? '（满' + c.minSpend + '可用）' : '（无门槛）' }} · 有效期至 {{ (c.expireTime || '').replace('T', ' ').slice(0, 16) }}</text>
      </view>
      <text class="mine__status" :class="{ used: c.status === 'USED' }">{{ c.status === 'USED' ? '已使用' : '未使用' }}</text>
    </view>
    <view v-if="!myCoupons.length" class="empty"><text>还没有优惠券，去上面兑换吧</text></view>
  </view>
</template>

<script>
import { shopPoints, shopCoupons, shopExchangeCoupon, shopMyCoupons } from '../../common/api.js';

export default {
  data() {
    return {
      balance: 0,
      coupons: [],
      myCoupons: []
    };
  },
  onShow() {
    this.load();
  },
  methods: {
    async load() {
      try {
        const p = await shopPoints();
        this.balance = p.balance;
        this.coupons = (await shopCoupons()) || [];
        this.myCoupons = (await shopMyCoupons()) || [];
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async exchange(c) {
      try {
        await shopExchangeCoupon(c.id);
        uni.showToast({ title: '兑换成功', icon: 'success' });
        this.load();
      } catch (e) {
        /* 拦截器已提示（含积分不足提示） */
      }
    }
  }
};
</script>

<style scoped>
.page {
  padding: 24rpx 32rpx;
}

.points {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding: 36rpx 32rpx;
  background: linear-gradient(135deg, #FF9A62, #FF7A45);
}

.points__label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}

.points__value {
  font-size: 64rpx;
  font-weight: 700;
  color: #FFFFFF;
}

.points__rule {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
}

.group__label {
  display: block;
  margin: 30rpx 8rpx 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
}

.coupon {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx 28rpx;
  margin-bottom: 16rpx;
}

.coupon__left {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-right: 24rpx;
  border-right: 2rpx dashed #E5E6EB;
}

.coupon__discount {
  font-size: 40rpx;
  font-weight: 700;
  color: #FF7A45;
}

.coupon__condition {
  font-size: 20rpx;
  color: #A9B0BC;
}

.coupon__left.off .coupon__discount {
  color: #C0C4CC;
}

.coupon__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.coupon__name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
}

.coupon__meta {
  font-size: 22rpx;
  color: #A9B0BC;
}

.coupon__btn {
  margin: 0;
  width: 160rpx;
  height: 64rpx;
  line-height: 64rpx;
  padding: 0;
  border: none;
  border-radius: 999rpx;
  background: #FF7A45;
  color: #FFFFFF;
  font-size: 24rpx;
}

.coupon__btn.off {
  background: #E5E6EB;
  color: #A9B0BC;
}

.mine {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 24rpx 28rpx;
  margin-bottom: 16rpx;
}

.mine__main {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.mine__name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
}

.mine__meta {
  font-size: 22rpx;
  color: #A9B0BC;
}

.mine__status {
  font-size: 24rpx;
  color: #0F6F63;
}

.mine__status.used {
  color: #A9B0BC;
}

.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #A9B0BC;
  font-size: 24rpx;
}
</style>
