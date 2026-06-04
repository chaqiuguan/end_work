<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <h3>🐟 管理后台</h3>
      <el-menu :default-active="tab" @select="tab=$event" background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="dashboard"><el-icon><DataAnalysis /></el-icon>数据看板</el-menu-item>
        <el-menu-item index="users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-menu-item index="products"><el-icon><Goods /></el-icon>商品管理</el-menu-item>
        <el-menu-item index="orders"><el-icon><Document /></el-icon>订单管理</el-menu-item>
        <el-menu-item index="categories"><el-icon><Menu /></el-icon>分类管理</el-menu-item>
        <el-menu-item index="banners"><el-icon><Picture /></el-icon>轮播管理</el-menu-item>
        <el-menu-item index="coupons"><el-icon><Ticket /></el-icon>优惠券管理</el-menu-item>
        <el-menu-item index="announcements"><el-icon><Bell /></el-icon>公告管理</el-menu-item>
        <el-menu-item index="feedbacks"><el-icon><ChatDotRound /></el-icon>反馈管理</el-menu-item>
        <el-menu-item index="reviews"><el-icon><Star /></el-icon>评价管理</el-menu-item>
      </el-menu>
      <div class="sidebar-footer"><el-button @click="$router.push('/')" block>返回客户端</el-button></div>
    </aside>
    <main class="admin-main">
      <!-- ===== Dashboard ===== -->
      <div v-if="tab==='dashboard'">
        <h2>数据看板</h2>
        <el-row :gutter="16" class="stats-row">
          <el-col :span="6" v-for="s in stats" :key="s.label"><el-card><div class="stat-num">{{ s.value }}</div><div class="stat-label">{{ s.label }}</div></el-card></el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top:16px">
          <el-col :span="12"><el-card><h4>订单状态分布</h4><div ref="orderChartDom" style="height:300px"></div></el-card></el-col>
          <el-col :span="12"><el-card><h4>热销商品 TOP5</h4><div ref="hotChartDom" style="height:300px"></div></el-card></el-col>
        </el-row>
      </div>

      <!-- ===== Users ===== -->
      <div v-if="tab==='users'">
        <h2>用户管理</h2>
        <el-input v-model="userKw" placeholder="搜索" style="width:300px;margin-bottom:12px" @keyup.enter="fetchUsers"/><el-button type="primary" @click="fetchUsers" style="margin-left:8px">搜索</el-button>
        <el-table :data="users"><el-table-column prop="id" label="ID" width="60"/><el-table-column prop="username" label="用户名"/><el-table-column prop="email" label="邮箱"/><el-table-column prop="nickname" label="昵称"/>
          <el-table-column prop="status" label="状态"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{row.status===1?'正常':'封禁'}}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="160"><template #default="{row}"><el-button size="small" @click="userDetail(row.id)">详情</el-button><el-button size="small" :type="row.status===1?'danger':'success'" @click="toggleBan(row)">{{row.status===1?'封禁':'解封'}}</el-button></template></el-table-column></el-table>
        <Pagination :total="userTotal" :page="userPage" :size="20" @change="onUserPage"/>
      </div>

      <!-- ===== Products ===== -->
      <div v-if="tab==='products'">
        <h2>商品管理</h2>
        <el-table :data="products"><el-table-column prop="id" label="ID" width="60"/><el-table-column prop="title" label="名称"/><el-table-column prop="price" label="价格"/>
          <el-table-column prop="status" label="状态"><template #default="{row}"><el-tag>{{['待审核','上架','下架','售罄'][row.status]||'未知'}}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="200"><template #default="{row}"><el-button v-if="row.status===0" size="small" type="success" @click="approve(row.id)">通过</el-button><el-button v-if="row.status===0" size="small" type="warning" @click="reject(row.id)">拒绝</el-button><el-button size="small" type="danger" @click="delProduct(row.id)">删除</el-button></template></el-table-column></el-table>
        <Pagination :total="prodTotal" :page="prodPage" :size="20" @change="onProdPage"/>
      </div>

      <!-- ===== Orders ===== -->
      <div v-if="tab==='orders'">
        <h2>订单管理</h2>
        <el-select v-model="orderStatus" placeholder="筛选" clearable @change="fetchOrders"><el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="Number(k)"/></el-select>
        <el-table :data="orders" style="margin-top:12px"><el-table-column prop="orderNo" label="订单号" width="180"/><el-table-column prop="payAmount" label="金额"/><el-table-column prop="status" label="状态"><template #default="{row}"><el-tag>{{statusMap[row.status]}}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="200"><template #default="{row}"><el-button v-if="row.status===1" size="small" type="primary" @click="ship(row.id)">发货</el-button><el-button size="small" type="warning" @click="refund(row.id)">退款</el-button></template></el-table-column></el-table>
      </div>

      <!-- ===== Categories ===== -->
      <div v-if="tab==='categories'"><h2>分类管理</h2>
        <el-input v-model="catName" placeholder="新分类名" style="width:200px"/><el-button type="primary" @click="addCat" style="margin-left:8px">新增</el-button>
        <el-table :data="categories" style="margin-top:12px"><el-table-column prop="name" label="名称"/><el-table-column prop="sortOrder" label="排序"/><el-table-column label="操作" width="100"><template #default="{row}"><el-button size="small" type="danger" @click="delCat(row.id)">删除</el-button></template></el-table-column></el-table>
      </div>

      <!-- ===== Banners ===== -->
      <div v-if="tab==='banners'"><h2>轮播管理</h2>
        <el-form :inline="true"><el-form-item><el-input v-model="bannerForm.title" placeholder="标题"/></el-form-item><el-form-item><el-input v-model="bannerForm.imageUrl" placeholder="图片URL"/></el-form-item><el-form-item><el-input v-model="bannerForm.linkUrl" placeholder="跳转链接(选填)"/></el-form-item><el-form-item><el-button type="primary" @click="addBanner">新增</el-button></el-form-item></el-form>
        <el-table :data="banners"><el-table-column prop="title" label="标题"/><el-table-column prop="imageUrl" label="图片"><template #default="{row}"><img :src="row.imageUrl" style="height:40px"/></template></el-table-column><el-table-column prop="sortOrder" label="排序"/>
          <el-table-column label="操作" width="120"><template #default="{row}"><el-button size="small" @click="bannerForm={...row};editBanner=true">编辑</el-button><el-button size="small" type="danger" @click="delBanner(row.id)">删除</el-button></template></el-table-column></el-table>
        <el-dialog v-model="editBanner" title="编辑轮播"><el-form><el-form-item label="标题"><el-input v-model="bannerForm.title"/></el-form-item><el-form-item label="图片URL"><el-input v-model="bannerForm.imageUrl"/></el-form-item><el-form-item label="链接"><el-input v-model="bannerForm.linkUrl"/></el-form-item><el-form-item label="排序"><el-input-number v-model="bannerForm.sortOrder"/></el-form-item></el-form><template #footer><el-button @click="editBanner=false">取消</el-button><el-button type="primary" @click="updateBanner">保存</el-button></template></el-dialog>
      </div>

      <!-- ===== Coupons ===== -->
      <div v-if="tab==='coupons'"><h2>优惠券管理</h2>
        <el-form :inline="true"><el-form-item><el-input v-model="cpForm.name" placeholder="名称"/></el-form-item><el-form-item><el-input-number v-model="cpForm.discount" :min="1" placeholder="优惠金额(元)"/></el-form-item><el-form-item><el-input-number v-model="cpForm.minAmount" :min="0" placeholder="最低消费(元)"/></el-form-item><el-form-item><el-input-number v-model="cpForm.totalStock" :min="1" placeholder="发行量"/></el-form-item><el-form-item><el-input-number v-model="cpForm.expireDays" :min="1" placeholder="有效天数"/></el-form-item><el-form-item><el-button type="primary" @click="addCoupon">新增</el-button></el-form-item></el-form>
        <el-table :data="coupons"><el-table-column prop="name" label="名称"/><el-table-column prop="discount" label="优惠(元)"/><el-table-column prop="minAmount" label="最低消费"/><el-table-column prop="remainStock" label="剩余/总量"><template #default="{row}">{{row.remainStock}}/{{row.totalStock}}</template></el-table-column>
          <el-table-column label="操作" width="80"><template #default="{row}"><el-button size="small" type="danger" @click="delCoupon(row.id)">删除</el-button></template></el-table-column></el-table>
      </div>

      <!-- ===== Announcements ===== -->
      <div v-if="tab==='announcements'"><h2>公告管理</h2>
        <el-form :inline="true"><el-form-item><el-input v-model="annForm.title" placeholder="标题"/></el-form-item><el-form-item><el-input v-model="annForm.content" type="textarea" :rows="2" placeholder="内容"/></el-form-item><el-form-item><el-button type="primary" @click="addAnn">发布</el-button></el-form-item></el-form>
        <el-table :data="announcements"><el-table-column prop="title" label="标题"/><el-table-column prop="content" label="内容" width="300"/><el-table-column prop="createdAt" label="发布时间"/>
          <el-table-column label="操作" width="80"><template #default="{row}"><el-button size="small" type="danger" @click="delAnn(row.id)">删除</el-button></template></el-table-column></el-table>
      </div>

      <!-- ===== Feedbacks ===== -->
      <div v-if="tab==='feedbacks'"><h2>反馈管理</h2>
        <el-table :data="feedbacks"><el-table-column prop="userName" label="用户"/><el-table-column prop="content" label="内容" width="300"/><el-table-column prop="reply" label="回复"/>
          <el-table-column prop="status" label="状态"><template #default="{row}"><el-tag :type="row.status?'success':'warning'">{{row.status?'已回复':'待处理'}}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="120"><template #default="{row}"><el-button v-if="!row.status" size="small" type="primary" @click="replyFbId=row.id;replyText=''">回复</el-button></template></el-table-column></el-table>
        <el-dialog v-model="!!replyFbId" title="回复反馈" @close="replyFbId=null"><el-input v-model="replyText" type="textarea" :rows="4" placeholder="输入回复..."/><template #footer><el-button @click="replyFbId=null">取消</el-button><el-button type="primary" @click="doReply">回复</el-button></template></el-dialog>
      </div>

      <!-- ===== Reviews ===== -->
      <div v-if="tab==='reviews'"><h2>评价管理</h2>
        <div v-if="reviewList.length===0" style="color:#999;padding:20px">暂无评价</div>
        <div v-for="r in reviewList" :key="r.id" class="review-item"><strong>{{r.userName}}</strong> ({{r.rating}}⭐)<p>{{r.content}}</p><el-button size="small" type="danger" @click="delReview(r.id)">删除</el-button></div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { DataAnalysis, User, Goods, Document, Menu, Picture, Ticket, Bell, ChatDotRound, Star } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/api/request'
