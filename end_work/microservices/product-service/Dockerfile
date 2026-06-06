# 第一阶段：Maven 构建
FROM maven:3.8-openjdk-11 AS builder
WORKDIR /build

# 复制 common 模块
COPY common/pom.xml common/pom.xml
COPY common/src common/src

# 先安装 common 到本地仓库
RUN cd common && mvn install -DskipTests -q

# 复制当前服务
COPY product-service/pom.xml product-service/pom.xml
COPY product-service/src product-service/src

# 构建当前服务
RUN cd product-service && mvn package -DskipTests -q

# 第二阶段：运行
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /build/product-service/target/product-service-2.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
