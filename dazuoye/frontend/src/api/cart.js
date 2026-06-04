import request from './request'

/** 获取购物车列表 */
export function getCartAPI() {
  return request.get('/cart/list')
}

/** 添加到购物车 */
export function addCartAPI(data) {
  return request.post('/cart/add', data)
}

/** 从购物车移除 */
export function removeCartAPI(productId) {
  return request.delete(`/cart/${productId}`)
}

/** 更新数量 */
export function updateCartQtyAPI(productId, quantity) {
  return request.put('/cart/quantity', { productId, quantity })
}

/** 更新选中状态 */
export function updateCartSelectedAPI(productId, selected) {
  return request.put('/cart/selected', { productId, selected })
}
