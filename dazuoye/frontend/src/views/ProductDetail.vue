<template>
  <div class="page-container detail-page">
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="product">
      <!-- 商品图片和基本信息 -->
      <div class="detail-top">
        <!-- 图片轮播 -->
        <div class="detail-gallery">
          <img
            v-if="product.images?.length"
            :src="currentImage"
            :alt="product.title"
            class="main-image"
          />
          <div class="thumb-list">
            <img
              v-for="(img, idx) in product.images"
              :key="idx"
              :src="img"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            />
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="detail-info">
          <h1 class="product-title">{{ product.title }}</h1>

          <div class="price-block">
            <span class="price-current">¥{{ product.price }}</span>
            <span v-if="product.originalPrice > product.price" class="price-original">
              ¥{{ product.originalPrice }}
            </span>
            <el-tag v-if="product.conditionText" size="small" type="danger">
              {{ product.conditionText }}
            </el-tag>
          </div>

          <div class="meta-row">
            <span>分类：{{ product.categoryName || '未分类' }}</span>
            <span>卖家：{{ product.sellerName }}</span>
            <span>浏览：{{ (product.viewCount || 0) + 1 }} 次</span>
          </div>

          <div class="stock-row">
            <span v-if="product.stock > 0" class="in-stock">库存充足 ({{ product.stock }} 件)</span>
            <span v-else class="out-stock">已售罄</span>
          </div>

          <!-- 操作按钮 -->
          <div class="action-row">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              size="large"
            />
            <el-button type="danger" size="large" @click="addToCart" :loading="addingCart">
              🛒 加入购物车
            </el-button>
            <el-button size="large" @click="toggleFavorite">
              {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="detail-description">
        <h3>商品描述</h3>
        <p>{{ product.description || '卖家未提供描述信息' }}</p>
      </div>

      <!-- 商品评价 -->
      <div class="detail-reviews">
        <h3>商品评价 ({{ reviews.length }})</h3>
        <div v-if="userStore.isLoggedIn" class="review-form">
          <el-input v-model="reviewContent" type="textarea" :rows="3" placeholder="写下您的评价..." />
          <el-rate v-model="reviewRating" />
          <el-button type="primary" size="small" @click="submitReview" style="margin-top:8px">提交评价</el-button>
        </div>
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <strong>{{ r.userName }}</strong>
          <el-rate :model-value="r.rating" disabled show-score size="small" />
          <p>{{ r.content }}</p>
          <span class="review-time">{{ r.createdAt }}</span>
        </div>
      </div>
    </template>

    <el-empty v-else description="商品不存在或已下架" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getProductDetailAPI } from '@/api/product'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const currentImage = ref('')
const quantity = ref(1)
const addingCart = ref(false)
const isFavorited = ref(false)
const reviews = ref([])
const reviewContent = ref('')
const reviewRating = ref(5)

onMounted(async () => {
  const id = route.params.id
  try {
    const [prodRes, favRes, revRes] = await Promise.all([
      getProductDetailAPI(id),
      userStore.isLoggedIn ? request.get(`/favorite/check/${id}`).catch(() => ({data:{data:false}})) : {data:{data:false}},
      request.get(`/review/list/${id}`).catch(() => ({data:{data:{records:[]}}}))
    ])
    product.value = prodRes.data.data
    isFavorited.value = favRes.data.data
    reviews.value = revRes.data.data.records || []
    if (product.value?.images?.length) {
      currentImage.value = product.value.images[0]
    }
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
})

async function addToCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  addingCart.value = true
  try {
    await cartStore.addToCart(product.value.id, quantity.value)
    ElMessage.success('已加入购物车')
  } finally {
    addingCart.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  await request.post(`/favorite/toggle/${product.value.id}`)
  isFavorited.value = !isFavorited.value
  ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
}

async function submitReview() {
  if (!reviewContent.value.trim()) { ElMessage.warning('请输入评价内容'); return }
  try {
    await request.post('/review/submit', { productId: product.value.id, content: reviewContent.value, rating: reviewRating.value })
    ElMessage.success('评价已提交')
    reviewContent.value = ''
    reviewRating.value = 5
    // Reload reviews
    const r = await request.get(`/review/list/${product.value.id}`)
    reviews.value = r.data.data.records || []
  } catch {}
}
</script>

<style scoped>
.detail-top {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.detail-gallery {
  background: #fff;
  border-radius: var(--radius);
  padding: 20px;
}

.main-image {
  width: 100%;
  aspect-ratio: 1;
  object-fit: contain;
  border-radius: 6px;
  margin-bottom: 12px;
}

.thumb-list {
  display: flex;
  gap: 8px;
}
.thumb-list img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
}
.thumb-list img.active { border-color: var(--primary-color); }

.detail-info {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px;
}

.product-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
}

.price-block {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: var(--primary-light);
  border-radius: 6px;
}

.meta-row {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.stock-row { margin-bottom: 20px; }
.in-stock { color: var(--success-color); }
.out-stock { color: var(--primary-color); }

.action-row {
  display: flex;
  gap: 12px;
}

.detail-description {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px;
}
.detail-description h3 { margin-bottom: 12px; }
.detail-description p { line-height: 1.8; white-space: pre-wrap; }

.detail-reviews {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px;
  margin-top: 16px;
}
.detail-reviews h3 { margin-bottom: 16px; }
.review-form { margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid var(--border-color); }
.review-item { padding: 12px 0; border-bottom: 1px solid var(--border-color); }
.review-item p { margin: 6px 0; line-height: 1.5; }
.review-time { color: var(--text-secondary); font-size: 12px; }

.loading-wrap {
  background: #fff;
  padding: 24px;
  border-radius: var(--radius);
}

@media (max-width: 768px) {
  .detail-top { grid-template-columns: 1fr; }
}
</style>
