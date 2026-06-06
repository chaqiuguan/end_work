<template>
  <div class="navbar-wrapper">
  <header class="navbar">
    <div class="navbar-inner">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <span class="logo-icon">🐟</span>
        <span class="logo-text">转鱼宝猫</span>
      </router-link>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索商品..."
          size="large"
          @keyup.enter="search"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button :icon="Search" @click="search" />
          </template>
        </el-input>
      </div>

      <!-- 导航菜单 -->
      <div class="nav-actions">
        <router-link to="/product/list" class="nav-item">
          <el-icon><Goods /></el-icon>
          <span>全部商品</span>
        </router-link>

        <router-link to="/cart" class="nav-item cart-item">
          <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
            <el-icon><ShoppingCart /></el-icon>
          </el-badge>
          <span>购物车</span>
        </router-link>

        <!-- 用户菜单 -->
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click">
            <div class="nav-item user-item">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/user/center')">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/order/list')">
                  <el-icon><Document /></el-icon>我的订单
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/favorites')">
                  <el-icon><Star /></el-icon>我的收藏
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')">
                  <el-icon><Setting /></el-icon>管理后台
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <router-link to="/login" class="nav-item login-btn">
            <el-button type="primary" size="small">登录</el-button>
          </router-link>
        </template>
      </div>
    </div>
  </header>

    <!-- 品类导航栏 (1688风格) -->
    <nav class="category-bar">
      <div class="category-inner">
        <router-link to="/" class="cat-label">首页</router-link>
        <div class="cat-menu">
          <router-link
            to="/product/list"
            class="cat-item"
            :class="{ active: activeCat === 0 && route.path !== '/' }"
            @click="activeCat = 0"
          >
            <span class="cat-item-icon">🔥</span>
            <span class="cat-item-text">
              <span class="cat-item-name">全部</span>
              <span class="cat-item-sub">All</span>
            </span>
          </router-link>
          <router-link
            v-for="cat in categories"
            :key="cat.id"
            :to="`/product/list?categoryId=${cat.id}`"
            class="cat-item"
            :class="{ active: activeCat === cat.id }"
            @click="activeCat = cat.id"
          >
            <span class="cat-item-icon">{{ getCatIcon(cat.name) }}</span>
            <span class="cat-item-text">
              <span class="cat-item-name">{{ cat.name }}</span>
              <span class="cat-item-sub">{{ getCatSub(cat.name) }}</span>
            </span>
          </router-link>
        </div>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getCategoryListAPI } from '@/api/product'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const keyword = ref('')
const categories = ref([])
const activeCat = ref(0)

function search() {
  const kw = keyword.value.trim()
  if (kw) {
    router.push({ path: '/product/list', query: { keyword: kw } })
  } else {
    router.push({ path: '/product/list' })
  }
}

onMounted(async () => {
  try {
    const res = await getCategoryListAPI()
    categories.value = res.data.data || []
    syncActiveCat()
  } catch {}
})

// 路由变化时同步分类高亮
watch(() => route.query.categoryId, () => syncActiveCat())

function syncActiveCat() {
  const catId = route.query.categoryId
  activeCat.value = catId ? Number(catId) : 0
}

// 品类图标映射
const catIconMap = {
  '数码电子': '💻', '服饰鞋包': '👗', '图书教材': '📚',
  '家居生活': '🏠', '运动户外': '⚽', '美妆个护': '💄',
  '母婴玩具': '🍼', '其他': '📦',
}
function getCatIcon(name) {
  return catIconMap[name] || '📦'
}
function getCatSub(name) {
  const map = {
    '数码电子': 'Digital', '服饰鞋包': 'Fashion', '图书教材': 'Books',
    '家居生活': 'Home', '运动户外': 'Sports', '美妆个护': 'Beauty',
    '母婴玩具': 'Baby', '其他': 'More',
  }
  return map[name] || 'More'
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    cartStore.clearCart()
    router.push('/')
  }).catch(() => {})
}
</script>

<style scoped>
.navbar-wrapper {
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}
.logo-icon { font-size: 24px; }
.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
}

.search-bar {
  flex: 1;
  max-width: 480px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--text-primary);
  font-size: 14px;
  white-space: nowrap;
  transition: color 0.2s;
}
.nav-item:hover { color: var(--primary-color); }

.user-item {
  gap: 8px;
}
.user-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 分类导航栏 (1688风格) */
.category-bar {
  background: #fff;
  border-top: 1px solid var(--border-color);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.category-bar::-webkit-scrollbar { display: none; }

.category-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 0;
  height: 56px;
}

.cat-label {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  padding-right: 16px;
  border-right: 1px solid var(--border-color);
  margin-right: 8px;
  white-space: nowrap;
  text-decoration: none;
  transition: color 0.2s;
}
.cat-label.router-link-exact-active {
  color: var(--primary-color);
}

.cat-label:hover { color: var(--primary-color); }

.cat-menu {
  display: flex;
  gap: 2px;
  overflow-x: auto;
  flex: 1;
  flex-wrap: nowrap;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.cat-menu::-webkit-scrollbar { display: none; }

.cat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  white-space: nowrap;
  padding: 6px 12px;
  border-radius: 8px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 90px;
}

.cat-item:hover { background: #f5f5f5; }
.cat-item.active {
  background: var(--primary-light, #fef0f0);
  color: var(--primary-color);
}

.cat-item-icon {
  font-size: 22px;
  line-height: 1;
  flex-shrink: 0;
}

.cat-item-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.cat-item-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
}
.cat-item.active .cat-item-name { color: var(--primary-color); }

.cat-item-sub {
  font-size: 10px;
  color: var(--text-secondary);
  white-space: nowrap;
}

@media (max-width: 768px) {
  .cat-label { display: none; }
  .cat-item-sub { display: none; }
  .cat-item { min-width: 60px; padding: 4px 8px; }
  .category-inner { height: 44px; }
}

@media (max-width: 768px) {
  .search-bar { display: none; }
  .nav-item span { display: none; }
}
</style>
