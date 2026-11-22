<template>
  <div class="rescue-report">
    <div class="wrap">
      <div class="report-col">
        <header class="page-head">
          <h1>流浪动物上报</h1>
          <p>发现流浪猫狗需要救助时提交线索，救助机构会按紧急程度安排跟进。</p>
        </header>

        <el-form ref="reportForm" :model="form" :rules="rules" label-position="top" @submit.native.prevent>
          <!-- 第 1 屏：线索说明 -->
          <section class="report-card" aria-label="线索说明">
            <h2 class="report-card__title">线索说明</h2>

            <el-row :gutter="24">
              <el-col :xs="24" :sm="12">
                <el-form-item label="物种" prop="categoryId">
                  <el-select
                    v-model="form.categoryId"
                    class="report-select"
                    clearable
                    placeholder="不确定可选其他">
                    <el-option v-for="c in speciesOptions" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="紧急程度" prop="urgency">
                  <el-radio-group v-model="form.urgency">
                    <el-radio v-for="u in urgencies" :key="u.value" :label="u.value">{{ u.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <p class="urgency-hint">{{ currentUrgencyHint }}</p>
              </el-col>
              <el-col :span="24">
                <el-form-item label="情况描述" prop="description">
                  <el-input
                    v-model="form.description"
                    type="textarea"
                    :rows="6"
                    maxlength="500"
                    show-word-limit
                    placeholder="描述动物特征（毛色/体型/项圈）与所处环境（街道/小区/桥下），以及当前状态" />
                </el-form-item>
              </el-col>
            </el-row>
          </section>

          <!-- 第 2 屏：发生位置 -->
          <section class="report-card" aria-label="发生位置">
            <h2 class="report-card__title">发生位置</h2>

            <el-row :gutter="24">
              <el-col :xs="24" :sm="8">
                <el-form-item label="省份" prop="province">
                  <el-select v-model="form.province" class="report-select" placeholder="选择省份" @change="onProvinceChange">
                    <el-option v-for="p in regionOptions" :key="p.name" :label="p.name" :value="p.name" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-form-item label="城市" prop="city">
                  <el-select v-model="form.city" class="report-select" placeholder="选择城市" @change="onCityChange">
                    <el-option v-for="c in cityOptions" :key="c.name" :label="c.name" :value="c.name" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-form-item label="区县" prop="district">
                  <el-select v-model="form.district" class="report-select" clearable placeholder="选择区县">
                    <el-option v-for="d in districtOptions" :key="d" :label="d" :value="d" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="详细地址" prop="address">
                  <el-input v-model="form.address" maxlength="255" placeholder="街道、门牌或地标，如：文一西路 88 号东侧高架下" />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 演示地图选点 -->
            <div class="map-block">
              <p class="map-block__label">地图选点</p>
              <div
                ref="map"
                class="demo-map"
                :class="{ picked: !!marker }"
                role="button"
                tabindex="0"
                aria-label="演示地图，点击选择位置"
                @click="onMapClick"
                @keydown.enter="onMapClick">
                <span v-if="marker" class="demo-map__marker" :style="markerStyle" aria-hidden="true"></span>
                <span v-else class="demo-map__placeholder">点击地图选择大致位置</span>
              </div>
              <p v-if="form.longitude !== null" class="map-block__coords num">
                已选点：经度 {{ form.longitude }} · 纬度 {{ form.latitude }}
              </p>
              <p class="map-block__note">演示地图，用于提交大致经纬度，救助机构会按详细地址上门。</p>
            </div>

            <!-- 现场照片 -->
            <div class="photos-block">
              <p class="photos-block__label">现场照片</p>
              <ImageUploader v-model="images" biz-type="report" :limit="9" />
            </div>
          </section>

          <div class="report-footer">
            <el-button :disabled="submitting" @click="resetForm">清空重填</el-button>
            <el-button type="primary" :loading="submitting" @click="submit">提交线索</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import ImageUploader from '@/components/ImageUploader.vue';
import { createReport } from '@/api/rescue';
import { categories } from '@/api/pet';

export default {
  name: 'RescueReportPage',
  components: { ImageUploader },
  data() {
    return {
      submitting: false,
      speciesOptions: [],
      regionOptions: [
        {
          name: '浙江省',
          cities: [
            { name: '杭州市', districts: ['上城区', '拱墅区', '西湖区', '滨江区', '余杭区', '临平区', '萧山区'] },
            { name: '宁波市', districts: ['海曙区', '江北区', '鄞州区'] },
            { name: '温州市', districts: ['鹿城区', '龙湾区', '瓯海区'] }
          ]
        },
        {
          name: '四川省',
          cities: [
            { name: '成都市', districts: ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '高新区', '双流区'] },
            { name: '绵阳市', districts: ['涪城区', '游仙区'] },
            { name: '德阳市', districts: ['旌阳区', '罗江区'] }
          ]
        }
      ],
      urgencies: [
        { value: 1, label: '一般', hint: '动物状态稳定，能自主活动，暂无直接危险。' },
        { value: 2, label: '紧急', hint: '动物受伤、被困或处于车流附近，需要尽快到场处理。' },
        { value: 3, label: '危急', hint: '动物生命垂危或处于溺水、坠井等危险环境，需要立即救援。' }
      ],
      images: [],
      marker: null,
      form: {
        categoryId: null,
        urgency: 1,
        description: '',
        province: '',
        city: '',
        district: '',
        address: '',
        longitude: null,
        latitude: null
      },
      rules: {
        description: [
          { required: true, message: '请描述动物特征与所处环境', trigger: 'blur' },
          { max: 500, message: '描述最长 500 字', trigger: 'blur' }
        ],
        province: [{ required: true, message: '请选择省份', trigger: 'change' }],
        city: [{ required: true, message: '请选择城市', trigger: 'change' }],
        address: [{ required: true, message: '请填写详细地址', trigger: 'blur' }]
      }
    };
  },
  computed: {
    cityOptions() {
      const province = this.regionOptions.find((p) => p.name === this.form.province);
      return province ? province.cities : [];
    },
    districtOptions() {
      const city = this.cityOptions.find((c) => c.name === this.form.city);
      return city ? city.districts : [];
    },
    currentUrgencyHint() {
      const u = this.urgencies.find((i) => i.value === this.form.urgency);
      return u ? u.hint : '';
    },
    markerStyle() {
      return {
        left: `${this.marker.x}%`,
        top: `${this.marker.y}%`
      };
    }
  },
  async mounted() {
    if (!this.$store.state.user.token) {
      this.$router.replace(`/login?redirect=${encodeURIComponent(this.$route.fullPath)}`);
      return;
    }
    try {
      this.speciesOptions = (await categories()) || [];
    } catch (e) {
      /* 拦截器已提示 */
    }
  },
  methods: {
    onProvinceChange() {
      this.form.city = '';
      this.form.district = '';
    },
    onCityChange() {
      this.form.district = '';
    },
    onMapClick(e) {
      if (typeof e.clientX !== 'number' || typeof e.clientY !== 'number') return;
      const rect = this.$refs.map.getBoundingClientRect();
      const ratioX = Math.min(Math.max((e.clientX - rect.left) / rect.width, 0), 1);
      const ratioY = Math.min(Math.max((e.clientY - rect.top) / rect.height, 0), 1);
      this.marker = { x: ratioX * 100, y: ratioY * 100 };
      this.form.longitude = Number((119.9 + ratioX * 0.4).toFixed(4));
      this.form.latitude = Number((30.4 - ratioY * 0.3).toFixed(4));
    },
    resetForm() {
      this.form = {
        categoryId: null,
        urgency: 1,
        description: '',
        province: '',
        city: '',
        district: '',
        address: '',
        longitude: null,
        latitude: null
      };
      this.images = [];
      this.marker = null;
      this.$nextTick(() => {
        this.$refs.reportForm.clearValidate();
      });
    },
    async submit() {
      this.$refs.reportForm.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        try {
          await createReport({
            categoryId: this.form.categoryId || null,
            description: this.form.description,
            images: this.images.map((img) => img.url),
            province: this.form.province,
            city: this.form.city,
            district: this.form.district,
            address: this.form.address,
            longitude: this.form.longitude,
            latitude: this.form.latitude,
            urgency: this.form.urgency
          });
          this.$message.success('线索已提交，救助机构会尽快跟进');
          this.resetForm();
        } catch (e) {
          /* 拦截器已提示 */
        } finally {
          this.submitting = false;
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.wrap {
  @include wrap;
}

.rescue-report {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
  padding: 40px 0 80px;
}

.report-col {
  max-width: 720px;
  margin: 0 auto;
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

.report-card {
  @include card;

  padding: 24px;
  margin-bottom: 24px;

  &__title {
    font-size: 16px;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid $border-color;
  }
}

.report-select {
  width: 100%;
}

.urgency-hint {
  margin: -8px 0 16px;
  font-size: 13px;
  color: $text-secondary;
}

// 演示地图
.map-block {
  margin-top: 4px;

  &__label {
    font-size: 14px;
    color: $text-primary;
    margin-bottom: 8px;
  }

  &__coords {
    margin-top: 12px;
    font-size: 13px;
    color: $color-primary-text;
  }

  &__note {
    margin-top: 4px;
    font-size: 12px;
    color: $text-secondary;
  }
}

.demo-map {
  position: relative;
  height: 280px;
  border-radius: $radius-base;
  border: 1px solid $border-color;
  cursor: crosshair;
  overflow: hidden;
  background:
    linear-gradient($border-color 1px, transparent 1px),
    linear-gradient(90deg, $border-color 1px, transparent 1px),
    linear-gradient(160deg, $color-secondary-light 0%, $color-primary-light 55%, $color-secondary-light 100%);
  background-size: 40px 40px, 40px 40px, cover;

  &:focus-visible {
    @include focus-ring;
  }

  &__marker {
    position: absolute;
    width: 16px;
    height: 16px;
    border-radius: $radius-circle;
    background: $color-primary;
    border: 2px solid $bg-card;
    box-shadow: $shadow-1;
    transform: translate(-50%, -50%);

    &::after {
      content: "";
      position: absolute;
      left: 50%;
      top: 50%;
      width: 56px;
      height: 56px;
      border-radius: $radius-circle;
      background: rgba($color-primary, 0.12);
      transform: translate(-50%, -50%);
    }
  }

  &__placeholder {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    padding: 8px 16px;
    border-radius: $radius-base;
    background: rgba($bg-card, 0.85);
    color: $text-secondary;
    font-size: 13px;
    white-space: nowrap;
  }
}

.photos-block {
  margin-top: 20px;

  &__label {
    font-size: 14px;
    color: $text-primary;
    margin-bottom: 8px;
  }
}

.report-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-bottom: 8px;
}
</style>
