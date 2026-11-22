<template>
  <view class="page">
    <!-- 分类 chips -->
    <view class="filter">
      <scroll-view scroll-x class="filter__scroll">
        <view class="filter__row">
          <view class="filter__chip" :class="{ active: !category }" @click="setCategory(null)"><text>全部</text></view>
          <view v-for="c in categories" :key="c.value" class="filter__chip" :class="{ active: category === c.value }" @click="setCategory(c.value)">
            <text>{{ c.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view
      v-for="a in articles"
      :key="a.id"
      class="article card"
      @click="goDetail(a.id)">
      <view class="article__text">
        <text class="article__title">{{ a.title }}</text>
        <text class="article__summary">{{ a.summary }}</text>
        <text class="article__meta num">{{ date(a.publishTime) }} · {{ a.viewCount }} 阅读 · {{ a.likeCount }} 赞</text>
      </view>
      <image class="article__cover" :src="a.cover" mode="aspectFill" />
    </view>

    <view v-if="loading && !articles.length">
      <pet-loading text="正在翻阅资讯…" />
    </view>

    <view v-if="!articles.length && !loading" class="empty">
      <text class="empty__title">暂无资讯</text>
    </view>
  </view>
</template>

<script>
import { pageArticles } from '../../common/api.js';

export default {
  data() {
    return {
      articles: [],
      category: null,
      page: 1,
      finished: false,
      loading: true,
      categories: [
        { value: 'RAISE', label: '饲养指导' },
        { value: 'KNOWLEDGE', label: '科普百科' },
        { value: 'NEWS', label: '平台动态' },
        { value: 'STORY', label: '救助故事' }
      ]
    };
  },
  onLoad(query) {
    if (query && query.category) {
      this.category = query.category;
    }
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
      this.loading = true;
      if (reset) {
        this.page = 1;
        this.finished = false;
      }
      try {
        const data = await pageArticles({
          pageNum: this.page,
          pageSize: 10,
          category: this.category
        });
        const list = (data && data.list) || [];
        this.articles = reset ? list : this.articles.concat(list);
        this.finished = this.articles.length >= (data.total || 0);
        this.page += 1;
      } catch (e) { /* */ } finally {
        this.loading = false;
      }
    },
    setCategory(c) {
      this.category = c;
      this.load(true);
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pagesUser/article-detail/article-detail?id=' + id });
    },
    date: (t) => (t ? String(t).slice(0, 10) : '')
  }
};
</script>

<style scoped>
.filter {
  padding: 16rpx 32rpx;
}

.filter__scroll {
  white-space: nowrap;
}

.filter__row {
  display: inline-flex;
  gap: 16rpx;
}

.filter__chip {
  display: inline-flex;
  align-items: center;
  height: 56rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #FFFFFF;
  border: 2rpx solid #E5E6EB;
  font-size: 26rpx;
  color: #4E5969;
  margin-right: 16rpx;
}

.filter__chip.active {
  background: #FFF1EB;
  border-color: #FF7A45;
  color: #B23A0E;
  font-weight: 500;
}

.article {
  display: flex;
  align-items: center;
}

.article__text {
  flex: 1;
  min-width: 0;
  margin-right: 20rpx;
}

.article__title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2733;
  line-height: 42rpx;
}

.article__summary {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #667080;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article__meta {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #A9B0BC;
}

.article__cover {
  width: 180rpx;
  height: 135rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  background: #F2F3F5;
}
</style>
