-- 用户相关表
-- Flyway 迁移脚本 V1.0.1

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `student_id` varchar(50) DEFAULT NULL COMMENT '学号',
    `school_name` varchar(100) DEFAULT NULL COMMENT '学校名称',
    `status` tinyint DEFAULT 0 COMMENT '状态: 0-正常, 1-禁用',
    `is_verified` tinyint DEFAULT 0 COMMENT '是否认证: 0-未认证, 1-已认证',
    `credit_score` int DEFAULT 100 COMMENT '信誉分数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS `product_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `parent_id` bigint DEFAULT 0 COMMENT '父分类ID',
    `sort` int DEFAULT 0 COMMENT '排序',
    `icon` varchar(255) DEFAULT NULL COMMENT '图标',
    `status` tinyint DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `user_id` bigint NOT NULL COMMENT '发布用户ID',
    `category_id` bigint NOT NULL COMMENT '分类ID',
    `title` varchar(200) NOT NULL COMMENT '商品标题',
    `description` text COMMENT '商品描述',
    `price` decimal(10,2) NOT NULL COMMENT '价格',
    `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
    `condition_level` tinyint DEFAULT 5 COMMENT '新旧程度: 1-10',
    `images` json COMMENT '商品图片JSON数组',
    `location` varchar(200) DEFAULT NULL COMMENT '交易地点',
    `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
    `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
    `view_count` int DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` int DEFAULT 0 COMMENT '收藏次数',
    `status` tinyint DEFAULT 0 COMMENT '状态: 0-待审核, 1-上架, 2-已售, 3-下架, 4-违规',
    `is_seckill` tinyint DEFAULT 0 COMMENT '是否秒杀商品',
    `seckill_price` decimal(10,2) DEFAULT NULL COMMENT '秒杀价',
    `seckill_stock` int DEFAULT 0 COMMENT '秒杀库存',
    `seckill_start_time` datetime DEFAULT NULL COMMENT '秒杀开始时间',
    `seckill_end_time` datetime DEFAULT NULL COMMENT '秒杀结束时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS `trade_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` varchar(64) NOT NULL COMMENT '订单编号',
    `buyer_id` bigint NOT NULL COMMENT '买家ID',
    `seller_id` bigint NOT NULL COMMENT '卖家ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `product_title` varchar(200) NOT NULL COMMENT '商品标题',
    `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
    `price` decimal(10,2) NOT NULL COMMENT '成交价格',
    `pickup_location` varchar(200) DEFAULT NULL COMMENT '自提地点',
    `coupon_id` bigint DEFAULT NULL COMMENT '优惠券ID',
    `coupon_amount` decimal(10,2) DEFAULT 0 COMMENT '优惠券金额',
    `actual_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
    `status` tinyint DEFAULT 0 COMMENT '状态: 0-待付款, 1-待自提, 2-已完成, 3-已取消, 4-已退款',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
    `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 用户地址表
CREATE TABLE IF NOT EXISTS `t_user_address` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `address_type` varchar(20) DEFAULT NULL COMMENT '地址类型（PICK_POINT/CUSTOM）',
    `pick_point_id` bigint DEFAULT NULL COMMENT '自提点ID',
    `custom_address` varchar(255) DEFAULT NULL COMMENT '自定义详细地址',
    `longitude` decimal(10,6) DEFAULT NULL COMMENT '地址经度',
    `latitude` decimal(10,6) DEFAULT NULL COMMENT '地址纬度',
    `is_default` tinyint DEFAULT 0 COMMENT '是否默认地址（0-否, 1-是）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- 校园自提点表
CREATE TABLE IF NOT EXISTS `t_campus_pick_point` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `pick_point_name` varchar(100) NOT NULL COMMENT '自提点名称',
    `campus_area` varchar(50) DEFAULT NULL COMMENT '所属校区',
    `detail_address` varchar(200) DEFAULT NULL COMMENT '详细位置',
    `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
    `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
    `enable_status` tinyint DEFAULT 1 COMMENT '启用状态（0-禁用, 1-启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园自提点表';

-- 插入默认商品分类
INSERT IGNORE INTO product_category (id, name, parent_id, sort, status) VALUES
(1, '教材书籍', 0, 1, 1),
(2, '电子产品', 0, 2, 1),
(3, '生活用品', 0, 3, 1),
(4, '体育器材', 0, 4, 1),
(5, '服装鞋帽', 0, 5, 1),
(6, '其他', 0, 99, 1);