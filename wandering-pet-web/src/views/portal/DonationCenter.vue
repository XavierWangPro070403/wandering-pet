<template>
  <div class="donation-center">
    <div class="wrap">
      <!-- 第 1 段：页面头 -->
      <header class="page-head">
        <h1>捐赠中心</h1>
        <p>善款用于待领养宠物的免疫、绝育与治疗，每笔捐赠都可查询明细。</p>
      </header>

      <!-- 第 2 段：进行中的项目 -->
      <section aria-labelledby="sec-projects">
        <h2 id="sec-projects" class="section-title">进行中的募捐项目</h2>

        <div v-if="loading" class="project-grid">
          <div v-for="i in 2" :key="i" class="project-skeleton">
            <el-skeleton :rows="5" animated />
          </div>
        </div>

        <div v-else-if="projects.length" class="project-grid">
          <article
            v-for="p in projects"
            :key="p.id"
            class="project-card"
            :class="{ active: activeProject && activeProject.id === p.id }"
            role="button"
            tabindex="0"
            @click="openDetail(p)"
            @keydown.enter="openDetail(p)">
            <div class="project-card__media">
              <img :src="p.cover" :alt="`项目封面：${p.title}`" loading="lazy" />
            </div>
            <div class="project-card__body">
              <h3 class="project-card__title">{{ p.title }}</h3>
              <p class="project-card__shelter">{{ p.shelterName }} · {{ p.shelterCity }}</p>
              <div class="project-card__bar" role="img" :aria-label="`筹款进度 ${p.progress}%`">
                <span :style="{ width: Math.min(p.progress || 0, 100) + '%' }"></span>
              </div>
              <p class="project-card__meta num">
                已筹 {{ money(p.raisedAmount) }} / 目标 {{ money(p.targetAmount) }} · {{ p.donorCount }} 人捐赠
              </p>
              <div class="project-card__actions">
                <el-button type="primary" size="small" @click.stop="openDonate(p)">捐赠善款</el-button>
                <el-button size="small" @click.stop="openDetail(p)">查看详情</el-button>
              </div>
            </div>
          </article>
        </div>

        <EmptyState
          v-else
          title="暂无进行中的募捐项目"
          desc="新项目上线后会出现在这里，可以先看看待领养的宠物。"
          action-text="浏览待领养"
          @action="$router.push('/pets')" />
      </section>

      <!-- 第 3 段：项目详情（点击展开） -->
      <section v-if="activeProject" ref="detailSection" class="detail-block" aria-label="项目详情">
        <header class="detail-block__head">
          <h2>{{ activeProject.title }}</h2>
          <el-button type="text" icon="el-icon-close" aria-label="收起详情" @click="closeDetail">收起详情</el-button>
        </header>
        <p class="detail-block__meta num">
          {{ activeProject.shelterName }} · 募捐期 {{ activeProject.startDate || '—' }} 至 {{ activeProject.endDate || '—' }} · 进度 {{ activeProject.progress }}%
        </p>

        <div v-if="detailLoading" class="detail-block__loading">
          <el-skeleton :rows="4" animated />
        </div>

        <template v-else>
          <p class="detail-block__content">{{ activeProject.content || '该项目暂未填写详细说明，可联系救助站了解善款安排。' }}</p>

          <div class="detail-block__grid">
            <!-- 捐赠明细 -->
            <div class="detail-pane">
              <h3>捐赠明细</h3>
              <el-table v-if="records.length" :data="records" size="small">
                <el-table-column label="捐赠人" prop="donorName" min-width="110" />
                <el-table-column label="金额" min-width="100">
                  <template slot-scope="scope">
                    <span class="num">{{ money(scope.row.amount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="留言" prop="message" min-width="140" show-overflow-tooltip />
                <el-table-column label="时间" min-width="150">
                  <template slot-scope="scope">
                    <span class="num">{{ scope.row.payTime }}</span>
                  </template>
                </el-table-column>
              </el-table>
              <p v-else class="detail-pane__empty">还没有捐赠记录，第一笔善款从这里开始。</p>
            </div>

            <!-- 捐赠榜 -->
            <div class="detail-pane">
              <h3>捐赠榜</h3>
              <ol v-if="rank.length" class="rank-list">
                <li v-for="r in rank" :key="r.rank" class="rank-list__item">
                  <span class="rank-list__no num" :class="{ top: r.rank <= 3 }">{{ r.rank }}</span>
                  <span class="rank-list__name">{{ r.donorName }}</span>
                  <span class="rank-list__amount num">{{ money(r.totalAmount) }} · {{ r.donationCount }} 次</span>
                </li>
              </ol>
              <p v-else class="detail-pane__empty">捐赠榜虚位以待。</p>
            </div>
          </div>

          <div class="detail-block__cta">
            <el-button type="primary" size="large" @click="openDonate(activeProject)">捐赠善款</el-button>
          </div>
        </template>
      </section>

      <!-- 第 4 段：说明 -->
      <section class="about-block" aria-labelledby="sec-about">
        <h2 id="sec-about" class="section-title">捐赠说明</h2>
        <div class="about-grid">
          <div class="about-item">
            <h3>善款用途</h3>
            <p>全部善款用于待领养宠物的免疫、绝育、驱虫与治疗，以及救助站的日常饲喂开销。</p>
          </div>
          <div class="about-item">
            <h3>公示频率</h3>
            <p>每个项目每 7 天在项目详情与机构主页公示一次收支明细，结项后 15 天内发布总结。</p>
          </div>
          <div class="about-item">
            <h3>联系方式</h3>
            <p>对善款使用有疑问，可通过站内消息联系对应救助站，或发送邮件至 contact@wanderingpet.example。</p>
          </div>
        </div>
      </section>
    </div>

    <!-- 捐赠弹窗 -->
    <el-dialog :title="donateProject ? `捐赠：${donateProject.title}` : '捐赠'" :visible.sync="donateVisible" width="460px" top="10vh">
      <div class="donate-form">
        <p class="donate-form__label">选择金额</p>
        <div class="donate-form__chips">
          <button
            v-for="a in quickAmounts"
            :key="a"
            class="chip"
            :class="{ active: donateForm.amount === a }"
            type="button"
            @click="donateForm.amount = a">
            {{ a }} 元
          </button>
        </div>
        <el-input-number
          v-model="donateForm.amount"
          class="donate-form__number"
          :min="0.01"
          :max="9999999"
          :precision="2"
          :step="10"
          controls-position="right" />

        <el-checkbox v-model="donateForm.anonymous" class="donate-form__anon">匿名展示这笔捐赠</el-checkbox>

        <p class="donate-form__label">留言（选填）</p>
        <el-input
          v-model="donateForm.message"
          type="textarea"
          :rows="3"
          maxlength="255"
          show-word-limit
          placeholder="写一句想对救助站说的话" />
      </div>
      <template slot="footer">
        <el-button @click="donateVisible = false">再想想</el-button>
        <el-button type="primary" :loading="donateSubmitting" @click="submitDonate">确认捐赠</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue';
import { ongoingProjects, projectDetail, donate, projectRecords, projectRank } from '@/api/donation';
import { money } from '@/utils/format';

export default {
  name: 'DonationCenterPage',
  components: { EmptyState },
  data() {
    return {
      loading: true,
      projects: [],
      activeProject: null,
      detailLoading: false,
      records: [],
      rank: [],
      donateVisible: false,
      donateSubmitting: false,
      donateProject: null,
      quickAmounts: [50, 100, 200, 500],
      donateForm: {
        amount: 50,
        anonymous: false,
        message: ''
      }
    };
  },
  async mounted() {
    await this.load();
  },
  methods: {
    money,
    async load() {
      this.loading = true;
      try {
        this.projects = (await ongoingProjects()) || [];
      } catch (e) {
        this.projects = [];
      } finally {
        this.loading = false;
      }
    },
    async openDetail(p) {
      this.activeProject = p;
      this.detailLoading = true;
      this.records = [];
      this.rank = [];
      this.$nextTick(() => {
        if (this.$refs.detailSection) {
          this.$refs.detailSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      });
      try {
        const [full, records, rank] = await Promise.all([
          projectDetail(p.id),
          projectRecords(p.id, 10).catch(() => []),
          projectRank(p.id, 5).catch(() => [])
        ]);
        this.activeProject = { ...p, ...full };
        this.records = records || [];
        this.rank = rank || [];
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.detailLoading = false;
      }
    },
    closeDetail() {
      this.activeProject = null;
      this.records = [];
      this.rank = [];
    },
    openDonate(p) {
      this.donateProject = p;
      this.donateForm = { amount: 50, anonymous: false, message: '' };
      this.donateVisible = true;
    },
    async submitDonate() {
      const amount = Number(this.donateForm.amount);
      if (!amount || amount < 0.01) {
        this.$message.warning('请输入正确的捐赠金额');
        return;
      }
      this.donateSubmitting = true;
      try {
        await donate(this.donateProject.id, {
          amount,
          isAnonymous: this.donateForm.anonymous ? 1 : 0,
          message: this.donateForm.message.trim()
        });
        this.$message.success('捐赠成功（模拟支付），感谢你的支持');
        this.donateVisible = false;
        await this.load();
        if (this.activeProject) {
          this.openDetail(this.activeProject);
        }
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.donateSubmitting = false;
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

.donation-center {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
  padding: 40px 0 80px;
}

.page-head {
  margin-bottom: 32px;

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

.section-title {
  font-size: 20px;
  line-height: 30px;
  margin-bottom: 20px;
}

// 项目卡
.project-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

.project-skeleton {
  @include card;
  padding: 16px;
}

.project-card {
  @include card;
  @include card-hover;

  border-radius: $radius-card;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;

  &:focus-visible {
    @include focus-ring;
  }

  &.active {
    border-color: $color-primary;
  }

  &__media {
    aspect-ratio: 4 / 3;
    // 防止 height:100% 子图按自然比例撑破比例框（竖图会把卡片撑高）
    min-height: 0;
    background: $bg-page;
    @include img-zoom;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    padding: 16px 20px 20px;
    display: flex;
    flex-direction: column;
    flex: 1;
  }

  &__title {
    font-size: 17px;
    line-height: 24px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__shelter {
    margin-top: 4px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__bar {
    margin-top: 12px;
    height: 8px;
    border-radius: $radius-circle;
    background: $bg-page;
    overflow: hidden;

    span {
      display: block;
      height: 100%;
      border-radius: $radius-circle;
      background: $color-primary;
      transition: width $duration-panel $ease-out-quart;
    }
  }

  &__meta {
    margin-top: 8px;
    font-size: 13px;
    color: $text-regular;
  }

  &__actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;

    .el-button + .el-button {
      margin-left: 0;
    }
  }
}

// 详情区块
.detail-block {
  @include card;

  margin-top: 24px;
  padding: 24px;
  scroll-margin-top: calc(#{$portal-header-h} + 16px);

  &__head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;

    h2 {
      font-size: 20px;
      line-height: 30px;
    }
  }

  &__meta {
    margin-top: 8px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__loading {
    margin-top: 16px;
  }

  &__content {
    margin-top: 16px;
    font-size: 15px;
    line-height: 26px;
    color: $text-regular;
    white-space: pre-line;
  }

  &__grid {
    margin-top: 24px;
    display: grid;
    grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
    gap: 24px;

    @media (max-width: 991px) {
      grid-template-columns: 1fr;
    }
  }

  &__cta {
    margin-top: 24px;
    text-align: center;
  }
}

.detail-pane {
  background: $bg-page;
  border-radius: $radius-card;
  padding: 20px;

  h3 {
    font-size: 15px;
    margin-bottom: 12px;
  }

  &__empty {
    font-size: 13px;
    color: $text-secondary;
  }
}

.rank-list {
  margin: 0;
  padding: 0;
  list-style: none;

  &__item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 0;
    border-top: 1px solid $border-color;
    font-size: 14px;

    &:first-child {
      border-top: none;
    }
  }

  &__no {
    width: 24px;
    height: 24px;
    border-radius: $radius-circle;
    background: $border-color;
    color: $text-secondary;
    font-size: 12px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &.top {
      background: $color-primary;
      color: $text-primary;
      font-weight: 600;
    }
  }

  &__name {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $text-primary;
  }

  &__amount {
    color: $text-secondary;
    font-size: 13px;
  }
}

// 说明段
.about-block {
  margin-top: 48px;
}

.about-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

.about-item {
  @include card;
  padding: 24px;

  h3 {
    font-size: 15px;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    line-height: 24px;
    color: $text-regular;
  }
}

// 捐赠弹窗
.donate-form {
  &__label {
    font-size: 14px;
    color: $text-primary;
    margin: 4px 0 8px;
  }

  &__chips {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  &__number {
    width: 100%;
    margin-bottom: 16px;
  }

  &__anon {
    margin-bottom: 16px;
    display: block;
  }
}

.chip {
  flex: 1;
  height: 36px;
  border-radius: $radius-base;
  border: 1px solid $border-color;
  background: $bg-card;
  color: $text-regular;
  font-size: 14px;
  cursor: pointer;
  transition: all $duration-hover-in $ease-out-quart;

  &:hover {
    border-color: $color-primary;
    color: $color-primary-text;
  }

  &.active {
    background: $color-primary-light;
    border-color: $color-primary;
    color: $color-primary-text;
    font-weight: 600;
  }
}
</style>
