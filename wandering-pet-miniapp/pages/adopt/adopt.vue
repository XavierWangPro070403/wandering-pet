<template>
  <view class="page">
    <!-- 筛选 chips（物种 + 城市） -->
    <view class="filter">
      <scroll-view scroll-x class="filter__scroll">
        <view class="filter__row">
          <view class="filter__chip" :class="{ active: !categoryId }" @click="setCategory(null)"><text>全部</text></view>
          <view
            v-for="c in categories"
            :key="c.id"
            class="filter__chip"
            :class="{ active: categoryId === c.id }"
            @click="setCategory(c.id)">
            <text>{{ c.name }}</text>
          </view>
        </view>
      </scroll-view>
      <picker :range="cities" :value="cityIndex" @change="onCityChange">
        <view class="filter__city">
          <text>{{ cityIndex > 0 ? cities[cityIndex] : '全部城市' }}</text>
          <text class="filter__arrow">▾</text>
        </view>
      </picker>
    </view>

    <!-- 列表：单列 4:3 图 -->
    <view v-if="loading && page === 1">
      <pet-loading text="正在寻找小伙伴…" />
    </view>
    <view v-else>
      <view
        v-for="pet in pets"
        :key="pet.id"
        class="pet-item card"
        @click="goDetail(pet.id)">
        <image class="pet-item__img" :src="pet.coverImage" mode="widthFix" />
        <view class="pet-item__body">
          <view class="pet-item__row">
            <text class="pet-item__name">{{ pet.name }}</text>
            <text class="tag" :class="'tag--' + tone(pet.status)">{{ label(pet.status) }}</text>
          </view>
          <text class="pet-item__meta">{{ pet.breedName || pet.categoryName }} · {{ gender(pet.gender) }} · {{ age(pet.ageMonth) }} · {{ sizeLabel(pet.size) }}</text>
          <text class="pet-item__desc">{{ pet.characterDesc }}</text>
          <view class="pet-item__badges">
            <text v-if="pet.sterilized === 1" class="pet-item__badge">已绝育</text>
            <text v-if="pet.vaccinated === 1" class="pet-item__badge">已疫苗</text>
            <text v-if="pet.dewormed === 1" class="pet-item__badge">已驱虫</text>
          </view>
        </view>
      </view>
      <view v-if="!pets.length" class="empty">
        <text class="empty__title">没有符合条件的宠物</text>
        <text class="empty__desc">换个筛选条件试试</text>
      </view>
      <view v-if="finished && pets.length" class="list-end"><text>没有更多了</text></view>
    </view>
  </view>
</template>

<script>
import { pagePets, petCategories } from '../../common/api.js';

export default {
  data() {
    return {
      loading: true,
      pets: [],
      total: 0,
      page: 1,
      pageSize: 10,
      finished: false,
      categories: [],
      categoryId: null,
      cities: ['全部城市', '杭州', '成都'],
      cityIndex: 0
    };
  },
  onLoad() {
    this.loadCategories();
  },
  onShow() {
    // 每次进入/切回该 tab 都重新拉取，保证申请/撤回后宠物状态（可领养/审核中）即时刷新
    this.load(true);
  },
  onPullDownRefresh() {
    this.load(true).then(() => uni.stopPullDownRefresh());
  },
  onReachBottom() {
    if (!this.finished && !this.loading) {
      this.load(false);
    }
  },
  methods: {
    async loadCategories() {
      try {
        this.categories = (await petCategories()) || [];
      } catch (e) { /* */ }
    },
    async load(reset) {
      this.loading = true;
      if (reset) {
        this.page = 1;
        this.finished = false;
      }
      try {
        const data = await pagePets({
          pageNum: this.page,
          pageSize: this.pageSize,
          categoryId: this.categoryId,
          city: this.cityIndex > 0 ? this.cities[this.cityIndex] : null
        });
        const list = (data && data.list) || [];
        this.pets = reset ? list : this.pets.concat(list);
        this.total = data.total || 0;
        this.finished = this.pets.length >= this.total;
        this.page += 1;
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.loading = false;
      }
    },
    setCategory(id) {
      this.categoryId = id;
      this.load(true);
    },
    onCityChange(e) {
      this.cityIndex = Number(e.detail.value);
      this.load(true);
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pagesUser/pet-detail/pet-detail?id=' + id });
    },
    tone: (s) => ({ AVAILABLE: 'success', PENDING: 'primary', ADOPTED: 'success', MEDICAL: 'warning', OFFLINE: 'danger' }[s] || 'neutral'),
    label: (s) => ({ AVAILABLE: '可领养', PENDING: '审核中', ADOPTED: '已领养', MEDICAL: '治疗中', OFFLINE: '已下架' }[s] || s),
    gender: (g) => ({ MALE: '公', FEMALE: '母' }[g] || '未知'),
    age(m) {
      if (!m && m !== 0) return '';
      if (m < 12) return m + ' 个月';
      const y = Math.floor(m / 12);
      return m % 12 === 0 ? y + ' 岁' : y + ' 岁 ' + (m % 12) + ' 个月';
    },
    sizeLabel: (s) => ({ SMALL: '小型', MEDIUM: '中型', LARGE: '大型' }[s] || '')
  }
};
</script>

<style scoped>
/* 筛选栏贴顶：滚动列表时条件始终可见 */
.filter {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  padding: 20rpx 32rpx;
  background: rgba(247, 248, 250, 0.96);
  border-bottom: 2rpx solid #EDEFF2;
}

.filter__scroll {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
}

/* 隐藏横向滚动条（H5 生效，小程序端无副作用） */
.filter__scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

.filter__row {
  display: inline-flex;
  gap: 16rpx;
  padding-right: 8rpx;
}

.filter__chip {
  display: inline-flex;
  align-items: center;
  height: 64rpx;
  padding: 0 28rpx;
  flex-shrink: 0;
  border-radius: 999rpx;
  background: #FFFFFF;
  border: 2rpx solid #E5E6EB;
  font-size: 26rpx;
  color: #4E5969;
  transition: background-color 150ms ease, color 150ms ease, border-color 150ms ease;
}

.filter__chip.active {
  background: #FF7A45;
  border-color: #FF7A45;
  color: #1F2733;
  font-weight: 600;
}

.filter__city {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  height: 64rpx;
  padding: 0 22rpx;
  margin-left: 16rpx;
  background: #FFFFFF;
  border: 2rpx solid #E5E6EB;
  border-radius: 999rpx;
  font-size: 26rpx;
  color: #4E5969;
  white-space: nowrap;
}

.filter__arrow {
  margin-left: 8rpx;
  color: #A9B0BC;
  font-size: 20rpx;
}

/* 宠物卡：4:3 大图 + 信息区 */
.pet-item {
  padding: 0;
  overflow: hidden;
}

.pet-item__img {
  width: 100%;
  /* widthFix：宽度铺满、高度随图片比例自适应，完整展示宠物全貌不裁切 */
  height: auto;
  display: block;
  background: #F2F3F5;
}

.pet-item__body {
  padding: 28rpx;
}

.pet-item__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pet-item__name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2733;
}

.pet-item__meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #667080;
}

.pet-item__desc {
  margin-top: 10rpx;
  font-size: 26rpx;
  line-height: 40rpx;
  color: #4E5969;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.pet-item__badges {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
}

.pet-item__badge {
  font-size: 22rpx;
  color: #0F6F63;
  background: #E6F7F4;
  border-radius: 8rpx;
  padding: 6rpx 14rpx;
}

.list-end {
  text-align: center;
  padding: 40rpx;
  color: #A9B0BC;
  font-size: 24rpx;
}
</style>


