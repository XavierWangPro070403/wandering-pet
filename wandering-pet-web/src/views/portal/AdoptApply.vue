<template>
  <div class="adopt-apply">
    <div class="wrap">
      <div class="apply-col">
        <header class="apply-head">
          <h1>领养申请</h1>
          <p>填写真实的居住条件与养宠准备，救助站会据此判断匹配度，3 个工作日内完成初筛。</p>
        </header>

        <!-- 宠物摘要 -->
        <section class="pet-summary" aria-label="申请宠物摘要">
          <template v-if="loading">
            <el-skeleton :rows="2" animated />
          </template>
          <template v-else>
            <div class="pet-summary__media">
              <img :src="petCover" :alt="`${pet.name}的照片`" />
            </div>
            <div class="pet-summary__body">
              <div class="pet-summary__head">
                <h2 class="pet-summary__name">{{ pet.name }}</h2>
                <StatusTag :status="pet.status" />
              </div>
              <p class="pet-summary__meta">
                {{ pet.categoryName || '—' }}{{ pet.breedName ? ' · ' + pet.breedName : '' }}
                · {{ ageText(pet.ageMonth) || '月龄未知' }}
                <template v-if="pet.shelter && pet.shelter.name"> · {{ pet.shelter.name }}</template>
              </p>
              <p v-if="!canApply" class="pet-summary__warn">{{ applyDisabledReason }}，暂不能提交新申请。</p>
              <p v-else class="pet-summary__tip">提交后救助站将在 3 个工作日内完成初筛。</p>
            </div>
          </template>
        </section>

        <!-- 分步表单 -->
        <section class="apply-card">
          <el-steps class="apply-steps" :active="step" align-center finish-status="success">
            <el-step title="基本信息" description="申请人与联系方式" />
            <el-step title="居住与经验" description="居住条件与养宠准备" />
            <el-step title="确认提交" description="核对信息后提交" />
          </el-steps>

          <!-- 第 1 步：基本信息 -->
          <el-form
            v-show="step === 0"
            ref="formStep1"
            :model="form"
            :rules="rules1"
            label-position="top"
            class="apply-form"
            @submit.native.prevent>
            <el-alert
              v-if="autoFilled"
              class="apply-form__tip"
              title="🐾 已自动带入你的领养资料，可直接修改"
              type="success"
              :closable="false"
              show-icon />
            <el-row :gutter="24">
              <el-col :xs="24" :sm="12">
                <el-form-item label="姓名" prop="applicantName">
                  <el-input v-model="form.applicantName" maxlength="32" placeholder="填写申请人姓名" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="手机号" prop="applicantPhone">
                  <el-input v-model="form.applicantPhone" maxlength="11" placeholder="用于救助站联系你" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="年龄" prop="applicantAge">
                  <el-input-number
                    v-model="form.applicantAge"
                    :min="18"
                    :max="100"
                    controls-position="right"
                    class="apply-form__number"
                    placeholder="18-100" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="职业" prop="occupation">
                  <el-input v-model="form.occupation" maxlength="32" placeholder="选填，如：教师" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="现居住城市" prop="city">
                  <el-input v-model="form.city" maxlength="32" placeholder="如：杭州" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <!-- 第 2 步：居住与经验 -->
          <el-form
            v-show="step === 1"
            ref="formStep2"
            :model="form"
            :rules="rules2"
            label-position="top"
            class="apply-form"
            @submit.native.prevent>
            <el-row :gutter="24">
              <el-col :xs="24" :sm="12">
                <el-form-item label="住房类型" prop="housingType">
                  <el-select v-model="form.housingType" class="apply-form__select" placeholder="请选择住房类型">
                    <el-option label="自有住房" value="OWN" />
                    <el-option label="租赁住房" value="RENT" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="养宠经验" prop="hasPetExperience">
                  <el-radio-group v-model="form.hasPetExperience">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="家人同意养宠" prop="familyAgree">
                  <el-radio-group v-model="form.familyAgree">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="家中有儿童" prop="hasChildren">
                  <el-radio-group v-model="form.hasChildren">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="家人有过敏史" prop="hasAllergy">
                  <el-radio-group v-model="form.hasAllergy">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="领养原因" prop="reason">
                  <el-input
                    v-model="form.reason"
                    type="textarea"
                    :rows="5"
                    maxlength="500"
                    show-word-limit
                    placeholder="说说你为什么想领养这只宠物，以及住所环境与照顾安排" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <!-- 第 3 步：确认回显 -->
          <div v-show="step === 2" class="apply-review">
            <h3 class="apply-review__title">请核对申请信息</h3>
            <dl class="apply-review__grid">
              <div v-for="item in reviewItems" :key="item.label" class="apply-review__item">
                <dt>{{ item.label }}</dt>
                <dd :class="{ 'apply-review__reason': item.label === '领养原因' }">{{ item.value }}</dd>
              </div>
            </dl>
            <p class="apply-review__tip">提交后救助站会通过电话或站内消息联系你，请保持手机畅通。</p>
          </div>

          <!-- 底部固定操作条 -->
          <div class="apply-footer">
            <el-button v-if="step > 0" :disabled="submitting" @click="prevStep">上一步</el-button>
            <el-button v-if="step < 2" type="primary" :disabled="!canApply" @click="nextStep">下一步</el-button>
            <el-button v-else type="primary" :disabled="!canApply" :loading="submitting" @click="confirmVisible = true">提交申请</el-button>
          </div>
        </section>
      </div>
    </div>

    <!-- 提交确认弹窗 -->
    <el-dialog title="确认提交申请？" :visible.sync="confirmVisible" width="440px" top="12vh">
      <ul class="apply-confirm__list">
        <li><span>申请宠物</span><strong>{{ pet.name || '—' }}</strong></li>
        <li><span>申请人</span><strong>{{ form.applicantName }} · {{ form.applicantPhone }}</strong></li>
        <li><span>现居住城市</span><strong>{{ form.city || '—' }}</strong></li>
        <li><span>住房类型</span><strong>{{ housingText }}</strong></li>
        <li><span>养宠经验</span><strong>{{ yesNoText(form.hasPetExperience) }}</strong></li>
        <li><span>领养原因</span><strong>{{ reasonBrief }}</strong></li>
      </ul>
      <p class="apply-confirm__tip">提交后可在"我的申请"中查看进度，审核期间可撤回。</p>
      <template slot="footer">
        <el-button @click="confirmVisible = false">再检查一下</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import { petDetail } from '@/api/pet';
