-- 管理员相关表
-- Flyway 迁移脚本 V1.0.8

-- 管理员表
CREATE TABLE IF NOT EXISTS `t_admin` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像地址',
    `role` varchar(20) NOT NULL DEFAULT 'OPERATOR' COMMENT '角色（SUPER_ADMIN/OPERATOR/SERVICE）',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0:禁用 1:启用）',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 管理员权限表
CREATE TABLE IF NOT EXISTS `t_admin_permission` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `admin_id` bigint NOT NULL COMMENT '管理员ID',
    `permission_code` varchar(50) NOT NULL COMMENT '权限代码',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_permission` (`admin_id`, `permission_code`),
    KEY `idx_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员权限表';

-- 插入默认超级管理员（密码: admin123）
INSERT INTO `t_admin` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 'SUPER_ADMIN', 1);

-- 插入默认权限
INSERT INTO `t_admin_permission` (`admin_id`, `permission_code`) VALUES
(1, 'user:manage'),
(1, 'product:audit'),
(1, 'order:manage'),
(1, 'dispute:handle'),
(1, 'config:manage'),
(1, 'log:view'),
(1, 'statistics:view');
