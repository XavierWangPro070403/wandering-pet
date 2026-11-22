<template>
  <div class="volunteers">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1 class="page-head__title">志愿者审核</h1>
        <p class="page-head__desc">审核报名信息，通过后即可安排志愿者参与救助工作。</p>
      </div>
    </div>

    <div class="table-card">
      <!-- 筛选条 -->
      <div class="filter-bar">
        <el-select v-model="query.status" placeholder="全部状态" clearable size="small" style="width: 140px" @change="search">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button type="text" size="small" @click="reset">重置</el-button>
      </div>

      <!-- 表格 -->
      <div v-if="loading" class="table-skeleton">
        <el-skeleton :rows="6" animated />
      </div>
      <el-table v-else-if="list.length" :data="list">
        <el-table-column prop="realName" label="姓名" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.realName || '—' }}</template>
        </el-table-column>
        <el-table-column label="电话" width="130">
          <template #default="{ row }">
            <span class="num">{{ row.phone || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="昵称" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.userNickname || '—' }}</template>
        </el-table-column>
        <el-table-column label="技能" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.skills || '—' }}</template>
        </el-table-column>
        <el-table-column label="可参与时间" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.availableTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="160">
          <template #default="{ row }">
            <span class="num">{{ row.createTime || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" label-class-name="op-col" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <template v-if="row.status === 'PENDING'">
                <el-button type="text" size="small" @click="openAudit(row, true)">通过</el-button>
                <el-button type="text" size="small" @click="openAudit(row, false)">驳回</el-button>
              </template>
              <span v-else class="audited-tip">已审核</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-else
        title="暂无志愿者报名"
        desc="门户用户提交的志愿者申请会出现在这里，及时审核能更快组织救助活动。" />

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

    <!-- 审核弹窗 -->
    <el-dialog
      :title="auditForm.pass ? '通过志愿者申请' : '驳回志愿者申请'"
      :visible.sync="auditVisible"
      width="480px"
      :close-on-click-modal="false">
      <p class="audit-target">
        申请人：<strong>{{ current.realName || current.userNickname || '—' }}</strong>
        <span class="audit-target__meta">技能：{{ current.skills || '—' }}</span>
      </p>
      <el-form label-position="top" @submit.native.prevent>
        <el-form-item :label="auditForm.pass ? '审核备注' : '驳回原因'">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            :placeholder="auditForm.pass ? '可补充后续活动安排说明' : '请说明驳回原因，会同步给申请人'" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditSubmitting" @click="submitAudit">确认提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { shelterVolunteers, auditVolunteer } from '@/api/shelterBiz';

/**
 * 志愿者审核：状态筛选 + 通过/驳回弹窗
 */
export default {
  name: 'ShelterVolunteers',
  components: { StatusTag, EmptyState },
  data() {
    return {
      loading: true,
      list: [],
      total: 0,
      statusOptions: [
        { value: 'PENDING', label: '待审核' },
        { value: 'APPROVED', label: '已通过' },
        { value: 'REJECTED', label: '未通过' }
      ],
      query: {
        pageNum: 1,
        pageSize: 10,
        status: null
      },
      auditVisible: false,
      auditSubmitting: false,
      current: {},
      auditForm: {
        pass: true,
        remark: ''
      }
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
        const data = await shelterVolunteers(params);
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
      this.query = { pageNum: 1, pageSize: 10, status: null };
      this.load();
    },
    openAudit(row, pass) {
      this.current = row;
      this.auditForm = { pass, remark: '' };
      this.auditVisible = true;
    },
    submitAudit() {
      if (!this.auditForm.pass && !this.auditForm.remark.trim()) {
        this.$message.warning('请填写驳回原因');
        return;
      }
      this.auditSubmitting = true;
      auditVolunteer(this.current.id, {
        pass: this.auditForm.pass,
        remark: this.auditForm.remark.trim()
      })
        .then(() => {
          this.$message.success(this.auditForm.pass ? '已通过申请' : '已驳回申请');
          this.auditVisible = false;
          this.load();
        })
        .catch(() => {})
        .finally(() => {
          this.auditSubmitting = false;
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
}

.table-skeleton {
  padding: $space-6 0;
}

.pagination {
  margin-top: $space-5;
  text-align: right;
}

.audited-tip {
  font-size: $font-w-caption;
  color: $text-disabled;
}

.audit-target {
  font-size: $font-w-body;
  color: $text-regular;
  margin-bottom: $space-4;

  strong {
    color: $text-primary;
  }

  &__meta {
    margin-left: $space-3;
    font-size: $font-w-caption;
    color: $text-secondary;
  }
}
</style>


