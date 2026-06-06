<template>
  <router-link :to="`/product/${product.id}`" class="product-card">
    <div class="card-image">
      <img
        :src="product.images?.[0] || '/placeholder.png'"
        :alt="product.title"
        @error="onImageError"
      />
      <span v-if="product.conditionText" class="condition-tag">
        {{ product.conditionText }}
      </span>
    </div>
    <div class="card-body">
      <h3 class="card-title" :title="product.title">{{ product.title }}</h3>
      <div class="card-price">
        <span class="price-current">¥{{ product.price }}</span>
        <span v-if="product.originalPrice > product.price" class="price-original">
          ¥{{ product.originalPrice }}
        </span>
      </div>
      <div class="card-footer">
        <span class="seller-name">{{ product.sellerName }}</span>
        <span class="view-count">{{ product.viewCount }} 浏览</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const imgError = ref(false)

function onImageError(e) {
  if (!imgError.value) {
    imgError.value = true
    e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><rect fill="%23f0f0f0" width="200" height="200"/><text x="100" y="110" text-anchor="middle" fill="%23ccc" font-size="48">🐟</text></svg>'
  }
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  transition: transform 0.2s, box-shadow 0.2s;
  display: block;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  background: #f0f0f0;
  overflow: hidden;
}
.card-image img {
  position: absolute;
  top: 0; left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.condition-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  background: rgba(231, 76, 60, 0.85);
  color: #fff;
  font-size: 11px;
  border-radius: 4px;
}

.card-body {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
  min-height: 39px;
}

.card-price {
  margin-bottom: 8px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
