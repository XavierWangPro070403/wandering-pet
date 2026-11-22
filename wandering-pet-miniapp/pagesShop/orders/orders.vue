<template>
  <view class="page">
    <!-- 状态 tabs -->
    <scroll-view scroll-x class="tabs">
      <view class="tabs__row">
        <view class="tabs__item" v-for="t in tabs" :key="t.value" :class="{ active: status === t.value }" @click="setStatus(t.value)">
          <text>{{ t.label }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="loading && page === 1">
      <view class="skeleton card" style="height:220rpx;" v-for="i in 3" :key="i"></view>
    </view>
    <view v-else class="list">
      <view class="order card" v-for="o in orders" :key="o.id">
        <view class="order__head">
          <text class="order__no num">{{ o.orderNo }}</text>
          <text class="order__status" :class="'s--' + o.status">{{ statusText(o.status) }}</text>
        </view>
        <view class="order__item" v-for="it in o.items" :key="it.id">
          <image class="order__img" :src="it.productCover" mode="aspectFill" />
          <view class="order__info">
            <text class="order__name">{{ it.productName }}</text>
            <text class="order__qty num">¥{{ it.price }} × {{ it.quantity }}</text>
          </view>
        </view>
        <view class="order__amounts">
          <text v-if="Number(o.discountAmount) > 0" class="order__discount">券抵扣 ¥{{ o.discountAmount }}</text>
          <text class="order__pay num">实付 ¥{{ o.payAmount }} <text class="order__points">（+{{ o.pointsEarned }} 积分）</text></text>
        </view>
        <view class="order__actions" v-if="o.status === 'PENDING_PAYMENT'">
          <button class="btn-plain order__btn" @click="cancel(o)">取消订单</button>
          <button class="btn-primary order__btn" @click="pay(o)">立即支付</button>
        </view>
      </view>
      <view v-if="!orders.length" class="empty"><text>暂无订单</text></view>
      <view v-if="finished && orders.length" class="list-end"><text>没有更多了</text></view>
    </view>
  </view>
</template>

<script>
import { shopOrders, shopPayOrder, shopCancelOrder } from '../../common/api.js';

export default {
  data() {
    return {
      tabs: [
        { value: null, label: '全部' },
        { value: 'PENDING_PAYMENT', label: '待支付' },
        { value: 'PAID', label: '已支付' },
        { value: 'SHIPPED', label: '已发货' },
        { value: 'CANCELLED', label: '已取消' }
      ],
      status: null,
      orders: [],
      page: 1,
      finished: false,
      loading: true
    };
  },
  onShow() {
    this.load(true);
  },
  onReachBottom() {
    if (!this.finished && !this.loading) this.load(false);
  },
  methods: {
    statusText(s) {
      return { PENDING_PAYMENT: '待支付', PAID: '已支付', SHIPPED: '已发货', CANCELLED: '已取消' }[s] || s;
    },
    setStatus(v) {
      this.status = v;
      this.load(true);
    },
    async load(reset) {
      this.loading = true;
      if (reset) {
        this.page = 1;
        this.finished = false;
      }
      try {
        const data = await shopOrders({ pageNum: this.page, pageSize: 10, status: this.status || undefined });
        const list = ((data && data.list) || []).map((o) => ({ ...o, items: o.items || [] }));
        this.orders = reset ? list : this.orders.concat(list);
        this.finished = this.orders.length >= (data.total || 0);
        this.page += 1;
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.loading = false;
      }
    },
    async pay(o) {
      try {
        const paid = await shopPayOrder(o.id);
        uni.showToast({ title: `支付成功 +${paid.pointsEarned} 积分`, icon: 'success' });
        this.load(true);
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async cancel(o) {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({
          title: '取消订单',
          content: '库存与优惠券将退回',
          success: (res) => resolve(res.confirm)
        });
      });
      if (!confirmed) return;
      try {
        await shopCancelOrder(o.id);
        uni.showToast({ title: '已取消', icon: 'none' });
        this.load(true);
      } catch (e) {
        /* 拦截器已提示 */
      }
    }
  }
};
</script>

<style scoped>
.tabs {
  white-space: nowrap;
  padding: 20rpx 32rpx;
  background: #FFFFFF;
}

.tabs__row {
  display: inline-flex;
  gap: 28rpx;
}

.tabs__item {
  font-size: 28rpx;
  color: #4E5969;
  padding: 8rpx 4rpx;
}

.tabs__item.active {
  color: #FF7A45;
  font-weight: 600;
  border-bottom: 4rpx solid #FF7A45;
}

.list {
  padding: 24rpx 32rpx;
}

.order {
  margin-bottom: 24rpx;
  padding: 24rpx 28rpx;
}

.order__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #F2F3F5;
}

.order__no {
  font-size: 24rpx;
  color: #667080;
}

.order__status {
  font-size: 26rpx;
  font-weight: 600;
}

.s--PENDING_PAYMENT { color: #FF7A45; }
.s--PAID { color: #0F6F63; }
.s--SHIPPED { color: #1677FF; }
.s--CANCELLED { color: #A9B0BC; }

.order__item {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 0;
}

.order__img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 14rpx;
  background: #F2F3F5;
  flex-shrink: 0;
}

.order__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8rpx;
}

.order__name {
  font-size: 28rpx;
  color: #1F2733;
}

.order__qty {
  font-size: 24rpx;
  color: #A9B0BC;
}

.order__amounts {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  align-items: baseline;
  padding-top: 12rpx;
}

.order__discount {
  font-size: 24rpx;
  color: #0F6F63;
}

.order__pay {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
}

.order__points {
  font-size: 22rpx;
  font-weight: 400;
  color: #FF7A45;
}

.order__actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  padding-top: 16rpx;
}

.order__btn {
  margin: 0;
  width: 200rpx;
  height: 68rpx;
  line-height: 68rpx;
  font-size: 26rpx;
  padding: 0;
}

.empty {
  text-align: center;
  padding: 160rpx 0;
  color: #A9B0BC;
  font-size: 26rpx;
}

.list-end {
  text-align: center;
  padding: 24rpx;
  color: #A9B0BC;
  font-size: 24rpx;
}
</style>
