<template>
  <div class="volunteer-page">
    <div class="wrap">
      <div class="volunteer-grid">
        <!-- 第 1 屏：志愿说明 -->
        <section class="intro-card" aria-labelledby="sec-intro">
          <div class="intro-card__media">
            <img src="/imgs/volunteer/volunteer.jpg" alt="志愿者陪救助站的狗狗玩耍" loading="lazy" />
          </div>
          <div class="intro-card__body">
            <h1 id="sec-intro">志愿者报名</h1>
            <p class="intro-card__lead">
              志愿者由入驻救助站统一安排，周末半天也可以帮上忙。报名后救助站会通过站内消息通知审核结果。
            </p>
            <ul class="intro-card__list">
              <li>
                <h3>日常照看</h3>
                <p>喂食、清洁犬舍猫舍，陪狗狗散步，帮小猫适应与人接触。</p>
              </li>
              <li>
                <h3>专业支持</h3>
                <p>摄影记录、文案撰写、运输协助、送医陪同，有一技之长都可以发挥。</p>
              </li>
              <li>
                <h3>活动协助</h3>
                <p>领养开放日布置与引导、线上答疑，让更多宠物被看到。</p>
              </li>
            </ul>
          </div>
        </section>

        <!-- 第 2 屏：报名表单 -->
        <section class="form-card" aria-labelledby="sec-form">
          <h2 id="sec-form">填写报名信息</h2>
          <el-form ref="volunteerForm" :model="form" :rules="rules" label-position="top" @submit.native.prevent>
            <el-form-item label="选择救助站" prop="shelterId">
              <el-select
                v-model="form.shelterId"
                class="form-card__select"
                placeholder="选择想加入的救助站">
                <el-option
                  v-for="s in shelters"
                  :key="s.id"
                  :label="`${s.name}（${s.city || '城市待定'}）`"
                  :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" maxlength="32" placeholder="填写真实姓名" />
            </el-form-item>
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" maxlength="11" placeholder="用于救助站联系你" />
            </el-form-item>
            <el-form-item label="技能特长" prop="skills">
              <el-input v-model="form.skills" maxlength="255" placeholder="选填，如：摄影 / 有车可运输 / 兽医护理经验" />
            </el-form-item>
            <el-form-item label="可参与时间" prop="availableTime">
              <el-input v-model="form.availableTime" maxlength="255" placeholder="选填，如：每周六下午 / 工作日晚上" />
            </el-form-item>
            <el-button type="primary" class="form-card__submit" :loading="submitting" @click="submit">提交报名</el-button>
            <p class="form-card__tip">提交后救助站审核，审核结果会通过站内消息通知你。</p>
          </el-form>
        </section>
      </div>
    </div>
  </div>
</template>

<script>
import { applyVolunteer } from '@/api/volunteer';
import { shelters } from '@/api/shelter';

export default {
  name: 'VolunteerPage',
  data() {
    return {
      submitting: false,
      shelters: [],
      form: {
        shelterId: null,
        realName: '',
        phone: '',
        skills: '',
        availableTime: ''
      },
      rules: {
        shelterId: [{ required: true, message: '请选择救助站', trigger: 'change' }],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ]
      }
    };
  },
  async mounted() {
    if (!this.$store.state.user.token) {
      this.$router.replace(`/login?redirect=${encodeURIComponent(this.$route.fullPath)}`);
      return;
    }
    try {
      this.shelters = (await shelters()) || [];
    } catch (e) {
      /* 拦截器已提示 */
    }
  },
  methods: {
    resetForm() {
      this.form = { shelterId: null, realName: '', phone: '', skills: '', availableTime: '' };
      this.$nextTick(() => {
        this.$refs.volunteerForm.clearValidate();
      });
    },
    submit() {
      this.$refs.volunteerForm.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        try {
          await applyVolunteer({
            shelterId: this.form.shelterId,
            realName: this.form.realName,
            phone: this.form.phone,
            skills: this.form.skills,
            availableTime: this.form.availableTime
          });
          this.$message.success('报名已提交，救助站审核后会通过站内消息通知你');
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

.volunteer-page {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
  padding: 40px 0 80px;
}

.volunteer-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr);
  gap: 24px;
  align-items: start;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

// 说明卡
.intro-card {
  @include card;

  overflow: hidden;

  &__media {
    aspect-ratio: 4 / 3;
    background: $bg-page;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    padding: 24px;
  }

  h1 {
    font-size: $font-h3;
    line-height: $lh-h3;
  }

  &__lead {
    margin-top: 12px;
    font-size: 14px;
    line-height: 24px;
    color: $text-regular;
  }

  &__list {
    margin: 20px 0 0;
    padding: 0;
    list-style: none;

    li {
      padding: 16px 0;
      border-top: 1px solid $border-color;
    }

    h3 {
      font-size: 15px;
    }

    p {
      margin-top: 4px;
      font-size: 13px;
      line-height: 22px;
      color: $text-secondary;
    }
  }
}

// 表单卡
.form-card {
  @include card;

  padding: 24px;

  h2 {
    font-size: 20px;
    line-height: 30px;
    margin-bottom: 20px;
  }

  &__select {
    width: 100%;
  }

  &__submit {
    width: 100%;
    margin-top: 8px;
  }

  &__tip {
    margin-top: 12px;
    font-size: 13px;
    color: $text-secondary;
  }
}
</style>
