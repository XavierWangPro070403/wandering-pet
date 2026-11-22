<template>
  <view class="page" v-if="product.id">
    <image class="hero" :src="product.coverImage" mode="widthFix" />
    <view class="info card">
      <view class="price-row">
        <view class="price"><text class="price-sym">¥</text><text class="price-num num">{{ product.price }}</text></view>
        <text class="sales">已售 {{ product.salesCount }}</text>
      </view>
      <text class="name">{{ product.name }}</text>
      <text class="desc">{{ product.description }}</text>
    </view>
    <view class="params card">
      <view class="param"><text class="param__label">分类</text><text class="param__value">{{ categoryText(product.category) }}</text></view>
      <view class="param"><text class="param__label">库存</text><text class="param__value num">{{ product.stock }} 件</text></view>
      <view class="param"><text class="param__label">配送</text><text class="param__value">救助站统一打包发货</text></view>
    </view>

    <!-- 底部购买栏 -->
    <view class="buy-bar">
      <view class="buy-bar__points"><text>积分 {{ points }}</text></view>
      <button class="buy-bar__btn buy-bar__btn--off" :disabled="buyDisabled" @click="openCheckout">
        {{ buyText }}
      </button>
    </view>

    <!-- 确认下单弹层 -->
    <view class="mask" v-if="checkout" @click="checkout = false">
      <view class="sheet" @click.stop>
        <text class="sheet__title">确认订单</text>
        <view class="sheet__row">
          <text class="sheet__label">数量</text>
          <view class="stepper">
            <view class="stepper__btn" @click="qty > 1 && qty--"><text>－</text></view>
            <text class="stepper__num num">{{ qty }}</text>
            <view class="stepper__btn" @click="qty < product.stock ? qty++ : null"><text>＋</text></view>
          </view>
        </view>
        <!-- 收货地址：有默认地址直接用，没有就现场填写并保存到地址簿 -->
        <template v-if="address">
          <view class="sheet__row">
            <text class="sheet__label">收货人</text>
            <text class="sheet__value">{{ address.receiver }} {{ address.phone }}</text>
          </view>
          <view class="sheet__row">
            <text class="sheet__label">地址</text>
            <text class="sheet__value sheet__value--wrap">{{ address.receiverText }}</text>
          </view>
        </template>
        <template v-else>
          <view class="sheet__addr-title">填写收货地址（保存到地址簿）</view>
          <input class="sheet__input" v-model="newAddr.receiver" placeholder="收货人姓名" maxlength="32" />
          <input class="sheet__input" v-model="newAddr.phone" type="number" placeholder="联系电话" maxlength="11" />
          <input class="sheet__input" v-model="newAddr.addressText" placeholder="省 市 区 详细地址（空格分隔）" maxlength="120" />
        </template>
        <view class="sheet__row">
          <text class="sheet__label">优惠券</text>
          <picker :range="couponNames" @change="onCouponChange">
            <view class="sheet__value sheet__value--link"><text>{{ couponLabel }}</text></view>
          </picker>
        </view>
        <view class="sheet__row">
          <text class="sheet__label">合计</text>
          <text class="sheet__total num">¥{{ payAmount }}</text>
        </view>
        <text class="sheet__coupon-tip" v-if="couponIndex >= 0 && discount > 0">已抵扣 ¥{{ discount }}，支付后可获 {{ Math.floor(payAmount) }} 积分</text>
        <button class="sheet__submit" :loading="submitting" @click="submit">提交订单并支付</button>
      </view>
    </view>
  </view>
</template>

<script>
import { shopProductDetail, shopCreateOrder, shopPayOrder, shopPoints, shopMyCoupons, myAddresses, saveAddress } from '../../common/api.js';

