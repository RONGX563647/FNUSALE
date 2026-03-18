-- AI相关表
-- Flyway 迁移脚本 V1.0.6

-- AI价格参考表
CREATE TABLE IF NOT EXISTS `t_ai_price_reference` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_id` bigint NOT NULL COMMENT '品类ID',
    `new_degree` varchar(20) NOT NULL COMMENT '新旧程度',
    `min_price` decimal(10,2) DEFAULT NULL COMMENT '参考最低价格（元）',
    `max_price` decimal(10,2) DEFAULT NULL COMMENT '参考最高价格（元）',
    `sample_count` int DEFAULT 0 COMMENT '参考样本数',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_degree` (`category_id`, `new_degree`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI价格参考表';

-- 智能客服问题表
CREATE TABLE IF NOT EXISTS `t_ai_service_question` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `question_content` varchar(255) NOT NULL COMMENT '问题内容',
    `answer_content` text NOT NULL COMMENT '回答内容',
    `keyword` varchar(100) DEFAULT NULL COMMENT '匹配关键词',
    `enable_status` tinyint DEFAULT 1 COMMENT '启用状态（0-禁用, 1-启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能客服问题表';