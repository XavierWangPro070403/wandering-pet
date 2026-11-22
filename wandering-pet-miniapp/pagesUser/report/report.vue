<template>
  <view class="page">
    <view class="card">
      <text class="card-title">情况描述</text>

      <view class="form-item">
        <text class="form-label">动物物种（可空）</text>
        <picker :range="categoryNames" @change="onCategory">
          <view class="form-picker"><text>{{ categoryName || '请选择' }}</text><text class="arrow">›</text></view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">紧急程度</text>
        <view class="seg">
          <view class="seg__item" :class="{ active: form.urgency === 1 }" @click="form.urgency = 1"><text>一般</text></view>
          <view class="seg__item" :class="{ active: form.urgency === 2 }" @click="form.urgency = 2"><text>紧急</text></view>
          <view class="seg__item seg__item--danger" :class="{ active: form.urgency === 3 }" @click="form.urgency = 3"><text>危急(受伤/被困)</text></view>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">动物特征与所处环境</text>
        <textarea class="form-textarea" v-model="form.description" maxlength="500" placeholder="品种/毛色/体型、精神状态、具体位置与周边环境（500 字内）" />
      </view>

      <view class="form-item">
        <text class="form-label">现场照片（最多 9 张）</text>
        <view class="images">
          <view class="images__item" v-for="(img, i) in images" :key="i">
            <image class="images__img" :src="img" mode="aspectFill" @click="preview(i)" />
            <view class="images__del" @click="removeImage(i)"><text>×</text></view>
          </view>
          <view class="images__add" v-if="images.length < 9" @click="chooseImage"><text>＋</text></view>
        </view>
      </view>
    </view>

    <view class="card">
      <text class="card-title">位置</text>
      <view class="form-item">
        <text class="form-label">省份 / 城市</text>
        <view class="form-row">
          <input class="form-input form-input--half" v-model="form.province" placeholder="省份" />
          <input class="form-input form-input--half" v-model="form.city" placeholder="城市" />
        </view>
      </view>
      <view class="form-item">
        <text class="form-label">区县</text>
        <input class="form-input" v-model="form.district" placeholder="如：西湖区" />
      </view>
      <view class="form-item">
        <text class="form-label">位置描述（区 + 街道/地标）</text>
        <input class="form-input" v-model="form.address" placeholder="如：文三路东侧小区绿化带" />
      </view>
      <view class="form-item">
        <text class="form-label">地图选点</text>
        <view class="map" @click="chooseLocation">
          <view v-if="form.longitude" class="map__pin"><text>📍</text></view>
          <text class="map__text">{{ form.longitude ? '已选点：' + form.longitude.toFixed(4) + ', ' + form.latitude.toFixed(4) : '点击选择位置（演示地图）' }}</text>
        </view>
      </view>
    </view>

    <view style="padding: 0 32rpx;">
      <button class="btn-primary" :disabled="submitting" @click="submit">提交线索</button>
      <text class="tip">提交后平台会通知附近救助站跟进处理，处理进度可在“我的上报”查看。</text>
    </view>

    <!-- 我的上报 -->
    <view class="card" style="margin-top:40rpx;">
      <text class="card-title">我的上报</text>
      <view class="report" v-for="r in reports" :key="r.id">
        <view class="report__row">
          <text class="report__no num">{{ r.reportNo }}</text>
          <text class="tag" :class="'tag--' + tone(r.status)">{{ label(r.status) }}</text>
        </view>
        <text class="report__desc">{{ r.description }}</text>
        <text v-if="r.handleRemark" class="report__handle">处理说明：{{ r.handleRemark }}</text>
      </view>
      <view v-if="!reports.length" class="empty"><text class="empty__title">暂无上报记录</text></view>
    </view>
  </view>
</template>

<script>
import { createReport, myReports, petCategories } from '../../common/api.js';
import http from '../../common/request.js';