import Pagination from '@/components/Pagination.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const tab = ref('dashboard')
const statusMap = {0:'待付款',1:'待发货',2:'待收货',3:'已完成',4:'已取消'}
const orderChartDom = ref(null), hotChartDom = ref(null)

// Dashboard
const stats = reactive([{label:'总用户',value:0},{label:'总订单',value:0},{label:'总销售额',value:'¥0'},{label:'今日订单',value:0}])
async function loadDashboard() {
  const d = (await request.get('/admin/dashboard/stats')).data.data
  stats[0].value=d.totalUsers; stats[1].value=d.totalOrders; stats[2].value='¥'+d.totalSales; stats[3].value=d.todayOrders
  const os = (await request.get('/admin/dashboard/order-status')).data.data
  const hp = (await request.get('/admin/dashboard/hot-products')).data.data
  await nextTick()
  renderCharts(os, hp)
}
function renderCharts(orderData, hotData) {
  const labels = ['待付款','待发货','待收货','已完成','已取消']
  if (orderChartDom.value) {
    const c1 = echarts.init(orderChartDom.value)
    c1.setOption({ tooltip:{}, series:[{type:'pie',radius:['40%','70%'],data:Object.entries(orderData).map(([k,v])=>({name:labels[Number(k)],value:v})),label:{show:true}}] })
  }
  if (hotChartDom.value) {
    const c2 = echarts.init(hotChartDom.value)
    c2.setOption({ tooltip:{}, xAxis:{type:'category',data:hotData.map(h=>h.title?.substring(0,8))}, yAxis:{type:'value'}, series:[{type:'bar',data:hotData.map(h=>h.viewCount||0),itemStyle:{color:'#e74c3c'}}] })
  }
}

