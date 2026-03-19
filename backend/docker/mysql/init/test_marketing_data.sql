-- 营销模块测试数据初始化脚本

-- 清理旧数据
DELETE FROM t_user_coupon WHERE user_id IN (1, 2, 3);
DELETE FROM t_coupon WHERE id >= 100;
DELETE FROM t_seckill_activity WHERE id >= 100;

-- 插入测试优惠券
INSERT INTO t_coupon (id, name, type, reduce_amount, min_amount, discount_rate, total_count, received_count, per_limit, start_time, end_time, status, product_ids, category_ids, create_time, update_time) VALUES
(100, '新人专享优惠券', 'DISCOUNT', 10.00, 50.00, NULL, 1000, 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NULL, NULL, NOW(), NOW()),
(101, '满100减20', 'FULL_REDUCTION', 20.00, 100.00, NULL, 500, 50, 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NULL, NULL, NOW(), NOW()),
(102, '全场9折券', 'DISCOUNT', NULL, 30.00, 90, 200, 10, 1, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 1, NULL, NULL, NOW(), NOW()),
(103, '限时特惠券', 'DISCOUNT', 50.00, 200.00, NULL, 100, 80, 1, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, NULL, NULL, NOW(), NOW()),
(104, '已过期优惠券', 'DISCOUNT', 5.00, 10.00, NULL, 100, 100, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 1, NULL, NULL, NOW(), NOW()),
(105, '已禁用优惠券', 'DISCOUNT', 15.00, 80.00, NULL, 200, 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 0, NULL, NULL, NOW(), NOW());

-- 插入用户优惠券（已领取）
INSERT INTO t_user_coupon (id, user_id, coupon_id, status, receive_time, use_time, expire_time, order_id, create_time, update_time) VALUES
(100, 1, 100, 'UNUSED', NOW(), NULL, DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, NOW(), NOW()),
(101, 1, 101, 'UNUSED', NOW(), NULL, DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, NOW(), NOW()),
(102, 1, 102, 'USED', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW(), NOW()),
(103, 2, 100, 'UNUSED', NOW(), NULL, DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, NOW(), NOW()),
(104, 2, 101, 'EXPIRED', DATE_SUB(NOW(), INTERVAL 31 DAY), NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NOW(), NOW());

-- 插入测试秒杀活动
INSERT INTO t_seckill_activity (id, title, product_id, seckill_price, original_price, stock, sold_count, per_limit, start_time, end_time, status, cover_image, description, create_time, update_time) VALUES
(100, '限时秒杀-蓝牙耳机', 1, 99.00, 199.00, 100, 0, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 'ONGOING', NULL, '高品质蓝牙耳机限时秒杀', NOW(), NOW()),
(101, '午间特惠-充电宝', 2, 49.00, 99.00, 50, 10, 2, DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), 'PENDING', NULL, '大容量充电宝午间特惠', NOW(), NOW()),
(102, '晚间秒杀-手机壳', 3, 9.90, 29.90, 200, 0, 3, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 10 HOUR), 'PENDING', NULL, '时尚手机壳晚间秒杀', NOW(), NOW()),
(103, '已结束秒杀', 4, 19.90, 59.90, 100, 100, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'ENDED', NULL, '已结束的秒杀活动', NOW(), NOW());

-- 更新 Redis 缓存（库存预热）
-- 注意：这些命令需要在 Redis 中执行
-- SET seckill:stock:100 100
-- SET seckill:stock:101 50
-- SET seckill:stock:102 200
-- SET coupon:stock:100 1000
-- SET coupon:stock:101 450
-- SET coupon:stock:102 190
-- SET coupon:stock:103 20

SELECT '测试数据初始化完成' AS result;