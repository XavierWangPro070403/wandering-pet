<template>
  <div class="shelter-audit">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1>机构审核</h1>
        <p class="page-head__desc">审核救助站的入驻申请，通过后机构即可维护宠物档案。</p>
      </div>
    </div>

    <!-- 筛选条 -->
    <div class="filter-bar">
      <el-select
        v-model="query.auditStatus"
        placeholder="全部状态"
        clearable
        style="width: 150px"
        @change="search">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="机构名称 / 信用代码 / 联系人"
        clearable
        style="width: 260px"
        @keyup.enter.native="search"
        @clear="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-skeleton v-if="loading && firstLoad" :rows="6" animated />
      <el-table
        v-else
        v-loading="loading"
        :data="list"
        empty-text="">
        <template slot="empty">
          <EmptyState
            v-if="error"
            title="机构列表加载失败"
            desc="请检查网络后重试。"
            action-text="重新加载"
            @action="load" />
          <EmptyState v-else title="暂无待审核机构" desc="新的入驻申请提交后会显示在这里。" />
        </template>
        <el-table-column prop="name" label="机构" min-width="180" show-overflow-tooltip />
        <el-table-column prop="licenseNo" label="信用代码" min-width="170" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="contactPhone" label="电话" width="130" />
        <el-table-column prop="city" label="城市" width="90" />
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <StatusTag :status="statusKey(row.auditStatus)" :label="statusLabel(row.auditStatus)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="210" label-class-name="op-col" fixed="right">
          <template slot-scope="{ row }">
            <div class="row-actions">
              <el-button type="text" size="small" @click="openDetail(row)">详情</el-button>
              <el-button
                v-if="row.auditStatus === 0"
                type="text"
                size="small"
                class="op-pass"
                @click="openAudit(row, true)">通过</el-button>
              <el-button
                v-if="row.auditStatus === 0"
                type="text"
                size="small"
                class="op-reject"
                @click="openAudit(row, false)">驳回</el-button>
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
      title="机构详情"
      :visible.sync="detailVisible"
      size="480px">
      <div v-if="current" class="drawer-body">
        <div class="drawer-shelter">
          <img class="drawer-shelter__logo" :src="current.logo || '/imgs/avatar-default.svg'" alt="" />
          <div>
            <p class="drawer-shelter__name">{{ current.name }}</p>
            <p class="drawer-shelter__meta">{{ current.province }} {{ current.city }} {{ current.district }}</p>
          </div>
          <StatusTag :status="statusKey(current.auditStatus)" :label="statusLabel(current.auditStatus)" />
        </div>

        <dl class="drawer-fields">
          <div class="drawer-fields__row">
            <dt>信用代码</dt>
            <dd class="num">{{ current.licenseNo || '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>联系人</dt>
            <dd>{{ current.contactName || '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>联系电话</dt>
            <dd>{{ current.contactPhone || '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>详细地址</dt>
            <dd>{{ current.address || '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>关联账号</dt>
            <dd>{{ current.accountUsername || '—' }}</dd>
          </div>
          <div class="drawer-fields__row">
            <dt>申请时间</dt>
            <dd>{{ current.createTime || '—' }}</dd>
          </div>
          <div class="drawer-fields__row" v-if="current.auditTime">
            <dt>审核时间</dt>
            <dd>{{ current.auditTime }}</dd>
          </div>
          <div class="drawer-fields__row" v-if="current.auditRemark">
            <dt>审核备注</dt>
            <dd>{{ current.auditRemark }}</dd>
          </div>
        </dl>

        <div class="drawer-section">
          <p class="drawer-section__title">机构简介</p>
          <p class="drawer-section__text">{{ current.intro || '暂无简介' }}</p>
        </div>

        <div v-if="current.auditStatus === 0" class="drawer-actions">
          <el-button type="primary" @click="openAudit(current, true)">通过申请</el-button>
          <el-button @click="openAudit(current, false)">驳回申请</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 审核 dialog -->
    <el-dialog
      :title="auditForm.pass ? '通过机构申请' : '驳回机构申请'"
      :visible.sync="auditVisible"
      width="440px"
      :close-on-click-modal="false"
      @closed="$refs.auditFormRef && $refs.auditFormRef.clearValidate()">
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-position="top">
        <p class="audit-target">机构：{{ current && current.name }}</p>
        <el-form-item label="审核备注" prop="remark">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            :placeholder="auditForm.pass ? '可填写入驻须知等说明' : '请填写驳回原因'" />
        </el-form-item>
      </el-form>
      <template slot="footer">
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button
          :type="auditForm.pass ? 'primary' : 'danger'"
          :loading="submitting"
          @click="submitAudit">{{ auditForm.pass ? '确认通过' : '确认驳回' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { adminShelters, auditShelter } from '@/api/system';

/** 审核状态 0-待审核 1-已通过 2-已驳回（映射 StatusTag 色调） */
const AUDIT_STATUS = {
  0: { key: 'PENDING', label: '待审核' },
  1: { key: 'APPROVED', label: '已通过' },
  2: { key: 'REJECTED', label: '已驳回' }
};

/**
 * 管理端机构审核（/admin/shelters）
 */
export default {
  name: 'AdminShelters',
  components: { StatusTag, EmptyState },
  data() {
    return {
      loading: false,
      firstLoad: true,
      error: false,
      list: [],
      total: 0,
      query: {
        pageNum: 1,
        pageSize: 10,
        auditStatus: null,
        keyword: ''
      },
      detailVisible: false,
      current: null,
      auditVisible: false,
      submitting: false,
      auditForm: { id: null, pass: true, remark: '' }
    };
  },
  computed: {
    /** 通过时备注非必填，驳回时必填 */
    auditRules() {
      return {
        remark: [
          {
            required: !this.auditForm.pass,
            message: this.auditForm.pass ? '请填写备注' : '请填写驳回原因',
            trigger: 'blur'
          }
        ]
      };
    }
  },
  mounted() {
    this.load();
  },
  methods: {
    async load() {
      this.loading = true;
      this.error = false;
      try {
        const params = {
          pageNum: this.query.pageNum,
          pageSize: this.query.pageSize
        };
        if (this.query.auditStatus !== null && this.query.auditStatus !== '') params.auditStatus = this.query.auditStatus;
        if (this.query.keyword) params.keyword = this.query.keyword;
        const data = await adminShelters(params);
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
      this.query = { pageNum: 1, pageSize: 10, auditStatus: null, keyword: '' };
      this.load();
    },
    statusKey(status) {
      return (AUDIT_STATUS[status] || {}).key || 'DRAFT';
    },
    statusLabel(status) {
      return (AUDIT_STATUS[status] || {}).label || '未知';
    },
    openDetail(row) {
      this.current = row;
      this.detailVisible = true;
    },
    openAudit(row, pass) {
      this.current = row;
      this.auditForm = { id: row.id, pass, remark: '' };
      this.auditVisible = true;
    },
    submitAudit() {
      this.$refs.auditFormRef.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        try {
          await auditShelter(this.auditForm.id, {
            pass: this.auditForm.pass,
            remark: this.auditForm.remark
          });
          this.$message.success(this.auditForm.pass ? '已通过该机构申请' : '已驳回该机构申请');
          this.auditVisible = false;
          this.load();
        } catch (e) {
          /* 错误提示由 request 统一处理 */
        } finally {
          this.submitting = false;
        }
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

.op-pass {
  color: $color-secondary-text;
}

.op-reject {
  color: $color-danger-text;
}

.drawer-body {
  padding: 0 $space-5 $space-6;
}

.drawer-shelter {
  display: flex;
  align-items: center;
  gap: $space-3;

  &__logo {
    width: 56px;
    height: 56px;
    border-radius: $radius-base;
    object-fit: cover;
    border: 1px solid $border-color;
    flex-shrink: 0;
  }

  &__name {
    font-size: $font-w-h4;
    line-height: $lh-w-h4;
    font-weight: 600;
    color: $text-primary;
  }

  &__meta {
    margin-top: 2px;
    font-size: $font-w-caption;
    color: $text-secondary;
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

.drawer-actions {
  margin-top: $space-6;
  display: flex;
  justify-content: flex-end;
}

.audit-target {
  margin-bottom: $space-3;
  font-size: $font-w-body;
  color: $text-regular;
}
</style>


