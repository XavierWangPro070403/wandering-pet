<template>
  <view class="page">
    <!-- 积分条 -->
    <view class="points-bar card">
      <text class="points-bar__icon">🐾</text>
      <view class="points-bar__body">
        <text class="points-bar__label">我的积分</text>
        <text class="points-bar__value num">{{ points }}</text>
      </view>
      <text class="points-bar__tip">1 元消费 = 1 积分，可兑换优惠券</text>
      <view class="points-bar__btn" @click="go('/pagesShop/coupons/coupons')"><text>领券中心</text></view>
    </view>

    <!-- 分类筛选 -->
    <scroll-view scroll-x class="filter">
      <view class="filter__row">
        <view class="filter__chip" :class="{ active: !category }" @click="setCategory(null)"><text>全部</text></view>
        <view class="filter__chip" v-for="c in categories" :key="c.value" :class="{ active: category === c.value }" @click="setCategory(c.value)">
          <text>{{ c.label }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 商品网格 -->
    <view v-if="loading && page === 1">
      <view class="skeleton card" style="height:300rpx;" v-for="i in 4" :key="i"></view>
    </view>
    <view v-else class="grid">
      <view
        class="product card"
        v-for="p in products"
        :key="p.id"
        :class="{ 'is-soldout': isSoldout(p) }"
        @click="go('/pagesShop/product/product?id=' + p.id)">
        <view class="product__img-wrap">
          <image class="product__img" :src="p.coverImage" mode="aspectFill" />
          <view class="product__soldout" v-if="isSoldout(p)"><text>已售罄</text></view>
        </view>
        <view class="product__body">
          <text class="product__name">{{ p.name }}</text>
          <view class="product__row">
            <view class="product__price"><text class="product__price-sym">¥</text><text class="product__price-num num">{{ p.price }}</text></view>
            <text class="product__sales">已售 {{ p.salesCount }}</text>
          </view>
        </view>
      </view>
      <view v-if="!products.length" class="empty"><text>没有符合条件的商品</text></view>
      <view v-if="finished && products.length" class="list-end"><text>没有更多了</text></view>
    </view>
  </view>
</template>

<script>
import { shopProducts, shopPoints } from '../../common/api.js';

export default {
  data() {
    return {
      points: '—',
      products: [],
      categories: [
        { value: 'FOOD', label: '粮食' },
        { value: 'TOY', label: '玩具' },
        { value: 'CLEAN', label: '清洁' },
        { value: 'CARE', label: '护理' },
        { value: 'OTHER', label: '其他' }
      ],
      category: null,
      keyword: '',
      page: 1,
      finished: false,
      loading: true
    };
  },
  onShow() {
    this.loadPoints();
    this.load(true);
  },
  onReachBottom() {
    if (!this.finished && !this.loading) this.load(false);
  },
  onPullDownRefresh() {
    this.load(true).then(() => uni.stopPullDownRefresh());
  },
  methods: {
    go(url) {
      uni.navigateTo({ url });
    },
    /** 售罄判定：下架或库存为 0 */
    isSoldout(p) {
      return p.status === 'OFF_SHELF' || p.stock <= 0;
    },
    async loadPoints() {
      try {
        const p = await shopPoints();
        this.points = p.balance;
      } catch (e) {
        this.points = '—';
      }
    },
    async load(reset) {
      this.loading = true;
      if (reset) {
        this.page = 1;
        this.finished = false;
      }
      try {
        const data = await shopProducts({ pageNum: this.page, pageSize: 10, category: this.category || undefined, keyword: this.keyword || undefined });
        const list = (data && data.list) || [];
        this.products = reset ? list : this.products.concat(list);
        this.finished = this.products.length >= (data.total || 0);
        this.page += 1;
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.loading = false;
      }
    },
    setCategory(v) {
      this.category = v;
      this.load(true);
    }
  }
};
</script>

<style scoped>
.page {
  padding-bottom: 40rpx;
  background: #F7F8FA;
  min-height: 100vh;
}

/* 积分条 */
.points-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin: 24rpx 32rpx 0;
  padding: 24rpx 28rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #FF9A62, #FF7A45);
  color: #FFFFFF;
}

.points-bar__icon {
  font-size: 36rpx;
}

.points-bar__body {
  display: flex;
  flex-direction: column;
  gap: 2rpx;
}

.points-bar__label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
}

.points-bar__value {
  font-size: 34rpx;
  font-weight: 700;
  color: #FFFFFF;
}

.points-bar__tip {
  flex: 1;
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.85);
}

.points-bar__btn {
  padding: 12rpx 26rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.95);
  font-size: 24rpx;
  font-weight: 600;
  color: #B23A0E;
}

/* 分类筛选（吸顶） */
.filter {
  position: sticky;
  top: 0;
  z-index: 20;
  white-space: nowrap;
  padding: 20rpx 32rpx;
  background: #F7F8FA;
}

.filter__row {
  display: inline-flex;
  gap: 16rpx;
}

.filter__chip {
  padding: 12rpx 30rpx;
  border-radius: 999rpx;
  background: #FFFFFF;
  border: 2rpx solid #E5E6EB;
  font-size: 26rpx;
  color: #4E5969;
}

.filter__chip.active {
  background: #FF7A45;
  border-color: #FF7A45;
  color: #FFFFFF;
  font-weight: 600;
}

/* 商品网格 */
.grid {
  padding: 24rpx 32rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.product {
  /* 覆盖全局 .card 的 margin（左右 32rpx 外边距会把卡片挤成一行一个），
     间距改用下方 margin-bottom，列间距由 space-between 自动分配 */
  margin: 0 0 22rpx;
  width: calc(50% - 12rpx);
  padding: 0;
  overflow: hidden;
}

.product__img-wrap {
  position: relative;
  width: 100%;
  height: 320rpx;
}

.product__img {
  width: 100%;
  height: 320rpx;
  display: block;
  background: #F2F3F5;
}

/* 售罄：整卡灰显 + 角标 */
.product.is-soldout .product__img,
.product.is-soldout .product__name {
  filter: grayscale(1);
  opacity: 0.55;
}

.product__soldout {
  position: absolute;
  left: 16rpx;
  top: 16rpx;
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.92);
}

.product__soldout text {
  font-size: 22rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
  color: #86909C;
}

.product__body {
  padding: 20rpx 22rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.product__name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product__row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 4rpx;
}

.product__price {
  display: flex;
  align-items: baseline;
  color: #FF7A45;
}

.product__price-sym {
  font-size: 22rpx;
  font-weight: 600;
  margin-right: 2rpx;
}

.product__price-num {
  font-size: 34rpx;
  font-weight: 700;
}

.product__sales {
  font-size: 20rpx;
  color: #A9B0BC;
}

.empty {
  width: 100%;
  text-align: center;
  padding: 120rpx 0;
  color: #A9B0BC;
  font-size: 26rpx;
}

.list-end {
  width: 100%;
  text-align: center;
  padding: 30rpx;
  color: #A9B0BC;
  font-size: 24rpx;
}
</style></style>
