import request from './request'

/** 创建订单 */
export function createOrderAPI(data) {
  return request.post('/order/create', data)
}

/** 模拟支付 */
export function payOrderAPI(orderId) {
  return request.post(`/order/${orderId}/pay`)
}

/** 取消订单 */
export function cancelOrderAPI(orderId) {
  return request.post(`/order/${orderId}/cancel`)
}

/** 确认收货 */
export function confirmOrderAPI(orderId) {
  return request.post(`/order/${orderId}/confirm`)
}

/** 订单详情 */
export function getOrderDetailAPI(orderId) {
  return request.get(`/order/${orderId}`)
}

/** 订单列表 */
export function getOrderListAPI(params) {
  return request.get('/order/list', { params })
}
