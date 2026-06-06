import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '转鱼宝猫 - 首页' }
  },
  {
    path: '/product/list',
    name: 'ProductList',
    component: () => import('@/views/ProductList.vue'),
    meta: { title: '商品列表' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
    meta: { title: '购物车', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/user/center',
    name: 'UserCenter',
    component: () => import('@/views/UserCenter.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/views/OrderList.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { title: '确认订单', requiresAuth: true }
  },
  {
    path: '/password-reset',
    name: 'PasswordReset',
    component: () => import('@/views/PasswordReset.vue'),
    meta: { title: '找回密码' }
  },
  {
    path: '/favorites',
    name: 'FavoriteList',
    component: () => import('@/views/FavoriteList.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/AdminDashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫 —— 未登录时温馨提醒但不强制拦截
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '转鱼宝猫'

  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.token) {
      // 允许浏览页面，不做强制跳转；页面内部会处理未登录状态
      console.warn(`[路由] 页面 "${to.meta.title}" 需要登录，当前未登录`)
    }
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router
