<template>
  <div class="page-container order-page">
    <h2 class="page-title">我的订单</h2>

    <!-- 订单状态 Tab -->
    <el-tabs v-model="activeTab" @tab-change="fetchOrders">
      <el-tab-pane label="全部" :name="-1" />
      <el-tab-pane label="待付款" :name="0" />
      <el-tab-pane label="待发货" :name="1" />
      <el-tab-pane label="待收货" :name="2" />
      <el-tab-pane label="已完成" :name="3" />
      <el-tab-pane label="已取消" :name="4" />
    </el-tabs>

    <!-- 订单列表 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="4" animated />
    </div>

    <el-empty v-else-if="orders.length === 0" description="暂无订单">
      <el-button type="primary" @click="$router.push('/product/list')">去逛逛</el-button>
    </el-empty>

    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <!-- 订单头 -->
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag :type="statusMap[order.status]?.color">
            {{ order.statusText }}
          </el-tag>
        </div>

        <!-- 订单项目 -->
        <div v-if="order.items" class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <img :src="item.productImage || '/placeholder.png'" class="item-pic" />
            <div class="item-info">
              <span class="item-title">{{ item.productTitle }}</span>
              <span>¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <span class="order-amount">实付：<strong>¥{{ order.payAmount }}</strong></span>
          <div class="order-actions">
            <el-button v-if="order.status === 0" type="danger" size="small"
              @click="handlePay(order.id)">
              立即付款
            </el-button>
            <el-button v-if="order.status === 0" size="small"
              @click="handleCancel(order.id)">
              取消订单
            </el-button>
            <el-button v-if="order.status === 2" type="success" size="small"
              @click="handleConfirm(order.id)">
              确认收货
            </el-button>
            <el-button size="small" @click="$router.push(`/order/${order.id}`)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <Pagination
      :total="total"
      :page="page"
      :size="size"
      @change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderListAPI, payOrderAPI, cancelOrderAPI, confirmOrderAPI } from '@/api/order'
import { orderStatusMap } from '@/utils/validators'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()

const orders = ref([])
const loading = ref(true)
const activeTab = ref(-1)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusMap = orderStatusMap

async function fetchOrders() {
  loading.value = true
  try {
    const params = {
      status: activeTab.value,
      page: page.value,
      size: size.value
    }
    const res = await getOrderListAPI(params)
    const data = res.data.data
    orders.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    console.error('获取订单失败:', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange({ page: p, size: s }) {
  page.value = p
  size.value = s
  fetchOrders()
}

async function handlePay(orderId) {
  try {
    await payOrderAPI(orderId)
    ElMessage.success('支付成功（模拟）')
    fetchOrders()
  } catch {}
}

async function handleCancel(orderId) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrderAPI(orderId)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch {}
}

async function handleConfirm(orderId) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', { type: 'info' })
    await confirmOrderAPI(orderId)
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch {}
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-page { padding-bottom: 60px; }
.page-title { font-size: 24px; font-weight: 600; margin-bottom: 16px; }

.loading-state {
  background: #fff;
  padding: 24px;
  border-radius: var(--radius);
}

.order-card {
  background: #fff;
  border-radius: var(--radius);
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: var(--shadow);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.order-no { font-size: 13px; color: var(--text-secondary); }

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}
.item-pic { width: 60px; height: 60px; border-radius: 6px; object-fit: cover; }
.item-info { flex: 1; font-size: 14px; }
.item-title { display: block; margin-bottom: 4px; }

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
}

.order-amount { font-size: 14px; }
.order-amount strong { color: var(--primary-color); font-size: 18px; }

.order-actions { display: flex; gap: 8px; }

@media (max-width: 768px) {
  .order-footer { flex-direction: column; gap: 8px; align-items: flex-end; }
}
</style>