// Users
const users=ref([]),userKw=ref(''),userPage=ref(1),userTotal=ref(0)
async function fetchUsers(){const r=await request.get('/admin/user/list',{params:{keyword:userKw.value,page:userPage.value}});users.value=r.data.data.records;userTotal.value=r.data.data.total}
async function toggleBan(row){await(row.status===1?request.post('/admin/user/'+row.id+'/ban'):request.post('/admin/user/'+row.id+'/unban'));fetchUsers();ElMessage.success('操作成功')}
async function userDetail(id){const r=await request.get('/admin/user/detail/'+id);alert(JSON.stringify(r.data.data,null,2))}
function onUserPage({page}){userPage.value=page;fetchUsers()}

// Products
const products=ref([]),prodPage=ref(1),prodTotal=ref(0)
async function fetchProducts(){const r=await request.get('/admin/product/pending',{params:{page:prodPage.value}});products.value=r.data.data.records;prodTotal.value=r.data.data.total}
async function approve(id){await request.post('/admin/product/'+id+'/approve');fetchProducts()}
async function reject(id){await request.post('/admin/product/'+id+'/reject',{reason:''});fetchProducts()}
async function delProduct(id){try{await ElMessageBox.confirm('确认删除?','提示',{type:'warning'});await request.delete('/admin/product/'+id);fetchProducts()}catch{}}
function onProdPage({page}){prodPage.value=page;fetchProducts()}

