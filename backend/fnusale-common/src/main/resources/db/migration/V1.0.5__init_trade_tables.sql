-- 交易相关表
-- Flyway 迁移脚本 V1.0.5

-- 订单基础表
CREATE TABLE IF NOT EXISTS `t_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` varchar(64) NOT NULL COMMENT '订单编号',
    `user_id` bigint NOT NULL COMMENT '买家ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `product_price` decimal(10,2) NOT NULL COMMENT '商品原价（元）',
    `coupon_id` bigint DEFAULT NULL COMMENT '优惠券ID',
    `coupon_deduct_amount` decimal(10,2) DEFAULT 0.00 COMMENT '优惠券抵扣金额（元）',
    `actual_pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额（元）',
    `pick_point_id` bigint DEFAULT NULL COMMENT '自提点ID',
    `pay_type` varchar(20) DEFAULT NULL COMMENT '支付方式：WECHAT/ALIPAY/CAMPUS_CARD',
    `pay_status` varchar(20) DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID/PAID/REFUNDED',
    `order_status` varchar(20) DEFAULT 'UNPAID' COMMENT '订单状态：UNPAID/WAIT_PICK/SUCCESS/CANCEL',
    `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
    `ready_time` datetime DEFAULT NULL COMMENT '商品备好时间',
    `extend_receive_days` int DEFAULT 0 COMMENT '延长收货天数',
    `success_time` datetime DEFAULT NULL COMMENT '成交时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除标记：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单基础表';

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
    `is_anonymous` tinyint DEFAULT 0 COMMENT '是否匿名评价（0-否, 1-是）',
    `reply_content` varchar(500) DEFAULT NULL COMMENT '卖家回复内容',
    `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
    `append_content` varchar(500) DEFAULT NULL COMMENT '追加评价内容',
    `append_image_url` varchar(500) DEFAULT NULL COMMENT '追加评价图片地址',
    `append_time` datetime DEFAULT NULL COMMENT '追加评价时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_evaluator_id` (`evaluator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';

-- 评价举报表
CREATE TABLE IF NOT EXISTS `t_evaluation_report` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `evaluation_id` bigint NOT NULL COMMENT '评价ID',
    `reporter_id` bigint NOT NULL COMMENT '举报者ID',
    `report_reason` varchar(255) DEFAULT NULL COMMENT '举报原因',
    `report_desc` varchar(500) DEFAULT NULL COMMENT '举报说明',
    `status` varchar(20) DEFAULT 'PENDING' COMMENT '处理状态（PENDING-待处理, APPROVED-已通过, REJECTED-已拒绝）',
    `handle_result` varchar(255) DEFAULT NULL COMMENT '处理结果',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_evaluation_id` (`evaluation_id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价举报表';

-- 交易纠纷表
CREATE TABLE IF NOT EXISTS `t_trade_dispute` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `initiator_id` bigint NOT NULL COMMENT '发起者ID',
    `accused_id` bigint NOT NULL COMMENT '被投诉者ID',
    `dispute_type` varchar(50) NOT NULL COMMENT '纠纷类型：PRODUCT_NOT_MATCH/NO_DELIVERY/OTHER',
    `evidence_url` varchar(500) DEFAULT NULL COMMENT '举证材料地址',
    `dispute_status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/RESOLVED',
    `process_result` varchar(255) DEFAULT NULL COMMENT '处理结果',
    `admin_id` bigint DEFAULT NULL COMMENT '处理管理员ID',
    `process_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易纠纷表';
