<template>
  <div class="pet-manage">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1 class="page-head__title">宠物管理</h1>
        <p class="page-head__desc">维护机构在册的宠物档案，管理上下架与领养状态。</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="$router.push('/shelter/pets/edit')">发布宠物</el-button>
    </div>

    <div class="table-card">
      <!-- 筛选条一行 -->
      <div class="filter-bar">
        <el-select v-model="query.status" placeholder="全部状态" clearable size="small" style="width: 140px" @change="search">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="按名字或性格描述搜索"
          clearable
          size="small"
          style="width: 240px"
          prefix-icon="el-icon-search"
          @keyup.enter.native="search"
          @clear="search" />
        <el-button type="text" size="small" @click="reset">重置</el-button>
        <div class="filter-bar__spacer"></div>
        <template v-if="selected.length">
          <span class="filter-bar__selected">已选 <strong class="num">{{ selected.length }}</strong> 条</span>
          <el-button size="small" @click="batchChange('AVAILABLE')">批量上架</el-button>
          <el-button size="small" @click="batchChange('OFFLINE')">批量下架</el-button>
        </template>
      </div>

      <!-- 表格 -->
      <div v-if="loading" class="table-skeleton">
        <el-skeleton :rows="6" animated />
      </div>
      <el-table
        v-else-if="pets.length"
        :data="pets"
        @selection-change="onSelectionChange">
        <el-table-column type="selection" width="44" />
        <el-table-column label="封面" width="72">
          <template #default="{ row }">
            <img class="pet-cover" :src="row.coverImage" :alt="`${row.name}的封面`" loading="lazy" />
          </template>
        </el-table-column>
        <el-table-column label="档案号" width="130">
          <template #default="{ row }">
            <span class="num">{{ row.petNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名字" min-width="100" show-overflow-tooltip />
        <el-table-column label="品种" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.breedName || row.categoryName || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="月龄" width="90">
          <template #default="{ row }">
            <span class="num">{{ ageText(row.ageMonth) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="浏览/收藏" width="110" align="center">
          <template #default="{ row }">
            <span class="num">{{ row.viewCount || 0 }}</span>
            <span class="slash">/</span>
            <span class="num">{{ row.favoriteCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="进行中申请" width="100" align="center">
          <template #default="{ row }">
            <span class="num">{{ row.activeApplicationCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" label-class-name="op-col" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button type="text" size="small" @click="$router.push(`/shelter/pets/edit/${row.id}`)">编辑</el-button>
              <el-button v-if="row.status === 'OFFLINE'" type="text" size="small" @click="changeStatus(row, 'AVAILABLE')">上架</el-button>
              <el-button v-else type="text" size="small" @click="changeStatus(row, 'OFFLINE')">下架</el-button>
              <el-button type="text" size="small" class="danger-text" @click="openDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-else
        title="还没有宠物档案"
        desc="发布第一份宠物档案，完善健康信息与相册后即可上架待领养。"
        action-text="发布宠物"
        @action="$router.push('/shelter/pets/edit')" />

      <!-- 分页 -->
      <el-pagination
        v-if="total > query.pageSize"
        class="pagination"
        layout="total, prev, pager, next, jumper"
        :total="total"
        :page-size="query.pageSize"
        :current-page.sync="query.pageNum"
        @current-change="load" />
    </div>

    <!-- 删除确认弹窗 -->
    <el-dialog
      title="删除宠物档案"
      :visible.sync="deleteVisible"
      width="440px"
      :close-on-click-modal="false">
      <p class="delete-tip">
        确定删除 <strong>{{ currentPet && currentPet.name }}</strong>（档案号
        <span class="num">{{ currentPet && currentPet.petNo }}</span>）？
        删除后档案与相册不可恢复，进行中的申请也会失效。
      </p>
      <div slot="footer">
        <el-button @click="deleteVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleting" @click="doDelete">确认删除</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { shelterPets, deletePet, changePetStatus } from '@/api/shelter';
import { ageText } from '@/utils/format';

/**
 * 宠物管理：筛选 → 表格 → 分页；支持批量上下架与删除
 */
export default {
  name: 'ShelterPetManage',
  components: { StatusTag, EmptyState },
  data() {
    return {
      loading: true,
      pets: [],
      total: 0,
      selected: [],
      statusOptions: [
        { value: 'AVAILABLE', label: '可领养' },
        { value: 'PENDING', label: '审核中' },
        { value: 'ADOPTED', label: '已领养' },
        { value: 'MEDICAL', label: '治疗中' },
        { value: 'OFFLINE', label: '已下架' }
      ],
      query: {
        pageNum: 1,
        pageSize: 10,
        status: null,
        keyword: ''
      },
      deleteVisible: false,
      deleting: false,
      currentPet: null
    };
  },
  mounted() {
    this.load();
  },
  methods: {
    ageText,
    async load() {
      this.loading = true;
      try {
        const params = { ...this.query };
        if (!params.keyword) delete params.keyword;
        if (!params.status) delete params.status;
        const data = await shelterPets(params);
        this.pets = data.list || [];
        this.total = data.total || 0;
      } catch (e) {
        this.pets = [];
        this.total = 0;
      } finally {
        this.loading = false;
      }
    },
    search() {
      this.query.pageNum = 1;
      this.load();
    },
    reset() {
      this.query = { pageNum: 1, pageSize: 10, status: null, keyword: '' };
      this.load();
    },
    onSelectionChange(rows) {
      this.selected = rows;
    },
    changeStatus(row, status) {
      const text = status === 'AVAILABLE' ? '上架' : '下架';
      this.$confirm(`确定将「${row.name}」${text}？${text === '上架' ? '上架后立即进入待领养列表。' : '下架后门户不再展示该档案。'}`, `${text}确认`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => changePetStatus({ status, ids: [row.id] }))
        .then(() => {
          this.$message.success(`${row.name} 已${text}`);
          this.load();
        })
        .catch(() => {});
    },
    batchChange(status) {
      const text = status === 'AVAILABLE' ? '上架' : '下架';
      this.$confirm(`确定将选中的 ${this.selected.length} 只宠物批量${text}？`, '批量操作确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => changePetStatus({ status, ids: this.selected.map((r) => r.id) }))
        .then(() => {
          this.$message.success(`已批量${text} ${this.selected.length} 条`);
          this.load();
        })
        .catch(() => {});
    },
    openDelete(row) {
      this.currentPet = row;
      this.deleteVisible = true;
    },
    doDelete() {
      if (!this.currentPet) return;
      this.deleting = true;
      deletePet(this.currentPet.id)
        .then(() => {
          this.$message.success('档案已删除');
          this.deleteVisible = false;
          this.load();
        })
        .catch(() => {})
        .finally(() => {
          this.deleting = false;
        });
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
  gap: 16px;
  margin-bottom: $space-6;

  &__title {
    font-size: $font-w-h1;
    line-height: $lh-w-h1;
  }

  &__desc {
    margin-top: $space-1;
    font-size: $font-w-body;
    line-height: $lh-w-body;
    color: $text-secondary;
  }
}

.table-card {
  @include card;

  padding: $space-5 $space-6;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: $space-3;
  margin-bottom: $space-4;
  flex-wrap: wrap;

  &__spacer {
    flex: 1;
  }

  &__selected {
    font-size: $font-w-caption;
    color: $text-secondary;

    strong {
      color: $color-primary-text;
    }
  }
}

.pet-cover {
  width: 48px;
  height: 48px;
  border-radius: $radius-base;
  object-fit: cover;
  display: block;
  aspect-ratio: 1 / 1;
}

.slash {
  margin: 0 4px;
  color: $text-disabled;
}

.danger-text {
  color: $color-danger-text;

  &:hover {
    background: $color-danger-light;
    color: $color-danger-text;
  }
}

.table-skeleton {
  padding: $space-6 0;
}

.pagination {
  margin-top: $space-5;
  text-align: right;
}

.delete-tip {
  font-size: $font-w-body;
  line-height: $lh-w-body;
  color: $text-regular;

  strong {
    color: $text-primary;
  }
}
</style>


