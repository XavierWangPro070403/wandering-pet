<template>
  <div class="pet-edit">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1 class="page-head__title">{{ isEdit ? '编辑宠物档案' : '发布宠物' }}</h1>
        <p class="page-head__desc">分四步完善档案：基本信息、健康性格、相册与救助故事。</p>
      </div>
      <div class="page-head__actions">
        <el-button
          v-if="isEdit"
          size="small"
          icon="el-icon-document"
          @click="openHealthDrawer">健康档案</el-button>
        <el-button size="small" @click="$router.push('/shelter/pets')">返回列表</el-button>
      </div>
    </div>

    <div class="form-card">
      <el-steps :active="step" align-center finish-status="success" class="steps">
        <el-step title="基本信息" />
        <el-step title="健康与性格" />
        <el-step title="相册" />
        <el-step title="故事与发布" />
      </el-steps>

      <div v-if="loading" class="form-skeleton">
        <el-skeleton :rows="6" animated />
      </div>

      <el-form
        v-else
        ref="petForm"
        :model="form"
        label-position="top"
        class="form-body"
        @submit.native.prevent>
        <!-- 第 1 步：基本信息 -->
        <div v-show="step === 0">
          <div class="form-grid">
            <el-form-item label="名字" required>
              <el-input v-model="form.name" placeholder="给宠物起个好记的名字" maxlength="32" />
            </el-form-item>
            <el-form-item label="物种" required>
              <el-select v-model="form.categoryId" placeholder="选择物种" style="width: 100%" @change="onCategoryChange">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="品种">
              <el-select v-model="form.breedId" placeholder="先选择物种，再选品种" clearable style="width: 100%">
                <el-option v-for="b in breeds" :key="b.id" :label="b.name" :value="b.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="性别" required>
              <el-radio-group v-model="form.gender">
                <el-radio label="MALE">公</el-radio>
                <el-radio label="FEMALE">母</el-radio>
                <el-radio label="UNKNOWN">未知</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="月龄" required>
              <el-input-number v-model="form.ageMonth" :min="0" :max="360" controls-position="right" style="width: 100%" />
            </el-form-item>
            <el-form-item label="体重（kg）">
              <el-input-number v-model="form.weight" :min="0" :max="100" :precision="1" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
            <el-form-item label="体型" required>
              <el-select v-model="form.size" placeholder="选择体型" style="width: 100%">
                <el-option label="小型" value="SMALL" />
                <el-option label="中型" value="MEDIUM" />
                <el-option label="大型" value="LARGE" />
              </el-select>
            </el-form-item>
            <el-form-item label="毛色">
              <el-input v-model="form.color" placeholder="如：橘白 / 黑白花" maxlength="32" />
            </el-form-item>
          </div>
        </div>

        <!-- 第 2 步：健康与性格 -->
        <div v-show="step === 1">
          <div class="switch-row">
            <div class="switch-item">
              <el-switch v-model="form.sterilized" :active-value="1" :inactive-value="0" />
              <span>已绝育</span>
            </div>
            <div class="switch-item">
              <el-switch v-model="form.vaccinated" :active-value="1" :inactive-value="0" />
              <span>已接种疫苗</span>
            </div>
            <div class="switch-item">
              <el-switch v-model="form.dewormed" :active-value="1" :inactive-value="0" />
              <span>已驱虫</span>
            </div>
            <el-form-item label="健康状况" class="switch-item__select" required>
              <el-select v-model="form.healthStatus" placeholder="选择当前健康状况" style="width: 200px">
                <el-option label="健康" value="HEALTHY" />
                <el-option label="健康一般（需关注）" value="GENERAL" />
                <el-option label="治疗中" value="ILL" />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="性格描述">
            <el-input
              v-model="form.characterDesc"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
              placeholder="活泼粘人 / 安静独立，与人和其他动物相处情况" />
          </el-form-item>
          <el-form-item label="领养要求">
            <el-input
              v-model="form.adoptionRequirements"
              type="textarea"
              :rows="3"
              maxlength="512"
              show-word-limit
              placeholder="如：有封窗条件、定期回访、家人同意、科学喂养不离不弃" />
          </el-form-item>
        </div>

        <!-- 第 3 步：相册 -->
        <div v-show="step === 2">
          <el-form-item label="封面图" required>
            <el-upload
              class="cover-uploader"
              action=""
              accept="image/jpeg,image/png,image/webp"
              :show-file-list="false"
              :http-request="uploadCover">
              <img v-if="form.coverImage" :src="form.coverImage" class="cover-uploader__img" alt="封面图" />
              <i v-else class="el-icon-plus cover-uploader__trigger" aria-hidden="true"></i>
            </el-upload>
            <p class="form-tip">封面展示在列表卡片与详情主图（4:3 比例效果最佳），上传后数据库存 objectName + url。</p>
          </el-form-item>
          <el-form-item label="相册">
            <ImageUploader v-model="gallery" biz-type="pet" :limit="9" />
            <p class="form-tip">相册图按上传顺序展示在详情页画廊；数据库存 objectName + url。</p>
          </el-form-item>
        </div>

        <!-- 第 4 步：救助故事与发布 -->
        <div v-show="step === 3">
          <el-form-item label="救助故事">
            <el-input
              v-model="form.story"
              type="textarea"
              :rows="6"
              maxlength="2000"
              show-word-limit
              placeholder="记录救助经过、恢复情况与性格养成，帮助领养人了解它" />
          </el-form-item>
          <el-form-item label="发布方式" required>
            <el-radio-group v-model="form.status">
              <el-radio label="AVAILABLE">直接发布，进入待领养列表</el-radio>
              <el-radio label="OFFLINE">保存为下架，稍后上架</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>

      <!-- 底部固定操作条 -->
      <div class="action-bar">
        <el-button v-if="step > 0" @click="step--">上一步</el-button>
        <el-button v-if="step < 3" type="primary" @click="next">下一步</el-button>
        <span class="action-bar__spacer"></span>
        <el-button type="primary" :loading="saving" @click="save">保存档案</el-button>
      </div>
    </div>

    <!-- 健康档案抽屉 -->
    <el-drawer title="健康档案" :visible.sync="healthVisible" size="480px">
      <div class="health-drawer">
        <div class="health-drawer__head">
          <p class="health-drawer__pet">{{ form.name || '宠物' }} 的免疫、驱虫与治疗记录</p>
          <el-button type="primary" size="small" icon="el-icon-plus" @click="openRecordDialog()">新增记录</el-button>
        </div>

        <div v-if="healthLoading" class="health-drawer__body">
          <el-skeleton :rows="4" animated />
        </div>
        <div v-else-if="healthList.length" class="health-drawer__body">
          <el-timeline>
            <el-timeline-item
              v-for="record in healthList"
              :key="record.id"
              :timestamp="record.recordDate || ''"
              :type="recordTypeTone(record.recordType)"
              placement="top">
              <div class="health-item">
                <div class="health-item__head">
                  <span class="health-item__title">{{ record.title }}</span>
                  <span class="health-item__type">{{ recordTypeText(record.recordType) }}</span>
                </div>
                <p v-if="record.content" class="health-item__content">{{ record.content }}</p>
                <p v-if="record.vetName" class="health-item__vet">兽医：{{ record.vetName }}</p>
                <div class="health-item__ops">
                  <el-button type="text" size="small" @click="openRecordDialog(record)">编辑</el-button>
                  <el-button type="text" size="small" class="danger-text" @click="removeRecord(record)">删除</el-button>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
        <EmptyState
          v-else
          title="暂无健康档案"
          desc="为宠物补充免疫、驱虫与体检记录，领养人会更放心。" />
      </div>
    </el-drawer>

    <!-- 新增/编辑健康记录弹窗 -->
    <el-dialog
      :title="recordForm.id ? '编辑健康记录' : '新增健康记录'"
      :visible.sync="recordVisible"
      width="480px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form :model="recordForm" label-position="top" @submit.native.prevent>
        <el-form-item label="记录类型" required>
          <el-select v-model="recordForm.recordType" placeholder="选择类型" style="width: 100%">
            <el-option v-for="t in recordTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期">
          <el-date-picker
            v-model="recordForm.recordDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择日期"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="recordForm.title" placeholder="如：第二针猫三联" maxlength="64" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="recordForm.content" type="textarea" :rows="3" maxlength="500" placeholder="检查结果、用药与注意事项" />
        </el-form-item>
        <el-form-item label="兽医">
          <el-input v-model="recordForm.vetName" placeholder="接诊兽医姓名" maxlength="32" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="recordVisible = false">取消</el-button>
        <el-button type="primary" :loading="recordSaving" @click="saveRecord">保存记录</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import ImageUploader from '@/components/ImageUploader.vue';
