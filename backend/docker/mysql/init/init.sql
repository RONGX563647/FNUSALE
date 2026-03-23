-- 校园二手交易平台 - 数据库初始化脚本
-- 创建时间: 2024
-- 说明: 初始化数据库和Nacos所需表结构

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 使用数据库
USE fnusale;

-- ==================== Nacos 配置表 ====================
-- Nacos 2.x 所需的数据库表结构

CREATE TABLE IF NOT EXISTS `config_info` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id` varchar(255) NOT NULL COMMENT 'data_id',
    `group_id` varchar(128) DEFAULT NULL COMMENT 'group_id',
    `content` longtext NOT NULL COMMENT 'content',
    `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user` text COMMENT 'source user',
    `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
    `app_name` varchar(128) DEFAULT NULL,
    `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
    `c_desc` varchar(256) DEFAULT NULL,
    `c_use` varchar(64) DEFAULT NULL,
    `effect` varchar(64) DEFAULT NULL,
    `type` varchar(64) DEFAULT NULL,
    `c_schema` text,
    `encrypted_data_key` text NOT NULL COMMENT '密钥',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='config_info';

CREATE TABLE IF NOT EXISTS `config_info_aggr` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id` varchar(255) NOT NULL COMMENT 'data_id',
    `group_id` varchar(128) NOT NULL COMMENT 'group_id',
    `datum_id` varchar(255) NOT NULL COMMENT 'datum_id',
    `content` longtext NOT NULL COMMENT '内容',
    `gmt_modified` datetime NOT NULL COMMENT '修改时间',
    `app_name` varchar(128) DEFAULT NULL,
    `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='增加租户字段';

CREATE TABLE IF NOT EXISTS `config_info_beta` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id` varchar(255) NOT NULL COMMENT 'data_id',
    `group_id` varchar(128) NOT NULL COMMENT 'group_id',
    `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
    `content` longtext NOT NULL COMMENT 'content',
    `beta_ips` varchar(1024) DEFAULT NULL COMMENT 'betaIps',
    `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user` text COMMENT 'source user',
    `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
    `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
    `encrypted_data_key` text NOT NULL COMMENT '密钥',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='config_info_beta';

CREATE TABLE IF NOT EXISTS `config_info_tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id` varchar(255) NOT NULL COMMENT 'data_id',
    `group_id` varchar(128) NOT NULL COMMENT 'group_id',
    `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
    `tag_id` varchar(128) NOT NULL COMMENT 'tag_id',
    `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
    `content` longtext NOT NULL COMMENT 'content',
    `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user` text COMMENT 'source user',
    `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='config_info_tag';

