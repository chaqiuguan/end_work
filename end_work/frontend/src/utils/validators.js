/**
 * 通用表单校验规则
 */

/** 手机号校验 */
export const phoneValidator = (rule, value, callback) => {
  if (!value) return callback()
  if (!/^1[3-9]\d{9}$/.test(value)) {
    return callback(new Error('手机号格式不正确'))
  }
  callback()
}

/** 邮箱校验 */
export const emailValidator = (rule, value, callback) => {
  if (!value) return callback()
  if (!/^[\w.-]+@[\w.-]+\.\w+$/.test(value)) {
    return callback(new Error('邮箱格式不正确'))
  }
  callback()
}

/** 价格校验（最多两位小数） */
export const priceValidator = (rule, value, callback) => {
  if (value === '' || value === null || value === undefined) {
    return callback(new Error('价格不能为空'))
  }
  if (!/^\d+(\.\d{1,2})?$/.test(value.toString())) {
    return callback(new Error('价格格式不正确（最多两位小数）'))
  }
  if (Number(value) <= 0) {
    return callback(new Error('价格必须大于0'))
  }
  callback()
}

/** 订单状态映射 */
export const orderStatusMap = {
  0: { text: '待付款', color: 'warning' },
  1: { text: '待发货', color: 'primary' },
  2: { text: '待收货', color: 'success' },
  3: { text: '已完成', color: 'info' },
  4: { text: '已取消', color: 'danger' }
}

/** 商品成色映射 */
export const conditionMap = {
  0: '全新',
  1: '几乎全新',
  2: '轻微使用',
  3: '明显使用'
}
