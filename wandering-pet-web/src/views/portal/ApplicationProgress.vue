<template>
  <div class="app-progress">
    <div class="wrap">
      <header class="page-head">
        <h1>领养进度</h1>
        <p>从提交申请到领养完成，每个环节的记录都在这里。</p>
        <el-button type="text" class="page-head__back" icon="el-icon-back" @click="$router.push('/my/applications')">返回我的申请</el-button>
      </header>

      <template v-if="loading">
        <div class="progress-card"><el-skeleton :rows="5" animated /></div>
      </template>

      <template v-else>
        <!-- 摘要卡 -->
        <section class="summary-card" aria-label="申请摘要">
          <img class="summary-card__cover" :src="detail.petCover" :alt="`${detail.petName}的照片`" />
          <div class="summary-card__info">
            <div class="summary-card__head">
              <h2 class="summary-card__name">{{ detail.petName }}</h2>
              <StatusTag :status="detail.status" />
            </div>
            <p class="summary-card__no num">申请号 {{ detail.applyNo }}</p>
            <dl class="summary-card__facts">
              <div><dt>受理机构</dt><dd>{{ detail.shelterName || '—' }}</dd></div>
              <div><dt>申请人</dt><dd>{{ detail.applicantName }} · {{ detail.applicantPhone }}</dd></div>
              <div><dt>现居住城市</dt><dd>{{ detail.city || '—' }}</dd></div>
              <div><dt>提交时间</dt><dd class="num">{{ detail.createTime }}</dd></div>
              <template v-if="detail.interviewTime">
                <div><dt>面谈时间</dt><dd class="num">{{ detail.interviewTime }}</dd></div>
              </template>
              <template v-if="detail.homeVisitTime">
                <div><dt>家访时间</dt><dd class="num">{{ detail.homeVisitTime }}</dd></div>
              </template>
            </dl>
          </div>
          <div class="summary-card__actions">
            <el-button type="primary" @click="contactShelter">联系机构</el-button>
            <el-button v-if="canCancel" @click="cancelVisible = true">撤回申请</el-button>
          </div>
        </section>

        <!-- 终态提示（驳回/过期/撤回/退养） -->
        <div v-if="isFail" class="fail-banner" role="alert">
          <i class="el-icon-warning-outline" aria-hidden="true"></i>
          <div>
            <p class="fail-banner__title">{{ failTitle }}</p>
            <p class="fail-banner__reason">原因：{{ failReason }}</p>
          </div>
        </div>

        <!-- 横向步骤条 -->
        <section class="progress-card" aria-label="审核流程">
          <ol class="flow">
            <li
              v-for="(s, i) in flowSteps"
              :key="s.status"
              class="flow__step"
              :class="stepClass(i)">
              <span class="flow__dot"></span>
              <span class="flow__label">{{ s.label }}</span>
            </li>
          </ol>
          <p class="progress-card__note">{{ flowNote }}</p>
        </section>

        <!-- 状态流水 -->
        <section class="progress-card" aria-label="状态流水">
          <h3 class="progress-card__title">状态流水</h3>
          <el-timeline v-if="logs.length" class="flow-timeline">
            <el-timeline-item
              v-for="log in logs"
              :key="log.id"
              :timestamp="log.createTime"
              :type="logTone(log.toStatus)"
              placement="top">
              <div class="flow-log">
                <p class="flow-log__head">
                  <span class="flow-log__operator">{{ operatorText(log.operatorType) }}</span>
                  <span class="flow-log__status">{{ statusText(log.toStatus) }}</span>
                </p>
                <p v-if="log.remark" class="flow-log__remark">{{ log.remark }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <EmptyState v-else title="暂无流程记录" desc="救助站受理后会在这里更新每一步记录。" />
        </section>
      </template>
    </div>

    <!-- 撤回确认弹窗 -->
    <el-dialog title="撤回申请" :visible.sync="cancelVisible" width="420px" top="16vh">
      <p class="cancel-text">
        撤回后该申请终止，宠物将重新开放给其他申请人。确定撤回申请
        <span class="num cancel-text__no">{{ detail.applyNo }}</span> 吗？
      </p>
      <template slot="footer">
        <el-button @click="cancelVisible = false">再想想</el-button>
        <el-button type="primary" :loading="cancelSubmitting" @click="doCancel">确认撤回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue';
import StatusTag from '@/components/StatusTag.vue';
import { applicationDetail, flowLogs, cancelApplication } from '@/api/adoption';
import { getOrCreateSession } from '@/api/chat';

const FAIL_STATUSES = ['REJECTED', 'EXPIRED', 'CANCELED', 'RETURNED'];

export default {
  name: 'ApplicationProgressPage',
  components: { EmptyState, StatusTag },
  data() {
    return {
      loading: true,
      detail: {},
      logs: [],
      flowSteps: [
        { status: 'SUBMITTED', label: '已提交' },
        { status: 'REVIEWING', label: '受理审核' },
        { status: 'INTERVIEW', label: '面谈' },
        { status: 'HOME_VISIT', label: '家访' },
        { status: 'APPROVED', label: '审核通过' },
        { status: 'SIGNED', label: '签约' },
        { status: 'ADOPTED', label: '领养完成' }
      ],
      cancelVisible: false,
      cancelSubmitting: false
    };
  },
  computed: {
    statusText: () => (s) => ({
      DRAFT: '草稿', SUBMITTED: '已提交', REVIEWING: '受理审核', INTERVIEW: '面谈', HOME_VISIT: '家访',
      APPROVED: '审核通过', SIGNED: '签约', ADOPTED: '领养完成', REJECTED: '已驳回',
      CANCELED: '已撤回', EXPIRED: '已过期', RETURNED: '已退养'
    }[s] || s),
    isFail() {
      return FAIL_STATUSES.includes(this.detail.status);
    },
    canCancel() {
      return this.detail.status === 'SUBMITTED' || this.detail.status === 'REVIEWING';
    },
    currentIndex() {
      return this.flowSteps.findIndex((s) => s.status === this.detail.status);
    },
    reachedIndex() {
      if (!this.isFail) return this.currentIndex;
      // 终止时按最后一条流水回溯到走到的节点
      for (let i = (this.logs || []).length - 1; i >= 0; i -= 1) {
        const idx = this.flowSteps.findIndex((s) => s.status === this.logs[i].toStatus);
        if (idx >= 0) return idx;
      }
      return 0;
    },
    failTitle() {
      return {
        REJECTED: '申请未通过审核',
        EXPIRED: '申请已过期',
        CANCELED: '申请已撤回',
        RETURNED: '宠物已被退养'
      }[this.detail.status] || '申请已终止';
    },
    failReason() {
      if (this.detail.status === 'REJECTED') return this.detail.auditRemark || '未满足救助站的领养条件，可联系机构了解详情';
      if (this.detail.status === 'EXPIRED') return '超过有效期未完成签约，申请自动失效';
      if (this.detail.status === 'CANCELED') return '申请人主动撤回';
      if (this.detail.status === 'RETURNED') return this.lastRemark('RETURNED') || this.detail.auditRemark || '领养后退回救助站';
      return '—';
    },
    flowNote() {
      if (this.isFail) return '流程在该环节终止，可在下方状态流水查看每条记录。';
      if (this.detail.status === 'ADOPTED') return '领养已完成，记得按约定配合完成定期回访。';
      return '当前环节由救助站推进，有更新时会在消息中心通知你。';
    }
  },
  watch: {
    '$route.params.id'() {
      this.load();
    }
  },
  async mounted() {
    await this.load();
  },
  methods: {
    operatorText(t) {
      return { USER: '用户', SHELTER: '机构', SYSTEM: '系统' }[t] || t;
    },
    logTone(toStatus) {
      if (['REJECTED', 'CANCELED', 'RETURNED'].includes(toStatus)) return 'danger';
      if (['ADOPTED', 'APPROVED', 'SIGNED'].includes(toStatus)) return 'success';
      return 'primary';
    },
    lastRemark(toStatus) {
      const logs = this.logs || [];
      for (let i = logs.length - 1; i >= 0; i -= 1) {
        if (logs[i].toStatus === toStatus && logs[i].remark) return logs[i].remark;
      }
      return '';
    },
    stepClass(i) {
      if (!this.isFail && this.currentIndex >= i) {
        return { 'is-done': this.currentIndex > i, 'is-current': this.currentIndex === i };
      }
      if (this.isFail && this.reachedIndex >= i) {
        return { 'is-done': this.reachedIndex > i, 'is-interrupt': this.reachedIndex === i };
      }
      return {};
    },
    async load() {
      this.loading = true;
      const id = this.$route.params.id;
      try {
        const [detail, logs] = await Promise.all([
          applicationDetail(id),
          flowLogs(id).catch(() => [])
        ]);
        this.detail = detail || {};
        this.logs = (logs && logs.length ? logs : detail.flowLogs) || [];
      } catch (e) {
        this.$message.error('申请不存在或无权查看');
        this.$router.replace('/my/applications');
      } finally {
        this.loading = false;
      }
    },
    async contactShelter() {
      if (!this.detail.shelterId) {
        this.$message.warning('暂未获取到受理机构信息');
        return;
      }
      try {
        await getOrCreateSession(this.detail.shelterId);
        this.$router.push(`/me?tab=chat&session=${this.detail.shelterId}`);
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async doCancel() {
      this.cancelSubmitting = true;
      try {
        await cancelApplication(this.detail.id);
        this.$message.success('申请已撤回');
        this.cancelVisible = false;
        this.load();
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.cancelSubmitting = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.wrap {
  @include wrap;
}

.app-progress {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
  padding: 40px 0 80px;
}

.page-head {
  margin-bottom: 24px;
  position: relative;

  h1 {
    font-size: $font-h3;
    line-height: $lh-h3;
  }

  p {
    margin-top: 8px;
    font-size: 14px;
    color: $text-secondary;
  }

  &__back {
    position: absolute;
    right: 0;
    top: 4px;
    padding: 0;
  }
}

.progress-card {
  @include card;

  padding: 24px;
  margin-bottom: 24px;

  &__title {
    font-size: 16px;
    margin-bottom: 16px;
  }

  &__note {
    margin-top: 20px;
    font-size: 13px;
    color: $text-secondary;
    text-align: center;
  }
}

// 摘要卡
.summary-card {
  @include card;

  display: grid;
  grid-template-columns: 128px minmax(0, 1fr) auto;
  gap: 20px;
  padding: 20px;
  margin-bottom: 24px;

  @media (max-width: 767px) {
    grid-template-columns: 1fr;
  }

  &__cover {
    width: 128px;
    aspect-ratio: 4 / 3;
    border-radius: $radius-base;
    object-fit: cover;
    background: $bg-page;
    display: block;

    @media (max-width: 767px) {
      width: 100%;
    }
  }

  &__info {
    min-width: 0;
  }

  &__head {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__name {
    font-size: 20px;
    line-height: 28px;
  }

  &__no {
    margin-top: 4px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__facts {
    margin: 16px 0 0;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px 16px;

    dt {
      font-size: 13px;
      color: $text-secondary;
    }

    dd {
      margin: 2px 0 0;
      font-size: 14px;
      color: $text-primary;
      @include tabular-nums;
    }
  }

  &__actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
    justify-content: center;

    .el-button {
      margin: 0;
    }

    @media (max-width: 767px) {
      flex-direction: row;

      .el-button {
        flex: 1;
      }
    }
  }
}

// 终态提示
.fail-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  border-radius: $radius-card;
  background: $color-danger-light;
  color: $color-danger-text;
  margin-bottom: 24px;

  i {
    font-size: 22px;
    line-height: 1;
    margin-top: 2px;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
  }

  &__reason {
    margin-top: 4px;
    font-size: 13px;
  }
}

// 横向步骤条
.flow {
  margin: 8px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  overflow-x: auto;

  &__step {
    flex: 1;
    min-width: 72px;
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding-top: 4px;

    // 节点间连线
    &:not(:first-child)::before {
      content: "";
      position: absolute;
      top: calc(4px + 9px);
      left: -50%;
      width: 100%;
      height: 2px;
      background: $border-color;
    }

    &.is-done::before,
    &.is-current::before,
    &.is-interrupt::before {
      background: $color-primary;
    }
  }

  &__dot {
    width: 18px;
    height: 18px;
    border-radius: $radius-circle;
    background: $bg-card;
    border: 2px solid $border-color;
    position: relative;
    z-index: 1;
    flex-shrink: 0;
  }

  &__label {
    font-size: 13px;
    color: $text-secondary;
    white-space: nowrap;
  }

  &__step.is-done {
    .flow__dot {
      background: $color-primary;
      border-color: $color-primary;
    }

    .flow__label {
      color: $text-regular;
    }
  }

  &__step.is-current {
    .flow__dot {
      border-color: $color-primary;
      box-shadow: 0 0 0 4px $color-primary-light;
    }

    .flow__label {
      color: $color-primary-text;
      font-weight: 600;
    }
  }

  &__step.is-interrupt {
    .flow__dot {
      background: $color-danger;
      border-color: $color-danger;
      box-shadow: 0 0 0 4px $color-danger-light;
    }

    .flow__label {
      color: $color-danger-text;
      font-weight: 600;
    }
  }
}

// 流水时间轴
.flow-timeline {
  max-width: 640px;
  padding: 8px 0 0 4px;
}

.flow-log {
  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
  }

  &__operator {
    padding: 2px 8px;
    border-radius: $radius-small;
    background: $bg-page;
    color: $text-secondary;
    font-size: 12px;
  }

  &__status {
    color: $text-primary;
    font-weight: 500;
  }

  &__remark {
    margin-top: 4px;
    font-size: 13px;
    line-height: 22px;
    color: $text-regular;
  }
}

.cancel-text {
  font-size: 14px;
  line-height: 22px;
  color: $text-regular;

  &__no {
    color: $text-primary;
    font-weight: 500;
  }
}
</style>
