-- IM相关表
-- Flyway 迁移脚本 V1.0.3

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `t_im_session` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user1_id` bigint NOT NULL COMMENT '用户1ID',
    `user2_id` bigint NOT NULL COMMENT '用户2ID',
    `product_id` bigint DEFAULT NULL COMMENT '关联商品ID',
    `last_message_content` varchar(500) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_message_time` datetime DEFAULT NULL COMMENT '最后一条消息时间',
    `unread_count_u1` int DEFAULT 0 COMMENT '用户1未读消息数',
    `unread_count_u2` int DEFAULT 0 COMMENT '用户2未读消息数',
    `session_status` varchar(20) DEFAULT 'NORMAL' COMMENT '会话状态（NORMAL/CLOSED）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user1_id` (`user1_id`),
    KEY `idx_user2_id` (`user2_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `t_im_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id` bigint NOT NULL COMMENT '会话ID',
    `sender_id` bigint NOT NULL COMMENT '发送者ID',
    `receiver_id` bigint NOT NULL COMMENT '接收者ID',
    `message_type` varchar(20) DEFAULT 'TEXT' COMMENT '消息类型（TEXT/IMAGE/VOICE）',
    `message_content` text COMMENT '消息内容',
    `is_read` tinyint DEFAULT 0 COMMENT '是否已读（0-未读, 1-已读）',
    `sensitive_check_result` varchar(20) DEFAULT 'PASS' COMMENT '敏感词检测结果（PASS/FAIL）',
    `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除标记（0-未删除, 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 快捷回复模板表
CREATE TABLE IF NOT EXISTS `t_im_quick_reply` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `reply_content` varchar(200) NOT NULL COMMENT '模板内容',
    `enable_status` tinyint DEFAULT 1 COMMENT '启用状态（0-禁用, 1-启用）',
    `sort` int DEFAULT 0 COMMENT '排序',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='快捷回复模板表';