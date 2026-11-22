<template>
  <div class="pet-list">
    <div class="wrap pet-list__layout">
      <!-- 左侧筛选（≤992 转抽屉） -->
      <aside class="filters" aria-label="筛选条件">
        <h2 class="filters__title">筛选</h2>

        <div class="filters__group">
          <p class="filters__label">物种</p>
          <div class="chip-row">
            <button class="chip" :class="{ active: !query.categoryId }" @click="setCategory(null)">全部</button>
            <button v-for="c in categories" :key="c.id" class="chip" :class="{ active: query.categoryId === c.id }" @click="setCategory(c.id)">
              {{ c.name }}
            </button>
          </div>
        </div>

        <div class="filters__group">
          <p class="filters__label">城市</p>
          <el-select v-model="query.city" placeholder="全部城市" clearable size="small" @change="search">
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
        </div>

        <div class="filters__group">
          <p class="filters__label">体型</p>
          <div class="chip-row">
            <button class="chip" :class="{ active: !query.size }" @click="setSize(null)">全部</button>
            <button v-for="s in sizes" :key="s.value" class="chip" :class="{ active: query.size === s.value }" @click="setSize(s.value)">
              {{ s.label }}
            </button>
          </div>
        </div>

        <div class="filters__group">
          <p class="filters__label">月龄</p>
          <div class="chip-row">
            <button v-for="r in ageRanges" :key="r.label" class="chip" :class="{ active: query.ageMin === r.min && query.ageMax === r.max }" @click="setAge(r)">
              {{ r.label }}
            </button>
          </div>
        </div>

        <div class="filters__group">
          <p class="filters__label">健康状况</p>
          <el-checkbox v-model="sterilizedOnly" border size="small" @change="search">仅看已绝育</el-checkbox>
        </div>

        <el-button type="text" class="filters__reset" @click="reset">重置筛选</el-button>
      </aside>

      <!-- 右侧列表 -->
      <div class="list-area">
        <div class="list-toolbar">
          <p class="list-toolbar__total num" aria-live="polite">共 {{ total }} 只待领养</p>
          <div class="list-toolbar__right">
            <el-button class="filters-toggle" size="small" @click="drawerVisible = true">
              <i class="el-icon-s-operation" aria-hidden="true"></i> 筛选
            </el-button>
            <el-select v-model="sortValue" size="small" @change="onSortChange">
              <el-option label="最新发布" value="publish_time:desc" />
              <el-option label="浏览最多" value="view_count:desc" />
              <el-option label="收藏最多" value="favorite_count:desc" />
              <el-option label="月龄最小" value="age_month:asc" />
            </el-select>
          </div>
        </div>

        <div v-if="loading" class="cards">
          <div v-for="i in 6" :key="i" class="card-skeleton">
            <el-skeleton :rows="4" animated />
          </div>
        </div>
        <div v-else-if="pets.length" class="cards">
          <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" />
        </div>
        <EmptyState
          v-else
          title="没有符合条件的宠物"
          desc="换个筛选条件，或先收藏感兴趣的物种，新档案上架后会出现在首页。"
          action-text="重置筛选"
          @action="reset" />

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

    <!-- 移动端筛选抽屉 -->
    <el-drawer title="筛选" :visible.sync="drawerVisible" size="320px" direction="ltr">
      <div class="drawer-filters">
        <div class="filters__group">
          <p class="filters__label">物种</p>
          <div class="chip-row">
            <button class="chip" :class="{ active: !query.categoryId }" @click="setCategory(null)">全部</button>
            <button v-for="c in categories" :key="c.id" class="chip" :class="{ active: query.categoryId === c.id }" @click="setCategory(c.id)">
              {{ c.name }}
            </button>
          </div>
        </div>
        <div class="filters__group">
          <p class="filters__label">城市</p>
          <el-select v-model="query.city" placeholder="全部城市" clearable size="small" @change="search">
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
        </div>
        <div class="filters__group">
          <p class="filters__label">体型</p>
          <div class="chip-row">
            <button class="chip" :class="{ active: !query.size }" @click="setSize(null)">全部</button>
            <button v-for="s in sizes" :key="s.value" class="chip" :class="{ active: query.size === s.value }" @click="setSize(s.value)">
              {{ s.label }}
            </button>
          </div>
        </div>
        <el-button type="primary" style="width:100%" @click="drawerVisible = false">查看结果</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import PetCard from '@/components/PetCard.vue';
