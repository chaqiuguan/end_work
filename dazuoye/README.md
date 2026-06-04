# 🐟 转鱼宝猫 (ZhuanYuBaoMao) 电商平台

> **闲转好物，物尽其用** — 支持二手闲置与新品交易的轻量级电商平台

---

## 📋 项目简介

转鱼宝猫是一个前后端分离的轻量级电商平台，强调"闲转"概念，用户可发布、浏览、购买商品，支持基础订单流程与模拟支付。

### 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| **前端** | Vue 3 + Vite + Element Plus | 响应式设计，PC/移动端兼容 |
| **后端** | Spring Boot 2.7 + MyBatis-Plus | 分层架构：Controller → Service → Mapper |
| **数据库** | MySQL 8.0 | InnoDB, utf8mb4, 逻辑删除 |
| **缓存** | Redis 7 | 购物车缓存、JWT黑名单、热点商品 |
| **认证** | JWT (jjwt 0.9.1) | Access Token(24h) + Refresh Token(7d) |
| **安全** | Spring Security + BCrypt | 无状态会话，XSS/CSRF防护 |
| **部署** | Docker + Docker Compose | 一键启动全部服务 |

---

## 🚀 快速启动

### 方式一：Docker Compose（推荐）

```bash
# 1. 克隆项目
git clone <repo-url>
cd dazuoye

# 2. 一键启动（MySQL + Redis + 后端 + 前端）
docker-compose up -d

# 3. 查看运行状态
docker-compose ps

# 4. 初始化数据库（首次启动自动执行 schema.sql）
# 访问: http://localhost
```

### 方式二：本地开发

**前置条件：**
- JDK 17+
- Node.js 20+
- MySQL 8.0+
- Redis 7+

**启动后端：**

```bash
cd backend

# 修改 src/main/resources/application.yml 中的数据库和Redis连接信息

# 执行数据库初始化脚本
mysql -u root -p < src/main/resources/sql/schema.sql

# 编译运行
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# 后端启动在: http://localhost:8080/api
```

**启动前端：**

```bash
cd frontend

# 安装依赖
npm install --registry=https://registry.npmmirror.com

# 启动开发服务器
npm run dev
# 前端启动在: http://localhost:5173
```

---

## 📁 项目结构

```
dazuoye/
├── README.md                        # 本文档
├── docker-compose.yml               # Docker 编排文件
├── .github/workflows/ci.yml         # GitHub Actions CI/CD
│
├── backend/                         # Spring Boot 后端
│   ├── pom.xml                      # Maven 配置
│   ├── Dockerfile                   # 后端镜像
│   └── src/main/
│       ├── java/com/zhuanbaomao/
│       │   ├── ZhuanBaoMaoApplication.java    # 启动类
│       │   ├── config/               # 配置类
│       │   │   ├── SecurityConfig.java        # Spring Security
│       │   │   ├── WebSecurityConfig.java     # 安全过滤链
│       │   │   ├── RedisConfig.java           # Redis序列化
│       │   │   ├── MybatisPlusConfig.java     # MyBatis-Plus分页
│       │   │   ├── CorsConfig.java            # 跨域配置
│       │   │   ├── WebMvcConfig.java          # MVC配置
│       │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│       │   │   └── BusinessException.java     # 业务异常
│       │   ├── common/              # 通用工具
│       │   │   ├── Result.java              # 统一响应体
│       │   │   ├── ResultCode.java          # 状态码常量
│       │   │   └── PageResult.java          # 分页响应体
│       │   ├── security/            # 安全模块
│       │   │   ├── JwtTokenProvider.java    # JWT令牌提供器
│       │   │   ├── JwtAuthenticationFilter.java # JWT过滤器
│       │   │   ├── JwtUserDetails.java      # JWT用户信息
│       │   │   ├── CurrentUser.java         # @CurrentUser注解
│       │   │   └── CurrentUserResolver.java # 参数解析器
│       │   ├── entity/              # 实体类
│       │   │   ├── User.java / Address.java / Category.java
│       │   │   ├── Product.java / Cart.java
│       │   │   └── Order.java / OrderItem.java
│       │   ├── dto/                 # 请求DTO
│       │   │   ├── LoginDTO.java / RegisterDTO.java
│       │   │   ├── ProductQueryDTO.java / CartItemDTO.java
│       │   │   └── OrderCreateDTO.java
│       │   ├── vo/                  # 响应VO
│       │   │   ├── UserVO.java / ProductVO.java
│       │   │   ├── CartVO.java / OrderVO.java
│       │   ├── mapper/              # MyBatis Mapper
│       │   ├── service/             # 业务接口
│       │   │   └── impl/            # 业务实现
│       │   └── controller/          # REST控制器
│       │       ├── UserController.java      # 用户API
│       │       ├── ProductController.java   # 商品API
│       │       ├── CartController.java      # 购物车API
│       │       ├── OrderController.java     # 订单API
│       │       ├── AdminController.java     # 管理后台API
│       │       └── CategoryController.java  # 分类API
│       └── resources/
│           ├── application.yml      # 主配置文件
│           └── sql/schema.sql       # 数据库建表脚本
│
├── frontend/                        # Vue 3 前端
│   ├── package.json                 # NPM 配置
│   ├── vite.config.js               # Vite 配置
│   ├── index.html                   # HTML 入口
│   ├── nginx.conf                   # Nginx配置（Docker）
│   ├── Dockerfile                   # 前端镜像
│   └── src/
│       ├── main.js                  # 应用入口
│       ├── App.vue                  # 根组件
│       ├── assets/global.css        # 全局样式
│       ├── router/index.js          # Vue Router（路由懒加载）
│       ├── store/                   # Pinia 状态管理
│       │   ├── user.js              # 用户状态（持久化）
│       │   └── cart.js              # 购物车状态
│       ├── api/                     # API 请求层
│       │   ├── request.js           # Axios 封装（拦截器）
│       │   ├── user.js / product.js
│       │   ├── cart.js / order.js
│       ├── views/                   # 页面组件
│       │   ├── Home.vue             # 首页
│       │   ├── ProductList.vue      # 商品列表（筛选/排序/分页）
│       │   ├── ProductDetail.vue    # 商品详情
│       │   ├── Cart.vue             # 购物车
│       │   ├── Login.vue            # 登录
│       │   ├── Register.vue         # 注册
│       │   ├── UserCenter.vue       # 个人中心
│       │   ├── Checkout.vue         # 结算页
│       │   └── OrderList.vue        # 订单列表
│       ├── components/              # 公共组件
│       │   ├── Navbar.vue           # 导航栏
│       │   ├── ProductCard.vue      # 商品卡片
│       │   ├── SkeletonLoader.vue   # 骨架屏
│       │   └── Pagination.vue       # 分页器
│       └── utils/
│           ├── auth.js              # Token 工具
│           └── validators.js        # 表单校验规则
```

