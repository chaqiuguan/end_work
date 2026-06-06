<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <section class="page-container" v-if="banners.length > 0">
      <el-carousel :interval="4000" height="340px" indicator-position="outside">
        <el-carousel-item v-for="(b, idx) in banners" :key="b.id">
          <div
            class="banner-slide"
            :style="{ background: bannerColors[idx % bannerColors.length] }"
            @click="b.linkUrl ? window.open(b.linkUrl, '_blank') : null"
          >
            <div class="banner-text">
              <h2 class="banner-title">{{ b.title }}</h2>
              <p class="banner-sub">{{ getBannerSub(idx) }}</p>
            </div>
            <div class="banner-cutout">
              <img
                :src="getCutoutUrl(idx)"
                :alt="b.title"
                @error="e => e.target.style.display = 'none'"
              />
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 品类市场 + 排行榜 并排 -->
    <section class="page-container">
      <div class="cat-rank-row">
        <!-- 左侧：品类市场 -->
        <aside class="cat-sidebar">
          <h3 class="cat-sidebar-title">品类市场</h3>
          <ul class="cat-menu-list">
            <li
              v-for="cat in categories"
              :key="cat.id"
              class="cat-menu-row"
            >
              <router-link
                :to="`/product/list?categoryId=${cat.id}`"
                class="cat-menu-link"
              >{{ cat.name }}</router-link>
            </li>
          </ul>
        </aside>

        <!-- 右侧：热卖榜单 (占2/3) -->
        <div class="rank-main">
          <h2 class="section-title">热卖榜单</h2>
          <div class="ranking-grid">
            <div
              v-for="rank in rankings"
              :key="rank.title"
              class="ranking-card"
              @click="$router.push({ path: '/product/list', query: { sortBy: rank.sortBy } })"
            >
              <div class="ranking-header">
                <span class="rank-dot" :class="'dot-' + rank.key"></span>
                <span class="ranking-title">{{ rank.title }}</span>
                <span class="ranking-arrow">&rsaquo;</span>
              </div>
              <div class="ranking-images">
                <img
                  v-for="(img, i) in rank.images"
                  :key="i"
                  :src="img"
                  :alt="rank.title + i"
                  @error="e => e.target.src = 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><rect fill=%22%23f0f0f0%22 width=%22100%22 height=%22100%22/><text x=%2250%22 y=%2255%22 text-anchor=%22middle%22 fill=%22%23ccc%22 font-size=%2236%22>?</text></svg>'"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 推荐商品 -->
    <section class="page-container">
      <h2 class="section-title">热门好物</h2>
      <SkeletonLoader v-if="loading" :count="8" />
      <template v-else>
        <div class="product-waterfall">
          <ProductCard v-for="item in products" :key="item.id" :product="item" />
        </div>
        <div ref="loadMoreRef" class="load-more-sentinel">
          <span v-if="loadingMore" class="loading-tip">加载中...</span>
          <span v-else-if="!hasMore" class="loading-tip">— 已加载全部 —</span>
        </div>
      </template>
    </section>

    <!-- 系统公告 -->
    <section class="page-container" v-if="announcements.length > 0">
      <el-alert v-for="a in announcements" :key="a.id" :title="a.title" type="info" :description="a.content" show-icon :closable="false" style="margin-bottom:8px" />
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getProductListAPI, getCategoryListAPI } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import request from '@/api/request'

const router = useRouter()

const products = ref([])
const categories = ref([])
const banners = ref([])
const announcements = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const loadMoreRef = ref(null)

// Banner 配色
const bannerColors = [
  '#FF6B6B', '#4ECDC4', '#FFD93D', '#A8E6CF',
  '#FF8B94', '#C3AED6', '#74B9FF',
]
function getCutoutUrl(idx) {
  return `/banners/cutout_${idx % 7}.png`
}
const bannerSubs = [
  '初夏新品上新', '抢苹果惊喜券', '官方立减商品8.5折起',
  '索尼PS5 SLIM游戏主机', '手快有手慢无', '机械革命 极光X',
  '大牌零食品质保证',
]
function getBannerSub(idx) {
  return bannerSubs[idx % bannerSubs.length]
}

// 排行榜
const rankingProducts = ref([])
const rankings = computed(() => {
  const rankDefs = [
    { key: 'hot', title: '热卖排行', sortBy: 'view_desc' },
    { key: 'new', title: '新品首发', sortBy: 'newest' },
    { key: 'best', title: '好物精选', sortBy: 'price_asc' },
    { key: 'sale', title: '折扣专区', sortBy: 'discount' },
  ]
  const all = rankingProducts.value
  if (all.length < 12) {
    // 数据不够时复用
    const repeated = [...all, ...all, ...all].slice(0, 12)
    return rankDefs.map((def, i) => ({
      ...def,
      images: repeated.slice(i * 3, i * 3 + 3).map(p => p.image || (p.images?.[0])),
    }))
  }
  return rankDefs.map((def, i) => ({
    ...def,
    images: all.slice(i * 3, i * 3 + 3).map(p => p.image || (p.images?.[0])),
  }))
})

