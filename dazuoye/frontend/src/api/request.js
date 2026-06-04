import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'

/**
 * Axios 封装 —— 统一请求拦截、错误处理
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 —— 自动携带 Token
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 —— 统一错误处理
request.interceptors.response.use(
  response => {
    const res = response.data

    // 业务状态码非 200
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return response // 返回完整 response，调用方取 data
  },
  error => {
    const status = error.response?.status

    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      removeToken()
      // 跳转到登录页
      window.location.href = '/login'
      return Promise.reject(error)
    }

    if (status === 403) {
      ElMessage.error('无权限访问')
      return Promise.reject(error)
    }

    if (status === 500) {
      ElMessage.error('服务器内部错误')
      return Promise.reject(error)
    }

    ElMessage.error(error.message || '网络错误，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
