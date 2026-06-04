<template>
  <div class="home-page">
    <!-- 顶部横幅 -->
    <section class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">🐟 转鱼宝猫</h1>
        <p class="hero-subtitle">闲转好物，物尽其用 — 让每一件闲置找到新主人</p>
        <el-button type="danger" size="large" round @click="$router.push('/product/list')">
          开始探索 →
        </el-button>
      </div>
    </section>

    <!-- 轮播图 -->
    <section class="page-container" v-if="banners.length > 0">
      <el-carousel :interval="4000" type="card" height="280px">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <a :href="b.linkUrl || '#'" target="_blank">
            <img :src="b.imageUrl" :alt="b.title" style="width:100%;height:100%;object-fit:cover;border-radius:8px" />
          </a>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 系统公告 -->
    <section class="page-container" v-if="announcements.length > 0">
      <el-alert v-for="a in announcements" :key="a.id" :title="a.title" type="info" :description="a.content" show-icon :closable="false" style="margin-bottom:8px" />
    </section>

    <!-- 分类快捷入口 -->
    <section class="page-container">
      <h2 class="section-title">热门分类</h2>
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          @click="$router.push({ path: '/product/list', query: { categoryId: cat.id } })"
        >
          <span class="category-icon">{{ cat.icon || '📦' }}</span>
          <span class="category-name">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- 推荐商品 -->
    <section class="page-container">
      <h2 class="section-title">热门好物</h2>
      <SkeletonLoader v-if="loading" :count="8" />
      <div v-else class="product-grid">
        <ProductCard v-for="item in products" :key="item.id" :product="item" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProductListAPI, getCategoryListAPI } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import request from '@/api/request'

const products = ref([])
const categories = ref([])
const banners = ref([])
const announcements = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [prodRes, catRes, banRes, annRes] = await Promise.all([
      getProductListAPI({ page: 1, size: 8, sortBy: 'view_desc' }),
      getCategoryListAPI(),
      request.get('/banner/list').catch(() => ({data:{data:[]}})),
      request.get('/announcement/list').catch(() => ({data:{data:[]}}))
    ])
    products.value = prodRes.data.data.records || []
    categories.value = catRes.data.data || []
    banners.value = banRes.data.data || []
    announcements.value = annRes.data.data || []
  } catch (e) {
    console.error('首页加载失败:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero-banner {
  background: linear-gradient(135deg, #e74c3c 0%, #f39c12 50%, #e74c3c 100%);
  padding: 60px 20px;
  text-align: center;
  color: #fff;
}

.hero-title {
  font-size: 42px;
  margin-bottom: 12px;
}

.hero-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
  position: relative;
  padding-left: 12px;
}
.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 22px;
  background: var(--primary-color);
  border-radius: 2px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 30px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  background: #fff;
  border-radius: var(--radius);
  cursor: pointer;
  transition: transform 0.2s;
}
.category-item:hover { transform: scale(1.05); }

.category-icon { font-size: 32px; margin-bottom: 8px; }
.category-name { font-size: 13px; color: var(--text-primary); }
</style>
