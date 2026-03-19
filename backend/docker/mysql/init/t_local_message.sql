-- 本地消息表 - 用于分布式事务最终一致性
CREATE TABLE IF NOT EXISTS `t_local_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `message_id` VARCHAR(64) NOT NULL COMMENT '消息唯一ID',
    `message_type` VARCHAR(32) NOT NULL COMMENT '消息类型',
    `topic` VARCHAR(64) NOT NULL COMMENT '目标Topic',
    `tag` VARCHAR(32) DEFAULT NULL COMMENT '目标Tag',
    `message_content` TEXT NOT NULL COMMENT '消息内容（JSON）',
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING/SENT/FAILED）',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `next_retry_time` DATETIME NOT NULL COMMENT '下次重试时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id` (`message_id`),
    KEY `idx_status_next_retry` (`status`, `next_retry_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地消息表';