<template>
  <div class="page">
    <!-- 积分条 -->
    <div class="points-bar" v-if="loggedIn">
      <span class="points-bar__paw">🐾</span>
      <span>我的积分</span>
      <strong class="num">{{ points }}</strong>
      <span class="points-bar__rule">1 元消费 = 1 积分</span>
      <el-button size="small" round @click="couponVisible = true">领券中心</el-button>
    </div>

    <!-- 分类 + 搜索 -->
    <div class="filter">
      <el-radio-group v-model="category" size="small" @change="load(true)">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button v-for="c in categories" :key="c.value" :label="c.value">{{ c.label }}</el-radio-button>
      </el-radio-group>
      <el-input v-model="keyword" size="small" placeholder="搜索商品" class="filter__search" clearable @clear="load(true)" @keyup.enter.native="load(true)">
        <i slot="prefix" class="el-icon-search"></i>
      </el-input>
    </div>

    <!-- 商品网格 -->
    <div v-if="loading" class="grid">
      <el-skeleton v-for="i in 8" :key="i" :rows="4" animated class="grid__skeleton" />
    </div>
    <div v-else class="grid">
      <div class="product-card" v-for="p in products" :key="p.id" @click="openProduct(p)">
        <div class="product-card__media"><img :src="p.coverImage" :alt="p.name" loading="lazy" /></div>
        <div class="product-card__body">
          <h3>{{ p.name }}</h3>
          <p class="product-card__desc">{{ p.description }}</p>
          <div class="product-card__foot">
            <span class="num price">¥{{ p.price }}</span>
            <span class="sales">已售 {{ p.salesCount }}</span>
          </div>
        </div>
      </div>
      <div v-if="!products.length" class="empty"><el-empty description="没有符合条件的商品" /></div>
    </div>

    <!-- 商品详情 + 下单弹层 -->
    <el-dialog :visible.sync="detailVisible" width="720px" top="6vh" :show-close="true">
      <div class="detail" v-if="detail.id">
        <div class="detail__media"><img :src="detail.coverImage" :alt="detail.name" /></div>
        <h2 class="detail__name">{{ detail.name }}</h2>
        <div class="detail__price-row">
          <span class="num detail__price">¥{{ detail.price }}</span>
          <span class="sales">已售 {{ detail.salesCount }} · 库存 {{ detail.stock }}</span>
        </div>
        <p class="detail__desc">{{ detail.description }}</p>

        <el-divider></el-divider>

        <div class="detail__form" v-if="loggedIn">
          <div class="detail__row">
            <span class="detail__label">数量</span>
            <el-input-number v-model="form.quantity" :min="1" :max="Math.max(detail.stock, 1)" size="small"></el-input-number>
          </div>
          <div class="detail__row">
            <span class="detail__label">收货地址</span>
            <el-select v-model="form.addressId" placeholder="选择收货地址" style="width: 380px" no-data-text="暂无地址，请在「我的-收货地址」添加">
              <el-option v-for="a in addresses" :key="a.id" :value="a.id"
                :label="`${a.receiver} ${a.phone} ${a.province}${a.city}${a.district} ${a.detail}`"></el-option>
            </el-select>
          </div>
          <div class="detail__row">
            <span class="detail__label">优惠券</span>
            <el-select v-model="form.couponId" placeholder="不使用优惠券" style="width: 380px" clearable>
              <el-option v-for="c in usableCoupons" :key="c.id" :value="c.id"
                :label="`${c.couponName} 抵¥${c.discount}`"></el-option>
            </el-select>
          </div>
          <div class="detail__row detail__row--amounts">
            <span>商品总额 <strong class="num">¥{{ totalAmount }}</strong></span>
            <span v-if="discount > 0" class="discount">券抵扣 <strong class="num">-¥{{ discount }}</strong></span>
            <span>实付 <strong class="num pay">¥{{ payAmount }}</strong><span class="points-tip">（支付后 +{{ Math.floor(payAmount) }} 积分）</span></span>
          </div>
          <el-button type="primary" round :loading="submitting" @click="submitOrder">提交订单并支付</el-button>
          <p class="detail__points-tip">支付成功按实付金额发放积分（1 元 = 1 积分），可在「领券中心」用积分兑换优惠券。</p>
        </div>
        <div v-else class="detail__login">
          <el-button type="primary" round @click="$router.push('/login?redirect=' + encodeURIComponent('/shop'))">登录后购买</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 领券中心弹层 -->
    <el-dialog :visible.sync="couponVisible" width="560px" title="领券中心（积分兑换）">
      <div class="coupon-panel">
        <div class="coupon-panel__balance">当前积分 <strong class="num">{{ points }}</strong></div>
        <div class="coupon-item" v-for="c in coupons" :key="c.id">
          <div class="coupon-item__left">
            <strong class="num">¥{{ c.discount }}</strong>
            <span>{{ Number(c.minSpend) > 0 ? '满' + c.minSpend + '可用' : '无门槛' }}</span>
          </div>
          <div class="coupon-item__body">
            <strong>{{ c.name }}</strong>
            <span>需 {{ c.pointsCost }} 积分 · 已领 {{ c.receivedCount }}/{{ c.totalCount }}</span>
          </div>
          <el-button size="small" type="primary" round :disabled="!c.canExchange || c.receivedCount >= c.totalCount" @click="exchange(c)">
            {{ c.receivedCount >= c.totalCount ? '已抢完' : c.canExchange ? '积分兑换' : '积分不足' }}
          </el-button>
        </div>
        <el-divider></el-divider>
        <h4>我的优惠券</h4>
        <div class="coupon-item mine" v-for="c in myCoupons" :key="c.id">
          <div class="coupon-item__left"><strong class="num">¥{{ c.discount }}</strong><span>{{ Number(c.minSpend) > 0 ? '满' + c.minSpend + '可用' : '无门槛' }}</span></div>
          <div class="coupon-item__body"><strong>{{ c.couponName }}</strong><span>有效期至 {{ (c.expireTime || '').replace('T', ' ').slice(0, 16) }}</span></div>
          <el-tag size="small" :type="c.status === 'UNUSED' ? 'success' : 'info'">{{ c.status === 'UNUSED' ? '未使用' : '已使用' }}</el-tag>
        </div>
        <p class="coupon-panel__tip">购买时在订单里选择优惠券自动抵扣；支付成功按实付金额发放积分（1 元 = 1 积分）。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { shopProducts, shopProductDetail, shopCoupons, shopExchangeCoupon, shopMyCoupons, shopPoints, shopCreateOrder, shopPayOrder } from '@/api/shop';
