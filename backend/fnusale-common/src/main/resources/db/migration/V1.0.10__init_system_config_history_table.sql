-- 系统配置历史记录表
CREATE TABLE IF NOT EXISTS `t_system_config_history` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `config_key` varchar(100) NOT NULL COMMENT '配置键',
    `old_value` text COMMENT '修改前值',
    `new_value` text COMMENT '修改后值',
    `admin_id` bigint NOT NULL COMMENT '操作管理员ID',
    `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `operate_ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_config_key` (`config_key`),
    KEY `idx_operate_time` (`operate_time`),
    KEY `idx_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置历史记录表';
