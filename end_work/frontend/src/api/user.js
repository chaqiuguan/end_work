import request from './request'

/** 用户登录 */
export function loginAPI(data) {
  return request.post('/user/login', data)
}

/** 用户注册 */
export function registerAPI(data) {
  return request.post('/user/register', data)
}

/** 获取用户信息 */
export function getProfileAPI() {
  return request.get('/user/profile')
}

/** 刷新 Token */
export function refreshTokenAPI(refreshToken) {
  return request.post('/user/refresh-token', { refreshToken })
}

/** 切换身份 */
export function switchRoleAPI(role) {
  return request.put('/user/switch-role', { role })
}