export default {
  data() {
    return {
      product: {},
      points: 0,
      qty: 1,
      checkout: false,
      submitting: false,
      address: null,
      newAddr: { receiver: '', phone: '', addressText: '' },
      myCoupons: [],
      couponIndex: -1
    };
  },
  computed: {
    isLoggedIn() {
      return this.$store.getters.isLoggedIn;
    },
    discount() {
      const c = this.selectedCoupon;
      if (!c) return 0;
      const total = Number(this.product.price) * this.qty;
      return Math.min(Number(c.discount), total);
    },
    payAmount() {
      const total = Number(this.product.price) * this.qty;
      return (total - this.discount).toFixed(2);
    },
    /** 下架或售罄：禁用购买 */
    buyDisabled() {
      return this.product.status !== 'ON_SHELF' || this.product.stock <= 0;
    },
    buyText() {
      if (this.product.status !== 'ON_SHELF') return '已售罄';
      return this.product.stock > 0 ? '立即购买' : '已售罄';
    },
    selectedCoupon() {
      return this.couponIndex >= 0 ? this.myCoupons[this.couponIndex] : null;
    },
    couponNames() {
      return ['不使用优惠券', ...this.myCoupons.map((c) => `${c.couponName}（抵¥${c.discount}）`)];
    },
    couponLabel() {
      if (this.couponIndex < 0) return '不使用优惠券';
      const c = this.myCoupons[this.couponIndex];
      return `${c.couponName} 抵¥${c.discount}`;
    }
  },
  onLoad(query) {
    this.pid = query.id;
  },
  onShow() {
    this.load();
  },
  methods: {
    categoryText(c) {
      return { FOOD: '粮食', TOY: '玩具', CLEAN: '清洁', CARE: '护理', OTHER: '其他' }[c] || '其他';
    },
    async load() {
      try {
        this.product = await shopProductDetail(this.pid);
        try {
          const p = await shopPoints();
          this.points = p.balance;
        } catch (e) {
          this.points = 0;
        }
        if (this.isLoggedIn) {
          const list = (await myAddresses()) || [];
          this.address = list[0] || null;
          if (this.address) {
            this.address.receiverText = `${this.address.province}${this.address.city}${this.address.district} ${this.address.detail}`;
          }
          this.myCoupons = ((await shopMyCoupons({ status: 'UNUSED' })) || []).filter(
            (c) => Number(c.minSpend) <= Number(this.product.price) * this.qty
          );
          if (this.couponIndex >= this.myCoupons.length) this.couponIndex = -1;
        }
      } catch (e) {
        uni.showToast({ title: '商品不存在或已下架', icon: 'none' });
        setTimeout(() => uni.navigateBack(), 800);
      }
    },
    onCouponChange(e) {
      this.couponIndex = Number(e.detail.value) - 1;
    },
    openCheckout() {
      if (!this.isLoggedIn) {
        uni.navigateTo({ url: '/pagesUser/login/login' });
        return;
      }
      if (this.buyDisabled) {
        uni.showToast({ title: '该商品已售罄', icon: 'none' });
        return;
      }
      this.checkout = true;
    },
    /** 无地址时现场保存到地址簿 */
    async ensureAddress() {
      if (this.address) return this.address.id;
      const a = this.newAddr;
      if (!a.receiver || !/^1[3-9]\d{9}$/.test(a.phone) || !a.addressText) {
        uni.showToast({ title: '请完整填写收货信息', icon: 'none' });
        return null;
      }
      const parts = a.addressText.trim().split(/\s+/);
      const id = await saveAddress({
        receiver: a.receiver,
        phone: a.phone,
        province: parts[0] || '',
        city: parts[1] || '',
        district: parts[2] || '',
        detail: parts.slice(3).join(' ') || parts.join(' '),
        isDefault: true
      });
      this.address = { id, receiver: a.receiver, phone: a.phone, receiverText: a.addressText };
      return id;
    },
    async submit() {
      if (this.submitting) return;
      this.submitting = true;
      try {
        const addressId = await this.ensureAddress();
        if (!addressId) return;
        const order = await shopCreateOrder({
          productId: this.product.id,
          quantity: this.qty,
          addressId,
          couponId: this.selectedCoupon ? this.selectedCoupon.id : undefined
        });
        // 演示环境：提交后立即模拟支付（接微信支付后替换为真实支付流程）
        const paid = await shopPayOrder(order.id);
        uni.showToast({ title: `支付成功 +${paid.pointsEarned} 积分`, icon: 'success' });
        this.checkout = false;
        setTimeout(() => uni.navigateTo({ url: '/pagesShop/orders/orders' }), 900);
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.hero {
  width: 100%;
  display: block;
  background: #F2F3F5;
}

.info {
  padding: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.price {
  display: flex;
  align-items: baseline;
  color: #FF7A45;
}

.price-sym {
  font-size: 26rpx;
  font-weight: 600;
  margin-right: 4rpx;
}

.price-num {
  font-size: 44rpx;
  font-weight: 700;
}

.sales {
  font-size: 22rpx;
  color: #A9B0BC;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2733;
}

.desc {
  font-size: 26rpx;
  line-height: 40rpx;
  color: #4E5969;
}

.params {
  padding: 10rpx 28rpx;
}

.param {
  display: flex;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 2rpx solid #F2F3F5;
}

.param:last-child {
  border-bottom: none;
}

.param__label {
  font-size: 26rpx;
  color: #667080;
}

.param__value {
  font-size: 26rpx;
  color: #1F2733;
}

.buy-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 18rpx 32rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.97);
  border-top: 2rpx solid #EDEFF2;
}

.buy-bar__points {
  padding: 10rpx 22rpx;
  border-radius: 999rpx;
  background: #FFF1EB;
  font-size: 24rpx;
  color: #B23A0E;
}

.buy-bar__btn {
  flex: 1;
  margin: 0;
  height: 84rpx;
  line-height: 84rpx;
  border: none;
  border-radius: 42rpx;
  background: linear-gradient(135deg, #FF9A62, #FF7A45);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 600;
}

.buy-bar__btn--off,
.buy-bar__btn[disabled] {
  background: #C8C9CC !important;
  color: #FFFFFF !important;
}

.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 100;
}

.sheet {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.sheet__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2733;
  text-align: center;
}

.sheet__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}

.sheet__label {
  font-size: 26rpx;
  color: #667080;
  flex-shrink: 0;
}

.sheet__value {
  font-size: 26rpx;
  color: #1F2733;
  text-align: right;
}

.sheet__value--wrap {
  max-width: 440rpx;
  text-align: right;
}

.sheet__value--link {
  color: #FF7A45;
}

.sheet__total {
  font-size: 36rpx;
  font-weight: 700;
  color: #FF7A45;
}

.sheet__coupon-tip {
  font-size: 22rpx;
  color: #0F6F63;
  background: #E6F7F4;
  border-radius: 10rpx;
  padding: 12rpx 20rpx;
}

.sheet__addr-title {
  font-size: 24rpx;
  color: #B23A0E;
  background: #FFF6EF;
  border-radius: 10rpx;
  padding: 12rpx 20rpx;
}

.sheet__input {
  height: 76rpx;
  padding: 0 24rpx;
  border-radius: 12rpx;
  background: #F7F8FA;
  font-size: 26rpx;
  color: #1F2733;
}

.sheet__submit {
  margin-top: 8rpx;
  height: 84rpx;
  line-height: 84rpx;
  border: none;
  border-radius: 42rpx;
  background: linear-gradient(135deg, #FF9A62, #FF7A45);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 600;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.stepper__btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  background: #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: #4E5969;
}

.stepper__num {
  min-width: 60rpx;
  text-align: center;
  font-size: 30rpx;
  color: #1F2733;
}
</style>
