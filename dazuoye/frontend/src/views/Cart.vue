<template>
  <div class="page-container cart-page">
    <h2 class="page-title">我的购物车</h2>

    <div v-if="cartStore.loading" class="loading-state">
      <el-skeleton :rows="4" animated />
    </div>

    <el-empty v-else-if="cartStore.items.length === 0" description="购物车是空的">
      <el-button type="primary" @click="$router.push('/product/list')">去逛逛</el-button>
    </el-empty>

    <template v-else>
      <!-- 购物车列表 -->
      <div class="cart-list">
        <div v-for="item in cartStore.items" :key="item.id" class="cart-item">
          <el-checkbox
            v-model="item.selected"
            :true-value="1"
            :false-value="0"
            @change="handleSelectChange(item)"
          />

          <div class="item-image" @click="$router.push(`/product/${item.productId}`)">
            <img :src="item.productImage || '/placeholder.png'" :alt="item.productTitle" />
          </div>

          <div class="item-info">
            <router-link :to="`/product/${item.productId}`" class="item-title">
              {{ item.productTitle }}
            </router-link>
            <span class="item-price">¥{{ item.price }}</span>
          </div>

          <div class="item-quantity">
            <el-input-number
              v-model="item.quantity"
              :min="1"
              :max="item.stock"
              size="small"
              @change="handleQtyChange(item)"
            />
          </div>

          <div class="item-subtotal">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </div>

          <el-button
            type="danger"
            size="small"
            :icon="Delete"
            circle
            @click="handleRemove(item.productId)"
          />
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox
            v-model="selectAll"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >
            全选
          </el-checkbox>
        </div>
        <div class="footer-right">
          <span class="total-text">
            已选 <strong>{{ cartStore.selectedItems.length }}</strong> 件，合计：
          </span>
          <span class="total-price">¥{{ cartStore.totalPrice }}</span>
          <el-button
            type="danger"
            size="large"
            :disabled="cartStore.selectedItems.length === 0"
            @click="goCheckout"
          >
            去结算
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { updateCartSelectedAPI, removeCartAPI, updateCartQtyAPI } from '@/api/cart'
import { Delete } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

// 全选状态
const selectAll = computed(() =>
  cartStore.items.length > 0 && cartStore.items.every(i => i.selected === 1)
)
const isIndeterminate = computed(() =>
  cartStore.items.some(i => i.selected === 1) && !selectAll.value
)

async function handleSelectAll(val) {
  // 批量操作所有项的选中状态
  for (const item of cartStore.items) {
    const newVal = val ? 1 : 0
    if (item.selected !== newVal) {
      await updateCartSelectedAPI(item.productId, newVal)
    }
  }
  await cartStore.fetchCart()
}

async function handleSelectChange(item) {
  await updateCartSelectedAPI(item.productId, item.selected)
}

async function handleQtyChange(item) {
  if (item.quantity < 1) {
    await removeCartAPI(item.productId)
  } else {
    await updateCartQtyAPI(item.productId, item.quantity)
  }
  await cartStore.fetchCart()
}

async function handleRemove(productId) {
  try {
    await ElMessageBox.confirm('确定要移除这件商品吗？', '提示', { type: 'warning' })
    await cartStore.removeFromCart(productId)
  } catch {}
}

function goCheckout() {
  router.push('/checkout')
}

onMounted(() => {
  cartStore.fetchCart()
})
</script>

<style scoped>
.cart-page {
  padding-bottom: 100px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
}

.loading-state {
  background: #fff;
  padding: 24px;
  border-radius: var(--radius);
}

.cart-list {
  background: #fff;
  border-radius: var(--radius);
  overflow: hidden;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
}
.item-image img {
  width: 100%; height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}
.item-title {
  display: block;
  font-size: 14px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}
.item-title:hover { color: var(--primary-color); }
.item-price { color: var(--primary-color); font-weight: 600; }

.item-quantity {
  flex-shrink: 0;
}

.item-subtotal {
  font-weight: 600;
  color: var(--primary-color);
  width: 80px;
  text-align: center;
}

/* 底部结算栏 */
.cart-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid var(--border-color);
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.05);
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-text { font-size: 14px; }
.total-price {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
}

@media (max-width: 768px) {
  .cart-item { padding: 12px; gap: 10px; }
  .item-image { width: 60px; height: 60px; }
  .item-subtotal { display: none; }
  .total-text { font-size: 12px; }
  .total-price { font-size: 18px; }
}
</style>
