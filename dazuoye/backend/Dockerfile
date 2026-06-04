# ============================================
# 转鱼宝猫 - 后端 Dockerfile
# 多阶段构建：构建 → 运行
# ============================================

# 第一阶段：Maven 构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build
COPY pom.xml .
# 缓存依赖
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# 第二阶段：JRE 运行
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app
COPY --from=builder /build/target/zhuanbaomao.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
