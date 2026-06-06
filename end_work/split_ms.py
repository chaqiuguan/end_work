"""
单体 → 微服务拆分脚本
"""
import os, shutil

BASE = 'd:/NEUwork/web/end_work'
SRC = f'{BASE}/backend/src/main/java/com/zhuanbaomao'
MS = f'{BASE}/microservices'

# 模块划分
MODULES = {
    'user-service': {
        'package': 'user',
        'port': 8081,
        'controllers': ['UserController', 'AddressController', 'FavoriteController', 'FeedbackController'],
        'services': ['UserService', 'UserServiceImpl', 'EmailService', 'EmailServiceImpl', 'VerificationCodeService'],
        'mappers': ['UserMapper', 'AddressMapper', 'FavoriteMapper', 'FeedbackMapper'],
        'entities': ['User', 'Address', 'Favorite', 'Feedback'],
        'dto': ['LoginDTO', 'RegisterDTO'],
        'vo': ['UserVO'],
        'db': 'zhuanbaomao_user',
    },
    'product-service': {
        'package': 'product',
        'port': 8082,
        'controllers': ['ProductController', 'CategoryController', 'BannerController', 'AnnouncementController', 'ReviewController', 'DashboardController'],
        'services': ['ProductService', 'ProductServiceImpl'],
        'mappers': ['ProductMapper', 'CategoryMapper', 'BannerMapper', 'AnnouncementMapper', 'ReviewMapper'],
        'entities': ['Product', 'Category', 'Banner', 'Announcement', 'Review'],
        'dto': ['ProductQueryDTO'],
        'vo': ['ProductVO'],
        'db': 'zhuanbaomao_product',
    },
    'order-service': {
        'package': 'order',
        'port': 8083,
        'controllers': ['OrderController', 'CartController', 'CouponController'],
        'services': ['OrderService', 'OrderServiceImpl', 'CartService', 'CartServiceImpl'],
        'mappers': ['OrderMapper', 'OrderItemMapper', 'CartMapper', 'CouponMapper', 'UserCouponMapper'],
        'entities': ['Order', 'OrderItem', 'Cart', 'Coupon', 'UserCoupon'],
        'dto': ['CartItemDTO', 'OrderCreateDTO'],
        'vo': ['CartVO', 'OrderVO'],
        'db': 'zhuanbaomao_order',
    },
}

# ===== 1. 创建 common 模块 =====
def create_common():
    common_src = f'{MS}/common/src/main/java/com/zhuanbaomao/common'
    os.makedirs(common_src, exist_ok=True)

    # Copy shared files
    for f in ['Result.java', 'ResultCode.java', 'PageResult.java', 'BusinessException.java']:
        src = f'{SRC}/common/{f}' if 'Business' not in f else f'{SRC}/config/BusinessException.java'
        dst = f'{common_src}/{f}'
        if os.path.exists(src):
            shutil.copy2(src, dst)

    # Copy security
    sec_dst = f'{MS}/common/src/main/java/com/zhuanbaomao/security'
    os.makedirs(sec_dst, exist_ok=True)
    for f in os.listdir(f'{SRC}/security'):
        shutil.copy2(f'{SRC}/security/{f}', f'{sec_dst}/{f}')

    # Copy config (shared)
    cfg_dst = f'{MS}/common/src/main/java/com/zhuanbaomao/config'
    os.makedirs(cfg_dst, exist_ok=True)
    shared_configs = ['CorsConfig', 'GlobalExceptionHandler', 'SecurityConfig', 'WebSecurityConfig',
                      'JwtAuthenticationFilter', 'JwtTokenProvider', 'CurrentUser', 'CurrentUserResolver',
                      'WebMvcConfig', 'BusinessException']
    for f in shared_configs:
        # Try different locations
        for loc in [f'{SRC}/config/{f}', f'{SRC}/security/{f}']:
            name = f if not '/' in f else f.split('/')[-1]
            path = f'{loc}.java'
            if os.path.exists(path):
                dst_path = f'{cfg_dst}/{name}.java' if not name.endswith('.java') else f'{cfg_dst}/{name}'
                shutil.copy2(path, dst_path)

    # common module pom.xml
    pom = '''<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent><groupId>com.zhuanbaomao</groupId><artifactId>zhuanbaomao-microservices</artifactId><version>2.0.0</version></parent>
    <artifactId>common</artifactId>
    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
        <dependency><groupId>com.baomidou</groupId><artifactId>mybatis-plus-boot-starter</artifactId><version>3.5.3.1</version></dependency>
        <dependency><groupId>mysql</groupId><artifactId>mysql-connector-java</artifactId></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt</artifactId><version>0.9.1</version></dependency>
        <dependency><groupId>cn.hutool</groupId><artifactId>hutool-all</artifactId><version>5.8.23</version></dependency>
    </dependencies>
</project>'''
    with open(f'{MS}/common/pom.xml', 'w') as f: f.write(pom)
    print('common 模块创建完成')