export default {
  data() {
    return {
      form: {
        categoryId: null,
        urgency: 2,
        description: '',
        images: [],
        province: '',
        city: '',
        district: '',
        address: '',
        longitude: null,
        latitude: null
      },
      images: [],
      categories: [],
      reports: [],
      submitting: false
    };
  },
  computed: {
    categoryNames() {
      return (this.categories || []).map((c) => c.name);
    },
    categoryName() {
      const c = (this.categories || []).find((x) => x.id === this.form.categoryId);
      return c ? c.name : '';
    }
  },
  onLoad() {
    petCategories().then((list) => { this.categories = list || []; }).catch(() => {});
    this.loadReports();
  },
  methods: {
    onCategory(e) {
      const c = this.categories[Number(e.detail.value)];
      this.form.categoryId = c ? c.id : null;
    },
    async chooseImage() {
      const res = await new Promise((resolve) => {
        uni.chooseImage({ count: 9 - this.images.length, sizeType: ['compressed'], success: resolve, fail: () => resolve(null) });
      });
      if (!res) return;
      for (const path of res.tempFilePaths) {
        try {
          const data = await http.upload(path, 'report');
          this.images.push(data.url);
          this.form.images.push(data.url);
        } catch (e) { /* */ }
      }
    },
    removeImage(i) {
      this.images.splice(i, 1);
      this.form.images.splice(i, 1);
    },
    preview(i) {
      uni.previewImage({ urls: this.images, current: i });
    },
    chooseLocation() {
      // 优先调用微信地图选点；开发者工具不支持时退化为演示选点
      uni.chooseLocation({
        success: (res) => {
          this.form.longitude = res.longitude;
          this.form.latitude = res.latitude;
          if (res.address) this.form.address = res.address;
        },
        fail: () => {
          // 演示选点：城市中心附近随机偏移
          this.form.longitude = 120.1254 + Math.random() * 0.01;
          this.form.latitude = 30.2789 + Math.random() * 0.01;
          if (!this.form.province) {
            this.form.province = '浙江省';
            this.form.city = '杭州';
            this.form.district = '西湖区';
          }
        }
      });
    },
    async submit() {
      const f = this.form;
      if (!f.description || f.description.length < 10) {
        uni.showToast({ title: '请描述动物特征与环境（至少 10 字）', icon: 'none' });
        return;
      }
      if (!f.province || !f.city || !f.address) {
        uni.showToast({ title: '请填写位置信息', icon: 'none' });
        return;
      }
      this.submitting = true;
      try {
        await createReport(f);
        uni.showToast({ title: '线索已提交，机构会尽快跟进', icon: 'none' });
        f.description = '';
        this.images = [];
        f.images = [];
        this.loadReports();
      } catch (e) { /* */ } finally {
        this.submitting = false;
      }
    },
    async loadReports() {
      try {
        const data = await myReports({ pageNum: 1, pageSize: 10 });
        this.reports = (data && data.list) || [];
      } catch (e) { /* */ }
    },
    tone: (s) => ({ PENDING: 'warning', ASSIGNED: 'primary', PROCESSING: 'primary', RESOLVED: 'success', CLOSED: 'neutral' }[s] || 'neutral'),
    label: (s) => ({ PENDING: '待处理', ASSIGNED: '已分派', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }[s] || s)
  }
};
</script>

<style scoped>
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

.form-row {
  display: flex;
  gap: 16rpx;
}

.form-input--half {
  flex: 1;
}

.arrow {
  color: #A9B0BC;
}

.seg {
  display: flex;
  gap: 12rpx;
}

.seg__item {
  flex: 1;
  height: 72rpx;
  border-radius: 16rpx;
  background: #F7F8FA;
  color: #667080;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.seg__item.active {
  background: #FFF1EB;
  color: #B23A0E;
  font-weight: 500;
}

.seg__item--danger.active {
  background: #FFECEF;
  color: #B3122B;
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

.images {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.images__item {
  position: relative;
  width: 152rpx;
  height: 152rpx;
}

.images__img {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
}

.images__del {
  position: absolute;
  top: -12rpx;
  right: -12rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: rgba(31, 39, 51, 0.65);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.images__add {
  width: 152rpx;
  height: 152rpx;
  border-radius: 16rpx;
  border: 2rpx dashed #FF7A45;
  background: #FFF1EB;
  color: #B23A0E;
  font-size: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map {
  position: relative;
  height: 220rpx;
  border-radius: 16rpx;
  background:
    repeating-linear-gradient(0deg, #EBEEF2 0, #EBEEF2 2rpx, transparent 2rpx, transparent 40rpx),
    repeating-linear-gradient(90deg, #EBEEF2 0, #EBEEF2 2rpx, transparent 2rpx, transparent 40rpx),
    #F7F8FA;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map__pin {
  position: absolute;
  font-size: 44rpx;
}

.map__text {
  font-size: 26rpx;
  color: #667080;
}

.tip {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #8A5A00;
  background: #FFF7E6;
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
  line-height: 38rpx;
}

.report {
  padding: 20rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.report:last-child {
  border-bottom: none;
}

.report__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report__no {
  font-size: 26rpx;
  color: #1F2733;
  font-weight: 500;
}

.report__desc {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #4E5969;
}

.report__handle {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #0F6F63;
}
</style>
