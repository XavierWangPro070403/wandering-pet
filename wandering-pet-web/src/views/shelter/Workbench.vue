<template>
  <div class="workbench">
    <!-- 页面头：h1 + 说明 + 主行动按钮 -->
    <div class="page-head">
      <div>
        <h1 class="page-head__title">机构工作台</h1>
        <p class="page-head__desc">查看机构运营概况，处理待办领养与回访事务。</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="$router.push('/shelter/pets/edit')">发布宠物</el-button>
    </div>

    <!-- 统计卡一行 -->
    <div v-if="statsLoading" class="stat-row">
      <div v-for="i in 4" :key="i" class="stat-card">
        <el-skeleton :rows="2" animated />
      </div>
    </div>
    <div v-else class="stat-row">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <span class="stat-card__icon" :aria-hidden="true"><i :class="card.icon"></i></span>
        <div class="stat-card__meta">
          <p class="stat-card__value num" aria-live="polite">{{ statValue(card.value) }}</p>
          <p class="stat-card__label">{{ card.label }}</p>
        </div>
      </div>
    </div>

    <!-- 本月领养横幅 -->
    <div class="month-banner" aria-live="polite">
      <i class="el-icon-medal" aria-hidden="true"></i>
      <template v-if="!statsLoading && stats.monthAdopted">
        本月已陪伴 <strong class="num">{{ stats.monthAdopted }}</strong> 只宠物走进新家，感谢每一份认真负责的守护。
      </template>
      <template v-else-if="!statsLoading">本月暂无完成的领养，继续加油，新家庭正在路上。</template>
      <el-skeleton v-else :rows="1" animated style="width: 320px" />
    </div>

    <div class="workbench-grid">
      <!-- 快捷入口 -->
      <section class="quick-panel" aria-labelledby="quick-title">
        <h2 id="quick-title" class="panel-title">快捷入口</h2>
        <div class="quick-grid">
          <button
            v-for="q in quickEntries"
            :key="q.label"
            class="quick-card"
            type="button"
            @click="$router.push(q.path)">
            <span class="quick-card__icon" :aria-hidden="true"><i :class="q.icon"></i></span>
            <span class="quick-card__title">{{ q.label }}</span>
            <span class="quick-card__desc">{{ q.desc }}</span>
          </button>
        </div>
      </section>

      <!-- 机构信息卡 -->
      <section class="shelter-panel" aria-labelledby="shelter-title">
        <div class="shelter-panel__head">
          <h2 id="shelter-title" class="panel-title">机构信息</h2>
          <el-button type="text" size="small" icon="el-icon-edit-outline" @click="openEdit">维护机构信息</el-button>
        </div>
        <template v-if="!shelterLoading">
          <p class="shelter-panel__name">{{ shelter.name || '机构名称待完善' }}</p>
          <dl class="shelter-panel__facts">
            <div><dt>城市</dt><dd>{{ shelter.city || '—' }}{{ shelter.district ? ' · ' + shelter.district : '' }}</dd></div>
            <div><dt>地址</dt><dd>{{ shelter.address || '—' }}</dd></div>
            <div class="full"><dt>简介</dt><dd>{{ shelter.intro || '还没有填写机构简介，点击右上角完善。' }}</dd></div>
          </dl>
        </template>
        <el-skeleton v-else :rows="3" animated />
      </section>
    </div>

    <!-- 维护机构信息弹窗 -->
    <el-dialog
      title="维护机构信息"
      :visible.sync="editVisible"
      width="520px"
      :close-on-click-modal="false">
      <el-form ref="editForm" :model="editForm" label-position="top" @submit.native.prevent>
        <div class="readonly-row">
          <span class="readonly-row__label">机构名称</span>
          <span>{{ shelter.name || '—' }}</span>
          <span class="readonly-row__label">所在城市</span>
          <span>{{ shelter.city || '—' }}</span>
        </div>
        <el-form-item label="机构地址">
          <el-input v-model="editForm.address" placeholder="填写对外展示的详细地址" maxlength="120" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="editForm.contactName" placeholder="领养咨询联系人" maxlength="32" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.contactPhone" placeholder="用于领养人联系" maxlength="20" />
        </el-form-item>
        <el-form-item label="机构简介">
          <el-input
            v-model="editForm.intro"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="介绍救助站的成立时间、救助理念与领养流程" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveShelter">保存信息</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { myShelter, updateShelter } from '@/api/shelter';
import { shelterWorkbench } from '@/api/shelterBiz';

/**
 * 机构工作台（§8 工作台页规则）：统计卡 + 本月领养 + 快捷入口 + 机构信息
 */
