<template>
  <div class="pet-detail">
    <div class="wrap">
      <!-- 第 1 屏：画廊 + 信息卡（桌面 720/360，信息卡 sticky；移动端底部行动条） -->
      <div class="detail-grid">
        <div class="gallery">
          <div class="gallery__main">
            <img :src="currentImage" :alt="`${pet.name}的照片`" />
          </div>
          <div class="gallery__thumbs" role="tablist" aria-label="相册">
            <button
              v-for="(img, i) in pet.images"
              :key="i"
              class="gallery__thumb"
              :class="{ current: i === currentIndex }"
              role="tab"
              :aria-selected="i === currentIndex ? 'true' : 'false'"
              @click="currentIndex = i">
              <img :src="img.url" alt="" loading="lazy" />
            </button>
          </div>
        </div>

        <div class="info-card">
          <div class="info-card__head">
            <h1 class="info-card__name">{{ pet.name }}</h1>
            <StatusTag :status="pet.status" />
          </div>
          <p class="info-card__no num">档案编号 {{ pet.petNo }}</p>

          <dl class="info-card__facts">
            <div><dt>物种/品种</dt><dd>{{ pet.categoryName }}{{ pet.breedName ? ' · ' + pet.breedName : '' }}</dd></div>
            <div><dt>性别</dt><dd>{{ genderText(pet.gender) }}</dd></div>
            <div><dt>月龄</dt><dd class="num">{{ ageText(pet.ageMonth) }}</dd></div>
            <div><dt>体重</dt><dd class="num">{{ pet.weight ? pet.weight + ' kg' : '—' }}</dd></div>
            <div><dt>体型</dt><dd>{{ sizeText(pet.size) }}</dd></div>
            <div><dt>毛色</dt><dd>{{ pet.color || '—' }}</dd></div>
          </dl>

          <ul class="info-card__flags">
            <li :class="{ ok: pet.sterilized === 1 }">{{ pet.sterilized === 1 ? '已绝育' : '未绝育' }}</li>
            <li :class="{ ok: pet.vaccinated === 1 }">{{ pet.vaccinated === 1 ? '已疫苗' : '未齐疫苗' }}</li>
            <li :class="{ ok: pet.dewormed === 1 }">{{ pet.dewormed === 1 ? '已驱虫' : '未驱虫' }}</li>
            <li :class="{ ok: pet.healthStatus === 'HEALTHY' }">{{ healthText(pet.healthStatus) }}</li>
          </ul>

          <div class="info-card__shelter">
            <p class="info-card__shelter-name">{{ shelterName }}</p>
            <p class="info-card__shelter-city">{{ pet.shelter && pet.shelter.city }}{{ pet.shelter && pet.shelter.district ? ' · ' + pet.shelter.district : '' }}</p>
          </div>

          <div class="info-card__actions">
            <el-button type="primary" size="large" :disabled="!canApply" @click="goApply">申请领养</el-button>
            <el-button size="large" :icon="pet.favorited ? 'el-icon-star-on' : 'el-icon-star-off'" @click="toggleFavorite">
              {{ pet.favorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
          <p v-if="!canApply" class="info-card__tip">{{ applyDisabledReason }}</p>
          <p v-else class="info-card__tip">提交后救助站 3 个工作日内完成初筛</p>
        </div>
      </div>

      <!-- 第 2 屏：档案表 + 健康时间轴 -->
      <section class="section" aria-labelledby="sec-record">
        <h2 id="sec-record">健康档案</h2>
        <el-timeline v-if="pet.healthRecords && pet.healthRecords.length" class="timeline">
          <el-timeline-item
            v-for="record in pet.healthRecords"
            :key="record.id"
            :timestamp="record.recordDate"
            :type="timelineType(record.recordType)"
            placement="top">
            <div class="timeline-card">
              <h3>{{ record.title }}</h3>
              <p v-if="record.content">{{ record.content }}</p>
              <p v-if="record.vetName" class="timeline-card__vet">{{ record.vetName }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
        <EmptyState v-else title="暂无健康档案" desc="救助站会在每次免疫、驱虫与治疗后更新档案。" />
      </section>

      <!-- 第 3 屏：救助故事 -->
      <section class="section" aria-labelledby="sec-story">
        <h2 id="sec-story">救助故事</h2>
        <p class="story-text">{{ pet.story || '这只宠物还没有完整的故事记录，可以先查看健康档案，或直接联系救助站了解近况。' }}</p>
      </section>

      <!-- 第 4 屏：相似推荐 -->
      <section class="section section--gray" aria-labelledby="sec-similar">
        <h2 id="sec-similar">相似推荐</h2>
        <div class="similar-grid">
          <PetCard v-for="p in similar" :key="p.id" :pet="p" />
        </div>
        <EmptyState v-if="!similar.length && !loading" title="暂无相似推荐" action-text="返回列表" @action="$router.push('/pets')" />
      </section>
    </div>

    <!-- 移动端底部行动条 -->
    <div class="mobile-bar">
      <button class="mobile-bar__fav" :class="{ on: pet.favorited }" @click="toggleFavorite">
        <i :class="pet.favorited ? 'el-icon-star-on' : 'el-icon-star-off'" aria-hidden="true"></i>
        收藏
      </button>
      <el-button type="primary" class="mobile-bar__apply" :disabled="!canApply" @click="goApply">申请领养</el-button>
    </div>
  </div>
</template>

<script>
import PetCard from '@/components/PetCard.vue';
import EmptyState from '@/components/EmptyState.vue';
import StatusTag from '@/components/StatusTag.vue';
import { petDetail, similarPets, recordView, favorite, unfavorite } from '@/api/pet';
import { ageText, genderText, sizeText } from '@/utils/format';

export default {
  name: 'PetDetailPage',
  components: { PetCard, EmptyState, StatusTag },
  data() {
    return {
      loading: true,
      pet: {},
      similar: [],
      currentIndex: 0
    };
  },
  computed: {
    currentImage() {
      const imgs = this.pet.images || [];
      return imgs[this.currentIndex] ? imgs[this.currentIndex].url : (this.pet.coverImage || '');
    },
    shelterName() {
      return (this.pet.shelter && this.pet.shelter.name) || '救助站';
    },
    canApply() {
      return this.pet.status === 'AVAILABLE';
    },
    applyDisabledReason() {
      return {
        PENDING: '该宠物已有申请审核中，暂不接受新申请',
        ADOPTED: '该宠物已被领养',
        MEDICAL: '该宠物正在治疗中，暂不接受申请',
        OFFLINE: '该宠物已下架'
      }[this.pet.status] || '当前状态不可申请';
    },
    isLoggedIn() {
      return !!this.$store.state.user.token;
    }
  },
  watch: {
    '$route.params.id'() {
      this.load();
    }
  },
  async mounted() {
    await this.load();
  },
  methods: {
    ageText,
    genderText,
    sizeText,
    healthText(s) {
      return { HEALTHY: '健康', GENERAL: '健康一般', ILL: '治疗中' }[s] || s;
    },
    timelineType(t) {
      return { VACCINE: 'success', DEWORM: 'primary', STERILIZE: 'primary', TREATMENT: 'warning', CHECK: '' }[t] || '';
    },
    async load() {
      this.loading = true;
      const id = this.$route.params.id;
      try {
        this.pet = await petDetail(id);
        recordView(id).catch(() => {});
        this.similar = await similarPets(id, 3).catch(() => []);
      } catch (e) {
        this.$message.error('宠物档案不存在或已删除');
        this.$router.replace('/pets');
      } finally {
        this.loading = false;
      }
    },
    async toggleFavorite() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录');
        return;
      }
      try {
        if (this.pet.favorited) {
          await unfavorite('PET', this.pet.id);
          this.pet.favorited = false;
          this.pet.favoriteCount = Math.max(0, (this.pet.favoriteCount || 0) - 1);
          this.$message.success('已取消收藏');
        } else {
          await favorite('PET', this.pet.id);
          this.pet.favorited = true;
          this.pet.favoriteCount = (this.pet.favoriteCount || 0) + 1;
          this.$message.success('已收藏');
        }
      } catch (e) { /* */ }
    },
    goApply() {
      this.$router.push(`/adopt/apply/${this.pet.id}`);
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.pet-detail {
  background: $bg-card;
  padding-bottom: 96px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 48px;
  padding-top: 40px;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

.gallery {
  &__main {
    aspect-ratio: 4 / 3;
    // 防止 height:100% 子图按自然比例撑破比例框（竖图主图会把画廊撑高）
    min-height: 0;
    border-radius: $radius-card;
    overflow: hidden;
    background: $bg-page;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__thumbs {
    margin-top: 12px;
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }

  &__thumb {
    width: 88px;
    height: 66px;
    padding: 0;
    border-radius: $radius-base;
    overflow: hidden;
    border: 2px solid transparent;
    cursor: pointer;
    background: $bg-page;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    &.current {
      border-color: $color-primary;
    }

    &:hover {
      border-color: $color-primary-hover;
    }
  }
}

.info-card {
  @include card;

  padding: 24px;
  align-self: start;
  position: sticky;
  top: calc(#{$portal-header-h} + 24px);

  @media (max-width: 991px) {
    position: static;
  }

  &__head {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__name {
    font-size: 28px;
    line-height: 36px;
  }

  &__no {
    margin-top: 4px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__facts {
    margin: 20px 0 0;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px 16px;

    dt {
      font-size: 13px;
      color: $text-secondary;
    }

    dd {
      margin: 2px 0 0;
      font-size: 14px;
      color: $text-primary;
      @include tabular-nums;
    }
  }

  &__flags {
    margin: 20px 0 0;
    padding: 0;
    list-style: none;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;

    li {
      font-size: 13px;
      padding: 6px 10px;
      border-radius: $radius-small;
      background: #F2F3F5;
      color: #5A6472;

      &.ok {
        background: $color-secondary-light;
        color: $color-secondary-text;
      }
    }
  }

  &__shelter {
    margin-top: 20px;
    padding: 16px;
    background: $bg-page;
    border-radius: $radius-base;

    &-name {
      font-size: 14px;
      font-weight: 500;
      color: $text-primary;
    }

    &-city {
      margin-top: 2px;
      font-size: 13px;
      color: $text-secondary;
    }
  }

  &__actions {
    margin-top: 20px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;

    .el-button {
      width: 100%;
      margin: 0;
    }
  }

  &__tip {
    margin-top: 12px;
    font-size: 13px;
    color: $text-secondary;
  }
}

.section {
  > h2 {
    padding-top: 64px;
    margin-bottom: 24px;
    font-size: $font-h2;
    line-height: $lh-h2;

    @media (max-width: 767px) {
      padding-top: 40px;
    }
  }

  &--gray {
    margin-top: 40px;
    background: $bg-page;
    border-radius: $radius-card;
    padding: 40px;

    > h2 {
      padding-top: 0;

      @media (max-width: 767px) {
        padding-top: 0;
      }
    }
  }
}

.timeline {
  max-width: 720px;
}

.timeline-card {
  h3 {
    font-size: 15px;
  }

  p {
    margin-top: 4px;
    font-size: 14px;
    line-height: 22px;
    color: $text-regular;
  }

  &__vet {
    font-size: 13px;
    color: $text-secondary;
  }
}

.story-text {
  max-width: 720px;
  font-size: 16px;
  line-height: 30px;
  color: $text-regular;
  white-space: pre-line;
}

.similar-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;

  @media (max-width: 991px) { grid-template-columns: 1fr; }
}

// 移动端底部行动条
.mobile-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 90;
  height: 64px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid $border-color;
  display: none;
  gap: 12px;

  @media (max-width: 991px) {
    display: flex;
  }

  &__fav {
    width: 88px;
    border: 1px solid $border-color;
    border-radius: $radius-base;
    background: #fff;
    color: $text-regular;
    font-size: 13px;
    cursor: pointer;

    i {
      display: block;
      font-size: 20px;
    }

    &.on {
      color: $color-primary-text;
      border-color: $color-primary;
      background: $color-primary-light;
    }
  }

  &__apply {
    flex: 1;
  }
}
</style>
