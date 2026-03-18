-- 交易相关表
-- Flyway 迁移脚本 V1.0.5

-- 订单评价表
CREATE TABLE IF NOT EXISTS `t_order_evaluation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `evaluator_id` bigint NOT NULL COMMENT '评价者ID',
    `evaluated_id` bigint NOT NULL COMMENT '被评价者ID',
    `score` int NOT NULL COMMENT '评分（1-5星）',
    `evaluation_tag` varchar(100) DEFAULT NULL COMMENT '评价标签',
    `evaluation_content` varchar(500) DEFAULT NULL COMMENT '评价内容',
    `evaluation_image_url` varchar(500) DEFAULT NULL COMMENT '评价图片地址',
    `reply_content` varchar(500) DEFAULT NULL COMMENT '卖家回复内容',
    `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_evaluator_id` (`evaluator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';

-- 交易纠纷表
CREATE TABLE IF NOT EXISTS `t_trade_dispute` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `initiator_id` bigint NOT NULL COMMENT '发起者ID',
    `accused_id` bigint NOT NULL COMMENT '被投诉者ID',
    `dispute_type` varchar(50) NOT NULL COMMENT '纠纷类型（PRODUCT_NOT_MATCH/NO_DELIVERY/OTHER）',
    `evidence_url` varchar(500) DEFAULT NULL COMMENT '举证材料地址',
    `dispute_status` varchar(20) DEFAULT 'PENDING' COMMENT '状态（PENDING/PROCESSING/RESOLVED）',
    `process_result` varchar(255) DEFAULT NULL COMMENT '处理结果',
    `admin_id` bigint DEFAULT NULL COMMENT '处理管理员ID',
    `process_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易纠纷表';