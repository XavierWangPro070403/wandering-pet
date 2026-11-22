<template>
  <div class="followups">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1 class="page-head__title">回访管理</h1>
        <p class="page-head__desc">跟进领养后的宠物生活情况，处理逾期与归档。</p>
      </div>
    </div>

    <!-- 生成规则说明 -->
    <p class="rule-note">
      <i class="el-icon-info" aria-hidden="true"></i>
      领养完成后自动生成第 7 / 30 / 90 / 180 天共 4 次回访任务，领养人提交生活记录后由机构跟进归档。
    </p>

    <div class="table-card">
      <!-- 筛选条 -->
      <div class="filter-bar">
        <el-select v-model="query.status" placeholder="全部状态" clearable size="small" style="width: 140px" @change="search">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-select v-model="query.round" placeholder="全部轮次" clearable size="small" style="width: 140px" @change="search">
          <el-option v-for="r in 4" :key="r" :label="`第 ${r} 次回访`" :value="r" />
        </el-select>
        <el-button type="text" size="small" @click="reset">重置</el-button>
      </div>

      <!-- 表格 -->
      <div v-if="loading" class="table-skeleton">
        <el-skeleton :rows="6" animated />
      </div>
      <el-table v-else-if="list.length" :data="list">
        <el-table-column label="轮次" width="90" align="center">
          <template #default="{ row }">
            <span class="round-chip num">第 {{ row.round }} 次</span>
          </template>
        </el-table-column>
        <el-table-column prop="petName" label="宠物" min-width="110" show-overflow-tooltip />
        <el-table-column prop="userNickname" label="领养人" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.userNickname || '—' }}</template>
        </el-table-column>
        <el-table-column label="计划日期" width="120">
          <template #default="{ row }">
            <span class="num">{{ row.planDate || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实际日期" width="120">
          <template #default="{ row }">
            <span class="num">{{ row.actualDate || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="生活照" min-width="150">
          <template #default="{ row }">
            <div v-if="row.images && row.images.length" class="thumb-row">
              <el-image
                v-for="(img, i) in row.images.slice(0, 3)"
                :key="i"
                class="thumb"
                :src="img"
                :preview-src-list="row.images"
                fit="cover" />
            </div>
            <span v-else class="no-photo">未提交</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" label-class-name="op-col" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button type="text" size="small" @click="openDetail(row)">详情</el-button>
              <el-button v-if="row.status !== 'DONE'" type="text" size="small" @click="remind(row)">催办</el-button>
              <el-button v-if="row.status === 'DONE'" type="text" size="small" @click="archive(row)">归档</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-else
        title="暂无回访任务"
        desc="领养完成后会自动生成回访任务，届时可在这里跟进领养人提交的生活记录。" />

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

    <!-- 详情抽屉 -->
    <el-drawer title="回访详情" :visible.sync="detailVisible" size="480px">
      <div v-if="current.id" class="detail-drawer">
        <div class="detail-head">
          <span class="detail-head__round">第 {{ current.round }} 次回访</span>
          <StatusTag :status="current.status" />
        </div>

        <dl class="detail-grid">
          <div><dt>宠物</dt><dd>{{ current.petName || '—' }}</dd></div>
          <div><dt>领养人</dt><dd>{{ current.userNickname || '—' }}</dd></div>
          <div><dt>计划日期</dt><dd class="num">{{ current.planDate || '—' }}</dd></div>
          <div><dt>实际日期</dt><dd class="num">{{ current.actualDate || '—' }}</dd></div>
          <div><dt>体重</dt><dd class="num">{{ current.weight != null ? current.weight + ' kg' : '—' }}</dd></div>
          <div><dt>健康状况</dt><dd>{{ healthText(current.healthStatus) }}</dd></div>
          <div class="full"><dt>疫苗情况</dt><dd>{{ current.vaccineStatus || '—' }}</dd></div>
          <div class="full"><dt>生活近况</dt><dd class="pre-line">{{ current.lifeDesc || '领养人还未填写生活记录。' }}</dd></div>
        </dl>

        <template v-if="current.images && current.images.length">
          <h3 class="detail-section-title">生活照片</h3>
          <div class="photo-grid">
            <el-image
              v-for="(img, i) in current.images"
              :key="i"
              class="photo"
              :src="img"
              :preview-src-list="current.images"
              fit="cover" />
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { shelterFollowUps, remindFollowUp, archiveFollowUp } from '@/api/shelterBiz';

/**
 * 回访管理：状态/轮次筛选 + 催办/归档 + 详情抽屉
 */
export default {
  name: 'ShelterFollowUps',
  components: { StatusTag, EmptyState },
  data() {
    return {
      loading: true,
      list: [],
      total: 0,
      statusOptions: [
        { value: 'PENDING', label: '待回访' },
        { value: 'DONE', label: '已完成' },
        { value: 'OVERDUE', label: '已逾期' }
      ],
      query: {
        pageNum: 1,
        pageSize: 10,
        status: null,
        round: null
      },
      detailVisible: false,
      current: {}
    };
  },
  mounted() {
    this.load();
  },
  methods: {
    async load() {
      this.loading = true;
      try {
        const params = { ...this.query };
        if (!params.status) delete params.status;
        if (!params.round) delete params.round;
        const data = await shelterFollowUps(params);
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
      this.query = { pageNum: 1, pageSize: 10, status: null, round: null };
      this.load();
    },
    openDetail(row) {
      this.current = row;
      this.detailVisible = true;
    },
    remind(row) {
      this.$confirm(`向「${row.userNickname || '领养人'}」发送第 ${row.round} 次回访催办提醒？`, '催办确认', {
        confirmButtonText: '确定催办',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => remindFollowUp(row.id))
        .then(() => {
          this.$message.success('催办提醒已发送');
          this.load();
        })
        .catch(() => {});
    },
    archive(row) {
      this.$confirm(`归档第 ${row.round} 次回访？归档后由机构标记本次跟进完成。`, '归档确认', {
        confirmButtonText: '确定归档',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => archiveFollowUp(row.id))
        .then(() => {
          this.$message.success('回访已归档');
          this.load();
        })
        .catch(() => {});
    },
    healthText(s) {
      return { HEALTHY: '健康', GENERAL: '健康一般', ILL: '治疗中' }[s] || s || '—';
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
  margin-bottom: $space-4;

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

.rule-note {
  @include card;

  display: inline-flex;
  align-items: center;
  gap: $space-2;
  margin-bottom: $space-4;
  padding: $space-2 $space-4;
  border-radius: $radius-base;
  font-size: $font-w-caption;
  color: $text-secondary;

  i {
    color: $color-primary-text;
    font-size: 14px;
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

.round-chip {
  display: inline-block;
  font-size: $font-w-caption;
  color: $color-primary-text;
  background: $color-primary-light;
  border-radius: $radius-small;
  padding: 2px 8px;
}

.thumb-row {
  display: flex;
  gap: $space-2;
}

.thumb {
  width: 32px;
  height: 32px;
  border-radius: $radius-small;
  flex-shrink: 0;
  cursor: pointer;
}

.no-photo {
  font-size: $font-w-caption;
  color: $text-disabled;
}

.detail-drawer {
  padding: 0 $space-6 $space-6;
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $space-4;

  &__round {
    font-size: $font-w-body-l;
    font-weight: 600;
    color: $text-primary;
  }
}

.detail-grid {
  margin: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-3 $space-4;

  div {
    display: flex;
    gap: $space-2;

    &.full {
      flex-direction: column;
      gap: $space-1;
    }
  }

  dt {
    flex-shrink: 0;
    font-size: $font-w-caption;
    color: $text-secondary;
  }

  dd {
    margin: 0;
    font-size: $font-w-body;
    line-height: $lh-w-body;
    color: $text-regular;
  }
}

.pre-line {
  white-space: pre-line;
}

.detail-section-title {
  margin: $space-6 0 $space-3;
  font-size: $font-w-body-l;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-2;
}

.photo {
  aspect-ratio: 1 / 1;
  border-radius: $radius-base;
  cursor: pointer;
}
</style>