export default {
  name: 'ShelterWorkbench',
  data() {
    return {
      statsLoading: true,
      stats: {},
      shelterLoading: true,
      shelter: {},
      editVisible: false,
      saving: false,
      editForm: {
        address: '',
        contactName: '',
        contactPhone: '',
        intro: ''
      },
      quickEntries: [
        { label: '发布宠物', desc: '新建宠物档案，完善健康与相册信息', icon: 'el-icon-plus', path: '/shelter/pets/edit' },
        { label: '审核申请', desc: '处理待受理的领养申请与面谈安排', icon: 'el-icon-finished', path: '/shelter/applications' },
        { label: '登记回访', desc: '跟进回访任务，查看领养人提交的生活记录', icon: 'el-icon-camera', path: '/shelter/followups' }
      ]
    };
  },
  computed: {
    statCards() {
      return [
        { label: '在册宠物', value: this.stats.petTotal, icon: 'el-icon-notebook-2' },
        { label: '可领养', value: this.stats.availableCount, icon: 'el-icon-goods' },
        { label: '待处理申请', value: this.stats.pendingApplication, icon: 'el-icon-finished' },
        { label: '待回访', value: this.stats.followUpPending, icon: 'el-icon-camera' }
      ];
    }
  },
  mounted() {
    this.loadStats();
    this.loadShelter();
  },
  methods: {
    statValue(value) {
      if (!value) return '暂无数据';
      return value;
    },
    async loadStats() {
      this.statsLoading = true;
      try {
        this.stats = await shelterWorkbench();
      } catch (e) {
        this.stats = {};
      } finally {
        this.statsLoading = false;
      }
    },
    async loadShelter() {
      this.shelterLoading = true;
      try {
        this.shelter = await myShelter();
      } catch (e) {
        this.shelter = {};
      } finally {
        this.shelterLoading = false;
      }
    },
    openEdit() {
      this.editForm = {
        address: this.shelter.address || '',
        contactName: '',
        contactPhone: '',
        intro: this.shelter.intro || ''
      };
      this.editVisible = true;
    },
    saveShelter() {
      this.saving = true;
      updateShelter({ ...this.editForm })
        .then(() => {
          this.$message.success('机构信息已更新');
          this.editVisible = false;
          this.loadShelter();
        })
        .catch(() => {})
        .finally(() => {
          this.saving = false;
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

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-4;

  @media (max-width: 991px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  @include card;

  display: flex;
  align-items: center;
  gap: $space-4;
  padding: $space-5 $space-6;

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: $radius-circle;
    background: $color-primary-light;
    color: $color-primary-text;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    i {
      font-size: 22px;
    }
  }

  &__value {
    font-size: 30px;
    font-weight: 600;
    line-height: 38px;
    color: $text-primary;
    @include tabular-nums;
  }

  &__label {
    margin-top: 2px;
    font-size: $font-w-caption;
    color: $text-secondary;
  }
}

.month-banner {
  @include card;

  margin-top: $space-4;
  padding: $space-4 $space-6;
  display: flex;
  align-items: center;
  gap: $space-3;
  font-size: $font-w-body;
  color: $text-regular;

  i {
    font-size: 20px;
    color: $color-primary-text;
  }

  strong {
    color: $color-primary-text;
    font-size: 18px;
  }
}

.workbench-grid {
  margin-top: $space-6;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: $space-4;
  align-items: start;

  @media (max-width: 1199px) {
    grid-template-columns: 1fr;
  }
}

.quick-panel,
.shelter-panel {
  @include card;

  padding: $space-6;
}

.panel-title {
  font-size: $font-w-h4;
  line-height: $lh-w-h4;
}

.quick-grid {
  margin-top: $space-5;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-4;

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.quick-card {
  @include card;

  border-radius: $radius-base;
  padding: $space-5;
  text-align: left;
  cursor: pointer;
  background: $bg-card;
  display: flex;
  flex-direction: column;
  gap: $space-2;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-2;
    border-color: $color-primary;
  }

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: $radius-circle;
    background: $color-primary-light;
    color: $color-primary-text;
    display: flex;
    align-items: center;
    justify-content: center;

    i {
      font-size: 18px;
    }
  }

  &__title {
    font-size: $font-w-body-l;
    font-weight: 600;
    color: $text-primary;
  }

  &__desc {
    font-size: $font-w-caption;
    line-height: $lh-w-caption;
    color: $text-secondary;
  }
}

.shelter-panel {
  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $space-2;
  }

  &__name {
    margin-top: $space-5;
    font-size: $font-w-h4;
    font-weight: 600;
    color: $text-primary;
  }

  &__facts {
    margin: $space-4 0 0;
    display: grid;
    grid-template-columns: 1fr;
    gap: $space-3;

    div {
      display: flex;
      gap: $space-3;

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
}

.readonly-row {
  display: grid;
  grid-template-columns: auto 1fr auto 1fr;
  gap: $space-2 $space-3;
  padding: $space-3 $space-4;
  margin-bottom: $space-5;
  background: $bg-page;
  border-radius: $radius-base;
  font-size: $font-w-body;
  color: $text-regular;

  &__label {
    color: $text-secondary;
    font-size: $font-w-caption;
  }
}
</style>
