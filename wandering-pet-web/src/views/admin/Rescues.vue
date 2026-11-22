<template>
  <div class="admin-rescues">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1>线索管理</h1>
        <p class="page-head__desc">处理用户上报的流浪动物线索，及时分派给对应救助站。</p>
      </div>
    </div>

    <!-- 筛选条 -->
    <div class="filter-bar">
      <el-select
        v-model="query.status"
        placeholder="全部状态"
        clearable
        style="width: 130px"
        @change="search">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select
        v-model="query.urgency"
        placeholder="全部紧急度"
        clearable
        style="width: 140px"
        @change="search">
        <el-option label="一般" :value="1" />
        <el-option label="紧急" :value="2" />
        <el-option label="危急" :value="3" />
      </el-select>
      <el-select
        v-model="query.city"
        placeholder="全部城市"
        clearable
        style="width: 120px"
        @change="search">
        <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="线索号 / 描述 / 上报人"
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
      <el-table v-else v-loading="loading" :data="list" empty-text="">
        <template slot="empty">
          <EmptyState
            v-if="error"
            title="线索列表加载失败"
            desc="请检查网络后重试。"
            action-text="重新加载"
            @action="load" />
          <EmptyState v-else title="暂无救助线索" desc="用户上报线索后会显示在这里。" />
        </template>
        <el-table-column prop="reportNo" label="线索号" width="150" />
        <el-table-column prop="categoryName" label="物种" width="90">
          <template slot-scope="{ row }">{{ row.categoryName || '—' }}</template>
        </el-table-column>
        <el-table-column label="描述" min-width="240">
          <template slot-scope="{ row }">
            <el-tooltip :content="row.description" placement="top" :disabled="!row.description || row.description.length <= 40">
              <span class="desc-text">{{ row.description || '—' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="紧急度" width="90">
          <template slot-scope="{ row }">
            <StatusTag :status="urgencyKey(row.urgency)" :label="urgencyText(row.urgency)" />
          </template>
        </el-table-column>
        <el-table-column label="城市 / 地址" min-width="200" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ locationText(row) }}</template>
        </el-table-column>
        <el-table-column prop="userNickname" label="上报人" width="120" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row.userNickname || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="96">
          <template slot-scope="{ row }">
            <StatusTag :status="row.status" :label="reportStatusText(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上报时间" width="170" />
        <el-table-column label="操作" width="210" label-class-name="op-col" fixed="right">
          <template slot-scope="{ row }">
            <div class="row-actions">
              <el-button type="text" size="small" @click="openDetail(row)">详情</el-button>
              <el-button
                v-if="row.status === 'PENDING' || row.status === 'ASSIGNED'"
                type="text"
                size="small"
                @click="openAssign(row)">分派</el-button>
              <el-button
                v-if="row.status !== 'CLOSED' && row.status !== 'RESOLVED'"
                type="text"
                size="small"
                class="op-danger"
                @click="close(row)">关闭</el-button>
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
        @current-change="load" />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer
      title="线索详情"
      :visible.sync="detailVisible"
      size="480px">
      <div v-if="current" class="drawer-body">
        <div class="detail-head">
          <p class="detail-head__no num">{{ current.reportNo }}</p>
          <div class="detail-head__tags">
            <StatusTag :status="urgencyKey(current.urgency)" :label="urgencyText(current.urgency)" />
            <StatusTag :status="current.status" :label="reportStatusText(current.status)" />
          </div>
        </div>

        <dl class="drawer-fields">
          <div class="drawer-fields__row">
            <dt>物种</dt>
            <dd>{{ current.categoryName || '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>上报人</dt>
            <dd>{{ current.userNickname || '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>位置</dt>
            <dd>{{ locationText(current) }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>上报时间</dt>
            <dd>{{ current.createTime || '—' }}</dd>
          </div>
          <div class="drawer-fields__row" v-if="current.handlerName">
            <dt>处理方</dt>
            <dd>{{ current.handlerName }}</dd>
          </div>
          <div class="drawer-fields__row" v-if="current.handleTime">
            <dt>处理时间</dt>
            <dd>{{ current.handleTime }}</dd>
          </div>
          <div class="drawer-fields__row" v-if="current.handleRemark">
            <dt>处理备注</dt>
            <dd>{{ current.handleRemark }}</dd>
          </div>
        </dl>

        <div class="drawer-section">
          <p class="drawer-section__title">线索描述</p>
          <p class="drawer-section__text">{{ current.description || '暂无描述' }}</p>
        </div>

        <div v-if="(current.images || []).length" class="drawer-section">
          <p class="drawer-section__title">现场照片</p>
          <div class="photo-grid">
            <el-image
              v-for="(img, index) in current.images"
              :key="index"
              class="photo-grid__item"
              :src="img"
              :preview-src-list="current.images"
              :initial-index="index"
              fit="cover" />
          </div>
        </div>

        <div v-if="current.status === 'PENDING' || current.status === 'ASSIGNED'" class="drawer-actions">
          <el-button type="primary" @click="openAssign(current)">分派处理</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 分派 dialog -->
    <el-dialog
      title="分派线索"
      :visible.sync="assignVisible"
      width="480px"
      :close-on-click-modal="false"
      @closed="$refs.assignFormRef && $refs.assignFormRef.clearValidate()">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-position="top">
        <p class="assign-target">线索：{{ current && current.reportNo }}（{{ current && current.categoryName }}）</p>
        <el-form-item label="救助站账号" prop="handlerId">
          <el-select
            v-model="assignForm.handlerId"
            style="width: 100%"
            placeholder="请选择处理方"
            filterable
            :loading="handlerLoading">
            <el-option
              v-for="u in handlers"
              :key="u.id"
              :label="u.realName ? `${u.realName}（${u.username}）` : u.username"
              :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="assignForm.remark"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="可补充现场情况、联系注意事项" />
        </el-form-item>
      </el-form>
      <template slot="footer">
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAssign">确认分派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { adminReports, assignReport, closeReport } from '@/api/rescue';
import { adminSysUsers } from '@/api/system';
import { reportStatusText } from '@/utils/format';

const STATUS_OPTIONS = [
  { value: 'PENDING', label: '待处理' },
  { value: 'ASSIGNED', label: '已分派' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'RESOLVED', label: '已解决' },
  { value: 'CLOSED', label: '已关闭' }
];

/** 紧急度：1 一般 / 2 紧急 / 3 危急（映射 StatusTag 色调） */
const URGENCY = {
  1: { key: 'DRAFT', label: '一般' },
  2: { key: 'PENDING', label: '紧急' },
  3: { key: 'REJECTED', label: '危急' }
};

/**
 * 管理端线索管理（/admin/rescues）
 */
export default {
  name: 'AdminRescues',
  components: { StatusTag, EmptyState },
  data() {
    return {
      loading: false,
      firstLoad: true,
      error: false,
      list: [],
      total: 0,
      statusOptions: STATUS_OPTIONS,
      cities: ['杭州', '成都'],
      query: { pageNum: 1, pageSize: 10, status: '', urgency: null, city: '', keyword: '' },
      detailVisible: false,
      current: null,
      assignVisible: false,
      submitting: false,
      handlers: [],
      handlerLoading: false,
      assignForm: { handlerId: null, remark: '' }
    };
  },
  computed: {
    assignRules() {
      return {
        handlerId: [{ required: true, message: '请选择救助站账号', trigger: 'change' }]
      };
    }
  },
  mounted() {
    this.load();
  },
  methods: {
    reportStatusText,
    urgencyKey(urgency) {
      return (URGENCY[urgency] || {}).key || 'DRAFT';
    },
    urgencyText(urgency) {
      return (URGENCY[urgency] || {}).label || '一般';
    },
    locationText(row) {
      const parts = [row.city, row.district, row.address].filter(Boolean);
      return parts.length ? parts.join(' ') : '—';
    },
    async load() {
      this.loading = true;
      this.error = false;
      try {
        const params = { pageNum: this.query.pageNum, pageSize: this.query.pageSize };
        if (this.query.status) params.status = this.query.status;
        if (this.query.urgency !== null && this.query.urgency !== '') params.urgency = this.query.urgency;
        if (this.query.city) params.city = this.query.city;
        if (this.query.keyword) params.keyword = this.query.keyword;
        const data = await adminReports(params);
        this.list = data.list || [];
        this.total = data.total || 0;
      } catch (e) {
        this.error = true;
        this.list = [];
        this.total = 0;
      } finally {
        this.loading = false;
        this.firstLoad = false;
      }
    },
    search() {
      this.query.pageNum = 1;
      this.load();
    },
    reset() {
      this.query = { pageNum: 1, pageSize: 10, status: '', urgency: null, city: '', keyword: '' };
      this.load();
    },
    openDetail(row) {
      this.current = row;
      this.detailVisible = true;
    },
    async openAssign(row) {
      this.current = row;
      this.assignForm = { handlerId: null, remark: '' };
      this.assignVisible = true;
      await this.loadHandlers();
    },
    async loadHandlers() {
      this.handlerLoading = true;
      try {
        const data = await adminSysUsers({ roleCode: 'SHELTER', pageNum: 1, pageSize: 100 });
        this.handlers = data.list || [];
      } catch (e) {
        this.handlers = [];
      } finally {
        this.handlerLoading = false;
      }
    },
    submitAssign() {
      this.$refs.assignFormRef.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        try {
          await assignReport(this.current.id, {
            handlerId: this.assignForm.handlerId,
            remark: this.assignForm.remark
          });
          this.$message.success('线索已分派');
          this.assignVisible = false;
          this.load();
        } catch (e) {
          /* 错误提示由 request 统一处理 */
        } finally {
          this.submitting = false;
        }
      });
    },
    close(row) {
      this.$confirm(`确认关闭线索「${row.reportNo}」吗？关闭后不再跟进。`, '关闭确认', { type: 'warning' })
        .then(async () => {
          await closeReport(row.id);
          this.$message.success('线索已关闭');
          this.load();
        })
        .catch(() => {});
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

.desc-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.op-danger {
  color: $color-danger-text;
}

.drawer-body {
  padding: 0 $space-5 $space-6;
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-3;

  &__no {
    font-size: $font-w-h4;
    line-height: $lh-w-h4;
    font-weight: 600;
    color: $text-primary;
  }

  &__tags {
    display: flex;
    gap: $space-2;
  }
}

.drawer-fields {
  margin: $space-5 0 0;
  padding-top: $space-4;
  border-top: 1px solid $border-color;

  &__row {
    display: flex;
    padding: $space-2 0;

    dt {
      width: 88px;
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

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-2;

  &__item {
    aspect-ratio: 1 / 1;
    // 防止子图按自然比例撑破比例框
    min-height: 0;
    width: 100%;
    border-radius: $radius-small;
    overflow: hidden;
  }
}

.drawer-actions {
  margin-top: $space-6;
  display: flex;
  justify-content: flex-end;
}

.assign-target {
  margin-bottom: $space-3;
  font-size: $font-w-body;
  color: $text-regular;
}
</style>