import EmptyState from '@/components/EmptyState.vue';
import { pagePets, categories } from '@/api/pet';

export default {
  name: 'PetListPage',
  components: { PetCard, EmptyState },
  data() {
    return {
      loading: true,
      pets: [],
      total: 0,
      categories: [],
      cities: ['杭州', '成都'],
      sizes: [
        { value: 'SMALL', label: '小型' },
        { value: 'MEDIUM', label: '中型' },
        { value: 'LARGE', label: '大型' }
      ],
      ageRanges: [
        { label: '1 岁内', min: 0, max: 11 },
        { label: '1-3 岁', min: 12, max: 36 },
        { label: '3 岁以上', min: 37, max: 300 }
      ],
      sortValue: 'publish_time:desc',
      sterilizedOnly: false,
      drawerVisible: false,
      query: {
        pageNum: 1,
        pageSize: 9,
        categoryId: null,
        city: null,
        size: null,
        ageMin: null,
        ageMax: null
      }
    };
  },
  async mounted() {
    try {
      this.categories = await categories();
    } catch (e) { /* */ }
    await this.load();
  },
  methods: {
    async load() {
      this.loading = true;
      try {
        const params = { ...this.query };
        if (this.sterilizedOnly) params.keyword = '';
        const [field, dir] = this.sortValue.split(':');
        params.orderBy = field;
        params.orderDirection = dir;
        const data = await pagePets(params);
        this.pets = data.list || [];
        this.total = data.total || 0;
      } catch (e) {
        this.pets = [];
      } finally {
        this.loading = false;
      }
    },
    search() {
      this.query.pageNum = 1;
      this.load();
    },
    setCategory(id) {
      this.query.categoryId = id;
      this.search();
    },
    setSize(size) {
      this.query.size = size;
      this.search();
    },
    setAge(range) {
      const active = this.query.ageMin === range.min && this.query.ageMax === range.max;
      this.query.ageMin = active ? null : range.min;
      this.query.ageMax = active ? null : range.max;
      this.search();
    },
    onSortChange() {
      this.search();
    },
    reset() {
      this.query = { pageNum: 1, pageSize: 9, categoryId: null, city: null, size: null, ageMin: null, ageMax: null };
      this.sortValue = 'publish_time:desc';
      this.sterilizedOnly = false;
      this.load();
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.pet-list {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
}

.pet-list__layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 32px;
  padding-top: 32px;
  padding-bottom: 64px;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

.filters {
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-card;
  padding: 24px;
  align-self: start;
  position: sticky;
  top: calc(#{$portal-header-h} + 24px);

  @media (max-width: 991px) {
    display: none;
  }

  &__title {
    font-size: 16px;
    margin-bottom: 16px;
  }

  &__group {
    padding: 16px 0;
    border-top: 1px solid $border-color;

    &:first-of-type {
      border-top: none;
    }
  }

  &__label {
    font-size: 13px;
    color: $text-secondary;
    margin-bottom: 8px;
  }

  &__reset {
    margin-top: 8px;
    padding: 0;
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  height: 32px;
  padding: 0 12px;
  border-radius: $radius-base;
  border: 1px solid $border-color;
  background: #fff;
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

.list-area {
  min-width: 0;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  &__total {
    font-size: 14px;
    color: $text-regular;
  }

  &__right {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

.filters-toggle {
  display: none;

  @media (max-width: 991px) {
    display: inline-flex;
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
  min-height: 320px;
}

.pagination {
  margin-top: 32px;
  text-align: right;
}

.drawer-filters {
  padding: 0 24px 24px;
}
</style>
