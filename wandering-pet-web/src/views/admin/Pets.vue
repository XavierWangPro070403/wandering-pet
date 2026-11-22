<template>
  <div class="admin-pets">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1>宠物全量管理</h1>
        <p class="page-head__desc">查看平台全部宠物档案；档案内容由机构维护，这里只做只读管理。</p>
      </div>
    </div>

    <!-- 筛选条 -->
    <div class="filter-bar">
      <el-select
        v-model="query.status"
        placeholder="全部状态"
        clearable
        style="width: 140px"
        @change="search">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select
        v-model="query.categoryId"
        placeholder="全部物种"
        clearable
        style="width: 140px"
        @change="search">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select
        v-model="query.city"
        placeholder="全部城市"
        clearable
        style="width: 140px"
        @change="search">
        <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="名字 / 性格描述"
        clearable
        style="width: 220px"
        @keyup.enter.native="search"
        @clear="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-skeleton v-if="loading && firstLoad" :rows="6" animated />
      <el-table v-else v-loading="loading" :data="pets" empty-text="">
        <template slot="empty">
          <EmptyState
            v-if="error"
            title="宠物列表加载失败"
            desc="请检查网络后重试。"
            action-text="重新加载"
            @action="load" />
          <EmptyState v-else title="没有符合条件的宠物档案" desc="换个筛选条件试试。" />
        </template>
        <el-table-column prop="petNo" label="档案号" width="130" />
        <el-table-column label="封面" width="96">
          <template slot-scope="{ row }">
            <el-image
              class="pet-cover"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="cover">
              <div slot="error" class="pet-cover__error"><i class="el-icon-picture-outline"></i></div>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名字" min-width="120" show-overflow-tooltip />
        <el-table-column label="物种 / 品种" min-width="150" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row.categoryName }} / {{ row.breedName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="shelterName" label="机构" min-width="150" show-overflow-tooltip />
        <el-table-column prop="city" label="城市" width="80" />
        <el-table-column label="状态" width="96">
          <template slot-scope="{ row }">
            <StatusTag :status="row.status" :label="petStatusText(row.status)" />
          </template>
        </el-table-column>
        <el-table-column label="浏览 / 收藏" width="120" align="center">
          <template slot-scope="{ row }">
            <span class="num">{{ row.viewCount || 0 }}</span>
            <span class="sep">/</span>
            <span class="num">{{ row.favoriteCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170">
          <template slot-scope="{ row }">{{ row.publishTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" label-class-name="op-col" fixed="right">
          <template slot-scope="{ row }">
            <div class="row-actions">
              <el-button type="text" size="small" @click="openDetail(row)">详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        layout="total, prev, pager, next, jumper"
        :total="total"
        :page-size="query.pageSize"
        :current-page.sync="query.pageNum"
        @current-change="onPageChange" />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer
      title="宠物档案"
      :visible.sync="detailVisible"
      size="480px">
      <div v-if="detail" class="drawer-body">
        <div class="pet-hero">
          <img class="pet-hero__cover" :src="detail.coverImage" :alt="detail.name" />
        </div>

        <div class="pet-hero__head">
          <p class="pet-hero__name">{{ detail.name }}</p>
          <StatusTag :status="detail.status" :label="petStatusText(detail.status)" />
        </div>
        <p class="pet-hero__no num">档案号 {{ detail.petNo }}</p>

        <div class="pet-tags">
          <span class="pet-tag">{{ detail.categoryName }}</span>
          <span v-if="detail.breedName" class="pet-tag">{{ detail.breedName }}</span>
          <span class="pet-tag">{{ genderText(detail.gender) }}</span>
          <span class="pet-tag">{{ ageText(detail.ageMonth) }}</span>
          <span class="pet-tag">{{ sizeText(detail.size) }}</span>
          <span v-if="detail.color" class="pet-tag">{{ detail.color }}</span>
        </div>

        <dl class="drawer-fields">
          <div class="drawer-fields__row">
            <dt>所属机构</dt>
            <dd>{{ detail.shelter ? detail.shelter.name : '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>所在城市</dt>
            <dd>{{ detail.shelter ? detail.shelter.city : '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>绝育 / 免疫 / 驱虫</dt>
            <dd>{{ yesNo(detail.sterilized) }} / {{ yesNo(detail.vaccinated) }} / {{ yesNo(detail.dewormed) }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>健康状况</dt>
            <dd>{{ detail.healthStatus || '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>浏览 / 收藏</dt>
            <dd><span class="num">{{ detail.viewCount || 0 }}</span> / <span class="num">{{ detail.favoriteCount || 0 }}</span></dd>
          </div>
          <div class="drawer-fields__row">
            <dt>发布时间</dt>
            <dd>{{ detail.publishTime || '—' }}</dd>
          </div>
        </dl>

        <div v-if="detail.characterDesc" class="drawer-section">
          <p class="drawer-section__title">性格描述</p>
          <p class="drawer-section__text">{{ detail.characterDesc }}</p>
        </div>
        <div v-if="detail.adoptionRequirements" class="drawer-section">
          <p class="drawer-section__title">领养要求</p>
          <p class="drawer-section__text">{{ detail.adoptionRequirements }}</p>
        </div>
        <div v-if="detail.story" class="drawer-section">
          <p class="drawer-section__title">救助故事</p>
          <p class="drawer-section__text">{{ detail.story }}</p>
        </div>

        <div v-if="galleryImages.length" class="drawer-section">
          <p class="drawer-section__title">相册</p>
          <div class="pet-gallery">
            <el-image
              v-for="(img, index) in galleryImages"
              :key="index"
              class="pet-gallery__item"
              :src="img"
              :preview-src-list="galleryImages"
              :initial-index="index"
              fit="cover" />
          </div>
        </div>
      </div>
      <div v-else class="drawer-body"><el-skeleton :rows="8" animated /></div>
    </el-drawer>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { pagePets, petDetail, categories } from '@/api/pet';
import { petStatusText, ageText, genderText, sizeText } from '@/utils/format';

const STATUS_OPTIONS = [
  { value: 'AVAILABLE', label: '可领养' },
  { value: 'PENDING', label: '审核中' },
  { value: 'ADOPTED', label: '已领养' },
  { value: 'MEDICAL', label: '治疗中' },
  { value: 'OFFLINE', label: '已下架' }
];

/**
 * 管理端宠物全量管理（/admin/pets），只读
 */
export default {
  name: 'AdminPets',
  components: { StatusTag, EmptyState },
  data() {
    return {
      loading: false,
      firstLoad: true,
      error: false,
      pets: [],
      allPets: [],
      total: 0,
      categories: [],
      cities: ['杭州', '成都'],
      statusOptions: STATUS_OPTIONS,
      query: {
        pageNum: 1,
        pageSize: 10,
        status: '',
        categoryId: null,
        city: '',
        keyword: ''
      },
      detailVisible: false,
      detail: null
    };
  },
  computed: {
    /** 详情抽屉相册（含封面去重） */
    galleryImages() {
      const images = (this.detail && this.detail.images) || [];
      const urls = images.map((img) => img.url).filter(Boolean);
      if (this.detail && this.detail.coverImage && !urls.includes(this.detail.coverImage)) {
        return [this.detail.coverImage].concat(urls);
      }
      return urls;
    }
  },
  async mounted() {
    try {
      this.categories = await categories();
    } catch (e) {
      /* 物种下拉为空时仍可按其他条件筛选 */
    }
    this.load();
  },
  methods: {
    petStatusText,
    ageText,
    genderText,
    sizeText,
    async load() {
      this.loading = true;
      this.error = false;
      try {
        const base = {
          categoryId: this.query.categoryId || undefined,
          city: this.query.city || undefined,
          keyword: this.query.keyword || undefined,
          orderBy: 'publish_time',
          orderDirection: 'desc'
        };
        if (this.query.status) {
          // 指定状态：走服务端分页
          const data = await pagePets({
            ...base,
            status: this.query.status,
            pageNum: this.query.pageNum,
            pageSize: this.query.pageSize
          });
          this.pets = data.list || [];
          this.total = data.total || 0;
        } else {
          // 全部状态：列表接口未指定状态时默认只返回可领养，
          // 因此按状态并行取全量后在前端合并分页（单状态单页上限 100）
          const results = await Promise.all(
            STATUS_OPTIONS.map((s) => pagePets({ ...base, status: s.value, pageNum: 1, pageSize: 100 }))
          );
          const merged = results.reduce((acc, data) => acc.concat(data.list || []), []);
          merged.sort((a, b) => String(b.publishTime || '').localeCompare(String(a.publishTime || '')));
          this.allPets = merged;
          this.total = merged.length;
          this.pets = this.pageSlice();
        }
      } catch (e) {
        this.error = true;
        this.pets = [];
        this.allPets = [];
        this.total = 0;
      } finally {
        this.loading = false;
        this.firstLoad = false;
      }
    },
    pageSlice() {
      const start = (this.query.pageNum - 1) * this.query.pageSize;
      return this.allPets.slice(start, start + this.query.pageSize);
    },
    /** 全部状态时数据已在本地合并，翻页只做切片；指定状态时走服务端分页 */
    onPageChange() {
      if (this.query.status) {
        this.load();
      } else {
        this.pets = this.pageSlice();
      }
    },
    search() {
      this.query.pageNum = 1;
      this.load();
    },
    reset() {
      this.query = { pageNum: 1, pageSize: 10, status: '', categoryId: null, city: '', keyword: '' };
      this.load();
    },
    yesNo(v) {
      return v === 1 ? '已完成' : '未完成';
    },
    async openDetail(row) {
      this.detail = null;
      this.detailVisible = true;
      try {
        this.detail = await petDetail(row.id);
      } catch (e) {
        this.detailVisible = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: $space-6;

  h1 {
    font-size: $font-w-h1;
    line-height: $lh-w-h1;
    color: $text-primary;
  }

  &__desc {
    margin-top: $space-2;
    font-size: $font-w-body;
    color: $text-secondary;
  }
}

.filter-bar {
  @include card;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $space-3;
  padding: $space-4 $space-5;
  margin-bottom: $space-4;
}

.table-card {
  @include card;
  padding: $space-4;
}

.pagination {
  margin-top: $space-4;
  text-align: right;
}

.pet-cover {
  width: 64px;
  height: 48px;
  border-radius: $radius-small;
  display: block;

  &__error {
    width: 64px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $bg-page;
    color: $text-disabled;
    font-size: 18px;
  }
}

.sep {
  margin: 0 4px;
  color: $text-disabled;
}

.drawer-body {
  padding: 0 $space-5 $space-6;
}

.pet-hero {
  &__cover {
    width: 100%;
    aspect-ratio: 4 / 3;
    object-fit: cover;
    border-radius: $radius-base;
    display: block;
    border: 1px solid $border-color;
  }

  &__head {
    margin-top: $space-4;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $space-3;
  }

  &__name {
    font-size: $font-w-h3;
    line-height: $lh-w-h3;
    font-weight: 600;
    color: $text-primary;
  }

  &__no {
    margin-top: $space-1;
    font-size: $font-w-caption;
    color: $text-secondary;
  }
}

.pet-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $space-2;
  margin-top: $space-3;
}

.pet-tag {
  height: 24px;
  line-height: 24px;
  padding: 0 $space-2;
  border-radius: $radius-small;
  background: $color-primary-light;
  color: $color-primary-text;
  font-size: $font-w-caption;
}

.drawer-fields {
  margin: $space-5 0 0;
  padding-top: $space-4;
  border-top: 1px solid $border-color;

  &__row {
    display: flex;
    padding: $space-2 0;

    dt {
      width: 128px;
      flex-shrink: 0;
      font-size: $font-w-body;
      color: $text-secondary;
    }

    dd {
      margin: 0;
      font-size: $font-w-body;
      color: $text-primary;
      word-break: break-all;
    }
  }
}

.drawer-section {
  margin-top: $space-5;

  &__title {
    font-size: $font-w-body;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: $space-2;
  }

  &__text {
    font-size: $font-w-body;
    line-height: $lh-w-body;
    color: $text-regular;
    white-space: pre-wrap;
  }
}

.pet-gallery {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-2;

  &__item {
    aspect-ratio: 1 / 1;
    width: 100%;
    border-radius: $radius-small;
    overflow: hidden;
  }
}
</style>

