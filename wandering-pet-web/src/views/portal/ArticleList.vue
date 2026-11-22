<template>
  <div class="article-list">
    <div class="wrap">
      <header class="page-head">
        <h1>资讯科普</h1>
        <p>饲养指导、领养知识与救助动态，帮新领养人少走弯路。</p>
      </header>

      <nav class="chip-row" aria-label="资讯分类">
        <button
          v-for="c in categories"
          :key="c.value"
          class="chip"
          :class="{ active: query.category === c.value }"
          @click="setCategory(c.value)">
          {{ c.label }}
        </button>
      </nav>

      <div v-if="loading" class="cards">
        <div v-for="i in 6" :key="i" class="card-skeleton">
          <el-skeleton :rows="4" animated />
        </div>
      </div>

      <div v-else-if="articles.length" class="cards">
        <article
          v-for="a in articles"
          :key="a.id"
          class="article-card"
          role="button"
          tabindex="0"
          @click="goDetail(a)"
          @keydown.enter="goDetail(a)">
          <div class="article-card__media">
            <img :src="a.cover" :alt="`文章封面：${a.title}`" loading="lazy" />
            <span v-if="a.isTop === 1" class="article-card__top">置顶</span>
          </div>
          <div class="article-card__body">
            <span class="article-card__category">{{ categoryText(a.category) }}</span>
            <h3 class="article-card__title">{{ a.title }}</h3>
            <p class="article-card__summary">{{ a.summary }}</p>
            <p class="article-card__meta num">
              {{ (a.publishTime || '').slice(0, 10) }} · {{ a.viewCount }} 次阅读
            </p>
          </div>
        </article>
      </div>

      <EmptyState
        v-else
        title="暂无相关资讯"
        desc="这个分类下还没有已发布的文章，可以先看看其他分类。"
        action-text="查看全部资讯"
        @action="setCategory('')" />

      <el-pagination
        v-if="total > query.pageSize"
        class="pagination"
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="query.pageSize"
        :current-page.sync="query.pageNum"
        @current-change="load" />
    </div>
  </div>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue';
import { pageArticles } from '@/api/article';

export default {
  name: 'ArticleListPage',
  components: { EmptyState },
  data() {
    return {
      loading: true,
      articles: [],
      total: 0,
      categories: [
        { value: '', label: '全部' },
        { value: 'RAISE', label: '饲养指导' },
        { value: 'KNOWLEDGE', label: '科普百科' },
        { value: 'NEWS', label: '平台动态' },
        { value: 'STORY', label: '救助故事' }
      ],
      query: {
        pageNum: 1,
        pageSize: 9,
        category: ''
      }
    };
  },
  watch: {
    '$route.query.category'(val) {
      const valid = this.categories.some((c) => c.value === val);
      const next = valid ? val : '';
      if (next !== this.query.category) {
        this.query.category = next;
        this.query.pageNum = 1;
        this.load();
      }
    }
  },
  async mounted() {
    const val = this.$route.query.category;
    if (this.categories.some((c) => c.value === val)) {
      this.query.category = val;
    }
    await this.load();
  },
  methods: {
    categoryText: (c) => ({
      RAISE: '饲养指导', KNOWLEDGE: '科普百科', NEWS: '平台动态', STORY: '救助故事'
    }[c] || c),
    async load() {
      this.loading = true;
      try {
        const params = { ...this.query };
        if (!params.category) delete params.category;
        const data = await pageArticles(params);
        this.articles = data.list || [];
        this.total = data.total || 0;
      } catch (e) {
        this.articles = [];
        this.total = 0;
      } finally {
        this.loading = false;
      }
    },
    setCategory(value) {
      if (this.query.category === value) return;
      this.query.category = value;
      this.query.pageNum = 1;
      this.$router.replace({ query: { ...this.$route.query, category: value || undefined } });
      this.load();
    },
    goDetail(a) {
      this.$router.push(`/articles/${a.id}`);
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.wrap {
  @include wrap;
}

.article-list {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
  padding: 40px 0 80px;
}

.page-head {
  margin-bottom: 24px;

  h1 {
    font-size: $font-h3;
    line-height: $lh-h3;
  }

  p {
    margin-top: 8px;
    font-size: 14px;
    color: $text-secondary;
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.chip {
  height: 32px;
  padding: 0 12px;
  border-radius: $radius-base;
  border: 1px solid $border-color;
  background: $bg-card;
  color: $text-regular;
  font-size: 13px;
  cursor: pointer;
  transition: all $duration-hover-in $ease-out-quart;

  &:hover {
    border-color: $color-primary;
    color: $color-primary-text;
  }

  &.active {
    background: $color-primary-light;
    border-color: $color-primary;
    color: $color-primary-text;
    font-weight: 500;
  }
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;

  @media (max-width: 1199px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 640px) { grid-template-columns: 1fr; }
}

.card-skeleton {
  @include card;
  padding: 16px;
  min-height: 280px;
}

.article-card {
  @include card;
  @include card-hover;
  @include img-zoom;

  border-radius: $radius-card;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;

  &:focus-visible {
    @include focus-ring;
  }

  &__media {
    position: relative;
    aspect-ratio: 4 / 3;
    // 同 PetCard：防止 height:100% 子图按自然比例撑破比例框
    min-height: 0;
    background: $bg-page;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__top {
    position: absolute;
    top: 12px;
    left: 12px;
    padding: 2px 8px;
    border-radius: $radius-small;
    background: $color-primary;
    color: $text-primary;
    font-size: 12px;
    line-height: 20px;
  }

  &__body {
    padding: 16px 20px 20px;
    display: flex;
    flex-direction: column;
    flex: 1;
  }

  &__category {
    align-self: flex-start;
    padding: 2px 8px;
    border-radius: $radius-small;
    background: $color-primary-light;
    color: $color-primary-text;
    font-size: 12px;
    line-height: 20px;
  }

  &__title {
    margin-top: 8px;
    font-size: 17px;
    line-height: 24px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__summary {
    margin-top: 8px;
    font-size: 13px;
    line-height: 22px;
    color: $text-regular;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__meta {
    margin-top: auto;
    padding-top: 12px;
    font-size: 13px;
    color: $text-secondary;
  }
}

.pagination {
  margin-top: 32px;
  text-align: right;
}
</style>
