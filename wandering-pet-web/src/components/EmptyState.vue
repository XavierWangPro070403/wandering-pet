<template>
  <div class="wb-empty">
    <img class="wb-empty__icon" src="/imgs/empty.svg" alt="" aria-hidden="true" />
    <p class="wb-empty__title">{{ title }}</p>
    <p v-if="desc" class="wb-empty__desc">{{ desc }}</p>
    <slot>
      <el-button v-if="actionText" type="primary" size="small" @click="$emit('action')">{{ actionText }}</el-button>
    </slot>
  </div>
</template>

<script>
/**
 * 空状态：说明发生了什么 + 下一步按钮（§8.5）
 */
export default {
  name: 'EmptyState',
  props: {
    title: { type: String, default: '暂无数据' },
    desc: { type: String, default: '' },
    actionText: { type: String, default: '' }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.wb-empty {
  text-align: center;
  padding: 48px 0;
  color: $text-secondary;

  &__icon {
    width: 72px;
    height: 72px;
    margin-bottom: 12px;
  }

  &__title {
    font-size: 14px;
    color: $text-regular;
    margin-bottom: 4px;
  }

  &__desc {
    font-size: 13px;
    margin-bottom: 16px;
  }
}
</style>
