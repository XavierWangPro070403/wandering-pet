<template>
  <div class="donations">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1 class="page-head__title">募捐项目管理</h1>
        <p class="page-head__desc">发起救助募捐项目，跟进筹款进度与捐赠情况。</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新建募捐项目</el-button>
    </div>

    <!-- 项目卡列表（2 列） -->
    <div v-if="loading" class="project-grid">
      <div v-for="i in 4" :key="i" class="project-card">
        <el-skeleton :rows="5" animated />
      </div>
    </div>
    <div v-else-if="projects.length" class="project-grid">
      <div v-for="p in projects" :key="p.id" class="project-card">
        <div class="project-card__media">
          <img :src="p.cover" :alt="`${p.title}封面`" loading="lazy" />
        </div>
        <div class="project-card__body">
          <div class="project-card__head">
            <h2 class="project-card__title">{{ p.title }}</h2>
            <StatusTag :status="p.status === 'ONGOING' ? 'PUBLISHED' : p.status" :label="statusText(p.status)" />
          </div>
          <div class="progress-bar" role="progressbar" :aria-valuenow="p.progress || 0" aria-valuemin="0" aria-valuemax="100">
            <span class="progress-bar__fill" :style="{ width: Math.min(p.progress || 0, 100) + '%' }"></span>
          </div>
          <p class="project-card__meta num">
            已筹 <strong>{{ money(p.raisedAmount) }}</strong> / 目标 {{ money(p.targetAmount) }} ·
            {{ p.donorCount || 0 }} 人捐赠
          </p>
          <p class="project-card__date num" v-if="p.endDate">结束日期 {{ p.endDate }}</p>
          <div class="project-card__ops">
            <el-button size="small" :disabled="p.status === 'CLOSED'" @click="openDialog(p)">编辑</el-button>
            <el-button size="small" :disabled="p.status === 'CLOSED'" @click="close(p)">关闭</el-button>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty-card">
      <EmptyState
        title="暂无募捐项目"
        desc="为救助中的动物发起募捐，筹款进度与捐赠明细会公开展示。"
        action-text="新建募捐项目"
        @action="openDialog()" />
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      :title="form.id ? '编辑募捐项目' : '新建募捐项目'"
      :visible.sync="dialogVisible"
      width="720px"
      :close-on-click-modal="false"
      @closed="resetForm">
      <el-form ref="projectForm" :model="form" label-position="top" @submit.native.prevent>
        <el-form-item label="项目标题" required>
          <el-input v-model="form.title" placeholder="如：受伤小猫手术费募捐" maxlength="128" />
        </el-form-item>
        <el-form-item label="项目封面">
          <ImageUploader v-model="coverList" biz-type="donation" :limit="1" />
          <p class="form-tip">第一张图作为项目封面，建议使用 4:3 横图。</p>
        </el-form-item>
        <div class="form-row">
          <el-form-item label="目标金额（元）" required>
            <el-input-number
              v-model="form.targetAmount"
              :min="1"
              :precision="2"
              :step="100"
              controls-position="right"
              placeholder="如 20000"
              style="width: 100%" />
          </el-form-item>
          <el-form-item label="起止日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%" />
            <p class="form-tip">留空表示长期有效，用于展示筹款倒计时。</p>
          </el-form-item>
        </div>
        <el-form-item label="项目详情">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            maxlength="5000"
            show-word-limit
            placeholder="说明救助对象、费用构成与资金用途" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存项目</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import ImageUploader from '@/components/ImageUploader.vue';
import { shelterProjects, createProject, updateProject, closeProject } from '@/api/shelterBiz';
import { money } from '@/utils/format';

/**
 * 募捐项目管理：项目卡列表 + 新建/编辑弹窗 + 关闭确认
 */
