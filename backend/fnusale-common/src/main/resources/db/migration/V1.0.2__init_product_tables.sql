-- 商品相关表
-- Flyway 迁移脚本 V1.0.2

-- 商品品类表
CREATE TABLE IF NOT EXISTS `t_product_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_name` varchar(50) NOT NULL COMMENT '品类名称',
    `parent_category_id` bigint DEFAULT NULL COMMENT '父品类ID',
    `ai_mapping_value` varchar(100) DEFAULT NULL COMMENT 'AI分类映射值',
    `enable_status` tinyint DEFAULT 1 COMMENT '启用状态（0-禁用, 1-启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_category_id` (`parent_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品品类表';

-- 商品基础表
CREATE TABLE IF NOT EXISTS `t_product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '发布者ID',
    `product_name` varchar(100) NOT NULL COMMENT '商品名称',
    `category_id` bigint NOT NULL COMMENT '品类ID',
    `new_degree` varchar(20) NOT NULL COMMENT '新旧程度（NEW/90_NEW/80_NEW/70_NEW/OLD）',
    `price` decimal(10,2) NOT NULL COMMENT '售价（元）',
    `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价（元）',
    `product_desc` varchar(500) DEFAULT NULL COMMENT '商品描述',
    `is_seckill` tinyint DEFAULT 0 COMMENT '是否秒杀商品（0-否, 1-是）',
    `seckill_stock` int DEFAULT NULL COMMENT '秒杀库存',
    `pick_point_id` bigint DEFAULT NULL COMMENT '自提点ID',
    `longitude` decimal(10,6) DEFAULT NULL COMMENT '发布时定位经度',
    `latitude` decimal(10,6) DEFAULT NULL COMMENT '发布时定位纬度',
    `product_status` varchar(20) NOT NULL COMMENT '状态（DRAFT/ON_SHELF/SOLD_OUT/OFF_SHELF/ILLEGAL）',
    `illegal_reason` varchar(200) DEFAULT NULL COMMENT '违规原因',
    `ai_category_result` varchar(500) DEFAULT NULL COMMENT 'AI分类结果',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除标记（0-未删除, 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_product_status` (`product_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品基础表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS `t_product_image` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `image_url` varchar(500) NOT NULL COMMENT '图片地址',
    `is_main_image` tinyint DEFAULT 0 COMMENT '是否主图（0-否, 1-是）',
    `sort` int DEFAULT 0 COMMENT '排序',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- 商品审核记录表
CREATE TABLE IF NOT EXISTS `t_product_audit` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `admin_id` bigint DEFAULT NULL COMMENT '审核管理员ID',
    `audit_result` varchar(20) DEFAULT NULL COMMENT '审核结果（PASS/REJECT）',
    `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
    `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品审核记录表';

-- 插入默认商品品类数据
INSERT IGNORE INTO t_product_category (id, category_name, parent_category_id, ai_mapping_value, enable_status) VALUES
(1, '教材书籍', NULL, 'books,textbook', 1),
(2, '电子产品', NULL, 'electronics,digital', 1),
(3, '生活用品', NULL, 'daily,lifestyle', 1),
(4, '体育器材', NULL, 'sports,equipment', 1),
(5, '服装鞋帽', NULL, 'clothing,fashion', 1),
(6, '其他', NULL, 'other', 1);
