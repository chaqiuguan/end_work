<template>
  <div class="page-container checkout-page">
    <h2 class="page-title">确认订单</h2>

    <div class="checkout-layout">
      <!-- 左侧：订单信息 -->
      <div class="checkout-main">
        <!-- 收货地址 -->
        <div class="section-card">
          <h3>收货地址</h3>
          <div v-if="addresses.length === 0" class="empty-hint">
            暂无收货地址，请先在个人中心添加
          </div>
          <el-radio-group v-else v-model="selectedAddressId">
            <div v-for="addr in addresses" :key="addr.id" class="address-option">
              <el-radio :value="addr.id">
                <strong>{{ addr.receiverName }}</strong>
                {{ addr.phone }}
                <br>
                {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
              </el-radio>
            </div>
          </el-radio-group>
        </div>

        <!-- 商品清单 -->
        <div class="section-card">
          <h3>商品清单</h3>
          <div v-for="item in cartStore.selectedItems" :key="item.id" class="checkout-item">
            <img :src="item.productImage || '/placeholder.png'" class="item-pic" />
            <div class="item-info">
              <span class="item-title">{{ item.productTitle }}</span>
              <span class="item-price">¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
            <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧：结算汇总 -->
      <div class="checkout-sidebar">
        <div class="summary-card">
          <h3>订单摘要</h3>
          <div class="summary-row">
            <span>商品总价</span>
            <span>¥{{ cartStore.totalPrice }}</span>
          </div>
          <div class="summary-row">
            <span>运费</span>
            <span class="free-shipping">免运费</span>
          </div>
          <el-divider />
          <div class="summary-row total">
            <span>应付金额</span>
            <span class="total-price">¥{{ cartStore.totalPrice }}</span>
          </div>

          <el-input
            v-model="remark"
            type="textarea"
            :rows="2"
            placeholder="买家备注（选填）"
            style="margin: 12px 0"
          />

          <el-button
            type="danger"
            size="large"
            block
            :loading="submitting"
            :disabled="cartStore.selectedItems.length === 0 || !selectedAddressId"
            @click="submitOrder"
          >
            提交订单（模拟支付）
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { createOrderAPI } from '@/api/order'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const cartStore = useCartStore()

const addresses = ref([])
const selectedAddressId = ref(null)
const remark = ref('')
const submitting = ref(false)

onMounted(async () => {
  // 获取用户地址列表（简化：通过 /user/profile 或其他来源）
  try {
    // 这里简化处理，实际应有专门的地址API
    const res = await request.get('/user/profile')
    // 地址列表需单独接口，这里先设置一个默认选项
    addresses.value = []
  } catch {}

  await cartStore.fetchCart()

  // 获取默认地址（简化实现）
  const savedAddr = JSON.parse(localStorage.getItem('zbm_default_address') || 'null')
  if (savedAddr) {
    addresses.value = [savedAddr]
    selectedAddressId.value = savedAddr.id
  }
})

async function submitOrder() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    const cartItemIds = cartStore.selectedItems.map(i => i.id)
    const res = await createOrderAPI({
      cartItemIds,
      addressId: selectedAddressId.value,
      remark: remark.value,
      payType: 0
    })

    const order = res.data.data
    ElMessage.success('下单成功！模拟支付中...')

    // 模拟支付
    await request.post(`/order/${order.id}/pay`)
    ElMessage.success('支付成功（模拟）')

    // 清空购物车回调
    await cartStore.fetchCart()
    router.push(`/order/list`)
  } catch (e) {
    console.error('下单失败:', e)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.checkout-layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  align-items: start;
}

.section-card, .summary-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 20px;
  margin-bottom: 20px;
}

.section-card h3, .summary-card h3 {
  font-size: 16px;
  margin-bottom: 16px;
}

.address-option {
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
}
.item-pic { width: 60px; height: 60px; border-radius: 6px; object-fit: cover; }
.item-info { flex: 1; }
.item-title { display: block; font-size: 14px; margin-bottom: 4px; }
.item-price { color: var(--primary-color); font-size: 14px; }
.item-subtotal { font-weight: 600; color: var(--primary-color); }

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}
.summary-row.total { font-size: 16px; font-weight: 600; }
.total-price { color: var(--primary-color); font-size: 22px; }
.free-shipping { color: var(--success-color); }

.empty-hint {
  color: var(--text-secondary);
  padding: 20px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .checkout-layout {
    grid-template-columns: 1fr;
  }
}
</style>
