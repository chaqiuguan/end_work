-- ============================================
-- 转鱼宝猫 (ZhuanYuBaoMao) 数据库初始化脚本
-- MySQL 8.0+
-- ============================================

CREATE DATABASE IF NOT EXISTS zhuanbaomao
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE zhuanbaomao;

-- 授权 Docker 用户
GRANT ALL PRIVILEGES ON zhuanbaomao.* TO 'zbm_user'@'%' IDENTIFIED BY 'zbm_pass_2024';
FLUSH PRIVILEGES;

-- ============================================
-- 1. 用户表 (user)
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`      VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    `password`      VARCHAR(255)    NOT NULL                 COMMENT '密码（BCrypt加密）',
    `phone`         VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    `email`         VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    `nickname`      VARCHAR(50)     DEFAULT NULL             COMMENT '昵称',
    `avatar`        VARCHAR(500)    DEFAULT NULL             COMMENT '头像URL',
    `role`          TINYINT         NOT NULL DEFAULT 0       COMMENT '角色: 0-普通用户, 1-卖家, 2-管理员',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态: 0-封禁, 1-正常',
    `points`        INT             NOT NULL DEFAULT 0       COMMENT '积分（预留扩展）',
    `last_login_at` DATETIME        DEFAULT NULL             COMMENT '最后登录时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 地址表 (address)
-- ============================================
CREATE TABLE IF NOT EXISTS `address` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '地址ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
    `receiver_name` VARCHAR(50)     NOT NULL                 COMMENT '收货人姓名',
    `phone`         VARCHAR(20)     NOT NULL                 COMMENT '收货人手机号',
    `province`      VARCHAR(50)     NOT NULL                 COMMENT '省份',
    `city`          VARCHAR(50)     NOT NULL                 COMMENT '城市',
    `district`      VARCHAR(50)     NOT NULL                 COMMENT '区/县',
    `detail`        VARCHAR(255)    NOT NULL                 COMMENT '详细地址',
    `is_default`    TINYINT         NOT NULL DEFAULT 0       COMMENT '是否默认: 0-否, 1-是',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ============================================
-- 3. 商品分类表 (category) —— 预留扩展
-- ============================================
CREATE TABLE IF NOT EXISTS `category` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    `name`          VARCHAR(50)     NOT NULL                 COMMENT '分类名称',
    `parent_id`     BIGINT          NOT NULL DEFAULT 0       COMMENT '父分类ID, 0为顶级',
    `sort_order`    INT             NOT NULL DEFAULT 0       COMMENT '排序',
    `icon`          VARCHAR(255)    DEFAULT NULL             COMMENT '分类图标',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ============================================
-- 4. 商品表 (product)
-- ============================================
CREATE TABLE IF NOT EXISTS `product` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '商品ID',
    `seller_id`     BIGINT          NOT NULL                 COMMENT '卖家用户ID',
    `category_id`   BIGINT          NOT NULL DEFAULT 0       COMMENT '分类ID',
    `title`         VARCHAR(200)    NOT NULL                 COMMENT '商品标题',
    `description`   TEXT            DEFAULT NULL             COMMENT '商品描述',
    `images`        JSON            DEFAULT NULL             COMMENT '商品图片URL列表（JSON数组）',
    `original_price` DECIMAL(10,2)  NOT NULL DEFAULT 0.00    COMMENT '原价',
    `price`         DECIMAL(10,2)   NOT NULL                 COMMENT '转闲价（售价）',
    `condition`     TINYINT         NOT NULL DEFAULT 0       COMMENT '成色: 0-全新, 1-几乎全新, 2-轻微使用, 3-明显使用',
    `status`        TINYINT         NOT NULL DEFAULT 0       COMMENT '状态: 0-待审核, 1-已上架, 2-已下架, 3-已售出',
    `stock`         INT             NOT NULL DEFAULT 1       COMMENT '库存数量',
    `view_count`    INT             NOT NULL DEFAULT 0       COMMENT '浏览量',
    `favorite_count` INT            NOT NULL DEFAULT 0       COMMENT '收藏数',
    `tags`          VARCHAR(500)    DEFAULT NULL             COMMENT '标签 (JSON数组)',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`),
    KEY `idx_title` (`title`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status_price` (`status`, `price`),
    FULLTEXT KEY `ft_title_description` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 5. 购物车表 (cart) —— MySQL持久化 + Redis缓存
-- ============================================
CREATE TABLE IF NOT EXISTS `cart` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '购物车记录ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
    `product_id`    BIGINT          NOT NULL                 COMMENT '商品ID',
    `quantity`      INT             NOT NULL DEFAULT 1       COMMENT '数量',
    `selected`      TINYINT         NOT NULL DEFAULT 1       COMMENT '是否选中: 0-否, 1-是',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================
-- 6. 订单表 (orders)
-- ============================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    `order_no`        VARCHAR(32)     NOT NULL                 COMMENT '订单号（雪花ID或UUID）',
    `user_id`         BIGINT          NOT NULL                 COMMENT '买家用户ID',
    `total_amount`    DECIMAL(10,2)   NOT NULL                 COMMENT '商品总金额',
    `freight`         DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '运费',
    `pay_amount`      DECIMAL(10,2)   NOT NULL                 COMMENT '实付金额',
    `pay_type`        TINYINT         DEFAULT NULL             COMMENT '支付方式: 0-模拟支付, 1-微信, 2-支付宝',
    `status`          TINYINT         NOT NULL DEFAULT 0       COMMENT '订单状态: 0-待付款, 1-待发货, 2-待收货, 3-已完成, 4-已取消',
    `receiver_name`   VARCHAR(50)     NOT NULL                 COMMENT '收货人姓名',
    `receiver_phone`  VARCHAR(20)     NOT NULL                 COMMENT '收货人手机号',
    `receiver_address` VARCHAR(500)   NOT NULL                 COMMENT '收货地址（快照）',
    `remark`          VARCHAR(500)    DEFAULT NULL             COMMENT '买家备注',
    `paid_at`         DATETIME        DEFAULT NULL             COMMENT '支付时间',
    `shipped_at`      DATETIME        DEFAULT NULL             COMMENT '发货时间',
    `received_at`     DATETIME        DEFAULT NULL             COMMENT '收货时间',
    `canceled_at`     DATETIME        DEFAULT NULL             COMMENT '取消时间',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 7. 订单详情表 (order_item)
-- ============================================
CREATE TABLE IF NOT EXISTS `order_item` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    `order_id`      BIGINT          NOT NULL                 COMMENT '订单ID',
    `order_no`      VARCHAR(32)     NOT NULL                 COMMENT '订单号',
    `product_id`    BIGINT          NOT NULL                 COMMENT '商品ID',
    `product_title` VARCHAR(200)    NOT NULL                 COMMENT '商品标题（快照）',
    `product_image` VARCHAR(500)    DEFAULT NULL             COMMENT '商品图片（快照）',
    `price`         DECIMAL(10,2)   NOT NULL                 COMMENT '商品单价（快照）',
    `quantity`      INT             NOT NULL DEFAULT 1       COMMENT '购买数量',
    `total_price`   DECIMAL(10,2)   NOT NULL                 COMMENT '小计金额',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- ============================================
-- 8. 商品收藏表 (favorite) —— 预留扩展
-- ============================================
CREATE TABLE IF NOT EXISTS `favorite` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '收藏ID',
    `user_id`       BIGINT      NOT NULL                 COMMENT '用户ID',
    `product_id`    BIGINT      NOT NULL                 COMMENT '商品ID',
    `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

-- ============================================
-- 9. 商品评价表 (review)
-- ============================================
CREATE TABLE IF NOT EXISTS `review` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '评价ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
    `product_id`    BIGINT          NOT NULL                 COMMENT '商品ID',
    `order_id`      BIGINT          DEFAULT NULL             COMMENT '关联订单ID',
    `content`       TEXT            NOT NULL                 COMMENT '评价内容',
    `images`        JSON            DEFAULT NULL             COMMENT '评价图片',
    `rating`        TINYINT         NOT NULL DEFAULT 5       COMMENT '评分 1-5',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- ============================================
-- 10. 秒杀活动表 (seckill) —— 预留扩展接口
-- ============================================
CREATE TABLE IF NOT EXISTS `seckill` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '秒杀活动ID',
    `product_id`    BIGINT          NOT NULL                 COMMENT '商品ID',
    `seckill_price` DECIMAL(10,2)   NOT NULL                 COMMENT '秒杀价',
    `stock`         INT             NOT NULL                 COMMENT '秒杀库存',
    `start_time`    DATETIME        NOT NULL                 COMMENT '秒杀开始时间',
    `end_time`      DATETIME        NOT NULL                 COMMENT '秒杀结束时间',
    `status`        TINYINT         NOT NULL DEFAULT 0       COMMENT '状态: 0-未开始, 1-进行中, 2-已结束',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_time` (`start_time`, `end_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- ============================================
-- 初始分类数据
-- ============================================
INSERT INTO `category` (`name`, `parent_id`, `sort_order`) VALUES
('数码电子', 0, 1),
('服饰鞋包', 0, 2),
('图书教材', 0, 3),
('家居生活', 0, 4),
('运动户外', 0, 5),
('美妆个护', 0, 6),
('母婴玩具', 0, 7),
('其他', 0, 8);
