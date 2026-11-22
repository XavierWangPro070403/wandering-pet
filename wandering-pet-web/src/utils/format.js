/**
 * 格式化工具：时间 / 金额 / 月龄 / 状态文案
 */

export function parseTime(time, pattern = '{y}-{m}-{d} {h}:{i}:{s}') {
  if (!time) return '';
  const date = typeof time === 'number' ? new Date(time) : new Date(String(time).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return String(time);
  const formatObj = {
    y: date.getFullYear(),
    m: String(date.getMonth() + 1).padStart(2, '0'),
    d: String(date.getDate()).padStart(2, '0'),
    h: String(date.getHours()).padStart(2, '0'),
    i: String(date.getMinutes()).padStart(2, '0'),
    s: String(date.getSeconds()).padStart(2, '0')
  };
  return pattern.replace(/\{([ymdhis])+}/g, (_, key) => formatObj[key] || '');
}

/** 金额：¥ 200.00（tabular 由 CSS 控制） */
export function money(value) {
  if (value === null || value === undefined || value === '') return '¥ 0.00';
  const n = Number(value);
  if (Number.isNaN(n)) return '¥ 0.00';
  return `¥ ${n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
}

/** 月龄 → X 岁 X 个月 / X 个月 */
export function ageText(month) {
  if (!month && month !== 0) return '';
  if (month < 12) return `${month} 个月`;
  const y = Math.floor(month / 12);
  const m = month % 12;
  return m === 0 ? `${y} 岁` : `${y} 岁 ${m} 个月`;
}

/** 宠物性别 */
export function genderText(g) {
  return { MALE: '公', FEMALE: '母', UNKNOWN: '未知' }[g] || '未知';
}

/** 体型 */
export function sizeText(s) {
  return { SMALL: '小型', MEDIUM: '中型', LARGE: '大型' }[s] || '-';
}

/** 宠物状态文案（颜色对见 StatusTag 组件） */
export function petStatusText(s) {
  return { AVAILABLE: '可领养', PENDING: '审核中', ADOPTED: '已领养', MEDICAL: '治疗中', OFFLINE: '已下架' }[s] || s;
}

/** 领养申请状态文案 */
export function applicationStatusText(s) {
  return {
    DRAFT: '草稿', SUBMITTED: '已提交', REVIEWING: '审核中', INTERVIEW: '待面谈', HOME_VISIT: '待家访',
    APPROVED: '审核通过', SIGNED: '已签约', ADOPTED: '领养完成', REJECTED: '已驳回',
    CANCELED: '已撤回', EXPIRED: '已过期', RETURNED: '已退养'
  }[s] || s;
}

/** 回访状态 */
export function followUpStatusText(s) {
  return { PENDING: '待回访', DONE: '已完成', OVERDUE: '已逾期' }[s] || s;
}

/** 线索状态 */
export function reportStatusText(s) {
  return { PENDING: '待处理', ASSIGNED: '已分派', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }[s] || s;
}

/** 志愿者状态 */
export function volunteerStatusText(s) {
  return { PENDING: '待审核', APPROVED: '已通过', REJECTED: '未通过' }[s] || s;
}

/** 文章分类 */
export function articleCategoryText(c) {
  return { RAISE: '饲养指导', KNOWLEDGE: '科普百科', NEWS: '平台动态', STORY: '救助故事' }[c] || c;
}

/** 手机号脱敏 */
export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '';
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}