# ===== 2. 创建 gateway 模块 =====
def create_gateway():
    gw_src = f'{MS}/gateway/src/main/java/com/zhuanbaomao/gateway'
    os.makedirs(gw_src, exist_ok=True)

    # GatewayApplication.java
    with open(f'{gw_src}/GatewayApplication.java', 'w', encoding='utf-8') as f:
        f.write('''package com.zhuanbaomao.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/api/user/**", "/api/address/**", "/api/favorite/**", "/api/feedback/**")
                .uri("http://user-service:8081"))
            .route("product-service", r -> r.path("/api/product/**", "/api/category/**", "/api/banner/**", "/api/announcement/**", "/api/review/**", "/api/dashboard/**")
                .uri("http://product-service:8082"))
            .route("order-service", r -> r.path("/api/order/**", "/api/cart/**", "/api/coupon/**")
                .uri("http://order-service:8083"))
            .build();
    }

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
''')

    # Gateway application.yml
    with open(f'{MS}/gateway/src/main/resources/application.yml', 'w', encoding='utf-8') as f:
        f.write('''server:
  port: 8080
  servlet:
    context-path: /api
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
''')

    # Gateway pom.xml
    pom = '''<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent><groupId>com.zhuanbaomao</groupId><artifactId>zhuanbaomao-microservices</artifactId><version>2.0.0</version></parent>
    <artifactId>gateway</artifactId>
    <dependencies>
        <dependency><groupId>org.springframework.cloud</groupId><artifactId>spring-cloud-starter-gateway</artifactId></dependency>
    </dependencies>
</project>'''
    with open(f'{MS}/gateway/pom.xml', 'w') as f: f.write(pom)

    # Dockerfile
    with open(f'{MS}/gateway/Dockerfile', 'w') as f:
        f.write('''FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/gateway-2.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
''')
    print('gateway 模块创建完成')