import { addresses as myAddresses } from '@/api/user';

export default {
  name: 'ShopPage',
  data() {
    return {
      categories: [
        { value: 'FOOD', label: '粮食' },
        { value: 'TOY', label: '玩具' },
        { value: 'CLEAN', label: '清洁' },
        { value: 'CARE', label: '护理' },
        { value: 'OTHER', label: '其他' }
      ],
      category: '',
      keyword: '',
      loading: false,
      products: [],
      points: 0,
      detailVisible: false,
      detail: {},
      form: { quantity: 1, addressId: null, couponId: null },
      addresses: [],
      myCoupons: [],
      submitting: false,
      couponVisible: false
    };
  },
  computed: {
    loggedIn() {
      return !!this.$store.state.user.token;
    },
    totalAmount() {
      return (Number(this.detail.price || 0) * this.form.quantity).toFixed(2);
    },
    selectedCoupon() {
      return this.myCoupons.find((c) => String(c.id) === String(this.form.couponId)) || null;
    },
    discount() {
      if (!this.selectedCoupon) return 0;
      return Math.min(Number(this.selectedCoupon.discount), Number(this.totalAmount));
    },
    payAmount() {
      return (Number(this.totalAmount) - this.discount).toFixed(2);
    },
    usableCoupons() {
      const total = Number(this.totalAmount);
      return this.myCoupons.filter((c) => c.status === 'UNUSED' && total >= Number(c.minSpend));
    }
  },
  mounted() {
    this.load(true);
    if (this.loggedIn) this.loadUserShopInfo();
  },
  methods: {
    async load(reset) {
      this.loading = true;
      try {
        const data = await shopProducts({ pageNum: 1, pageSize: 50, category: this.category || undefined, keyword: this.keyword || undefined });
        this.products = data.list || [];
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.loading = false;
      }
    },
    async loadUserShopInfo() {
      try {
        const p = await shopPoints();
        this.points = p.balance;
        this.myCoupons = (await shopMyCoupons()) || [];
        this.addresses = (await myAddresses()) || [];
        if (this.addresses.length) this.form.addressId = (this.addresses.find((a) => a.isDefault) || this.addresses[0]).id;
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async openProduct(p) {
      try {
        this.detail = await shopProductDetail(p.id);
        this.form.quantity = 1;
        this.form.couponId = null;
        if (this.loggedIn) {
          await this.loadUserShopInfo();
          const usable = this.myCoupons.filter((c) => Number(c.minSpend) <= Number(this.detail.price));
          this.form.couponId = usable.length ? usable[0].id : null;
        }
        this.detailVisible = true;
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async exchange(c) {
      try {
        await shopExchangeCoupon(c.id);
        this.$message.success('兑换成功');
        await this.loadUserShopInfo();
        this.myCoupons = (await shopMyCoupons()) || [];
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async submitOrder() {
      if (!this.form.addressId) {
        this.$message.warning('请选择收货地址（可在「我的-收货地址」添加）');
        return;
      }
      this.submitting = true;
      try {
        const order = await shopCreateOrder({
          productId: this.detail.id,
          quantity: this.form.quantity,
          addressId: this.form.addressId,
          couponId: this.form.couponId || undefined
        });
        // 演示环境：提交后立即模拟支付（接微信支付后替换为真实支付）
        const paid = await shopPayOrder(order.id);
        this.$message.success(`支付成功！获得 ${paid.pointsEarned} 积分`);
        this.detailVisible = false;
        await this.loadUserShopInfo();
        this.load(true);
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped lang="scss">
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.points-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  margin-bottom: 20px;
  border-radius: $radius-base;
  background: linear-gradient(135deg, #FFF6EF, #FFFFFF);
  border: 1px solid #FFE0CE;

  &__paw {
    font-size: 20px;
  }

  strong {
    color: $color-primary-text;
    font-size: 18px;
  }

  &__rule {
    color: $text-secondary;
    font-size: 13px;
    margin-right: auto;
  }
}

.filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;

  &__search {
    width: 240px;
  }
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 991px) { grid-template-columns: repeat(3, 1fr); }
  @media (max-width: 767px) { grid-template-columns: repeat(2, 1fr); }

  &__skeleton { border-radius: $radius-base; }
}

.product-card {
  @include card;
  @include card-hover;

  overflow: hidden;
  cursor: pointer;

  &__media {
    aspect-ratio: 4 / 3;
    min-height: 0;
    background: $bg-page;
    position: relative;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    // 售罄胶囊标签（左上角，与整体浅色胶囊风格一致）
    .product-card__soldout {
      position: absolute;
      left: 10px;
      top: 10px;
      padding: 4px 11px;
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.92);
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 1px;
      color: #86909C;
    }
  }

  &.is-soldout &__media img,
  &.is-soldout &__media .product-card__soldout {
    filter: grayscale(1);
  }

  &.is-soldout &__media img {
    opacity: 0.55;
  }

  &__body {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;

    h3 {
      font-size: 15px;
      color: $text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &__desc {
    font-size: 12px;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__foot {
    display: flex;
    justify-content: space-between;
    align-items: baseline;

    .price {
      font-size: 20px;
      font-weight: 700;
      color: $color-primary-text;
    }

    .sales {
      font-size: 12px;
      color: $text-secondary;
    }
  }
}

.empty {
  grid-column: 1 / -1;
}

.detail {
  &__media {
    aspect-ratio: 16 / 10;
    min-height: 0;
    border-radius: $radius-base;
    overflow: hidden;
    background: $bg-page;

    img { width: 100%; height: 100%; object-fit: cover; display: block; }
  }

  &__name { margin: 16px 0 8px; font-size: 20px; }

  &__price-row {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
  }

  &__price {
    font-size: 26px;
    font-weight: 700;
    color: $color-primary-text;
  }

  .sales { font-size: 12px; color: $text-secondary; }

  &__desc {
    margin-top: 8px;
    font-size: 14px;
    line-height: 22px;
    color: $text-regular;
  }

  &__form {
    display: flex;
    flex-direction: column;
    gap: 14px;
    margin-top: 6px;
  }

  &__row {
    display: flex;
    align-items: center;
    gap: 14px;

    &--amounts {
      justify-content: flex-end;
      gap: 18px;
      color: $text-regular;

      .pay { color: $color-primary-text; font-size: 18px; }
      .points-tip { font-size: 12px; color: $text-secondary; }
    }

    .detail__label {
      width: 70px;
      color: $text-secondary;
      font-size: 13px;
      flex-shrink: 0;
    }
  }

  .discount { color: #0F6F63; }

  &__soldout-tip {
    margin-top: 8px;
    font-size: 13px;
    color: #B3122B;
  }

  &__login { text-align: center; padding: 12px 0; }
}

// 富文本兼容：SCSS 嵌套写法由 vue-loader 编译
.coupon-panel {
  &__balance {
    margin-bottom: 14px;
    strong { color: $color-primary-text; font-size: 20px; }
  }

  .coupon-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px;
    border: 1px solid $border-color;
    border-radius: $radius-base;
    margin-bottom: 10px;

    &.mine { background: $bg-page; }

    &__left {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding-right: 14px;
      border-right: 1px dashed $border-color;

      strong { color: $color-primary-text; font-size: 18px; }
      span { font-size: 12px; color: $text-secondary; }
    }

    &__body {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;

      strong { font-size: 14px; color: $text-primary; }
      span { font-size: 12px; color: $text-secondary; }
    }
  }

  &__tip {
    font-size: 12px;
    color: $text-secondary;
  }
}

h4 { color: $text-primary; margin: 8px 0; }
</style>
