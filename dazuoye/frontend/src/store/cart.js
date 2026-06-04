import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartAPI, addCartAPI, removeCartAPI, updateCartQtyAPI } from '@/api/cart'

/**
 * 购物车状态管理
 */
export const useCartStore = defineStore('cart', () => {
  // ========== 状态 ==========
  const items = ref([])
  const loading = ref(false)

  // ========== 计算属性 ==========
  const totalCount = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )
  const selectedItems = computed(() =>
    items.value.filter(item => item.selected === 1)
  )
  const totalPrice = computed(() =>
    selectedItems.value.reduce(
      (sum, item) => sum + item.price * item.quantity, 0
    ).toFixed(2)
  )

  // ========== 方法 ==========

  async function fetchCart() {
    loading.value = true
    try {
      const res = await getCartAPI()
      items.value = res.data.data.items || []
    } finally {
      loading.value = false
    }
  }

  async function addToCart(productId, quantity = 1) {
    await addCartAPI({ productId, quantity })
    await fetchCart()
  }

  async function removeFromCart(productId) {
    await removeCartAPI(productId)
    await fetchCart()
  }

  async function updateQuantity(productId, quantity) {
    await updateCartQtyAPI(productId, quantity)
    await fetchCart()
  }

  function clearCart() {
    items.value = []
  }

  return {
    items,
    loading,
    totalCount,
    selectedItems,
    totalPrice,
    fetchCart,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart
  }
})
