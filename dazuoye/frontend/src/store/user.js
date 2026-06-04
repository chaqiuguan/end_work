import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginAPI, registerAPI, getProfileAPI, refreshTokenAPI } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // ========== 状态 ==========
  const token = ref(getToken() || '')
  const refreshToken = ref('')
  const userInfo = ref(null)

  // ========== 计算属性 ==========
  const isLoggedIn = computed(() => !!token.value)
  const isSeller = computed(() => userInfo.value?.role >= 1)
  const isAdmin = computed(() => userInfo.value?.role === 2)

  // ========== 方法 ==========

  /**
   * 用户登录
   */
  async function login(username, password) {
    const res = await loginAPI({ username, password })
    const data = res.data.data
    token.value = data.token
    refreshToken.value = data.refreshToken
    userInfo.value = data
    setToken(data.token, data.refreshToken)
    return data
  }

  /**
   * 用户注册
   */
  async function register(registerData) {
    const res = await registerAPI(registerData)
    return res.data
  }

  /**
   * 获取用户信息
   */
  async function fetchProfile() {
    const res = await getProfileAPI()
    userInfo.value = res.data.data
    return userInfo.value
  }

  /**
   * 刷新 Access Token
   */
  async function refreshAccessToken() {
    try {
      const res = await refreshTokenAPI(refreshToken.value)
      const data = res.data.data
      token.value = data.token
      refreshToken.value = data.refreshToken
      setToken(data.token, data.refreshToken)
      return true
    } catch {
      logout()
      return false
    }
  }

  /**
   * 退出登录
   */
  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    removeToken()
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    isSeller,
    isAdmin,
    login,
    register,
    fetchProfile,
    refreshAccessToken,
    logout
  }
}, {
  persist: {
    key: 'zbm-user',
    storage: localStorage,
    paths: ['token', 'refreshToken', 'userInfo']
  }
})