onMounted(async () => {
  try {
    const [prodRes, catRes, banRes, annRes] = await Promise.all([
      getProductListAPI({ page: 1, size: 8, sortBy: 'view_desc' }),
      getCategoryListAPI(),
      request.get('/banner/list').catch(() => ({data:{data:[]}})),
      request.get('/announcement/list').catch(() => ({data:{data:[]}}))
    ])
    const data = prodRes.data.data
    products.value = data.records || []
    hasMore.value = (data.records || []).length >= 8 && data.total > 8
    currentPage.value = 1
    categories.value = catRes.data.data || []
    banners.value = banRes.data.data || []
    announcements.value = annRes.data.data || []
    // 排行榜数据（多取一些用于分配）
    try {
      const rankRes = await getProductListAPI({ page: 1, size: 12, sortBy: 'view_desc' })
      rankingProducts.value = (rankRes.data.data.records || []).map(p => ({
        image: p.image || (p.images && p.images[0]) || '/placeholder.png',
        ...p,
      }))
    } catch { rankingProducts.value = products.value }
  } catch (e) {
    console.error('首页加载失败:', e)
  } finally {
    loading.value = false
  }

  // 无限滚动观察器
  let observer = null

  async function loadMore() {
    if (!hasMore.value || loadingMore.value) return
    loadingMore.value = true
    try {
      currentPage.value++
      const res = await getProductListAPI({ page: currentPage.value, size: 8, sortBy: 'view_desc' })
      const data = res.data.data
      const newItems = data.records || []
      products.value.push(...newItems)
      hasMore.value = newItems.length >= 8 && currentPage.value * 8 < data.total
    } catch {
      currentPage.value--
    } finally {
      loadingMore.value = false
    }
  }

  await nextTick()
  if (loadMoreRef.value) {
    observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) loadMore()
    }, { rootMargin: '200px' })
    observer.observe(loadMoreRef.value)
  }
})
</script>

<style scoped>
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

/* ===== Banner 轮播 ===== */
.banner-slide {
  width: 100%;
  height: 100%;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 60px;
  cursor: pointer;
  overflow: hidden;
  position: relative;
}

.banner-text {
  z-index: 1;
}

.banner-title {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.banner-sub {
  font-size: 16px;
  color: rgba(255,255,255,0.9);
  margin: 0;
}

.banner-cutout {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  align-items: flex-end;
}

.banner-cutout img {
  max-height: 90%;
  width: auto;
  object-fit: contain;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.15));
}

@media (max-width: 640px) {
  .banner-slide { padding: 0 24px; }
  .banner-title { font-size: 22px; }
  .banner-cutout img { max-height: 70%; }
}

/* ===== 品类市场 + 排行榜 并排布局 ===== */
.cat-rank-row {
  display: grid;
  grid-template-columns: 1fr 7fr;
  gap: 20px;
  align-items: stretch;
}

/* 左侧品类侧边栏 */
.cat-sidebar {
  background: #fff;
  border-radius: 12px;
  padding: 16px 0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  max-width: 200px;
}

.cat-sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  padding: 0 16px 12px;
  border-bottom: 1px solid #f0f0f0;
  margin: 0;
}

.cat-menu-list {
  list-style: none;
  margin: 0;
  padding: 4px 0;
}

.cat-menu-row {
  display: flex;
  align-items: center;
  padding: 0 16px;
  transition: background 0.15s;
  cursor: pointer;
}
.cat-menu-row:hover { background: #fafafa; }

.cat-menu-link {
  display: block;
  width: 100%;
  padding: 12px 0;
  font-size: 14px;
  color: var(--text-primary);
  text-decoration: none;
  white-space: nowrap;
  transition: color 0.15s;
  border-bottom: 1px solid #f5f5f5;
}
.cat-menu-link:hover { color: var(--primary-color); }
.cat-menu-row:last-child .cat-menu-link { border-bottom: none; }

/* 右侧热卖榜单 */
.rank-main {
  flex: 1;
  min-width: 0;
}
.rank-main .section-title {
  margin-top: 0;
  margin-bottom: 12px;
  line-height: 1.5;
}

.ranking-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.ranking-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.ranking-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.ranking-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.rank-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-hot  { background: #e74c3c; }
.dot-new  { background: #f39c12; }
.dot-best { background: #2ecc71; }
.dot-sale { background: #e91e63; }

.ranking-title { flex: 1; }

.ranking-arrow {
  font-size: 20px;
  color: #ccc;
  transition: color 0.2s;
  font-family: serif;
}
.ranking-card:hover .ranking-arrow { color: var(--primary-color); }

.ranking-images {
  display: flex;
  gap: 4px;
  padding: 0 8px 10px;
}

.ranking-images img {
  width: calc(33.33% - 3px);
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 6px;
  background: #f0f0f0;
}

@media (max-width: 640px) {
  .cat-rank-row { grid-template-columns: 1fr; }
  .cat-sidebar { max-width: none; }
  .ranking-grid { grid-template-columns: repeat(2, 1fr); }
}

/* ===== 加载更多 ===== */
.load-more-sentinel {
  text-align: center;
  padding: 20px 0 40px;
}
.loading-tip {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ===== 商品瀑布流 ===== */
.product-waterfall {
  column-count: 4;
  column-gap: 16px;
  margin-bottom: 30px;
}

.product-waterfall > * {
  break-inside: avoid;
  margin-bottom: 16px;
}

@media (max-width: 1024px) {
  .product-waterfall { column-count: 3; }
  .ranking-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .product-waterfall { column-count: 2; }
  .ranking-grid { grid-template-columns: repeat(2, 1fr); }
}

</style>
