<template>
  <div class="page-container">
    <h2 class="page-title">{{ pageTitle }}</h2>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-label">分类：</div>
        <el-select v-model="filters.categoryId" placeholder="全部分类" clearable size="default"
          @change="searchProducts">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>

        <div class="filter-label">价格：</div>
        <el-input-number v-model="filters.minPrice" :min="0" placeholder="最低" size="default"
          controls-position="right" style="width: 120px" @change="searchProducts" />
        <span class="price-sep">—</span>
        <el-input-number v-model="filters.maxPrice" :min="0" placeholder="最高" size="default"
          controls-position="right" style="width: 120px" @change="searchProducts" />

        <div class="filter-label">成色：</div>
        <el-select v-model="filters.condition" placeholder="全部成色" clearable size="default"
          @change="searchProducts">
          <el-option label="全新" :value="0" />
          <el-option label="几乎全新" :value="1" />
          <el-option label="轻微使用" :value="2" />
          <el-option label="明显使用" :value="3" />
        </el-select>

        <div class="filter-label">排序：</div>
        <el-select v-model="filters.sortBy" size="default" @change="searchProducts">
          <el-option label="最新发布" value="created_desc" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
          <el-option label="最多浏览" value="view_desc" />
        </el-select>
      </div>
    </div>

    <!-- 商品列表 -->
    <SkeletonLoader v-if="loading" :count="8" />
    <div v-else-if="products.length > 0" class="product-grid">
      <ProductCard v-for="item in products" :key="item.id" :product="item" />
    </div>
    <el-empty v-else description="暂无商品" />

    <!-- 分页 -->
    <Pagination
      :total="total"
      :page="filters.page"
      :size="filters.size"
      @change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getProductListAPI, getCategoryListAPI } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import Pagination from '@/components/Pagination.vue'

const route = useRoute()
const products = ref([])
const categories = ref([])
const total = ref(0)
const loading = ref(true)

// 动态标题：显示当前分类名称或搜索关键词
const pageTitle = computed(() => {
  if (filters.keyword) return `搜索: "${filters.keyword}"`
  if (filters.categoryId) {
    const cat = categories.value.find(c => c.id === filters.categoryId)
    return cat ? cat.name : '商品列表'
  }
  return '全部商品'
})

const filters = reactive({
  keyword: '',
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  condition: null,
  sortBy: 'created_desc',
  page: 1,
  size: 20
})

async function searchProducts() {
  loading.value = true
  try {
    const params = { ...filters }
    // 清理空值
    Object.keys(params).forEach(k => {
      if (params[k] === '' || params[k] === null) delete params[k]
    })

    const res = await getProductListAPI(params)
    const data = res.data.data
    products.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    console.error('商品查询失败:', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange({ page, size }) {
  filters.page = page
  filters.size = size
  searchProducts()
}

onMounted(async () => {
  const catRes = await getCategoryListAPI()
  categories.value = catRes.data.data || []
  // 首次加载时从 URL 读取参数
  updateFiltersFromRoute()
  searchProducts()
})

// 监听路由变化，切换分类/搜索时自动刷新商品列表
watch(() => route.query, () => {
  updateFiltersFromRoute()
  filters.page = 1
  searchProducts()
})

function updateFiltersFromRoute() {
  if (route.query.keyword) filters.keyword = route.query.keyword
  else filters.keyword = ''
  if (route.query.categoryId) filters.categoryId = Number(route.query.categoryId)
  else filters.categoryId = null
}
</script>

<style scoped>
.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
}

.filter-bar {
  background: #fff;
  padding: 16px;
  border-radius: var(--radius);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.price-sep {
  color: var(--text-secondary);
  margin: 0 -8px;
}

@media (max-width: 768px) {
  .filter-row {
    gap: 8px;
  }
}
</style>
