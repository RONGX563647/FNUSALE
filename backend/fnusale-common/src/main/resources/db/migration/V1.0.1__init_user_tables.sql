-- 用户相关表
-- Flyway 迁移脚本 V1.0.1

-- 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `student_teacher_id` varchar(50) DEFAULT NULL COMMENT '学号/工号',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `campus_email` varchar(100) DEFAULT NULL COMMENT '校园邮箱',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `identity_type` varchar(20) DEFAULT 'STUDENT' COMMENT '身份类型：STUDENT/TEACHER',
    `auth_status` varchar(20) DEFAULT 'UNAUTH' COMMENT '认证状态：UNAUTH/UNDER_REVIEW/AUTH_SUCCESS/AUTH_FAILED',
    `auth_image_url` varchar(500) DEFAULT NULL COMMENT '认证图片URL',
    `auth_result_remark` varchar(255) DEFAULT NULL COMMENT '认证审核备注',
    `location_permission` varchar(10) DEFAULT 'DENY' COMMENT '定位权限：ALLOW/DENY',
    `credit_score` int DEFAULT 100 COMMENT '信誉分',
    `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_teacher_id` (`student_teacher_id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_campus_email` (`campus_email`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

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

-- 用户签到记录表
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

-- 用户评分表
CREATE TABLE IF NOT EXISTS `t_user_rating` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `overall_rating` decimal(3,2) DEFAULT 5.00 COMMENT '综合评分（1.00-5.00）',
    `rating_level` varchar(20) DEFAULT 'EXCELLENT' COMMENT '评分等级（EXCELLENT/VERY_GOOD/GOOD/AVERAGE/POOR/VERY_POOR）',
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
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_overall_rating` (`overall_rating`),
    KEY `idx_positive_rate` (`positive_rate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户评价分表';

-- 用户地址表
CREATE TABLE IF NOT EXISTS `t_user_address` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `address_type` varchar(20) DEFAULT NULL COMMENT '地址类型：PICK_POINT/CUSTOM',
    `pick_point_id` bigint DEFAULT NULL COMMENT '自提点ID',
    `custom_address` varchar(255) DEFAULT NULL COMMENT '自定义详细地址',
    `longitude` decimal(10,6) DEFAULT NULL COMMENT '地址经度',
    `latitude` decimal(10,6) DEFAULT NULL COMMENT '地址纬度',
    `is_default` tinyint DEFAULT 0 COMMENT '是否默认地址：0-否, 1-是',
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
    `enable_status` tinyint DEFAULT 1 COMMENT '启用状态：0-禁用, 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园自提点表';

-- 积分变动日志表
CREATE TABLE IF NOT EXISTS `t_points_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `change_type` varchar(30) NOT NULL COMMENT '变动类型：SIGN_REWARD/CONTINUOUS_REWARD/REPAIR_COST/COUPON_EXCHANGE/PRODUCT_TOP/TRADE_REWARD/RANK_REWARD/BIRTHDAY_REWARD',
    `change_amount` int NOT NULL COMMENT '变动数量（正数增加，负数减少）',
    `before_points` int DEFAULT 0 COMMENT '变动前积分',
    `after_points` int DEFAULT 0 COMMENT '变动后积分',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分变动日志表';

-- 排行榜记录表
CREATE TABLE IF NOT EXISTS `t_ranking_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `rank_type` varchar(20) NOT NULL COMMENT '排行类型：ACTIVITY/TRADE/CREDIT/RATING/NEW_SELLER',
    `rank_date` date NOT NULL COMMENT '排行日期',
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
    `rank_type` varchar(20) NOT NULL COMMENT '排行类型：ACTIVITY/TRADE/CREDIT/RATING/NEW_SELLER',
    `rank_date` date NOT NULL COMMENT '排行日期',
    `rank_position` int NOT NULL COMMENT '排名',
    `reward_points` int DEFAULT 0 COMMENT '奖励积分',
    `reward_coupon_id` bigint DEFAULT NULL COMMENT '奖励优惠券ID',
    `is_claimed` tinyint DEFAULT 0 COMMENT '是否已领取：0-否, 1-是',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_rank_type_date` (`rank_type`, `rank_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排行榜奖励记录表';

-- 评价标签统计表
CREATE TABLE IF NOT EXISTS `t_evaluation_tag_stat` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
    `tag_count` int DEFAULT 0 COMMENT '标签次数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tag` (`user_id`, `tag_name`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价标签统计表';
