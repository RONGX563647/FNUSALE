-- 营销相关表
-- Flyway 迁移脚本 V1.0.4

-- 用户行为表
CREATE TABLE IF NOT EXISTS `t_user_behavior` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `behavior_type` varchar(20) NOT NULL COMMENT '行为类型（BROWSE/COLLECT/LIKE）',
    `behavior_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_behavior_time` (`behavior_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS `t_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `coupon_name` varchar(100) NOT NULL COMMENT '优惠券名称',
    `coupon_type` varchar(20) NOT NULL COMMENT '类型（FULL_REDUCE/DIRECT_REDUCE/CATEGORY）',
    `full_amount` decimal(10,2) DEFAULT NULL COMMENT '满减金额',
    `reduce_amount` decimal(10,2) NOT NULL COMMENT '抵扣金额',
    `category_id` bigint DEFAULT NULL COMMENT '品类ID',
    `total_count` int NOT NULL DEFAULT 0 COMMENT '发放总数',
    `received_count` int DEFAULT 0 COMMENT '已领数量',
    `used_count` int DEFAULT 0 COMMENT '已用数量',
    `start_time` datetime NOT NULL COMMENT '有效期开始时间',
    `end_time` datetime NOT NULL COMMENT '有效期结束时间',
    `enable_status` tinyint DEFAULT 1 COMMENT '启用状态（0-禁用, 1-启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `t_user_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
    `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time` datetime DEFAULT NULL COMMENT '使用时间',
    `expire_time` datetime NOT NULL COMMENT '过期时间',
    `coupon_status` varchar(20) DEFAULT 'UNUSED' COMMENT '状态（UNUSED/USED/EXPIRED）',
    `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS `t_seckill_activity` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `activity_name` varchar(100) NOT NULL COMMENT '活动名称',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价格（元）',
    `total_stock` int NOT NULL COMMENT '秒杀总库存',
    `remain_stock` int NOT NULL COMMENT '剩余库存',
    `start_time` datetime NOT NULL COMMENT '活动开始时间',
    `end_time` datetime NOT NULL COMMENT '活动结束时间',
    `activity_status` varchar(20) DEFAULT 'NOT_START' COMMENT '状态（NOT_START/ON_GOING/END）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- 秒杀提醒表
CREATE TABLE IF NOT EXISTS `t_seckill_reminder` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `activity_id` bigint NOT NULL COMMENT '活动ID',
    `remind_time` datetime NOT NULL COMMENT '提醒时间',
    `is_reminded` tinyint DEFAULT 0 COMMENT '是否已提醒（0-否，1-是）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀提醒表';