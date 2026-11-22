/**
 * 校验工具：手机号 / 账号 / 身份证 / 表单 rules 工厂
 */
export const REG_PHONE = /^1[3-9]\d{9}$/;
export const REG_USERNAME = /^[a-zA-Z0-9_]{4,32}$/;
export const REG_ID_CARD = /^\d{17}[\dXx]$/;

export function isPhone(v) {
  return REG_PHONE.test(v || '');
}

export function isUsername(v) {
  return REG_USERNAME.test(v || '');
}

export function isIdCard(v) {
  return REG_ID_CARD.test(v || '');
}

export const rules = {
  required(value, message, trigger = 'blur') {
    return { required: true, message, trigger, validator: undefined,
      // 支持 value 为正则或布尔函数的扩展
      ...(typeof value === 'function' ? { validator: (r, v, cb) => cb(value(v) ? new Error(message) : undefined) } : {}) };
  },
  phone(message = '手机号格式不正确') {
    return { pattern: REG_PHONE, message, trigger: 'blur' };
  },
  username(message = '账号 4-32 位字母数字下划线') {
    return { pattern: REG_USERNAME, message, trigger: 'blur' };
  }
};
