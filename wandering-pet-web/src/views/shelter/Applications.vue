<template>
  <div class="applications">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1 class="page-head__title">领养申请审核</h1>
        <p class="page-head__desc">按流程受理申请：受理、面谈、家访、通过、签约与完成。</p>
      </div>
    </div>

    <div class="table-card">
      <!-- 筛选条 -->
      <div class="filter-bar">
        <el-select v-model="query.status" placeholder="全部状态" clearable size="small" style="width: 150px" @change="search">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="申请号 / 宠物名 / 申请人"
          clearable
          size="small"
          style="width: 240px"
          prefix-icon="el-icon-search"
          @keyup.enter.native="search"
          @clear="search" />
        <el-button type="text" size="small" @click="reset">重置</el-button>
      </div>

      <!-- 表格 -->
      <div v-if="loading" class="table-skeleton">
        <el-skeleton :rows="6" animated />
      </div>
      <el-table v-else-if="list.length" :data="list">
        <el-table-column label="申请号" width="150">
          <template #default="{ row }">
            <span class="num">{{ row.applyNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="petName" label="宠物" min-width="110" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="申请人" width="110" show-overflow-tooltip />
        <el-table-column prop="applicantPhone" label="电话" width="130">
          <template #default="{ row }">
            <span class="num">{{ row.applicantPhone || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="90">
          <template #default="{ row }">{{ row.city || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.status" :label="applicationStatusText(row.status)" />
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            <span class="num">{{ row.createTime || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" label-class-name="op-col" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button type="text" size="small" @click="openDetail(row)">详情</el-button>
              <el-button
                v-for="op in rowOps(row.status)"
                :key="op.type"
                type="text"
                size="small"
                @click="openAction(op.type, row)">{{ op.label }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-else
        title="暂无领养申请"
        desc="门户用户提交的领养申请会出现在这里，及时受理能提高领养成功率。" />

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
    <el-drawer title="申请详情" :visible.sync="detailVisible" size="480px">
      <div v-if="detailLoading" class="detail-drawer">
        <el-skeleton :rows="8" animated />
      </div>
      <div v-else-if="detail.id" class="detail-drawer">
        <div class="detail-head">
          <span class="num detail-head__no">{{ detail.applyNo }}</span>
          <StatusTag :status="detail.status" :label="applicationStatusText(detail.status)" />
        </div>

        <div class="detail-pet">
          <img v-if="detail.petCover" :src="detail.petCover" :alt="`${detail.petName}的照片`" />
          <div>
            <p class="detail-pet__name">{{ detail.petName }}</p>
            <p class="detail-pet__shelter">{{ detail.shelterName }}</p>
          </div>
        </div>

        <h3 class="detail-section-title">申请人信息</h3>
        <dl class="detail-grid">
          <div><dt>申请人</dt><dd>{{ detail.applicantName || '—' }}</dd></div>
          <div><dt>联系电话</dt><dd class="num">{{ detail.applicantPhone || '—' }}</dd></div>
          <div><dt>年龄</dt><dd class="num">{{ detail.applicantAge != null ? detail.applicantAge + ' 岁' : '—' }}</dd></div>
          <div><dt>职业</dt><dd>{{ detail.occupation || '—' }}</dd></div>
          <div><dt>城市</dt><dd>{{ detail.city || '—' }}</dd></div>
          <div><dt>住房条件</dt><dd>{{ housingText(detail.housingType) }}</dd></div>
          <div><dt>养宠经验</dt><dd>{{ yesNo(detail.hasPetExperience) }}</dd></div>
          <div><dt>家人同意</dt><dd>{{ yesNo(detail.familyAgree) }}</dd></div>
          <div><dt>家有小孩</dt><dd>{{ yesNo(detail.hasChildren) }}</dd></div>
          <div><dt>有人过敏</dt><dd>{{ yesNo(detail.hasAllergy) }}</dd></div>
          <div class="full"><dt>申请理由</dt><dd class="pre-line">{{ detail.reason || '—' }}</dd></div>
        </dl>

        <h3 class="detail-section-title">状态流水</h3>
        <el-timeline v-if="detail.flowLogs && detail.flowLogs.length">
          <el-timeline-item
            v-for="log in detail.flowLogs"
            :key="log.id"
            :timestamp="log.createTime || ''"
            placement="top">
            <p class="flow-title">
              流转至「{{ applicationStatusText(log.toStatus) }}」
              <span class="flow-operator">· {{ operatorText(log.operatorType) }}</span>
            </p>
            <p v-if="log.remark" class="flow-remark">{{ log.remark }}</p>
          </el-timeline-item>
        </el-timeline>
        <p v-else class="detail-empty">暂无流转记录</p>
      </div>
    </el-drawer>

    <!-- 流程操作弹窗 -->
    <el-dialog
      :title="actionTitle"
      :visible.sync="actionVisible"
      width="480px"
      :close-on-click-modal="false"
      @closed="resetActionForm">
      <el-form label-position="top" @submit.native.prevent>
        <el-form-item v-if="actionType === 'interview'" label="面谈时间" required>
          <el-date-picker
            v-model="actionForm.time"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择面谈时间"
            style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="actionType === 'homeVisit'" label="家访时间" required>
          <el-date-picker
            v-model="actionForm.time"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择家访时间"
            style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="actionType === 'sign'" label="签约日期" required>
          <el-date-picker
            v-model="actionForm.date"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择签约日期"
            style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="actionType === 'reject'" label="驳回原因" required>
          <el-input
            v-model="actionForm.remark"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="请说明驳回原因，会同步给申请人" />
        </el-form-item>
        <el-form-item v-else-if="actionType === 'approve'" label="通过备注">
          <el-input v-model="actionForm.remark" type="textarea" :rows="3" maxlength="255" placeholder="可填写对申请人的补充说明" />
        </el-form-item>
        <el-form-item v-else-if="actionType === 'interview' || actionType === 'homeVisit'" :label="actionType === 'interview' ? '面谈备注' : '家访记录'">
          <el-input v-model="actionForm.remark" type="textarea" :rows="3" maxlength="255" placeholder="补充说明或现场记录" />
        </el-form-item>
        <p v-if="actionType === 'complete'" class="action-note">
          完成后将为该领养生成第 7 / 30 / 90 / 180 天共 4 次回访任务，请提醒领养人按时提交生活记录。
        </p>
      </el-form>
      <div slot="footer">
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionSubmitting" @click="submitAction">确认提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { applicationDetail } from '@/api/adoption';
import {
  shelterApplications,
  acceptApplication,
  interviewApplication,
  homeVisitApplication,
  approveApplication,
  rejectApplication,
  signApplication,
  completeApplication
} from '@/api/shelterBiz';
import { applicationStatusText } from '@/utils/format';

/**
 * 领养申请审核：列表 + 详情抽屉 + 流程动作
 */
export default {
  name: 'ShelterApplications',
  components: { StatusTag, EmptyState },
  data() {
    return {
      loading: true,
      list: [],
      total: 0,
      statusOptions: [
        { value: 'SUBMITTED', label: '已提交' },
        { value: 'REVIEWING', label: '审核中' },
        { value: 'INTERVIEW', label: '待面谈' },
        { value: 'HOME_VISIT', label: '待家访' },
        { value: 'APPROVED', label: '审核通过' },
        { value: 'SIGNED', label: '已签约' },
        { value: 'ADOPTED', label: '领养完成' },
        { value: 'REJECTED', label: '已驳回' }
      ],
      query: {
        pageNum: 1,
        pageSize: 10,
        status: null,
        keyword: ''
      },
      // 详情
      detailVisible: false,
      detailLoading: false,
      detail: {},
      // 流程动作
      actionVisible: false,
      actionType: '',
      actionRow: null,
      actionSubmitting: false,
      actionForm: { time: '', date: '', remark: '' }
    };
  },
  computed: {
    actionTitle() {
      return {
        accept: '受理申请',
        interview: '安排面谈',
        homeVisit: '安排家访',
        approve: '通过申请',
        reject: '驳回申请',
        sign: '签署领养协议',
        complete: '完成领养'
      }[this.actionType] || '流程操作';
    }
  },
  mounted() {
    this.load();
  },
  methods: {
    applicationStatusText,
    rowOps(status) {
      const map = {
        SUBMITTED: [{ type: 'accept', label: '受理' }, { type: 'reject', label: '驳回' }],
        REVIEWING: [{ type: 'interview', label: '面谈' }, { type: 'reject', label: '驳回' }],
        INTERVIEW: [{ type: 'homeVisit', label: '家访' }, { type: 'reject', label: '驳回' }],
        HOME_VISIT: [{ type: 'approve', label: '通过' }, { type: 'reject', label: '驳回' }],
        APPROVED: [{ type: 'sign', label: '签约' }],
        SIGNED: [{ type: 'complete', label: '完成' }]
      };
      return map[status] || [];
    },
    async load() {
      this.loading = true;
      try {
        const params = { ...this.query };
        if (!params.keyword) delete params.keyword;
        if (!params.status) delete params.status;
        const data = await shelterApplications(params);
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
      this.query = { pageNum: 1, pageSize: 10, status: null, keyword: '' };
      this.load();
    },
    refresh() {
      this.load();
      if (this.detailVisible && this.detail.id) {
        this.loadDetail(this.detail.id);
      }
    },

    // ---------------- 详情 ----------------
    openDetail(row) {
      this.detailVisible = true;
      this.loadDetail(row.id);
    },
    loadDetail(id) {
      this.detailLoading = true;
      applicationDetail(id)
        .then((data) => {
          this.detail = data || {};
        })
        .catch(() => {
          this.detail = {};
        })
        .finally(() => {
          this.detailLoading = false;
        });
    },
    housingText(v) {
      return { OWNED: '自有住房', RENTED: '租房', FAMILY: '与家人同住', DORM: '宿舍' }[v] || v || '—';
    },
    yesNo(v) {
      if (v === 1) return '是';
      if (v === 0) return '否';
      return '—';
    },
    operatorText(t) {
      return { USER: '申请人', SHELTER: '救助站', ADMIN: '平台' }[t] || t || '';
    },

    // ---------------- 流程动作 ----------------
    openAction(type, row) {
      if (type === 'accept') {
        this.$confirm(`受理「${row.applicantName}」对 ${row.petName} 的申请？受理后进入审核流程。`, '受理确认', {
          confirmButtonText: '确定受理',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => acceptApplication(row.id))
          .then(() => {
            this.$message.success('申请已受理，进入审核流程');
            this.refresh();
          })
          .catch(() => {});
        return;
      }
      if (type === 'complete') {
        this.$confirm(`确认完成「${row.applicantName}」对 ${row.petName} 的领养？完成后生成 4 次回访任务（第 7/30/90/180 天）。`, '完成领养确认', {
          confirmButtonText: '确定完成',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => completeApplication(row.id))
          .then(() => {
            this.$message.success('领养已完成，回访任务已生成');
            this.refresh();
          })
          .catch(() => {});
        return;
      }
      this.actionType = type;
      this.actionRow = row;
      this.actionForm = { time: '', date: '', remark: '' };
      this.actionVisible = true;
    },
    resetActionForm() {
      this.actionForm = { time: '', date: '', remark: '' };
      this.actionType = '';
      this.actionRow = null;
    },
    submitAction() {
      const row = this.actionRow;
      if (!row) return;
      const type = this.actionType;
      if ((type === 'interview' || type === 'homeVisit') && !this.actionForm.time) {
        this.$message.warning(type === 'interview' ? '请选择面谈时间' : '请选择家访时间');
        return;
      }
      if (type === 'sign' && !this.actionForm.date) {
        this.$message.warning('请选择签约日期');
        return;
      }
      if (type === 'reject' && !this.actionForm.remark.trim()) {
        this.$message.warning('请填写驳回原因');
        return;
      }
      this.actionSubmitting = true;
      const remark = this.actionForm.remark.trim();
      const request = {
        interview: () => interviewApplication(row.id, { interviewTime: this.actionForm.time, remark }),
        homeVisit: () => homeVisitApplication(row.id, { homeVisitTime: this.actionForm.time, remark }),
        approve: () => approveApplication(row.id, { remark }),
        reject: () => rejectApplication(row.id, { remark }),
        sign: () => signApplication(row.id, { signDate: this.actionForm.date })
      }[type]();
      request
        .then(() => {
          this.$message.success(this.actionTitle + '完成');
          this.actionVisible = false;
          this.refresh();
        })
        .catch(() => {})
        .finally(() => {
          this.actionSubmitting = false;
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

.detail-drawer {
  padding: 0 $space-6 $space-6;
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-3;
  margin-bottom: $space-4;

  &__no {
    font-size: $font-w-body-l;
    font-weight: 600;
    color: $text-primary;
  }
}

.detail-pet {
  @include card;

  display: flex;
  gap: $space-3;
  padding: $space-3;
  border-radius: $radius-base;

  img {
    width: 64px;
    height: 48px;
    border-radius: $radius-small;
    object-fit: cover;
    flex-shrink: 0;
  }

  &__name {
    font-size: $font-w-body-l;
    font-weight: 600;
    color: $text-primary;
  }

  &__shelter {
    margin-top: 2px;
    font-size: $font-w-caption;
    color: $text-secondary;
  }
}

.detail-section-title {
  margin: $space-6 0 $space-3;
  font-size: $font-w-body-l;
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

.flow-title {
  font-size: $font-w-body;
  color: $text-primary;
}

.flow-operator {
  font-size: $font-w-caption;
  color: $text-secondary;
}

.flow-remark {
  margin-top: $space-1;
  font-size: $font-w-caption;
  color: $text-secondary;
}

.detail-empty {
  font-size: $font-w-body;
  color: $text-secondary;
}

.action-note {
  font-size: $font-w-body;
  line-height: $lh-w-body;
  color: $text-secondary;
  background: $color-primary-light;
  border-radius: $radius-base;
  padding: $space-3 $space-4;
}
</style>