import { submitApplication } from '@/api/adoption';
import { getAdopterProfile } from '@/api/user';
import { ageText } from '@/utils/format';

export default {
  name: 'AdoptApplyPage',
  components: { StatusTag },
  data() {
    return {
      loading: true,
      submitting: false,
      autoFilled: false,
      step: 0,
      pet: {},
      confirmVisible: false,
      form: {
        applicantName: '',
        applicantPhone: '',
        applicantAge: null,
        occupation: '',
        city: '',
        housingType: '',
        hasPetExperience: null,
        familyAgree: null,
        hasChildren: null,
        hasAllergy: null,
        reason: ''
      },
      rules1: {
        applicantName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        applicantPhone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        applicantAge: [{ required: true, message: '请填写年龄（18-100）', trigger: 'change' }],
        city: [{ required: true, message: '请填写现居住城市', trigger: 'blur' }]
      },
      rules2: {
        housingType: [{ required: true, message: '请选择住房类型', trigger: 'change' }],
        hasPetExperience: [{ required: true, message: '请选择是否有养宠经验', trigger: 'change' }],
        familyAgree: [{ required: true, message: '请确认家人是否同意', trigger: 'change' }],
        hasChildren: [{ required: true, message: '请选择家中是否有儿童', trigger: 'change' }],
        hasAllergy: [{ required: true, message: '请选择是否有过敏史', trigger: 'change' }],
        reason: [
          { required: true, message: '请填写领养原因', trigger: 'blur' },
          { max: 500, message: '领养原因最长 500 字', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    petId() {
      return this.$route.params.petId;
    },
    petCover() {
      const imgs = this.pet.images || [];
      return (imgs[0] && imgs[0].url) || this.pet.coverImage || '';
    },
    canApply() {
      return this.pet.status === 'AVAILABLE';
    },
    applyDisabledReason() {
      return {
        PENDING: '该宠物已有申请审核中',
        ADOPTED: '该宠物已被领养',
        MEDICAL: '该宠物正在治疗中',
        OFFLINE: '该宠物已下架'
      }[this.pet.status] || '当前状态不可申请';
    },
    housingText() {
      return { OWN: '自有住房', RENT: '租赁住房' }[this.form.housingType] || '—';
    },
    reasonBrief() {
      const text = (this.form.reason || '').replace(/\s+/g, ' ');
      return text.length > 24 ? `${text.slice(0, 24)}…` : text || '—';
    },
    reviewItems() {
      return [
        { label: '申请宠物', value: this.pet.name || '—' },
        { label: '姓名', value: this.form.applicantName || '—' },
        { label: '手机号', value: this.form.applicantPhone || '—' },
        { label: '年龄', value: this.form.applicantAge ? `${this.form.applicantAge} 岁` : '—' },
        { label: '职业', value: this.form.occupation || '未填写' },
        { label: '现居住城市', value: this.form.city || '—' },
        { label: '住房类型', value: this.housingText },
        { label: '养宠经验', value: this.yesNoText(this.form.hasPetExperience) },
        { label: '家人同意养宠', value: this.yesNoText(this.form.familyAgree) },
        { label: '家中有儿童', value: this.yesNoText(this.form.hasChildren) },
        { label: '家人有过敏史', value: this.yesNoText(this.form.hasAllergy) },
        { label: '领养原因', value: this.form.reason || '—' }
      ];
    }
  },
  watch: {
    petId() {
      this.step = 0;
      this.load();
    }
  },
  async mounted() {
    if (!this.$store.state.user.token) {
      this.$router.replace(`/login?redirect=${encodeURIComponent(this.$route.fullPath)}`);
      return;
    }
    await this.load();
  },
  methods: {
    ageText,
    yesNoText(v) {
      return v === 1 ? '是' : v === 0 ? '否' : '—';
    },
    async load() {
      this.loading = true;
      try {
        this.pet = await petDetail(this.petId);
      } catch (e) {
        this.$message.error('宠物档案不存在或已删除');
        this.$router.replace('/pets');
      } finally {
        this.loading = false;
      }
      await this.fillFromProfile();
    },
    /** 领养资料自动填充：登录用户申请多只宠物时免重复填写 */
    async fillFromProfile() {
      try {
        const p = await getAdopterProfile();
        if (!p) return;
        const f = this.form;
        const has = (v) => v !== null && v !== undefined && v !== '';
        if (has(p.adopterName)) f.applicantName = p.adopterName;
        if (has(p.phone)) f.applicantPhone = p.phone;
        if (has(p.adopterAge)) f.applicantAge = p.adopterAge;
        if (has(p.occupation)) f.occupation = p.occupation;
        if (has(p.city)) f.city = p.city;
        if (has(p.housingType)) f.housingType = p.housingType;
        if (has(p.hasPetExperience)) f.hasPetExperience = p.hasPetExperience;
        if (has(p.familyAgree)) f.familyAgree = p.familyAgree;
        if (has(p.hasChildren)) f.hasChildren = p.hasChildren;
        if (has(p.hasAllergy)) f.hasAllergy = p.hasAllergy;
        this.autoFilled = has(f.applicantName) && has(f.applicantPhone);
      } catch (e) {
        /* 资料为空时静默忽略 */
      }
    },
    prevStep() {
      if (this.step > 0) this.step -= 1;
    },
    nextStep() {
      const ref = this.step === 0 ? this.$refs.formStep1 : this.$refs.formStep2;
      ref.validate((valid) => {
        if (valid) this.step += 1;
      });
    },
    async submit() {
      if (!this.canApply) {
        this.$message.warning('该宠物当前状态不可申请');
        this.confirmVisible = false;
        return;
      }
      this.submitting = true;
      try {
        await submitApplication({
          petId: this.petId,
          applicantName: this.form.applicantName,
          applicantPhone: this.form.applicantPhone,
          applicantAge: this.form.applicantAge,
          occupation: this.form.occupation,
          city: this.form.city,
          housingType: this.form.housingType,
          hasPetExperience: this.form.hasPetExperience,
          familyAgree: this.form.familyAgree,
          hasChildren: this.form.hasChildren,
          hasAllergy: this.form.hasAllergy,
          reason: this.form.reason
        });
        this.confirmVisible = false;
        this.$message.success('申请已提交，救助站将在 3 个工作日内完成初筛');
        this.$router.push('/my/applications');
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.submitting = false;
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

.adopt-apply {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
  padding: 40px 0 80px;
}

.apply-col {
  max-width: 720px;
  margin: 0 auto;
}

.apply-head {
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

// 宠物摘要
.pet-summary {
  @include card;

  display: flex;
  gap: 16px;
  padding: 16px;
  margin-bottom: 24px;

  &__media {
    width: 128px;
    aspect-ratio: 4 / 3;
    // 防止 height:100% 子图按自然比例撑破比例框
    min-height: 0;
    border-radius: $radius-base;
    overflow: hidden;
    flex-shrink: 0;
    background: $bg-page;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    min-width: 0;
    align-self: center;
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

  &__meta {
    margin-top: 4px;
    font-size: 13px;
    color: $text-secondary;
    @include tabular-nums;
  }

  &__tip {
    margin-top: 8px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__warn {
    margin-top: 8px;
    font-size: 13px;
    padding: 6px 10px;
    border-radius: $radius-small;
    background: $color-warning-light;
    color: $color-warning-text;
  }
}

// 表单卡
.apply-card {
  @include card;

  padding: 24px 24px 0;
}

.apply-steps {
  margin-bottom: 24px;

  ::v-deep .el-step__description {
    font-size: 12px;
  }
}

.apply-form {
  padding-top: 8px;

  &__tip {
    margin-bottom: 20px;
  }

  &__number {
    width: 100%;
  }

  &__select {
    width: 100%;
  }
}

// 第 3 步回显
.apply-review {
  &__title {
    font-size: 16px;
    margin-bottom: 16px;
  }

  &__grid {
    margin: 0;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px 24px;

    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }

  &__item {
    dt {
      font-size: 13px;
      color: $text-secondary;
    }

    dd {
      margin: 2px 0 0;
      font-size: 14px;
      color: $text-primary;
    }
  }

  &__reason {
    white-space: pre-line;
  }

  &__tip {
    margin-top: 16px;
    font-size: 13px;
    color: $text-secondary;
  }
}

// 底部固定操作条
.apply-footer {
  position: sticky;
  bottom: 0;
  z-index: 5;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin: 16px -24px 0;
  padding: 16px 24px;
  background: $bg-card;
  border-top: 1px solid $border-color;
  border-radius: 0 0 $radius-card $radius-card;
}

// 确认弹窗
.apply-confirm__list {
  margin: 0;
  padding: 0;
  list-style: none;

  li {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    padding: 8px 0;
    border-bottom: 1px solid $border-color;
    font-size: 14px;

    span {
      color: $text-secondary;
      flex-shrink: 0;
    }

    strong {
      color: $text-primary;
      font-weight: 500;
      text-align: right;
      min-width: 0;
      word-break: break-all;
    }
  }
}

.apply-confirm__tip {
  margin-top: 12px;
  font-size: 13px;
  color: $text-secondary;
}
</style>
