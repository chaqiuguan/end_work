# 转鱼宝猫 (ZhuanBaoMao) — 二手电商平台

## 项目概览
- **前端**: Vue 3 (Composition API) + Vite + Element Plus + Pinia + ECharts + Axios
- **后端**: Spring Boot 2.7 + MyBatis Plus + MySQL + Redis + JWT
- **部署**: Docker Compose (MySQL + Redis + 前端 Nginx + 后端 Java)

## 目录结构

```
end_work/
├── frontend/                  # Vue 3 前端
│   ├── src/
│   │   ├── api/               # Axios API 模块 (request.js 封装，base /api)
│   │   ├── components/        # 可复用组件 (ProductCard, Navbar, Pagination, SkeletonLoader)
│   │   ├── views/             # 路由页面 (Home, ProductList, ProductDetail, Cart, Checkout, ...)
│   │   ├── store/             # Pinia stores (user, cart, index)
│   │   ├── router/            # Vue Router 配置 (懒加载，路由守卫)
│   │   ├── utils/             # 工具函数 (auth.js, validators.js)
│   │   └── main.js            # 入口文件
│   ├── vite.config.js
│   └── package.json
├── backend/                   # Spring Boot 后端
│   └── src/main/java/com/zhuanbaomao/
│       ├── controller/        # REST Controllers (14个)
│       ├── service/           # Service 层 (接口 + impl)
│       ├── mapper/            # MyBatis Plus Mappers
│       ├── entity/            # 数据库实体
│       ├── dto/               # 请求 DTOs
│       ├── vo/                # 响应 VOs
│       ├── config/            # Spring 配置 (CORS, Security, MyBatis, Redis)
│       ├── security/          # JWT 认证 (JwtTokenProvider, CurrentUser)
│       └── common/            # 通用类 (Result, PageResult, ResultCode)
├── docker-compose.yml         # 一键部署
└── .claude/
    ├── workflows/             # 自定义工作流 (见下方)
    └── settings.json          # 权限配置
```

## 代码规范

### 前端
- **组件**: `<template>` → `<script setup>` → `<style scoped>`
- **CSS**: 使用 CSS 自定义属性 (`var(--radius)`, `var(--shadow)`, `var(--text-secondary)`)
- **API 调用**: 通过 `@/api/request.js` (base `/api`, 自动 token, 统一错误处理)
- **路由**: 懒加载 `() => import(...)`, meta.title + meta.requiresAuth
- **Store**: Pinia `defineStore` with `pinia-plugin-persistedstate`

### 后端
- **URL**: `/api/{entity}/{action}`
- **响应**: `Result<T>` (code=200 成功, code≠200 失败)
- **分页**: `PageResult<T>` (records, total, page, size)
- **认证**: JWT Bearer token, `@CurrentUser` 参数解析

## 自定义 Workflow

| Workflow | 用途 | 用法示例 |
|---|---|---|
| `vue-component` | 生成 Vue 3 组件 | `/workflow vue-component 商品评论卡片组件` |
| `api-check` | 前后端 API 一致性检查 | `/workflow api-check` |
| `frontend-review` | 前端代码质量审查 | `/workflow frontend-review` |
| `scaffold-feature` | 全栈功能脚手架 | `/workflow scaffold-feature name=Review desc=商品评价` |

## 常用命令

```bash
# 启动全部服务
docker-compose up -d

# 仅启动前端开发服务器
cd frontend && npm run dev

# 仅启动后端
cd backend && mvn spring-boot:run

# 前端 Lint
cd frontend && npm run lint

# 数据库管理 (需要先启动 MySQL 容器)
docker exec -it mysql mysql -u root -p123456 zhuanbaomao
```
