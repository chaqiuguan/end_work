<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <h3>🐟 管理后台</h3>
      <el-menu :default-active="activeMenu" @select="activeMenu=$event" router>
        <el-menu-item index="dashboard"><el-icon><DataAnalysis /></el-icon>数据看板</el-menu-item>
        <el-menu-item index="users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-menu-item index="products"><el-icon><Goods /></el-icon>商品管理</el-menu-item>
        <el-menu-item index="orders"><el-icon><Document /></el-icon>订单管理</el-menu-item>
        <el-menu-item index="categories"><el-icon><Menu /></el-icon>分类管理</el-menu-item>
        <el-menu-item index="reviews"><el-icon><ChatDotRound /></el-icon>评价管理</el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button @click="$router.push('/')" block>返回客户端</el-button>
      </div>
    </aside>
    <main class="admin-main">
      <!-- 数据看板 -->
      <div v-if="activeMenu === 'dashboard'">
        <h2>数据看板</h2>
        <el-row :gutter="16" class="stats-row">
          <el-col :span="6" v-for="s in stats" :key="s.label"><el-card><div class="stat-num">{{ s.value }}</div><div class="stat-label">{{ s.label }}</div></el-card></el-col>
        </el-row>
        <el-card style="margin-top:16px"><h4>订单状态分布</h4>
          <div v-for="(v,k) in orderStats" :key="k" style="margin:4px 0">{{ statusMap[k] }}: {{ v }}</div>
        </el-card>
        <el-card style="margin-top:16px"><h4>热销商品 TOP10</h4>
          <el-table :data="hotProducts" size="small"><el-table-column prop="id" label="ID" width="60"/><el-table-column prop="title" label="名称"/><el-table-column prop="price" label="价格"/><el-table-column prop="viewCount" label="浏览"/></el-table>
        </el-card>
      </div>

      <!-- 用户管理 -->
      <div v-if="activeMenu === 'users'">
        <h2>用户管理</h2>
        <el-input v-model="userKw" placeholder="搜索用户名/手机/昵称" style="width:300px;margin-bottom:12px" @keyup.enter="fetchUsers" />
        <el-button type="primary" @click="fetchUsers">搜索</el-button>
        <el-table :data="users" style="margin-top:12px">
          <el-table-column prop="id" label="ID" width="60"/>
          <el-table-column prop="username" label="用户名"/>
          <el-table-column prop="email" label="邮箱"/>
          <el-table-column prop="nickname" label="昵称"/>
          <el-table-column prop="status" label="状态"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{row.status===1?'正常':'封禁'}}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="160"><template #default="{row}">
            <el-button size="small" @click="userDetail(row.id)">详情</el-button>
            <el-button size="small" :type="row.status===1?'danger':'success'" @click="toggleBan(row)">{{row.status===1?'封禁':'解封'}}</el-button>
          </template></el-table-column>
        </el-table>
        <Pagination :total="userTotal" :page="userPage" :size="20" @change="onUserPage" />
      </div>

      <!-- 商品管理 -->
      <div v-if="activeMenu === 'products'">
        <h2>商品管理</h2>
        <el-table :data="products" style="margin-top:12px">
          <el-table-column prop="id" label="ID" width="60"/>
          <el-table-column prop="title" label="名称"/>
          <el-table-column prop="price" label="价格"/>
          <el-table-column prop="status" label="状态"><template #default="{row}"><el-tag>{{['待审核','上架','下架','售罄'][row.status]||'未知'}}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="200"><template #default="{row}">
            <el-button v-if="row.status===0" size="small" type="success" @click="approve(row.id)">审核通过</el-button>
            <el-button v-if="row.status===0" size="small" type="warning" @click="reject(row.id)">拒绝</el-button>
            <el-button size="small" type="danger" @click="delProduct(row.id)">删除</el-button>
          </template></el-table-column>
        </el-table>
        <Pagination :total="prodTotal" :page="prodPage" :size="20" @change="onProdPage" />
      </div>

      <!-- 订单管理 -->
      <div v-if="activeMenu === 'orders'">
        <h2>订单管理</h2>
        <el-select v-model="orderStatus" placeholder="筛选状态" clearable @change="fetchOrders">
          <el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="Number(k)"/>
        </el-select>
        <el-table :data="orders" style="margin-top:12px">
          <el-table-column prop="orderNo" label="订单号" width="180"/>
          <el-table-column prop="payAmount" label="金额"/>
          <el-table-column prop="status" label="状态"><template #default="{row}"><el-tag>{{statusMap[row.status]}}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="200"><template #default="{row}">
            <el-button v-if="row.status===1" size="small" type="primary" @click="ship(row.id)">发货</el-button>
            <el-button size="small" type="warning" @click="refund(row.id)">退款</el-button>
          </template></el-table-column>
        </el-table>
        <Pagination :total="orderTotal" :page="orderPage" :size="10" @change="onOrderPage" />
      </div>

      <!-- 分类管理 -->
      <div v-if="activeMenu === 'categories'">
        <h2>分类管理</h2>
        <el-input v-model="catName" placeholder="新分类名" style="width:200px" /><el-button type="primary" @click="addCat" style="margin-left:8px">新增</el-button>
        <el-table :data="categories" style="margin-top:12px">
          <el-table-column prop="name" label="名称"/><el-table-column prop="sortOrder" label="排序"/>
          <el-table-column label="操作" width="120"><template #default="{row}">
            <el-button size="small" type="danger" @click="delCat(row.id)">删除</el-button>
          </template></el-table-column>
        </el-table>
      </div>

      <!-- 评价管理 -->
      <div v-if="activeMenu === 'reviews'">
        <h2>评价管理</h2>
        <div v-if="reviewList.length===0" style="color:#999;padding:20px">暂无评价</div>
        <div v-for="r in reviewList" :key="r.id" class="review-item">
          <strong>{{ r.userName }}</strong> ({{ r.rating }}⭐) <span style="color:#999;font-size:12px">{{ r.createdAt }}</span>
          <p>{{ r.content }}</p>
          <el-button size="small" type="danger" @click="delReview(r.id)">删除</el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { DataAnalysis, User, Goods, Document, Menu, ChatDotRound } from '@element-plus/icons-vue'