import EmptyState from '@/components/EmptyState.vue';
import { categories, breeds, petDetail } from '@/api/pet';
import {
  createPet,
  updatePet,
  changePetStatus,
  healthRecords,
  createHealthRecord,
  updateHealthRecord,
  deleteHealthRecord
} from '@/api/shelter';
import { uploadFile } from '@/api/file';

/**
 * 宠物发布/编辑（分步表单 + 健康档案抽屉）
 */
export default {
  name: 'ShelterPetEdit',
  components: { ImageUploader, EmptyState },
  data() {
    return {
      loading: false,
      saving: false,
      step: 0,
      categories: [],
      breeds: [],
      gallery: [],
      loadedStatus: '',
      form: {
        name: '',
        categoryId: null,
        breedId: null,
        gender: 'UNKNOWN',
        ageMonth: 1,
        weight: undefined,
        size: '',
        color: '',
        sterilized: 0,
        vaccinated: 0,
        dewormed: 0,
        healthStatus: '',
        characterDesc: '',
        adoptionRequirements: '',
        story: '',
        coverImage: '',
        coverObjectName: '',
        status: 'AVAILABLE'
      },
      // 健康档案
      healthVisible: false,
      healthLoading: false,
      healthList: [],
      recordVisible: false,
      recordSaving: false,
      recordForm: {
        id: null,
        recordType: '',
        recordDate: '',
        title: '',
        content: '',
        vetName: ''
      },
      recordTypes: [
        { value: 'VACCINE', label: '免疫' },
        { value: 'DEWORM', label: '驱虫' },
        { value: 'STERILIZE', label: '绝育' },
        { value: 'TREATMENT', label: '治疗' },
        { value: 'CHECK', label: '体检' }
      ]
    };
  },
  computed: {
    petId() {
      return this.$route.params.id || '';
    },
    isEdit() {
      return !!this.petId;
    }
  },
  watch: {
    '$route.params.id'() {
      this.bootstrap();
    }
  },
  async mounted() {
    this.bootstrap();
  },
  methods: {
    async bootstrap() {
      try {
        this.categories = await categories();
      } catch (e) {
        this.categories = [];
      }
      if (this.isEdit) {
        await this.loadPet();
      } else {
        this.resetForm();
      }
    },
    resetForm() {
      this.form = {
        name: '',
        categoryId: null,
        breedId: null,
        gender: 'UNKNOWN',
        ageMonth: 1,
        weight: undefined,
        size: '',
        color: '',
        sterilized: 0,
        vaccinated: 0,
        dewormed: 0,
        healthStatus: '',
        characterDesc: '',
        adoptionRequirements: '',
        story: '',
        coverImage: '',
        coverObjectName: '',
        status: 'AVAILABLE'
      };
      this.gallery = [];
      this.breeds = [];
      this.loadedStatus = '';
      this.step = 0;
    },
    async loadPet() {
      this.loading = true;
      try {
        const detail = await petDetail(this.petId);
        this.loadedStatus = detail.status || '';
        this.form = {
          name: detail.name || '',
          categoryId: detail.categoryId || null,
          breedId: detail.breedId || null,
          gender: detail.gender || 'UNKNOWN',
          ageMonth: detail.ageMonth != null ? detail.ageMonth : 0,
          weight: detail.weight != null ? Number(detail.weight) : undefined,
          size: detail.size || '',
          color: detail.color || '',
          sterilized: detail.sterilized || 0,
          vaccinated: detail.vaccinated || 0,
          dewormed: detail.dewormed || 0,
          healthStatus: detail.healthStatus || '',
          characterDesc: detail.characterDesc || '',
          adoptionRequirements: detail.adoptionRequirements || '',
          story: detail.story || '',
          coverImage: detail.coverImage || '',
          coverObjectName: '',
          status: detail.status === 'OFFLINE' ? 'OFFLINE' : 'AVAILABLE'
        };
        // 相册：接口返回含封面的完整列表（第 0 位为封面），把非封面图映射为相册数据
        const images = detail.images || [];
        this.gallery = images
          .filter((img) => img.url && img.url !== detail.coverImage)
          .map((img) => ({ objectName: img.objectName || '', url: img.url }));
        if (this.form.categoryId) {
          this.loadBreeds(this.form.categoryId);
        }
        this.step = 0;
      } catch (e) {
        this.$message.error('宠物档案加载失败');
        this.$router.replace('/shelter/pets');
      } finally {
        this.loading = false;
      }
    },
    async loadBreeds(categoryId) {
      try {
        this.breeds = await breeds(categoryId);
      } catch (e) {
        this.breeds = [];
      }
    },
    onCategoryChange(val) {
      this.form.breedId = null;
      this.breeds = [];
      if (val) {
        this.loadBreeds(val);
      }
    },
    async uploadCover({ file, onError }) {
      try {
        const data = await uploadFile(file, 'pet');
        this.form.coverImage = data.url;
        this.form.coverObjectName = data.objectName || '';
      } catch (e) {
        onError(e);
      }
    },
    validateStep(idx) {
      if (idx === 0) {
        if (!this.form.name.trim()) {
          this.$message.warning('请输入宠物名字');
          return false;
        }
        if (!this.form.categoryId) {
          this.$message.warning('请选择物种');
          return false;
        }
        if (!this.form.gender) {
          this.$message.warning('请选择性别');
          return false;
        }
        if (this.form.ageMonth === undefined || this.form.ageMonth === null) {
          this.$message.warning('请填写月龄');
          return false;
        }
        if (!this.form.size) {
          this.$message.warning('请选择体型');
          return false;
        }
      }
      if (idx === 1 && !this.form.healthStatus) {
        this.$message.warning('请选择健康状况');
        return false;
      }
      if (idx === 2 && !this.form.coverImage) {
        this.$message.warning('请上传封面图');
        return false;
      }
      return true;
    },
    next() {
      if (!this.validateStep(this.step)) return;
      this.step += 1;
    },
    validateAll() {
      return this.validateStep(0) && this.validateStep(1) && this.validateStep(2);
    },
    buildPayload() {
      return {
        name: this.form.name.trim(),
        categoryId: this.form.categoryId,
        breedId: this.form.breedId || null,
        gender: this.form.gender,
        ageMonth: this.form.ageMonth,
        weight: this.form.weight === undefined ? null : this.form.weight,
        size: this.form.size,
        color: this.form.color,
        sterilized: this.form.sterilized,
        vaccinated: this.form.vaccinated,
        dewormed: this.form.dewormed,
        healthStatus: this.form.healthStatus,
        characterDesc: this.form.characterDesc,
        adoptionRequirements: this.form.adoptionRequirements,
        story: this.form.story,
        coverImage: this.form.coverImage,
        images: this.gallery.map((img, index) => ({
          objectName: img.objectName || '',
          url: img.url,
          sort: index
        })),
        status: this.form.status
      };
    },
    save() {
      if (!this.validateAll()) return;
      this.saving = true;
      const payload = this.buildPayload();
      const request = this.isEdit
        ? updatePet(this.petId, payload).then(() => {
            // 编辑接口不改动状态，状态变更走批量状态接口
            if (payload.status !== this.loadedStatus) {
              return changePetStatus({ status: payload.status, ids: [this.petId] });
            }
            return null;
          })
        : createPet(payload);
      request
        .then(() => {
          this.$message.success(this.isEdit ? '档案已更新' : '宠物档案已保存');
          this.$router.push('/shelter/pets');
        })
        .catch(() => {})
        .finally(() => {
          this.saving = false;
        });
    },

    // ---------------- 健康档案 ----------------
    openHealthDrawer() {
      this.healthVisible = true;
      this.loadHealth();
    },
    loadHealth() {
      this.healthLoading = true;
      healthRecords(this.petId)
        .then((list) => {
          this.healthList = list || [];
        })
        .catch(() => {
          this.healthList = [];
        })
        .finally(() => {
          this.healthLoading = false;
        });
    },
    openRecordDialog(record) {
      this.recordForm = record
        ? {
            id: record.id,
            recordType: record.recordType || '',
            recordDate: record.recordDate || '',
            title: record.title || '',
            content: record.content || '',
            vetName: record.vetName || ''
          }
        : { id: null, recordType: '', recordDate: '', title: '', content: '', vetName: '' };
      this.recordVisible = true;
    },
    saveRecord() {
      if (!this.recordForm.recordType) {
        this.$message.warning('请选择记录类型');
        return;
      }
      if (!this.recordForm.title.trim()) {
        this.$message.warning('请输入记录标题');
        return;
      }
      const data = {
        recordType: this.recordForm.recordType,
        recordDate: this.recordForm.recordDate || '',
        title: this.recordForm.title.trim(),
        content: this.recordForm.content,
        vetName: this.recordForm.vetName
      };
      this.recordSaving = true;
      const request = this.recordForm.id
        ? updateHealthRecord(this.recordForm.id, data)
        : createHealthRecord(this.petId, data);
      request
        .then(() => {
          this.$message.success('健康记录已保存');
          this.recordVisible = false;
          this.loadHealth();
        })
        .catch(() => {})
        .finally(() => {
          this.recordSaving = false;
        });
    },
    removeRecord(record) {
      this.$confirm(`确定删除记录「${record.title}」？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => deleteHealthRecord(record.id))
        .then(() => {
          this.$message.success('记录已删除');
          this.loadHealth();
        })
        .catch(() => {});
    },
    recordTypeText(t) {
      return { VACCINE: '免疫', DEWORM: '驱虫', STERILIZE: '绝育', TREATMENT: '治疗', CHECK: '体检' }[t] || t;
    },
    recordTypeTone(t) {
      return { VACCINE: 'success', DEWORM: 'primary', STERILIZE: 'primary', TREATMENT: 'warning', CHECK: '' }[t] || '';
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

  &__actions {
    display: flex;
    gap: $space-2;
  }
}

.form-card {
  @include card;

  padding: $space-8;
}

.steps {
  margin-bottom: $space-8;
}

.form-skeleton {
  padding: $space-6 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  column-gap: $space-6;

  @media (max-width: 767px) {
    grid-template-columns: 1fr;
  }
}

.switch-row {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: $space-6;
  margin-bottom: $space-5;
  padding: $space-4 $space-5;
  background: $bg-page;
  border-radius: $radius-base;

  .switch-item {
    display: flex;
    align-items: center;
    gap: $space-2;
    font-size: $font-w-body;
    color: $text-regular;
  }

  .switch-item__select {
    margin-bottom: 0;
  }
}

.cover-uploader {
  ::v-deep .el-upload {
    border: 1px dashed $color-primary;
    border-radius: $radius-base;
    background: $color-primary-light;
    cursor: pointer;
    overflow: hidden;
    transition: border-color $duration-hover-in $ease-out-quart;

    &:hover {
      border-color: $color-primary-hover;
    }
  }

  &__img {
    width: 240px;
    height: 180px;
    object-fit: cover;
    display: block;
    aspect-ratio: 4 / 3;
  }

  &__trigger {
    width: 240px;
    height: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: $color-primary-text;
  }
}

.form-tip {
  margin-top: $space-2;
  font-size: $font-w-caption;
  line-height: $lh-w-caption;
  color: $text-secondary;
}

.action-bar {
  position: sticky;
  bottom: 0;
  margin-top: $space-6;
  padding: $space-4 0 0;
  border-top: 1px solid $border-color;
  background: $bg-card;
  display: flex;
  align-items: center;
  gap: $space-3;

  &__spacer {
    flex: 1;
  }
}

.health-drawer {
  padding: 0 $space-6 $space-6;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $space-3;
    margin-bottom: $space-5;
  }

  &__pet {
    font-size: $font-w-body;
    color: $text-secondary;
  }

  &__body {
    max-height: calc(100vh - 220px);
    overflow-y: auto;
  }
}

.health-item {
  padding-bottom: $space-2;

  &__head {
    display: flex;
    align-items: center;
    gap: $space-2;
    flex-wrap: wrap;
  }

  &__title {
    font-size: $font-w-body-l;
    font-weight: 600;
    color: $text-primary;
  }

  &__type {
    font-size: $font-w-caption;
    color: $color-primary-text;
    background: $color-primary-light;
    border-radius: $radius-small;
    padding: 2px 8px;
  }

  &__content {
    margin-top: $space-1;
    font-size: $font-w-body;
    line-height: $lh-w-body;
    color: $text-regular;
  }

  &__vet {
    margin-top: $space-1;
    font-size: $font-w-caption;
    color: $text-secondary;
  }

  &__ops {
    margin-top: $space-1;

    .el-button {
      padding: 0;
      margin-right: $space-3;
    }
  }
}

.danger-text {
  color: $color-danger-text;

  &:hover {
    background: $color-danger-light;
    color: $color-danger-text;
  }
}
</style>
