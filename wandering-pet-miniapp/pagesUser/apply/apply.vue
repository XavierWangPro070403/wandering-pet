<template>
  <view class="page">
    <!-- 宠物摘要 -->
    <view class="pet card" v-if="pet.id">
      <image class="pet__img" :src="pet.coverImage" mode="aspectFill" />
      <view class="pet__body">
        <text class="pet__name">{{ pet.name }}</text>
        <text class="pet__meta">{{ pet.categoryName }} · {{ age(pet.ageMonth) }} · {{ pet.city }}</text>
      </view>
    </view>

    <!-- 步骤条 -->
    <view class="steps card">
      <view class="step" v-for="(s, i) in steps" :key="i" :class="{ active: i === step }">
        <view class="step__dot"><text>{{ i + 1 }}</text></view>
        <text class="step__label">{{ s }}</text>
      </view>
    </view>

    <!-- 第 1 步：基本信息 -->
    <view class="card" v-if="step === 0">
      <view v-if="autoFilled" class="fill-tip">
        <text class="fill-tip__paw">🐾</text>
        <text class="fill-tip__text">已自动带入你的领养资料，可直接修改</text>
      </view>
      <view class="form-item">
        <text class="form-label">申请人姓名</text>
        <input class="form-input" v-model="form.applicantName" placeholder="真实姓名" maxlength="32" />
      </view>
      <view class="form-item">
        <text class="form-label">联系电话</text>
        <input class="form-input" v-model="form.applicantPhone" type="number" placeholder="11 位手机号" maxlength="11" />
      </view>
      <view class="form-item">
        <text class="form-label">年龄</text>
        <input class="form-input" v-model="form.applicantAge" type="number" placeholder="须年满 18 周岁" />
      </view>
      <view class="form-item">
        <text class="form-label">职业</text>
        <input class="form-input" v-model="form.occupation" placeholder="如：教师 / 工程师（选填）" maxlength="32" />
      </view>
      <view class="form-item">
        <text class="form-label">现居住城市</text>
        <input class="form-input" v-model="form.city" placeholder="如：杭州" maxlength="32" />
      </view>
    </view>

    <!-- 第 2 步：居住与经验 -->
    <view class="card" v-if="step === 1">
      <view class="form-item">
        <text class="form-label">住房类型</text>
        <picker :range="['自有住房', '租房']" @change="(e) => (form.housingType = Number(e.detail.value) === 0 ? 'OWN' : 'RENT')">
          <view class="form-picker"><text>{{ form.housingType === 'OWN' ? '自有住房' : form.housingType === 'RENT' ? '租房' : '请选择' }}</text><text class="arrow">›</text></view>
        </picker>
      </view>
      <view class="form-item" v-for="q in yesNo" :key="q.key">
        <text class="form-label">{{ q.label }}</text>
        <view class="seg">
          <view class="seg__item" :class="{ active: form[q.key] === 1 }" @click="form[q.key] = 1"><text>是</text></view>
          <view class="seg__item" :class="{ active: form[q.key] === 0 }" @click="form[q.key] = 0"><text>否</text></view>
        </view>
      </view>
      <view class="form-item">
        <text class="form-label">领养原因</text>
        <textarea class="form-textarea" v-model="form.reason" maxlength="500" placeholder="说说你想领养它的原因、家里的情况（500 字内）" />
      </view>
    </view>

    <!-- 第 3 步：确认 -->
    <view class="card" v-if="step === 2">
      <text class="card-title">确认申请信息</text>
      <view class="confirm" v-for="row in confirmRows" :key="row.label">
        <text class="confirm__label">{{ row.label }}</text>
        <text class="confirm__value">{{ row.value }}</text>
      </view>
      <text class="confirm__tip">提交后救助站将在 3 个工作日内完成初筛，进度可在“我的申请”查看。</text>
    </view>

    <!-- 底部操作 -->
    <view class="footer">
      <button v-if="step > 0" class="btn-plain footer__prev" @click="step--">上一步</button>
      <button v-if="step < 2" class="btn-primary footer__next" @click="next">下一步</button>
      <button v-else class="btn-primary footer__next" :disabled="submitting" @click="submit">提交申请</button>
    </view>
  </view>
