/**
 * 格式化工具（与 Web 端 format.js 同源）
 */

export function ageText(month) {
  if (!month && month !== 0) return '';
  if (month < 12) return month + ' 个月';
  const y = Math.floor(month / 12);
  const m = month % 12;
  return m === 0 ? y + ' 岁' : y + ' 岁 ' + m + ' 个月';
}

export function genderText(g) {
  return { MALE: '公', FEMALE: '母', UNKNOWN: '未知' }[g] || '未知';
}

export function sizeText(s) {
  return { SMALL: '小型', MEDIUM: '中型', LARGE: '大型' }[s] || '-';
}

export function petStatusText(s) {
  return {
    AVAILABLE: '可领养', PENDING: '审核中', ADOPTED: '已领养',
    MEDICAL: '治疗中', OFFLINE: '已下架'
  }[s] || s;
}

export function petStatusTone(s) {
  return {
    AVAILABLE: 'success', PENDING: 'primary', ADOPTED: 'success',
    MEDICAL: 'warning', OFFLINE: 'danger'
  }[s] || 'neutral';
}

export function applicationStatusText(s) {
  return {
    DRAFT: '草稿', SUBMITTED: '已提交', REVIEWING: '审核中', INTERVIEW: '待面谈',
    HOME_VISIT: '待家访', APPROVED: '审核通过', SIGNED: '已签约', ADOPTED: '领养完成',
    REJECTED: '已驳回', CANCELED: '已撤回', EXPIRED: '已过期', RETURNED: '已退养'
  }[s] || s;
}

export function applicationStatusTone(s) {
  return {
    SUBMITTED: 'warning', REVIEWING: 'primary', INTERVIEW: 'primary', HOME_VISIT: 'primary',
    APPROVED: 'success', SIGNED: 'success', ADOPTED: 'success', REJECTED: 'danger',
    CANCELED: 'neutral', EXPIRED: 'neutral', RETURNED: 'danger'
  }[s] || 'neutral';
}

export function money(v) {
  if (v === null || v === undefined || v === '') return '¥ 0.00';
  const n = Number(v);
  if (isNaN(n)) return '¥ 0.00';
  return '¥ ' + n.toFixed(2);
}

export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '';
  return phone.slice(0, 3) + '****' + phone.slice(-4);
}