export default {
  name: 'ShelterDonations',
  components: { StatusTag, EmptyState, ImageUploader },
  data() {
    return {
      loading: true,
      projects: [],
      dialogVisible: false,
      saving: false,
      coverList: [],
      dateRange: null,
      form: {
        id: null,
        title: '',
        targetAmount: undefined,
        content: ''
      }
    };
  },
  mounted() {
    this.load();
  },
  methods: {
    money,
    statusText(s) {
      return { ONGOING: '进行中', CLOSED: '已关闭' }[s] || s;
    },
    async load() {
      this.loading = true;
      try {
        this.projects = (await shelterProjects()) || [];
      } catch (e) {
        this.projects = [];
      } finally {
        this.loading = false;
      }
    },
    openDialog(project) {
      if (project) {
        this.form = {
          id: project.id,
          title: project.title || '',
          targetAmount: project.targetAmount != null ? Number(project.targetAmount) : undefined,
          content: project.content || ''
        };
        this.coverList = project.cover ? [{ objectName: '', url: project.cover }] : [];
        this.dateRange = project.startDate && project.endDate ? [project.startDate, project.endDate] : null;
      } else {
        this.resetForm();
      }
      this.dialogVisible = true;
    },
    resetForm() {
      this.form = { id: null, title: '', targetAmount: undefined, content: '' };
      this.coverList = [];
      this.dateRange = null;
    },
    save() {
      if (!this.form.title.trim()) {
        this.$message.warning('请输入项目标题');
        return;
      }
      if (!this.form.targetAmount || this.form.targetAmount < 1) {
        this.$message.warning('请输入目标金额（不低于 1 元）');
        return;
      }
      const payload = {
        title: this.form.title.trim(),
        cover: this.coverList.length ? this.coverList[0].url : '',
        targetAmount: this.form.targetAmount,
        startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : '',
        endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : '',
        content: this.form.content
      };
      this.saving = true;
      const request = this.form.id ? updateProject(this.form.id, payload) : createProject(payload);
      request
        .then(() => {
          this.$message.success(this.form.id ? '项目已更新' : '募捐项目已创建');
          this.dialogVisible = false;
          this.load();
        })
        .catch(() => {})
        .finally(() => {
          this.saving = false;
        });
    },
    close(project) {
      this.$confirm(`确定关闭项目「${project.title}」？关闭后停止接收捐赠，已筹款项按平台规则处理。`, '关闭项目确认', {
        confirmButtonText: '确定关闭',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => closeProject(project.id))
        .then(() => {
          this.$message.success('项目已关闭');
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

.project-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-4;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

.project-card {
  @include card;
  @include card-hover;

  overflow: hidden;

  &__media {
    aspect-ratio: 4 / 3;
    // 防止 height:100% 子图按自然比例撑破比例框（竖图会把卡片撑高）
    min-height: 0;
    overflow: hidden;
    background: $bg-page;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    padding: $space-5;
  }

  &__head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: $space-3;
  }

  &__title {
    font-size: $font-w-body-l;
    line-height: $lh-w-body-l;
  }

  &__meta {
    margin-top: $space-2;
    font-size: $font-w-body;
    color: $text-regular;

    strong {
      color: $color-primary-text;
    }
  }

  &__date {
    margin-top: $space-1;
    font-size: $font-w-caption;
    color: $text-secondary;
  }

  &__ops {
    margin-top: $space-4;
    display: flex;
    gap: $space-2;
  }
}

.progress-bar {
  margin-top: $space-4;
  height: 8px;
  border-radius: $radius-circle;
  background: $bg-page;
  overflow: hidden;

  &__fill {
    display: block;
    height: 100%;
    border-radius: $radius-circle;
    background: $color-primary;
    transition: width $duration-panel $ease-out-quart;
  }
}

.empty-card {
  @include card;

  padding: $space-6;
}

.form-row {
  display: grid;
  // minmax(0,1fr)：日期区间的最小内容宽度很大，用 1fr 会把两列挤成不等宽
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: $space-5;

  > .el-form-item {
    min-width: 0;
  }
}

.form-tip {
  margin-top: $space-2;
  font-size: $font-w-caption;
  line-height: $lh-w-caption;
  color: $text-secondary;
}

// 金额与日期统一在框内居中：左右内边距取齐，避免右侧步进器把文字顶偏
::v-deep .el-input-number.is-controls-right .el-input__inner {
  padding-left: 15px;
  padding-right: 15px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}
</style>


