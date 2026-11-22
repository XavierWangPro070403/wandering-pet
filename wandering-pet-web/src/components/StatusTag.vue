<template>
  <span class="status-tag" :class="`status-tag--${tone}`">{{ text }}</span>
</template>

<script>
/**
 * 状态标签：颜色 + 圆点 + 文字（§8.2 状态色映射，禁止只靠颜色表达）
 */
const TONES = {
  // 成功/健康/可领养/已发布
  AVAILABLE: 'success', APPROVED: 'success', ADOPTED: 'success', DONE: 'success',
  RESOLVED: 'success', HEALTHY: 'success', SUCCESS: 'success', PUBLISHED: 'success',
  // 进行中/审核
  REVIEWING: 'primary', INTERVIEW: 'primary', HOME_VISIT: 'primary', SIGNED: 'primary',
  ASSIGNED: 'primary', PROCESSING: 'primary',
  // 待处理/警示
  SUBMITTED: 'warning', PENDING: 'warning', OVERDUE: 'warning', MEDICAL: 'warning',
  // 危险
  REJECTED: 'danger', RETURNED: 'danger', CLOSED: 'danger', OFFLINE: 'danger',
  // 中性
  DRAFT: 'neutral', CANCELED: 'neutral', EXPIRED: 'neutral'
};

const LABELS = {
  AVAILABLE: '可领养', PENDING: '待处理', ADOPTED: '已领养', MEDICAL: '治疗中', OFFLINE: '已下架',
  DRAFT: '草稿', SUBMITTED: '已提交', REVIEWING: '审核中', INTERVIEW: '待面谈', HOME_VISIT: '待家访',
  APPROVED: '审核通过', SIGNED: '已签约', REJECTED: '已驳回', CANCELED: '已撤回', EXPIRED: '已过期',
  RETURNED: '已退养', DONE: '已完成', OVERDUE: '已逾期', PUBLISHED: '已发布'
};

export default {
  name: 'StatusTag',
  props: {
    status: { type: String, default: '' },
    label: { type: String, default: '' }
  },
  computed: {
    tone() {
      return TONES[this.status] || 'neutral';
    },
    text() {
      return this.label || LABELS[this.status] || this.status;
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 24px;
  padding: 0 8px;
  border-radius: $radius-small;
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;

  &::before {
    content: "";
    width: 6px;
    height: 6px;
    border-radius: $radius-circle;
    background: currentColor;
  }

  &--success { color: $color-secondary-text; background: $color-secondary-light; }
  &--primary { color: $color-primary-text; background: $color-primary-light; }
  &--warning { color: $color-warning-text; background: $color-warning-light; }
  &--danger { color: $color-danger-text; background: $color-danger-light; }
  &--neutral { color: #5A6472; background: #F2F3F5; }
}
</style>
