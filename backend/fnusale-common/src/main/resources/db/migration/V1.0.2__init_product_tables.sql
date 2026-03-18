-- 商品相关表
-- Flyway 迁移脚本 V1.0.2

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