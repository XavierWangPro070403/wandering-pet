<template>
  <div class="my-applications">
    <div class="wrap">
      <header class="page-head">
        <h1>我的领养申请</h1>
        <p>跟踪每份申请的审核进度，已提交与审核中的申请可以撤回。</p>
      </header>

      <div class="list-toolbar">
        <el-select
          v-model="query.status"
          size="small"
          clearable
          placeholder="全部状态"
          class="list-toolbar__status"
          @change="search">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <p class="list-toolbar__total num" aria-live="polite">共 {{ total }} 份申请</p>
      </div>

      <div v-if="loading" class="app-list">
        <div v-for="i in 3" :key="i" class="app-list__skeleton">
          <el-skeleton :rows="3" animated />
        </div>
      </div>

      <div v-else-if="list.length" class="app-list">
        <article
          v-for="a in list"
          :key="a.id"
          class="app-row"
          role="button"
          tabindex="0"
          @click="goProgress(a)"
          @keydown.enter="goProgress(a)">
          <img class="app-row__cover" :src="a.petCover" :alt="`${a.petName}的照片`" loading="lazy" />
          <div class="app-row__info">
            <h3 class="app-row__name">{{ a.petName }}</h3>
            <p class="app-row__no num">申请号 {{ a.applyNo }}</p>
            <p class="app-row__meta">
              {{ a.shelterName || '救助站' }}
              <template v-if="a.city"> · {{ a.city }}</template>
              · 申请于 {{ a.createTime }}
            </p>
          </div>
          <div class="app-row__status">
            <StatusTag :status="a.status" />
          </div>
          <div class="app-row__actions">
            <el-button size="small" @click.stop="goProgress(a)">查看进度</el-button>
            <el-button v-if="canCancel(a)" size="small" @click.stop="openCancel(a)">撤回申请</el-button>
          </div>
        </article>
      </div>

      <EmptyState
        v-else
        title="还没有领养申请"
        desc="先看看待领养列表，找到合眼缘的宠物后即可提交申请。"
        action-text="浏览待领养"
        @action="$router.push('/pets')" />

      <el-pagination
        v-if="total > query.pageSize"
        class="pagination"
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="query.pageSize"
        :current-page.sync="query.pageNum"
        @current-change="load" />
    </div>

    <!-- 撤回确认弹窗 -->
    <el-dialog title="撤回申请" :visible.sync="cancelVisible" width="420px" top="16vh">
      <p class="cancel-text">
        撤回后该申请终止，宠物将重新开放给其他申请人。确定撤回申请
        <span class="num cancel-text__no">{{ cancelTarget && cancelTarget.applyNo }}</span> 吗？
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
import { myApplications, cancelApplication } from '@/api/adoption';

const STATUS_OPTIONS = [
  'SUBMITTED', 'REVIEWING', 'INTERVIEW', 'HOME_VISIT', 'APPROVED',
  'SIGNED', 'ADOPTED', 'REJECTED', 'CANCELED', 'EXPIRED', 'RETURNED'
];

export default {
  name: 'MyApplicationsPage',
  components: { EmptyState, StatusTag },
  data() {
    return {
      loading: true,
      list: [],
      total: 0,
      statusOptions: STATUS_OPTIONS.map((value) => ({ value, label: this.applicationStatusText(value) })),
      query: {
        pageNum: 1,
        pageSize: 8,
        status: null
      },
      cancelVisible: false,
      cancelSubmitting: false,
      cancelTarget: null
    };
  },
  async mounted() {
    await this.load();
  },
  methods: {
    applicationStatusText: (s) => ({
      DRAFT: '草稿', SUBMITTED: '已提交', REVIEWING: '审核中', INTERVIEW: '待面谈', HOME_VISIT: '待家访',
      APPROVED: '审核通过', SIGNED: '已签约', ADOPTED: '领养完成', REJECTED: '已驳回',
      CANCELED: '已撤回', EXPIRED: '已过期', RETURNED: '已退养'
    }[s] || s),
    canCancel(a) {
      return a.status === 'SUBMITTED' || a.status === 'REVIEWING';
    },
    async load() {
      this.loading = true;
      try {
        const data = await myApplications(this.query);
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
    goProgress(a) {
      this.$router.push(`/my/applications/${a.id}`);
    },
    openCancel(a) {
      this.cancelTarget = a;
      this.cancelVisible = true;
    },
    async doCancel() {
      if (!this.cancelTarget) return;
      this.cancelSubmitting = true;
      try {
        await cancelApplication(this.cancelTarget.id);
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

.my-applications {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
  padding: 40px 0 80px;
}

.page-head {
  margin-bottom: 24px;

  h1 {
    font-size: $font-h3;
    line-height: $lh-h3;
  }

  p {
    margin-top: 8px;
    font-size: 14px;
    color: $text-secondary;
  }
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;

  &__status {
    width: 180px;
  }

  &__total {
    font-size: 14px;
    color: $text-regular;
  }
}

.app-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.app-list__skeleton {
  @include card;
  padding: 20px;
}

.app-row {
  @include card;
  @include card-hover;

  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  cursor: pointer;

  &:focus-visible {
    @include focus-ring;
  }

  @media (max-width: 640px) {
    grid-template-columns: 64px minmax(0, 1fr);
    grid-auto-rows: auto;

    .app-row__status,
    .app-row__actions {
      grid-column: 2;
      justify-self: start;
    }
  }

  &__cover {
    width: 64px;
    height: 64px;
    border-radius: $radius-base;
    object-fit: cover;
    background: $bg-page;
    display: block;
  }

  &__info {
    min-width: 0;
  }

  &__name {
    font-size: 16px;
    line-height: 24px;
  }

  &__no {
    margin-top: 2px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__meta {
    margin-top: 2px;
    font-size: 13px;
    color: $text-secondary;
    @include tabular-nums;
  }

  &__actions {
    display: flex;
    gap: 8px;

    .el-button + .el-button {
      margin-left: 0;
    }
  }
}

.pagination {
  margin-top: 32px;
  text-align: right;
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