import request from '@/api/request'
import Pagination from '@/components/Pagination.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeMenu = ref('dashboard')
const statusMap = {0:'待付款',1:'待发货',2:'待收货',3:'已完成',4:'已取消'}

// Dashboard
const stats = reactive([{label:'总用户',value:0},{label:'总订单',value:0},{label:'总销售额',value:'¥0'},{label:'今日订单',value:0}])
const orderStats = ref({})
const hotProducts = ref([])

async function loadDashboard() {
  const res = await request.get('/admin/dashboard/stats')
  const d = res.data.data
  stats[0].value=d.totalUsers; stats[1].value=d.totalOrders; stats[2].value='¥'+d.totalSales; stats[3].value=d.todayOrders
  orderStats.value = (await request.get('/admin/dashboard/order-status')).data.data
  hotProducts.value = (await request.get('/admin/dashboard/hot-products')).data.data
}

// Users
const users = ref([]); const userKw = ref(''); const userPage = ref(1); const userTotal = ref(0)
async function fetchUsers() { const r = await request.get('/admin/user/list',{params:{keyword:userKw.value,page:userPage.value}}); users.value=r.data.data.records; userTotal.value=r.data.data.total }
async function toggleBan(row) { await (row.status===1?request.post('/admin/user/'+row.id+'/ban'):request.post('/admin/user/'+row.id+'/unban')); fetchUsers(); ElMessage.success('操作成功') }
async function userDetail(id) { const r = await request.get('/admin/user/detail/'+id); alert(JSON.stringify(r.data.data,null,2)) }
function onUserPage({page}) { userPage.value=page; fetchUsers() }

// Products
const products = ref([]); const prodPage = ref(1); const prodTotal = ref(0)
async function fetchProducts() { const r = await request.get('/admin/product/pending',{params:{page:prodPage.value}}); products.value=r.data.data.records; prodTotal.value=r.data.data.total }
async function approve(id) { await request.post('/admin/product/'+id+'/approve'); fetchProducts(); ElMessage.success('已通过') }
async function reject(id) { await request.post('/admin/product/'+id+'/reject',{reason:''}); fetchProducts(); ElMessage.success('已拒绝') }
async function delProduct(id) { try { await ElMessageBox.confirm('确认删除?','提示',{type:'warning'}); await request.delete('/admin/product/'+id); fetchProducts(); ElMessage.success('已删除') }catch{} }
function onProdPage({page}) { prodPage.value=page; fetchProducts() }

// Orders
const orders = ref([]); const orderStatus = ref(null); const orderPage = ref(1); const orderTotal = ref(0)
async function fetchOrders() { const r = await request.get('/admin/order/export'); orders.value=r.data.data; orderTotal.value=r.data.data.length }
async function ship(id) { await request.post('/admin/order/'+id+'/ship'); ElMessage.success('已发货'); fetchOrders() }
async function refund(id) { await request.post('/admin/order/'+id+'/refund'); ElMessage.success('已退款'); fetchOrders() }
function onOrderPage({page}) { orderPage.value=page; fetchOrders() }

// Categories
const categories = ref([]); const catName = ref('')
async function fetchCats() { const r = await request.get('/admin/category/list'); categories.value=r.data.data }
async function addCat() { if(!catName.value) return; await request.post('/admin/category/add',{name:catName.value,sortOrder:categories.value.length+1}); catName.value=''; fetchCats(); ElMessage.success('已添加') }
async function delCat(id) { await request.delete('/admin/category/'+id); fetchCats(); ElMessage.success('已删除') }

// Reviews
const reviewList = ref([])
async function fetchReviews() { try { const r = await request.get('/review/list/1'); reviewList.value=r.data.data.records||[] }catch{} }
async function delReview(id) { await request.delete('/review/admin/'+id); fetchReviews(); ElMessage.success('已删除') }

onMounted(() => { loadDashboard(); fetchUsers(); fetchProducts(); fetchOrders(); fetchCats(); fetchReviews() })
</script>

<style scoped>
.admin-layout { display:flex; min-height:calc(100vh - 60px); }
.admin-sidebar { width:220px; background:#304156; color:#fff; padding:16px 0; flex-shrink:0; display:flex; flex-direction:column; }
.admin-sidebar h3 { text-align:center; margin-bottom:16px; }
.admin-sidebar :deep(.el-menu) { border-right:none; background:transparent; }
.admin-sidebar :deep(.el-menu-item) { color:#bfcbd9; }
.admin-sidebar :deep(.el-menu-item.is-active) { color:#409eff; }
.sidebar-footer { padding:16px; margin-top:auto; }
.admin-main { flex:1; padding:20px; background:#f5f7fa; overflow-y:auto; }
.stats-row { margin-bottom:16px; }
.stat-num { font-size:28px; font-weight:700; color:var(--primary-color); }
.stat-label { font-size:13px; color:#999; margin-top:4px; }
.review-item { background:#fff; padding:12px; margin:8px 0; border-radius:4px; }
</style>
