import request from './request'

/** 商品列表（分页 + 筛选） */
export function getProductListAPI(params) {
  return request.get('/product/list', { params })
}

/** 商品详情 */
export function getProductDetailAPI(id) {
  return request.get(`/product/detail/${id}`)
}

/** 发布商品 */
export function publishProductAPI(data) {
  return request.post('/product/publish', data)
}

/** 更新商品 */
export function updateProductAPI(id, data) {
  return request.put(`/product/${id}`, data)
}

/** 相似商品推荐 */
export function getSimilarProductsAPI(id) {
  return request.get(`/product/similar/${id}`)
}

/** 获取分类列表 */
export function getCategoryListAPI() {
  return request.get('/category/list')
}