// Orders
const orders=ref([]),orderStatus=ref(null)
async function fetchOrders(){const r=await request.get('/admin/order/export');orders.value=r.data.data}
async function ship(id){await request.post('/admin/order/'+id+'/ship');fetchOrders()}
async function refund(id){await request.post('/admin/order/'+id+'/refund');fetchOrders()}

// Categories
const categories=ref([]),catName=ref('')
async function fetchCats(){const r=await request.get('/admin/category/list');categories.value=r.data.data}
async function addCat(){if(!catName.value)return;await request.post('/admin/category/add',{name:catName.value,sortOrder:categories.value.length+1});catName.value='';fetchCats()}
async function delCat(id){await request.delete('/admin/category/'+id);fetchCats()}

// Banners
const banners=ref([]),bannerForm=reactive({title:'',imageUrl:'',linkUrl:'',sortOrder:0}),editBanner=ref(false)
async function fetchBanners(){const r=await request.get('/admin/banner/list');banners.value=r.data.data}
async function addBanner(){await request.post('/admin/banner/add',bannerForm);Object.assign(bannerForm,{title:'',imageUrl:'',linkUrl:'',sortOrder:0});fetchBanners();ElMessage.success('添加成功')}
async function updateBanner(){await request.put('/admin/banner/'+bannerForm.id,bannerForm);editBanner.value=false;fetchBanners()}
async function delBanner(id){await request.delete('/admin/banner/'+id);fetchBanners()}

// Coupons
const coupons=ref([]),cpForm=reactive({name:'',discount:10,minAmount:0,totalStock:100,expireDays:7})
async function fetchCoupons(){const r=await request.get('/coupon/admin/list');coupons.value=r.data.data}
async function addCoupon(){await request.post('/coupon/admin/add',{...cpForm,remainStock:cpForm.totalStock});fetchCoupons();ElMessage.success('添加成功')}
async function delCoupon(id){await request.delete('/coupon/admin/'+id);fetchCoupons()}

// Announcements
const announcements=ref([]),annForm=reactive({title:'',content:''})
async function fetchAnns(){const r=await request.get('/admin/announcement/list');announcements.value=r.data.data}
async function addAnn(){await request.post('/admin/announcement/add',annForm);annForm.title='';annForm.content='';fetchAnns();ElMessage.success('发布成功')}
async function delAnn(id){await request.delete('/admin/announcement/'+id);fetchAnns()}

// Feedbacks
const feedbacks=ref([]),replyFbId=ref(null),replyText=ref('')
async function fetchFeedbacks(){const r=await request.get('/feedback/admin/list');feedbacks.value=r.data.data}
async function doReply(){await request.put('/feedback/admin/'+replyFbId.value+'/reply',{reply:replyText.value});replyFbId.value=null;fetchFeedbacks();ElMessage.success('已回复')}

// Reviews
const reviewList=ref([])
async function fetchReviews(){try{const r=await request.get('/review/list/1');reviewList.value=r.data.data.records||[]}catch{}}
async function delReview(id){await request.delete('/review/admin/'+id);fetchReviews()}

// Init
onMounted(()=>{loadDashboard();fetchUsers();fetchProducts();fetchOrders();fetchCats();fetchBanners();fetchCoupons();fetchAnns();fetchFeedbacks();fetchReviews()})
watch(tab, t=>{if(t==='dashboard')nextTick(()=>loadDashboard())})
</script>

<style scoped>
.admin-layout{display:flex;min-height:calc(100vh - 60px)}
.admin-sidebar{width:220px;background:#304156;color:#fff;padding:16px 0;flex-shrink:0;display:flex;flex-direction:column}
.admin-sidebar h3{text-align:center;margin-bottom:16px}
.sidebar-footer{padding:16px;margin-top:auto}
.admin-main{flex:1;padding:20px;background:#f5f7fa;overflow-y:auto}
.stats-row{margin-bottom:16px}
.stat-num{font-size:28px;font-weight:700;color:var(--primary-color)}
.stat-label{font-size:13px;color:#999;margin-top:4px}
.review-item{background:#fff;padding:12px;margin:8px 0;border-radius:4px}
</style>