CREATE TABLE IF NOT EXISTS `config_tags_relation` (
    `id` bigint(20) NOT NULL COMMENT 'id',
    `tag_name` varchar(128) NOT NULL COMMENT 'tag_name',
    `tag_type` varchar(64) DEFAULT NULL COMMENT 'tag_type',
    `data_id` varchar(255) NOT NULL COMMENT 'data_id',
    `group_id` varchar(128) NOT NULL COMMENT 'group_id',
    `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
    `nid` bigint(20) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`nid`),
    UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='config_tag_relation';

CREATE TABLE IF NOT EXISTS `group_capacity` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'Group ID',
    `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
    `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
    `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限',
    `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
    `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限',
    `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='集群、各Group容量信息表';

CREATE TABLE IF NOT EXISTS `his_config_info` (
    `id` bigint(20) unsigned NOT NULL,
    `nid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `data_id` varchar(255) NOT NULL,
    `group_id` varchar(128) NOT NULL,
    `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
    `content` longtext NOT NULL,
    `md5` varchar(32) DEFAULT NULL,
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `src_user` text,
    `src_ip` varchar(50) DEFAULT NULL,
    `op_type` char(10) DEFAULT NULL,
    `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
    `encrypted_data_key` text NOT NULL COMMENT '密钥',
    PRIMARY KEY (`nid`),
    KEY `idx_gmt_create` (`gmt_create`),
    KEY `idx_gmt_modified` (`gmt_modified`),
    KEY `idx_did` (`data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='多租户改造';

CREATE TABLE IF NOT EXISTS `tenant_capacity` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'Tenant ID',
    `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额',
    `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
    `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限',
    `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
    `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限',
    `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='租户容量信息表';

CREATE TABLE IF NOT EXISTS `tenant_info` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `kp` varchar(128) NOT NULL COMMENT 'kp',
    `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
    `tenant_name` varchar(128) DEFAULT '' COMMENT 'tenant_name',
    `tenant_desc` varchar(256) DEFAULT NULL COMMENT 'tenant_desc',
    `create_source` varchar(32) DEFAULT NULL COMMENT 'create_source',
    `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
    `gmt_modified` bigint(20) NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='tenant_info';

CREATE TABLE IF NOT EXISTS `users` (
    `username` varchar(50) NOT NULL PRIMARY KEY,
    `password` varchar(500) NOT NULL,
    `enabled` boolean NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `roles` (
    `username` varchar(50) NOT NULL,
    `role` varchar(50) NOT NULL,
    UNIQUE INDEX `idx_user_role` (`username` ASC, `role` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permissions` (
    `role` varchar(50) NOT NULL,
    `resource` varchar(255) NOT NULL,
    `action` varchar(8) NOT NULL,
    UNIQUE INDEX `uk_role_permission` (`role`,`resource`,`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='权限表';

-- 插入默认用户 (密码: nacos)
INSERT IGNORE INTO users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', TRUE);
INSERT IGNORE INTO roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');

-- ==================== 业务表结构 ====================
-- 以下为校园二手交易平台核心业务表

-- 用户表（t_user）
CREATE TABLE IF NOT EXISTS `t_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `student_teacher_id` varchar(50) DEFAULT NULL COMMENT '学号/工号',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `campus_email` varchar(100) DEFAULT NULL COMMENT '校园邮箱',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `identity_type` varchar(20) DEFAULT 'STUDENT' COMMENT '身份类型（STUDENT/TEACHER）',
    `auth_status` varchar(20) DEFAULT 'UNAUTH' COMMENT '认证状态（UNAUTH/UNDER_REVIEW/AUTH_SUCCESS/AUTH_FAILED）',
    `auth_image_url` varchar(500) DEFAULT NULL COMMENT '校园卡/学生证审核图片地址',
    `auth_result_remark` varchar(255) DEFAULT NULL COMMENT '认证审核备注',
    `location_permission` varchar(10) DEFAULT 'DENY' COMMENT '定位权限状态（ALLOW/DENY）',
    `credit_score` int DEFAULT 100 COMMENT '信誉分（默认100）',
    `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像地址',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除标记（0-未删除, 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_teacher_id` (`student_teacher_id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_campus_email` (`campus_email`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础表';

-- 管理员表（t_admin）
CREATE TABLE IF NOT EXISTS `t_admin` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
    `role` varchar(20) DEFAULT 'OPERATOR' COMMENT '角色（SUPER_ADMIN/OPERATOR/SERVICE）',
    `status` tinyint DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除标记（0-未删除, 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 管理员权限表
CREATE TABLE IF NOT EXISTS `t_admin_permission` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `admin_id` bigint NOT NULL COMMENT '管理员ID',
    `permission_code` varchar(50) NOT NULL COMMENT '权限代码（如user:manage）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除标记（0-未删除, 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    UNIQUE KEY `uk_admin_permission` (`admin_id`, `permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员权限表';

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

-- 订单表
CREATE TABLE IF NOT EXISTS `t_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` varchar(64) NOT NULL COMMENT '订单编号',
    `user_id` bigint NOT NULL COMMENT '买家ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `product_price` decimal(10,2) NOT NULL COMMENT '商品原价',
    `coupon_id` bigint DEFAULT NULL COMMENT '优惠券ID',
    `coupon_deduct_amount` decimal(10,2) DEFAULT 0 COMMENT '优惠券抵扣金额',
    `actual_pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
    `pick_point_id` bigint DEFAULT NULL COMMENT '自提点ID',
    `pay_type` varchar(20) DEFAULT NULL COMMENT '支付方式（WECHAT/ALIPAY/CAMPUS_CARD）',
    `pay_status` varchar(20) DEFAULT 'UNPAID' COMMENT '支付状态（UNPAID/PAID/REFUNDED）',
    `order_status` varchar(20) DEFAULT 'UNPAID' COMMENT '订单状态（UNPAID/WAIT_PICK/SUCCESS/CANCEL）',
    `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
    `ready_time` datetime DEFAULT NULL COMMENT '商品备好时间',
    `extend_receive_days` int DEFAULT 0 COMMENT '延长收货天数',
    `success_time` datetime DEFAULT NULL COMMENT '成交时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 插入默认商品品类
INSERT IGNORE INTO t_product_category (id, category_name, parent_category_id, ai_mapping_value, enable_status) VALUES
(1, '教材书籍', NULL, 'books,textbook', 1),
(2, '电子产品', NULL, 'electronics,digital', 1),
(3, '生活用品', NULL, 'daily,lifestyle', 1),
(4, '体育器材', NULL, 'sports,equipment', 1),
(5, '服装鞋帽', NULL, 'clothing,fashion', 1),
(6, '其他', NULL, 'other', 1);

-- ==================== 用户相关表 ====================

-- 用户地址表
CREATE TABLE IF NOT EXISTS `t_user_address` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `address_type` varchar(20) DEFAULT NULL COMMENT '地址类型（PICK_POINT/CUSTOM）',
    `pick_point_id` bigint DEFAULT NULL COMMENT '自提点ID',
    `custom_address` varchar(255) DEFAULT NULL COMMENT '自定义详细地址',
    `longitude` decimal(10,6) DEFAULT NULL COMMENT '地址经度',
    `latitude` decimal(10,6) DEFAULT NULL COMMENT '地址纬度',
    `is_default` tinyint DEFAULT 0 COMMENT '是否默认地址（0-否, 1-是）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- 校园自提点表
CREATE TABLE IF NOT EXISTS `t_campus_pick_point` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `pick_point_name` varchar(100) NOT NULL COMMENT '自提点名称',
    `campus_area` varchar(50) DEFAULT NULL COMMENT '所属校区',
    `detail_address` varchar(200) DEFAULT NULL COMMENT '详细位置',
    `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
    `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
    `enable_status` tinyint DEFAULT 1 COMMENT '启用状态（0-禁用, 1-启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园自提点表';

-- ==================== 商品相关表 ====================

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

-- ==================== IM相关表 ====================

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
    `is_pinned_u1` tinyint DEFAULT 0 COMMENT '用户1是否置顶（0-否, 1-是）',
    `is_pinned_u2` tinyint DEFAULT 0 COMMENT '用户2是否置顶（0-否, 1-是）',
    `pinned_time_u1` datetime DEFAULT NULL COMMENT '用户1置顶时间',
    `pinned_time_u2` datetime DEFAULT NULL COMMENT '用户2置顶时间',
    `is_deleted_u1` tinyint DEFAULT 0 COMMENT '用户1是否删除会话（0-否, 1-是）',
    `is_deleted_u2` tinyint DEFAULT 0 COMMENT '用户2是否删除会话（0-否, 1-是）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user1_id` (`user1_id`),
    KEY `idx_user2_id` (`user2_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_users_product` (`user1_id`, `user2_id`, `product_id`)
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

-- 用户自定义快捷回复表
CREATE TABLE IF NOT EXISTS `t_im_user_quick_reply` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `reply_content` varchar(200) NOT NULL COMMENT '回复内容',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自定义快捷回复表';

-- ==================== 用户行为表 ====================

-- 用户行为表
CREATE TABLE IF NOT EXISTS `t_user_behavior` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `behavior_type` varchar(20) NOT NULL COMMENT '行为类型（BROWSE/COLLECT/LIKE）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为表';

-- ==================== 营销相关表 ====================

-- 优惠券表
CREATE TABLE IF NOT EXISTS `t_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `coupon_name` varchar(100) NOT NULL COMMENT '优惠券名称',
    `coupon_type` varchar(20) NOT NULL COMMENT '类型（FULL_REDUCE/DIRECT_REDUCE/CATEGORY）',
    `full_amount` decimal(10,2) DEFAULT NULL COMMENT '满减金额',
    `reduce_amount` decimal(10,2) NOT NULL COMMENT '抵扣金额',
    `category_id` bigint DEFAULT NULL COMMENT '品类ID',
    `total_count` int NOT NULL DEFAULT 0 COMMENT '发放总数',
    `received_count` int DEFAULT 0 COMMENT '已领数量',
    `used_count` int DEFAULT 0 COMMENT '已用数量',
    `start_time` datetime NOT NULL COMMENT '有效期开始时间',
    `end_time` datetime NOT NULL COMMENT '有效期结束时间',
    `enable_status` tinyint DEFAULT 1 COMMENT '启用状态（0-禁用, 1-启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `t_user_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
    `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time` datetime DEFAULT NULL COMMENT '使用时间',
    `expire_time` datetime NOT NULL COMMENT '过期时间',
    `coupon_status` varchar(20) DEFAULT 'UNUSED' COMMENT '状态（UNUSED/USED/EXPIRED）',
    `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_user_status` (`user_id`, `coupon_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS `t_seckill_activity` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `activity_name` varchar(100) NOT NULL COMMENT '活动名称',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价格（元）',
    `total_stock` int NOT NULL COMMENT '秒杀总库存',
    `remain_stock` int NOT NULL COMMENT '剩余库存',
    `start_time` datetime NOT NULL COMMENT '活动开始时间',
    `end_time` datetime NOT NULL COMMENT '活动结束时间',
    `activity_status` varchar(20) DEFAULT 'NOT_START' COMMENT '状态（NOT_START/ON_GOING/END）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_status_start` (`activity_status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- 秒杀提醒表
CREATE TABLE IF NOT EXISTS `t_seckill_reminder` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `activity_id` bigint NOT NULL COMMENT '活动ID',
    `remind_time` datetime NOT NULL COMMENT '提醒时间',
    `is_reminded` tinyint NOT NULL DEFAULT 0 COMMENT '是否已提醒（0-否，1-是）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_remind_time` (`remind_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀提醒表';

-- ==================== 交易相关表 ====================

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

-- ==================== AI相关表 ====================

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

-- ==================== 系统相关表 ====================

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

-- 邮件发送日志表
CREATE TABLE IF NOT EXISTS `t_email_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `to_email` varchar(100) NOT NULL COMMENT '收件人邮箱',
    `subject` varchar(200) DEFAULT NULL COMMENT '邮件主题',
    `content` text COMMENT '邮件内容',
    `send_status` varchar(20) DEFAULT 'SUCCESS' COMMENT '发送状态（SUCCESS/FAILED）',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `send_time` datetime DEFAULT NULL COMMENT '发送时间',
    `retry_count` int DEFAULT 0 COMMENT '重试次数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_to_email` (`to_email`),
    KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送日志表';

-- 运营数据统计表
CREATE TABLE IF NOT EXISTS `t_operation_statistics` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `stat_date` date NOT NULL COMMENT '统计日期',
    `new_user_count` int DEFAULT 0 COMMENT '当日新增用户数',
    `active_user_count` int DEFAULT 0 COMMENT '当日活跃用户数',
    `product_publish_count` int DEFAULT 0 COMMENT '当日商品发布数',
    `order_success_count` int DEFAULT 0 COMMENT '当日成交订单数',
    `order_success_amount` decimal(12,2) DEFAULT 0.00 COMMENT '当日成交金额',
    `seckill_participate_count` int DEFAULT 0 COMMENT '当日秒杀参与数',
    `ai_category_accuracy` decimal(5,2) DEFAULT NULL COMMENT 'AI分类准确率（%）',
    `coupon_use_count` int DEFAULT 0 COMMENT '当日优惠券使用数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营数据统计表';

-- 系统配置历史记录表
CREATE TABLE IF NOT EXISTS `t_system_config_history` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `config_key` varchar(100) NOT NULL COMMENT '配置键',
    `old_value` text COMMENT '修改前值',
    `new_value` text COMMENT '修改后值',
    `admin_id` bigint DEFAULT NULL COMMENT '操作管理员ID',
    `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `operate_ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_config_key` (`config_key`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置历史记录表';

-- ==================== 签到相关表 ====================

-- 签到记录表
CREATE TABLE IF NOT EXISTS `t_user_sign_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `sign_date` date NOT NULL COMMENT '签到日期',
    `sign_time` datetime NOT NULL COMMENT '签到时间',
    `continuous_days` int DEFAULT 1 COMMENT '连续签到天数',
    `reward_points` int DEFAULT 0 COMMENT '获得积分',
    `is_repair` tinyint DEFAULT 0 COMMENT '是否补签（0-否, 1-是）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sign_date` (`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';

-- 用户积分表
CREATE TABLE IF NOT EXISTS `t_user_points` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `total_points` int DEFAULT 0 COMMENT '累计获得积分',
    `available_points` int DEFAULT 0 COMMENT '可用积分',
    `used_points` int DEFAULT 0 COMMENT '已使用积分',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分表';

-- 积分变动日志表
CREATE TABLE IF NOT EXISTS `t_points_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `change_type` varchar(30) NOT NULL COMMENT '变动类型（SIGN_REWARD/CONTINUOUS_REWARD/REPAIR_COST/COUPON_EXCHANGE/PRODUCT_TOP/TRADE_REWARD/RANK_REWARD/BIRTHDAY_REWARD）',
    `change_amount` int NOT NULL COMMENT '变动数量（正数增加，负数减少）',
    `before_points` int DEFAULT 0 COMMENT '变动前积分',
    `after_points` int DEFAULT 0 COMMENT '变动后积分',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分变动日志表';

-- ==================== 用户评价分表 ====================

-- 用户评价分表
CREATE TABLE IF NOT EXISTS `t_user_rating` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `overall_rating` decimal(3,2) DEFAULT 5.00 COMMENT '综合评分（1.00-5.00）',
    `rating_level` varchar(20) DEFAULT 'GOOD' COMMENT '评分等级（EXCELLENT/VERY_GOOD/GOOD/AVERAGE/POOR/VERY_POOR）',
    `total_evaluations` int DEFAULT 0 COMMENT '累计评价数',
    `positive_count` int DEFAULT 0 COMMENT '好评数（4-5星）',
    `neutral_count` int DEFAULT 0 COMMENT '中评数（3星）',
    `negative_count` int DEFAULT 0 COMMENT '差评数（1-2星）',
    `positive_rate` decimal(5,2) DEFAULT 100.00 COMMENT '好评率（%）',
    `last_30d_evaluations` int DEFAULT 0 COMMENT '近30天评价数',
    `last_30d_rating` decimal(3,2) DEFAULT 5.00 COMMENT '近30天评分',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户评价分表';

-- 评价标签统计表
CREATE TABLE IF NOT EXISTS `t_evaluation_tag_stat` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
    `tag_count` int DEFAULT 0 COMMENT '出现次数',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tag` (`user_id`, `tag_name`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价标签统计表';

-- ==================== 排行榜相关表 ====================

-- 排行榜记录表
CREATE TABLE IF NOT EXISTS `t_ranking_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rank_type` varchar(20) NOT NULL COMMENT '排行类型（ACTIVITY/TRADE/CREDIT/RATING/NEW_SELLER）',
    `rank_date` date NOT NULL COMMENT '排行日期',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `rank_position` int NOT NULL COMMENT '排名',
    `score` decimal(10,2) DEFAULT 0.00 COMMENT '得分',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_type_date` (`user_id`, `rank_type`, `rank_date`),
    KEY `idx_rank_type_date` (`rank_type`, `rank_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排行榜记录表';

-- 排行榜奖励记录表
CREATE TABLE IF NOT EXISTS `t_ranking_reward_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `rank_type` varchar(20) NOT NULL COMMENT '排行类型（ACTIVITY/TRADE/CREDIT/RATING/NEW_SELLER）',
    `rank_date` date NOT NULL COMMENT '排行日期',
    `rank_position` int NOT NULL COMMENT '排名',
    `reward_points` int DEFAULT 0 COMMENT '奖励积分',
    `reward_coupon_id` bigint DEFAULT NULL COMMENT '奖励优惠券ID',
    `is_claimed` tinyint DEFAULT 0 COMMENT '是否已领取（0-未领取, 1-已领取）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_rank_type_date` (`rank_type`, `rank_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排行榜奖励记录表';

-- ==================== 本地消息表 ====================

-- 本地消息表（用于分布式事务最终一致性）
CREATE TABLE IF NOT EXISTS `t_local_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `message_id` varchar(64) NOT NULL COMMENT '消息唯一ID',
    `message_type` varchar(32) NOT NULL COMMENT '消息类型',
    `topic` varchar(64) NOT NULL COMMENT '目标Topic',
    `tag` varchar(32) DEFAULT NULL COMMENT '目标Tag',
    `message_content` text NOT NULL COMMENT '消息内容（JSON）',
    `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING/SENT/FAILED）',
    `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` int NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `next_retry_time` datetime NOT NULL COMMENT '下次重试时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id` (`message_id`),
    KEY `idx_status_next_retry` (`status`, `next_retry_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地消息表';

-- ==================== 初始化数据 ====================

-- 插入默认管理员（密码: admin123，使用BCrypt加密）
INSERT IGNORE INTO t_admin (id, username, password, nickname, role, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 'SUPER_ADMIN', 1);

-- 插入默认商品品类
INSERT IGNORE INTO t_im_quick_reply (id, reply_content, enable_status, sort) VALUES
(1, '几成新？', 1, 1),
(2, '能小刀吗？', 1, 2),
(3, '什么时候自提？', 1, 3),
(4, '还在吗？', 1, 4),
(5, '价格可以再低点吗？', 1, 5);

-- 插入默认智能客服问题
INSERT IGNORE INTO t_ai_service_question (id, question_content, answer_content, keyword, enable_status) VALUES
(1, '如何认证校园身份？', '请在个人中心点击"校园认证"，上传学生证或校园卡照片，填写学号后提交审核，管理员会在1-3个工作日内完成审核。', '认证,身份认证,校园认证', 1),
(2, '如何发布商品？', '完成校园身份认证后，点击首页"+"按钮，填写商品信息并上传照片即可发布。首次发布前需授权定位权限。', '发布,发布商品,上架', 1),
(3, '秒杀规则是什么？', '每日限定时间段开放秒杀活动，每位用户限购一件，需在规定时间内完成支付。秒杀商品不支持退换。', '秒杀,秒杀规则', 1),
(4, '交易纠纷怎么处理？', '如遇交易纠纷，请在订单详情页发起投诉，上传相关证据，管理员会在3个工作日内介入处理。', '纠纷,投诉,交易纠纷', 1),
(5, '如何提升信誉分？', '按时完成交易、获得好评可以增加信誉分，违规行为会扣除信誉分。信誉分低于60将限制发布商品和参与秒杀。', '信誉分,信誉,评分', 1);

-- 插入系统配置
INSERT IGNORE INTO t_system_config (id, config_key, config_value, config_desc) VALUES
(1, 'campus_fence_center', '116.407526,39.904030', '校园围栏中心点经纬度'),
(2, 'campus_fence_radius', '1500', '校园围栏半径（米）'),
(3, 'seckill_qps_limit', '500', '秒杀接口QPS阈值'),
(4, 'sign_base_points', '1', '签到基础积分'),
(5, 'repair_points_cost', '10', '补签消耗积分'),
(6, 'repair_monthly_limit', '3', '每月补签次数上限');

-- 初始化完成
SELECT 'Database initialization completed!' AS message;