---

## 🔌 API 接口文档

### 用户模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/user/register` | 用户注册 | ❌ |
| POST | `/api/user/login` | 用户登录 | ❌ |
| GET | `/api/user/profile` | 获取个人信息 | ✅ |
| POST | `/api/user/refresh-token` | 刷新Token | ❌ |
| PUT | `/api/user/switch-role` | 切换身份 | ✅ |

### 商品模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/product/list` | 商品列表（分页+筛选） | ❌ |
| GET | `/api/product/detail/{id}` | 商品详情 | ❌ |
| POST | `/api/product/publish` | 发布商品 | ✅(卖家) |
| PUT | `/api/product/{id}` | 更新商品 | ✅(卖家) |
| PUT | `/api/product/{id}/off-shelf` | 下架商品 | ✅(卖家) |

### 购物车模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/cart/add` | 添加商品 | ✅ |
| GET | `/api/cart/list` | 购物车列表 | ✅ |
| PUT | `/api/cart/quantity` | 修改数量 | ✅ |
| PUT | `/api/cart/selected` | 修改选中 | ✅ |
| DELETE | `/api/cart/{productId}` | 移除商品 | ✅ |

### 订单模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/order/create` | 创建订单 | ✅ |
| POST | `/api/order/{id}/pay` | 模拟支付 | ✅ |
| POST | `/api/order/{id}/cancel` | 取消订单 | ✅ |
| POST | `/api/order/{id}/confirm` | 确认收货 | ✅ |
| GET | `/api/order/{id}` | 订单详情 | ✅ |
| GET | `/api/order/list` | 订单列表 | ✅ |

---

## 🗄️ 数据库设计

### ER关系

```
user (1) ──── (N) address      用户 → 收货地址
user (1) ──── (N) product      卖家 → 商品
user (1) ──── (N) cart         用户 → 购物车
user (1) ──── (N) orders       用户 → 订单
orders (1) ──── (N) order_item 订单 → 订单详情
product (1) ──── (N) order_item 商品 → 订单详情
```

### 核心表

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `user` | 用户表 | username, password, role, status |
| `address` | 地址表 | user_id, receiver_name, phone |
| `category` | 分类表 | name, parent_id |
| `product` | 商品表 | seller_id, title, price, status, stock, FULLTEXT索引 |
| `cart` | 购物车表 | user_id, product_id, quantity |
| `orders` | 订单表 | order_no, user_id, status, pay_amount |
| `order_item` | 订单详情表 | order_id, product_id, price(快照) |
| `favorite` | 收藏表 | user_id, product_id |
| `seckill` | 秒杀表 | product_id, seckill_price, stock |

---

## ⚙️ 核心设计亮点

### 1. 分布式锁防超卖（订单创建）
```java
// Redisson 分布式锁
RLock lock = redissonClient.getLock("product:lock:" + productId);
lock.tryLock(3, 10, TimeUnit.SECONDS);
// 原子扣减库存
productMapper.deductStock(productId, quantity);
```

### 2. 购物车二级缓存
- **Redis**：一级缓存，读写分离，TTL 7天
- **MySQL**：持久化兜底
- **合并策略**：未登录购物车在登录后自动合并

### 3. JWT 双Token续期
- Access Token：24小时有效期
- Refresh Token：7天有效期
- 自动续期：Access Token过期前1小时内自动刷新

### 4. 统一异常处理
- `GlobalExceptionHandler` 全局拦截
- 业务异常统一 `Result` 响应
- 参数校验自动转换友好提示

### 5. 安全防护
- Spring Security + JWT 无状态认证
- BCrypt 密码加密
- CORS 跨域配置
- XSS/CSRF 防护
- SQL注入防护（MyBatis-Plus参数化查询）

---

## 🔧 扩展预留

| 功能 | 状态 | 说明 |
|------|------|------|
| 积分系统 | 预留 | `user.points` 字段 + 积分服务接口 |
| 秒杀活动 | 预留 | `seckill` 表 + 秒杀接口 |
| Elasticsearch搜索 | 预留 | 替代MySQL FULLTEXT索引 |
| 微信/支付宝支付 | 预留 | 对接SDK替换模拟支付 |
| WebSocket实时通知 | 预留 | 订单状态变更推送 |

---

## 📝 许可证

本项目仅用于学习和实验目的。

---

**🐟 转鱼宝猫 — 让闲置流转，让美好延续**
