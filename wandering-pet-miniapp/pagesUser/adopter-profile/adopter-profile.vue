<template>
  <view class="page">
    <!-- 补全提示 -->
    <view class="head card">
      <view class="head__paw">🐾</view>
      <view class="head__body">
        <text class="head__title">领养资料</text>
        <text class="head__sub">补全后申请领养时自动带入，多只宠物免重复填写；每次提交申请也会自动更新这份资料。</text>
      </view>
    </view>

    <view class="card">
      <view class="form-item">
        <text class="form-label">申请人姓名</text>
        <input class="form-input" v-model="form.adopterName" placeholder="真实姓名" maxlength="32" />
      </view>
      <view class="form-item">
        <text class="form-label">联系电话</text>
        <view class="form-picker">
          <text>{{ phoneMasked || '未绑定手机号（在「绑定 / 更换手机号」中操作）' }}</text>
        </view>
      </view>
      <view class="form-item">
        <text class="form-label">年龄</text>
        <input class="form-input" v-model="form.adopterAge" type="number" placeholder="须年满 18 周岁" />
      </view>
      <view class="form-item">
        <text class="form-label">现居住城市</text>
        <input class="form-input" v-model="form.city" placeholder="如：杭州" maxlength="32" />
      </view>
      <view class="form-item">
        <text class="form-label">职业</text>
        <input class="form-input" v-model="form.occupation" placeholder="如：教师 / 工程师（选填）" maxlength="32" />
      </view>
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
    </view>

    <view class="footer">
      <button class="btn-primary" :loading="saving" :disabled="saving" @click="save">保存资料</button>
    </view>
  </view>
</template>

<script>
import { getAdopterProfile, updateAdopterProfile } from '../../common/api.js';

export default {
  data() {
    return {
      saving: false,
      phoneMasked: '',
      yesNo: [
        { key: 'hasPetExperience', label: '有养宠经验' },
        { key: 'familyAgree', label: '家人同意饲养' },
        { key: 'hasChildren', label: '家中有儿童' },
        { key: 'hasAllergy', label: '有过敏史' }
      ],
      form: {
        adopterName: '',
        city: '',
        adopterAge: '',
        occupation: '',
        housingType: '',
        hasPetExperience: null,
        familyAgree: null,
        hasChildren: null,
        hasAllergy: null
      }
    };
  },
  computed: {
    isLoggedIn() {
      return this.$store.getters.isLoggedIn;
    }
  },
  onLoad() {
    this.load();
  },
  methods: {
    async load() {
      if (!this.isLoggedIn) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        setTimeout(() => uni.redirectTo({ url: '/pagesUser/login/login' }), 800);
        return;
      }
      try {
        const p = await getAdopterProfile();
        if (!p) return;
        this.phoneMasked = p.phone ? this.mask(p.phone) : '';
        const f = this.form;
        const has = (v) => v !== null && v !== undefined && v !== '';
        if (has(p.adopterName)) f.adopterName = p.adopterName;
        if (has(p.city)) f.city = p.city;
        if (has(p.adopterAge)) f.adopterAge = String(p.adopterAge);
        if (has(p.occupation)) f.occupation = p.occupation;
        if (has(p.housingType)) f.housingType = p.housingType;
        if (has(p.hasPetExperience)) f.hasPetExperience = p.hasPetExperience;
        if (has(p.familyAgree)) f.familyAgree = p.familyAgree;
        if (has(p.hasChildren)) f.hasChildren = p.hasChildren;
        if (has(p.hasAllergy)) f.hasAllergy = p.hasAllergy;
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async save() {
      const f = this.form;
      if (!f.adopterName) {
        uni.showToast({ title: '请填写申请人姓名', icon: 'none' });
        return;
      }
      this.saving = true;
      try {
        await updateAdopterProfile(Object.assign({}, f, {
          adopterAge: f.adopterAge ? Number(f.adopterAge) : null
        }));
        uni.showToast({ title: '资料已保存', icon: 'success' });
        setTimeout(() => uni.navigateBack(), 800);
      } catch (e) {
        // 拦截器已提示
      } finally {
        this.saving = false;
      }
    },
    mask(phone) {
      return phone && phone.length >= 7 ? phone.substring(0, 3) + '****' + phone.substring(phone.length - 4) : phone;
    }
  }
};
</script>

<style scoped>
.page {
  padding: 24rpx 32rpx 160rpx;
}

.head {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 28rpx;
}

.head__paw {
  font-size: 44rpx;
  line-height: 1;
}

.head__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.head__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2733;
}

.head__sub {
  font-size: 24rpx;
  line-height: 36rpx;
  color: #667080;
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
  padding: 26rpx 8rpx;
  border-bottom: 2rpx solid #F2F3F5;
}

.form-label {
  font-size: 28rpx;
  color: #1F2733;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  color: #1F2733;
}

.form-picker {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  font-size: 28rpx;
  color: #1F2733;
}

.arrow {
  color: #A9B0BC;
}

.seg {
  display: flex;
  gap: 12rpx;
}

.seg__item {
  width: 104rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14rpx;
  border: 2rpx solid #E5E6EB;
  font-size: 26rpx;
  color: #4E5969;
}

.seg__item.active {
  background: #FFF1EB;
  border-color: #FF7A45;
  color: #B23A0E;
  font-weight: 600;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 32rpx calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  border-top: 2rpx solid #EDEFF2;
}
</style>
