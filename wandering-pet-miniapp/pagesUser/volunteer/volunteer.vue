<template>
  <view class="page">
    <!-- 说明 -->
    <view class="intro card">
      <image class="intro__img" src="http://localhost:8080/imgs/pet/p36.jpg" mode="aspectFill" />
      <text class="card-title">成为志愿者</text>
      <text class="intro__text">遛狗、摄影、协助送医、文案整理——每周半天也可以帮上忙。提交报名后，救助站审核通过会通过站内消息通知你。</text>
    </view>

    <!-- 报名表单 -->
    <view class="card">
      <view class="form-item">
        <text class="form-label">选择救助站</text>
        <picker :range="shelterNames" @change="onShelter">
          <view class="form-picker"><text>{{ shelterName || '请选择' }}</text><text class="arrow">›</text></view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">真实姓名</text>
        <input class="form-input" v-model="form.realName" maxlength="32" placeholder="与身份证一致" />
      </view>
      <view class="form-item">
        <text class="form-label">联系电话</text>
        <input class="form-input" v-model="form.phone" type="number" maxlength="11" placeholder="11 位手机号" />
      </view>
      <view class="form-item">
        <text class="form-label">个人技能（选填）</text>
        <input class="form-input" v-model="form.skills" maxlength="255" placeholder="摄影 / 医疗 / 运输 / 文案" />
      </view>
      <view class="form-item">
        <text class="form-label">可参与时间（选填）</text>
        <input class="form-input" v-model="form.availableTime" maxlength="255" placeholder="如：周末全天" />
      </view>
      <button class="btn-primary" style="margin-top:24rpx;" :disabled="submitting" @click="submit">提交报名</button>
      <text class="tip">报名通过审核后，请留意站内消息与机构任务安排。</text>
    </view>
  </view>
</template>

<script>
import { applyVolunteer, shelters } from '../../common/api.js';

export default {
  data() {
    return {
      shelters: [],
      form: {
        shelterId: null,
        realName: '',
        phone: '',
        skills: '',
        availableTime: ''
      },
      submitting: false
    };
  },
  computed: {
    shelterNames() {
      return (this.shelters || []).map((s) => s.name + '（' + s.city + '）');
    },
    shelterName() {
      const s = (this.shelters || []).find((x) => x.id === this.form.shelterId);
      return s ? s.name : '';
    }
  },
  onLoad() {
    shelters().then((list) => { this.shelters = list || []; }).catch(() => {});
  },
  methods: {
    onShelter(e) {
      const s = this.shelters[Number(e.detail.value)];
      this.form.shelterId = s ? s.id : null;
    },
    async submit() {
      if (!this.form.shelterId) {
        uni.showToast({ title: '请选择救助站', icon: 'none' });
        return;
      }
      if (!this.form.realName) {
        uni.showToast({ title: '请填写真实姓名', icon: 'none' });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        uni.showToast({ title: '手机号格式不正确', icon: 'none' });
        return;
      }
      this.submitting = true;
      try {
        await applyVolunteer(this.form);
        uni.showToast({ title: '报名已提交，等待机构审核', icon: 'none' });
        setTimeout(() => uni.navigateBack(), 900);
      } catch (e) { /* */ } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.intro {
  padding: 0;
  overflow: hidden;
  margin-top: 24rpx;
}

.intro__img {
  width: 100%;
  height: 280rpx;
  display: block;
}

.intro__text {
  display: block;
  padding: 0 24rpx 24rpx;
  font-size: 26rpx;
  color: #4E5969;
  line-height: 42rpx;
}

.intro .card-title {
  display: block;
  padding: 24rpx 24rpx 0;
}

.form-item {
  padding: 16rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.form-item:last-of-type {
  border-bottom: none;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #667080;
  margin-bottom: 12rpx;
}

.form-picker,
.form-input {
  height: 80rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1F2733;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.arrow {
  color: #A9B0BC;
}

.tip {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #A9B0BC;
}
</style>
