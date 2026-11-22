<template>
  <article class="pet-card" role="button" tabindex="0" @click="go" @keydown.enter="go">
    <div class="pet-card__media">
      <img :src="pet.coverImage" :alt="`${pet.name}（${pet.breedName || pet.categoryName || ''}）`" loading="lazy" />
      <span v-if="showStatus" class="pet-card__status">
        <StatusTag :status="pet.status" />
      </span>
    </div>
    <div class="pet-card__body">
      <div class="pet-card__head">
        <h3 class="pet-card__name">{{ pet.name }}</h3>
        <span class="pet-card__age">{{ ageText(pet.ageMonth) }}</span>
      </div>
      <p class="pet-card__meta">
        {{ pet.breedName || pet.categoryName }} · {{ genderText(pet.gender) }} · {{ pet.city }}
      </p>
      <p class="pet-card__traits">{{ pet.characterDesc }}</p>
      <div class="pet-card__badges">
        <span v-if="pet.sterilized === 1" class="badge badge--ok">已绝育</span>
        <span v-if="pet.vaccinated === 1" class="badge badge--ok">已疫苗</span>
        <span v-if="pet.dewormed === 1" class="badge badge--ok">已驱虫</span>
      </div>
    </div>
  </article>
</template>

<script>
import StatusTag from './StatusTag.vue';
import { ageText, genderText } from '@/utils/format';

export default {
  name: 'PetCard',
  components: { StatusTag },
  props: {
    pet: { type: Object, required: true },
    showStatus: { type: Boolean, default: false }
  },
  methods: {
    ageText,
    genderText,
    go() {
      this.$router.push(`/pets/${this.pet.id}`);
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.pet-card {
  @include card;
  @include card-hover;
  @include img-zoom;

  border-radius: $radius-card;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;

  &:focus-visible {
    @include focus-ring;
  }

  &__media {
    position: relative;
    aspect-ratio: 4 / 3;
    // aspect-ratio 盒有内容式最小高度，height:100% 的子图会按自然比例把容器撑高，
    // 竖图会撑破 4:3 框架；显式归零让比例约束真正生效
    min-height: 0;
    background: $bg-page;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__status {
    position: absolute;
    top: 12px;
    left: 12px;
  }

  &__body {
    padding: 16px 20px 20px;
  }

  &__head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 8px;
  }

  &__name {
    font-size: 18px;
    line-height: 26px;
  }

  &__age {
    font-size: 13px;
    color: $text-secondary;
    @include tabular-nums;
  }

  &__meta {
    margin-top: 4px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__traits {
    margin-top: 8px;
    font-size: 14px;
    line-height: 22px;
    color: $text-regular;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__badges {
    margin-top: 12px;
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: $radius-small;

  &--ok {
    color: $color-secondary-text;
    background: $color-secondary-light;
  }
}
</style>
