<template>
  <div class="donation-records">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1 class="page-head__title">捐赠明细</h1>
        <p class="page-head__desc">逐笔查看本机构募捐项目的捐赠记录，善款去向公开透明。</p>
      </div>
    </div>

    <div class="table-card">
      <!-- 筛选条 -->
      <div class="filter-bar">
        <el-select v-model="query.projectId" placeholder="全部项目" clearable size="small" style="width: 260px" @change="search">
          <el-option v-for="p in projects" :key="p.id" :label="p.title" :value="p.id" />
        </el-select>
        <el-button type="text" size="small" @click="reset">重置</el-button>
      </div>

      <!-- 表格 -->
      <div v-if="loading" class="table-skeleton">
        <el-skeleton :rows="6" animated />
      </div>
      <el-table v-else-if="list.length" :data="list">
        <el-table-column label="支付单号" width="140">
          <template #default="{ row }">
            <span class="num">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="项目" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ projectName(row.projectId) }}</template>
        </el-table-column>
        <el-table-column label="捐赠人" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ donorName(row) }}</template>
        </el-table-column>
        <el-table-column label="金额" width="130" align="right" prop="amount" sortable>
          <template #default="{ row }">
            <span class="amount num">{{ money(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="留言" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.message || '—' }}</template>
        </el-table-column>
        <el-table-column label="支付时间" width="160">
          <template #default="{ row }">
            <span class="num">{{ row.payTime || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-else
        title="暂无捐赠记录"
        desc="项目收到捐赠后会在这里逐笔展示，可在募捐项目管理中新建项目。" />

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
  </div>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue';
import { shelterProjects, shelterDonationRecords } from '@/api/shelterBiz';
import { money } from '@/utils/format';

/**
 * 捐赠明细：项目筛选 + 分页表格
 */
export default {
  name: 'ShelterDonationRecords',
  components: { EmptyState },
  data() {
    return {
      loading: true,
      projects: [],
      list: [],
      total: 0,
      query: {
        pageNum: 1,
        pageSize: 10,
        projectId: null
      }
    };
  },
  mounted() {
    this.loadProjects();
    this.load();
  },
  methods: {
    money,
    async loadProjects() {
      try {
        this.projects = (await shelterProjects()) || [];
      } catch (e) {
        this.projects = [];
      }
    },
    projectName(projectId) {
      const project = this.projects.find((p) => String(p.id) === String(projectId));
      return project ? project.title : '—';
    },
    donorName(row) {
      if (row.isAnonymous === 1) return '匿名爱心人士';
      return row.donorName || '—';
    },
    async load() {
      this.loading = true;
      try {
        const params = { ...this.query };
        if (!params.projectId) delete params.projectId;
        const data = await shelterDonationRecords(params);
        this.list = data.list || [];
        this.total = data.total || 0;
      } catch (e) {
        this.list = [];
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
      this.query = { pageNum: 1, pageSize: 10, projectId: null };
      this.load();
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
}

.table-skeleton {
  padding: $space-6 0;
}

.pagination {
  margin-top: $space-5;
  text-align: right;
}

.amount {
  font-weight: 600;
  color: $text-primary;
}
</style>
