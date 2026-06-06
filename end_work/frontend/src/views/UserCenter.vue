<template>
  <div class="page-container user-page">
    <h2 class="page-title">个人中心</h2>

    <div class="user-layout">
      <!-- 左侧：用户信息卡片 -->
      <div class="user-card">
        <el-avatar :size="80" :src="userStore.userInfo?.avatar">
          {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <h3>{{ userStore.userInfo?.nickname }}</h3>
        <p class="user-account">@{{ userStore.userInfo?.username }}</p>
        <el-tag :type="userStore.isSeller ? 'danger' : 'info'">
          {{ userStore.isSeller ? '卖家' : '买家' }}
        </el-tag>

        <div class="user-meta">
          <p>积分：{{ userStore.userInfo?.points || 0 }}</p>
          <p>手机：{{ userStore.userInfo?.phone || '未绑定' }}</p>
          <p>邮箱：{{ userStore.userInfo?.email || '未绑定' }}</p>
        </div>

        <el-button
          v-if="!userStore.isSeller"
          type="warning"
          block
          @click="switchRole(1)"
        >
          切换为卖家身份
        </el-button>
        <el-button
          v-else
          block
          @click="switchRole(0)"
        >
          切换为买家身份
        </el-button>
      </div>

      <!-- 右侧：快捷入口 -->
      <div class="user-content">
        <div class="menu-grid">
          <div class="menu-item" @click="$router.push('/order/list')">
            <el-icon size="28"><Document /></el-icon>
            <span>我的订单</span>
          </div>
          <div class="menu-item" @click="$router.push('/cart')">
            <el-icon size="28"><ShoppingCart /></el-icon>
            <span>购物车</span>
          </div>
          <div v-if="userStore.isSeller" class="menu-item">
            <el-icon size="28"><Plus /></el-icon>
            <span>发布商品</span>
          </div>
          <div class="menu-item" @click="handleLogout">
            <el-icon size="28"><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </div>

        <!-- 收货地址管理 -->
        <div class="address-section">
          <h3>收货地址</h3>
          <el-button size="small" type="primary" @click="showAddrDialog = true">
            + 添加地址
          </el-button>

          <div v-if="addresses.length === 0" class="empty-hint">
            暂无收货地址
          </div>

          <div v-for="addr in addresses" :key="addr.id" class="address-item">
            <div class="addr-info">
              <strong>{{ addr.receiverName }}</strong> {{ addr.phone }}
              <br>
              {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
              <el-tag v-if="addr.isDefault" size="small" type="danger">默认</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 简单添加地址弹窗 -->
    <el-dialog v-model="showAddrDialog" title="添加收货地址" width="90%" :max-width="500">
      <el-form :model="addrForm" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="addrForm.receiverName" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addrForm.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="所在地区">
          <el-input v-model="addrForm.province" placeholder="省" style="width:30%" />
          <el-input v-model="addrForm.city" placeholder="市" style="width:30%" />
          <el-input v-model="addrForm.district" placeholder="区" style="width:30%" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="addrForm.detail" placeholder="街道/小区/门牌号" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addrForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddrDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { switchRoleAPI } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, ShoppingCart, Plus, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const addresses = ref([])
const showAddrDialog = ref(false)

const addrForm = reactive({
  id: null,
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

onMounted(() => {
  loadAddresses()
})

function loadAddresses() {
  const saved = JSON.parse(localStorage.getItem('zbm_addresses') || '[]')
  addresses.value = saved
}

function saveAddress() {
  const newAddr = {
    ...addrForm,
    id: Date.now()
  }
  addresses.value.push(newAddr)
  if (newAddr.isDefault) {
    localStorage.setItem('zbm_default_address', JSON.stringify(newAddr))
  }
  localStorage.setItem('zbm_addresses', JSON.stringify(addresses.value))
  showAddrDialog.value = false
  ElMessage.success('地址已保存')
  // 重置表单
  Object.assign(addrForm, {
    id: null, receiverName: '', phone: '',
    province: '', city: '', district: '', detail: '', isDefault: 0
  })
}

async function switchRole(role) {
  try {
    await switchRoleAPI(role)
    userStore.userInfo.role = role
    ElMessage.success(role === 1 ? '已切换为卖家身份' : '已切换为买家身份')
  } catch {}
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/')
  } catch {}
}
</script>

<style scoped>
.user-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  align-items: start;
}

.user-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 30px 20px;
  text-align: center;
}
.user-card h3 { margin-top: 12px; margin-bottom: 4px; }
.user-account { color: var(--text-secondary); font-size: 13px; margin-bottom: 10px; }
.user-meta { text-align: left; padding: 16px 0; font-size: 14px; color: var(--text-secondary); }
.user-meta p { margin-bottom: 6px; }

.user-content {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  cursor: pointer;
  border-radius: var(--radius);
  transition: background 0.2s;
  font-size: 14px;
}
.menu-item:hover { background: var(--bg-gray); }

.address-section h3 { margin-bottom: 12px; }

.address-item {
  padding: 12px;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
}

.empty-hint {
  color: var(--text-secondary);
  padding: 20px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .user-layout { grid-template-columns: 1fr; }
  .menu-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
