<template>
  <div class="page-container">
    <h2 class="page-title">我的收藏</h2>
    <SkeletonLoader v-if="loading" :count="4" />
    <el-empty v-else-if="items.length === 0" description="暂无收藏">
      <el-button type="primary" @click="$router.push('/product/list')">去逛逛</el-button>
    </el-empty>
    <div v-else class="product-grid">
      <div v-for="item in items" :key="item.productId" class="fav-card">
        <router-link :to="`/product/${item.productId}`">
          <img :src="item.image || '/placeholder.png'" class="fav-image" />
          <div class="fav-info">
            <span class="fav-title">{{ item.title }}</span>
            <span class="fav-price">¥{{ item.price }}</span>
          </div>
        </router-link>
        <el-button size="small" type="danger" @click="toggleFav(item.productId)">取消收藏</el-button>
      </div>
    </div>
    <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import Pagination from '@/components/Pagination.vue'
import { ElMessage } from 'element-plus'

const items = ref([])
const loading = ref(true)
const page = ref(1)
const size = ref(20)
const total = ref(0)

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await request.get('/favorite/list', { params: { page: page.value, size: size.value } })
    items.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } finally { loading.value = false }
}

async function toggleFav(productId) {
  await request.post(`/favorite/toggle/${productId}`)
  ElMessage.success('已取消收藏')
  fetchFavorites()
}

function onPageChange({ page: p, size: s }) { page.value = p; size.value = s; fetchFavorites() }

onMounted(fetchFavorites)
</script>

<style scoped>
.page-container { max-width:1200px; margin:0 auto; padding:20px 16px; }
.page-title { font-size:24px; font-weight:600; margin-bottom:20px; }
.fav-card { background:#fff; border-radius:8px; padding:12px; display:flex; align-items:center; gap:12px; justify-content:space-between; margin-bottom:8px; }
.fav-card a { display:flex; align-items:center; gap:12px; flex:1; text-decoration:none; color:inherit; }
.fav-image { width:80px; height:80px; object-fit:cover; border-radius:6px; }
.fav-title { font-size:14px; display:block; max-width:300px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.fav-price { color:var(--primary-color); font-weight:600; }
.product-grid { margin-bottom:20px; }
</style>
