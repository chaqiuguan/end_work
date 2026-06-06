# ============================================
# 转鱼宝猫 - 前端 Dockerfile
# 多阶段构建：Node.js 构建 → Nginx 运行
# ============================================

# 第一阶段：Node.js 构建
FROM node:20-alpine AS builder

WORKDIR /build
COPY package*.json ./
RUN npm ci --registry=https://registry.npmmirror.com

COPY . .
RUN npm run build

# 第二阶段：Nginx 运行
FROM nginx:1.25-alpine

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