</template>

<script>
import { petDetail, submitApplication, getAdopterProfile } from '../../common/api.js';

export default {
  data() {
    return {
      pet: {},
      step: 0,
      submitting: false,
      autoFilled: false,
      steps: ['基本信息', '居住与经验', '确认提交'],
      yesNo: [
        { key: 'hasPetExperience', label: '有养宠经验' },
        { key: 'familyAgree', label: '家人同意' },
        { key: 'hasChildren', label: '家中有儿童' },
        { key: 'hasAllergy', label: '有过敏史' }
      ],
      form: {
        applicantName: '',
        applicantPhone: '',
        applicantAge: '',
        occupation: '',
        city: '',
        housingType: '',
        hasPetExperience: null,
        familyAgree: null,
        hasChildren: null,
        hasAllergy: null,
        reason: ''
      }
    };
  },
  computed: {
    petId() {
      return this.$root.$mp.query.petId;
    },
    confirmRows() {
      const f = this.form;
      return [
        { label: '姓名', value: f.applicantName },
        { label: '电话', value: f.applicantPhone },
        { label: '年龄', value: f.applicantAge },
        { label: '城市', value: f.city },
        { label: '住房', value: f.housingType === 'OWN' ? '自有住房' : f.housingType === 'RENT' ? '租房' : '—' },
        { label: '养宠经验', value: f.hasPetExperience === 1 ? '有' : '无' },
        { label: '家人同意', value: f.familyAgree === 1 ? '是' : '否' },
        { label: '领养原因', value: f.reason }
      ];
    }
  },
  onLoad() {
    this.load();
    this.fillFromProfile();
  },
  methods: {
    /** 领养资料自动填充：登录用户申请多只宠物时免重复填写 */
    async fillFromProfile() {
      if (!this.$store.getters.isLoggedIn) return;
      try {
        const p = await getAdopterProfile();
        if (!p) return;
        const f = this.form;
        const has = (v) => v !== null && v !== undefined && v !== '';
        if (has(p.adopterName)) f.applicantName = p.adopterName;
        if (has(p.phone)) f.applicantPhone = p.phone;
        if (has(p.adopterAge)) f.applicantAge = String(p.adopterAge);
        if (has(p.occupation)) f.occupation = p.occupation;
        if (has(p.city)) f.city = p.city;
        if (has(p.housingType)) f.housingType = p.housingType;
        if (has(p.hasPetExperience)) f.hasPetExperience = p.hasPetExperience;
        if (has(p.familyAgree)) f.familyAgree = p.familyAgree;
        if (has(p.hasChildren)) f.hasChildren = p.hasChildren;
        if (has(p.hasAllergy)) f.hasAllergy = p.hasAllergy;
        this.autoFilled = has(f.applicantName) && has(f.applicantPhone);
      } catch (e) {
        /* 未登录或资料为空时静默忽略 */
      }
    },
    async load() {
      try {
        this.pet = await petDetail(this.petId);
        this.form.city = this.$store.state.userInfo && this.$store.state.userInfo.city || '';
      } catch (e) {
        uni.navigateBack();
      }
    },
    validateStep() {
      const f = this.form;
      if (this.step === 0) {
        if (!f.applicantName) return '请填写申请人姓名';
        if (!/^1[3-9]\d{9}$/.test(f.applicantPhone)) return '请填写正确的手机号';
        if (!f.applicantAge || Number(f.applicantAge) < 18) return '领养人须年满 18 周岁';
        if (!f.city) return '请填写现居住城市';
      }
      if (this.step === 1) {
        if (!f.housingType) return '请选择住房类型';
        if (f.hasPetExperience === null || f.familyAgree === null || f.hasChildren === null || f.hasAllergy === null) {
          return '请完成居住与经验选项';
        }
        if (f.familyAgree !== 1) return '需全体家人同意才能申请领养';
        if (!f.reason || f.reason.length < 10) return '领养原因至少 10 个字';
      }
      return '';
    },
    next() {
      const msg = this.validateStep();
      if (msg) {
        uni.showToast({ title: msg, icon: 'none' });
        return;
      }
      this.step += 1;
    },
    async submit() {
      const msg = this.validateStep();
      if (msg) {
        uni.showToast({ title: msg, icon: 'none' });
        return;
      }
      const confirmed = await new Promise((resolve) => {
        uni.showModal({
          title: '确认提交申请？',
          content: '提交后请保持电话畅通，等待救助站初筛通知。',
          success: (res) => resolve(res.confirm)
        });
      });
      if (!confirmed) return;
      this.submitting = true;
      try {
        await submitApplication(Object.assign({ petId: this.petId }, this.form, {
          applicantAge: Number(this.form.applicantAge)
        }));
        uni.showToast({ title: '申请已提交', icon: 'success' });
        setTimeout(() => {
          uni.redirectTo({ url: '/pagesUser/my-applications/my-applications' });
        }, 900);
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.submitting = false;
      }
    },
    age(m) {
      if (!m && m !== 0) return '';
      if (m < 12) return m + ' 个月';
      const y = Math.floor(m / 12);
      return m % 12 === 0 ? y + ' 岁' : y + ' 岁 ' + (m % 12) + ' 个月';
    }
  }
};
</script>