# ===== 3. 创建业务微服务 =====
def create_service(name, config):
    pkg = config['package']
    svc_src = f'{MS}/{name}/src/main/java/com/zhuanbaomao/{pkg}'
    os.makedirs(svc_src, exist_ok=True)

    # Application.java
    app_name = name.replace('-', ' ').title().replace(' ', '')
    with open(f'{svc_src}/{app_name}Application.java', 'w', encoding='utf-8') as f:
        f.write(f'''package com.zhuanbaomao.{pkg};

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {{"com.zhuanbaomao"}})
@MapperScan("com.zhuanbaomao.{pkg}.mapper")
public class {app_name}Application {{
    public static void main(String[] args) {{
        SpringApplication.run({app_name}Application.class, args);
    }}
}}
''')

    # application.yml
    with open(f'{MS}/{name}/src/main/resources/application.yml', 'w', encoding='utf-8') as f:
        f.write(f'''server:
  port: {config['port']}
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/{config['db']}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

mybatis-plus:
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
    banner: false
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: ZhuanBaoMao2024SecretKeyForJWTTokenGenerationAndValidation
  expiration: 86400000
  refresh-expiration: 604800000
  header: Authorization
  token-prefix: "Bearer "
''')

    # pom.xml
    pom = f'''<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent><groupId>com.zhuanbaomao</groupId><artifactId>zhuanbaomao-microservices</artifactId><version>2.0.0</version></parent>
    <artifactId>{name}</artifactId>
    <dependencies>
        <dependency><groupId>com.zhuanbaomao</groupId><artifactId>common</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    </dependencies>
    <build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin></plugins></build>
</project>'''
    with open(f'{MS}/{name}/pom.xml', 'w') as f: f.write(pom)

    # Dockerfile
    with open(f'{MS}/{name}/Dockerfile', 'w') as f:
        f.write(f'''FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/{name}-2.0.0.jar app.jar
EXPOSE {config['port']}
ENTRYPOINT ["java","-jar","app.jar"]
''')

    # Copy controller, service, entity, mapper files
    for ctrl in config['controllers']:
        src = f'{SRC}/controller/{ctrl}.java'
        dst = f'{svc_src}/controller/{ctrl}.java'
        if os.path.exists(src):
            content = open(src, encoding='utf-8').read()
            content = content.replace('com.zhuanbaomao.', f'com.zhuanbaomao.{pkg}.')
            content = content.replace(f'com.zhuanbaomao.{pkg}.config', 'com.zhuanbaomao.config')
            content = content.replace(f'com.zhuanbaomao.{pkg}.security', 'com.zhuanbaomao.security')
            content = content.replace(f'com.zhuanbaomao.{pkg}.common', 'com.zhuanbaomao.common')
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            open(dst, 'w', encoding='utf-8').write(content)

    for serv in config['services']:
        is_impl = 'Impl' in serv
        src_dir = f'{SRC}/service/{"impl" if is_impl else ""}'
        src = f'{src_dir}/{serv}.java'
        dst_dir = f'{svc_src}/service/{"impl" if is_impl else ""}'
        if os.path.exists(src):
            content = open(src, encoding='utf-8').read()
            content = content.replace('com.zhuanbaomao.', f'com.zhuanbaomao.{pkg}.')
            content = content.replace(f'com.zhuanbaomao.{pkg}.config', 'com.zhuanbaomao.config')
            content = content.replace(f'com.zhuanbaomao.{pkg}.security', 'com.zhuanbaomao.security')
            content = content.replace(f'com.zhuanbaomao.{pkg}.common', 'com.zhuanbaomao.common')
            os.makedirs(dst_dir, exist_ok=True)
            open(os.path.join(dst_dir, f'{serv}.java'), 'w', encoding='utf-8').write(content)

    for mapper in config['mappers']:
        src = f'{SRC}/mapper/{mapper}.java'
        dst = f'{svc_src}/mapper/{mapper}.java'
        if os.path.exists(src):
            content = open(src, encoding='utf-8').read()
            content = content.replace('com.zhuanbaomao.', f'com.zhuanbaomao.{pkg}.')
            content = content.replace(f'com.zhuanbaomao.{pkg}.config', 'com.zhuanbaomao.config')
            content = content.replace(f'com.zhuanbaomao.{pkg}.common', 'com.zhuanbaomao.common')
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            open(dst, 'w', encoding='utf-8').write(content)

    for entity in config['entities']:
        src = f'{SRC}/entity/{entity}.java'
        dst = f'{svc_src}/entity/{entity}.java'
        if os.path.exists(src):
            content = open(src, encoding='utf-8').read()
            content = content.replace('com.zhuanbaomao.', f'com.zhuanbaomao.{pkg}.')
            content = content.replace(f'com.zhuanbaomao.{pkg}.config', 'com.zhuanbaomao.config')
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            open(dst, 'w', encoding='utf-8').write(content)

    for d in config.get('dto', []):
        src = f'{SRC}/dto/{d}.java'
        dst = f'{svc_src}/dto/{d}.java'
        if os.path.exists(src):
            content = open(src, encoding='utf-8').read()
            content = content.replace('com.zhuanbaomao.', f'com.zhuanbaomao.{pkg}.')
            content = content.replace(f'com.zhuanbaomao.{pkg}.config', 'com.zhuanbaomao.config')
            content = content.replace(f'com.zhuanbaomao.{pkg}.common', 'com.zhuanbaomao.common')
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            open(dst, 'w', encoding='utf-8').write(content)

    for v in config.get('vo', []):
        src = f'{SRC}/vo/{v}.java'
        dst = f'{svc_src}/vo/{v}.java'
        if os.path.exists(src):
            content = open(src, encoding='utf-8').read()
            content = content.replace('com.zhuanbaomao.', f'com.zhuanbaomao.{pkg}.')
            content = content.replace(f'com.zhuanbaomao.{pkg}.config', 'com.zhuanbaomao.config')
            content = content.replace(f'com.zhuanbaomao.{pkg}.common', 'com.zhuanbaomao.common')
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            open(dst, 'w', encoding='utf-8').write(content)

    print(f'{name} 模块创建完成')

# Run
create_common()
create_gateway()
for name, config in MODULES.items():
    create_service(name, config)

print('\n微服务拆分完成！')
print('模块: common, gateway, user-service(8081), product-service(8082), order-service(8083)')
