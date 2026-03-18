-- 系统相关表
-- Flyway 迁移脚本 V1.0.7

-- 系统配置表
CREATE TABLE IF NOT EXISTS `t_system_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `config_key` varchar(100) NOT NULL COMMENT '配置键',
    `config_value` text COMMENT '配置值',
    `config_desc` varchar(255) DEFAULT NULL COMMENT '配置描述',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `admin_id` bigint DEFAULT NULL COMMENT '最后修改管理员ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS `t_system_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operate_user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
    `module_name` varchar(50) DEFAULT NULL COMMENT '操作模块',
    `operate_type` varchar(50) DEFAULT NULL COMMENT '操作类型',
    `operate_content` varchar(500) DEFAULT NULL COMMENT '操作内容',
    `ip_address` varchar(50) DEFAULT NULL COMMENT '操作IP地址',
    `device_info` varchar(255) DEFAULT NULL COMMENT '设备信息',
    `exception_info` text COMMENT '异常信息',
    `log_type` varchar(20) DEFAULT 'OPERATE' COMMENT '日志类型（OPERATE/EXCEPTION）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_operate_user_id` (`operate_user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 告警记录表
CREATE TABLE IF NOT EXISTS `t_alert_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `alert_module` varchar(50) NOT NULL COMMENT '告警模块',
    `alert_content` varchar(500) NOT NULL COMMENT '告警内容',
    `alert_level` varchar(20) DEFAULT 'NORMAL' COMMENT '告警级别（NORMAL/URGENT）',
    `handle_status` varchar(20) DEFAULT 'UNHANDLED' COMMENT '处理状态（UNHANDLED/HANDLED）',
    `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

-- 运营数据统计表
CREATE TABLE IF NOT EXISTS `t_operation_statistics` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `stat_date` date NOT NULL COMMENT '统计日期',
    `product_publish_count` int DEFAULT 0 COMMENT '当日商品发布数',
    `order_success_count` int DEFAULT 0 COMMENT '当日成交订单数',
    `seckill_participate_count` int DEFAULT 0 COMMENT '当日秒杀参与数',
    `ai_category_accuracy` decimal(5,2) DEFAULT NULL COMMENT 'AI分类准确率（%）',
    `coupon_use_count` int DEFAULT 0 COMMENT '当日优惠券使用数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营数据统计表';