<style scoped>
.pet {
  display: flex;
  align-items: center;
  margin-top: 24rpx;
}

.pet__img {
  width: 112rpx;
  height: 112rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  background: #F2F3F5;
}

.pet__body {
  margin-left: 20rpx;
}

.pet__name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2733;
}

.pet__meta {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #667080;
}

.steps {
  display: flex;
  padding: 24rpx 32rpx;
}

.step {
  flex: 1;
  display: flex;
  align-items: center;
}

.step__dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #F2F3F5;
  color: #A9B0BC;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step.active .step__dot {
  background: #FF7A45;
  color: #1F2733;
  font-weight: 600;
}

.step__label {
  margin-left: 10rpx;
  font-size: 24rpx;
  color: #A9B0BC;
}

.step.active .step__label {
  color: #1F2733;
  font-weight: 500;
}

.form-item {
  padding: 16rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #667080;
  margin-bottom: 12rpx;
}

.form-input {
  height: 72rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1F2733;
}

.form-picker {
  height: 72rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #4E5969;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.arrow {
  color: #A9B0BC;
}

.seg {
  display: flex;
  gap: 16rpx;
}

.seg__item {
  flex: 1;
  height: 72rpx;
  border-radius: 16rpx;
  background: #F7F8FA;
  color: #667080;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seg__item.active {
  background: #FFF1EB;
  color: #B23A0E;
  font-weight: 500;
  border: 2rpx solid #FF7A45;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  color: #1F2733;
  box-sizing: border-box;
}

.confirm {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.confirm__label {
  width: 160rpx;
  font-size: 26rpx;
  color: #667080;
  flex-shrink: 0;
}

.confirm__value {
  flex: 1;
  font-size: 26rpx;
  color: #1F2733;
  word-break: break-all;
}

.confirm__tip {
  display: block;
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #8A5A00;
  background: #FFF7E6;
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
  line-height: 38rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  gap: 20rpx;
  padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  border-top: 2rpx solid #E5E6EB;
}

.footer__prev {
  width: 240rpx;
}

.footer__next {
  flex: 1;
}
.fill-tip {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 18rpx 22rpx;
  margin-bottom: 26rpx;
  border-radius: 14rpx;
  background: #FFF6EF;
  border: 2rpx solid #FFE0CE;
}

.fill-tip__paw {
  font-size: 26rpx;
}

.fill-tip__text {
  font-size: 24rpx;
  color: #B23A0E;
}

</style>
