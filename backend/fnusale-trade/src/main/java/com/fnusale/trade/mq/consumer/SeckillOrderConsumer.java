package com.fnusale.trade.mq.consumer;

import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.entity.Order;
import com.fnusale.common.enums.OrderStatus;
import com.fnusale.common.enums.PayStatus;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.trade.client.MarketingClient;
import com.fnusale.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单消费者（v4优化版）
 * 
 * v4优化内容：
 * 1. 添加数据库库存扣减，保证Redis与数据库一致性
 * 2. 添加订单创建失败时的Redis回滚机制
 * 3. 使用Lua脚本保证回滚原子性
 * 4. 添加监控指标记录
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.SECKILL_ORDER_TOPIC,
        consumerGroup = "seckill-order-consumer-group",
        selectorExpression = RocketMQConstants.SECKILL_ORDER_TAG_CREATE
)
@RequiredArgsConstructor
public class SeckillOrderConsumer implements RocketMQListener<SeckillOrderEvent> {

    private final StringRedisTemplate redisTemplate;
    private final OrderMapper orderMapper;
    private final MarketingClient marketingClient;

    /**
     * 订单创建幂等性 Key 前缀
     */
    private static final String SECKILL_ORDER_KEY_PREFIX = "seckill:order:";
    private static final String ORDER_NO_KEY_PREFIX = "order:no:";
    
    /**
     * 秒杀时间戳 Key 前缀（用于结果查询超时判断）
     */
    private static final String SECKILL_TIME_KEY_PREFIX = "seckill:time:";
    
    /**
     * 秒杀监控指标 Key 前缀
     */
    private static final String SECKILL_METRICS_PREFIX = "seckill:metrics:";
    
    /**
     * 结果查询超时时间（秒）
     */
    private static final int RESULT_TIMEOUT_SECONDS = 300;

    /**
     * Redis回滚Lua脚本
     */
    private static final String SECKILL_ROLLBACK_SCRIPT = """
            -- 1. 检查用户是否在购买集合中
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 0 then
                return 0
            end

            -- 2. 原子操作：移除购买标记 + 恢复库存
            redis.call('SREM', KEYS[2], ARGV[1])
            redis.call('INCR', KEYS[1])

            return 1
            """;

    @Override
    public void onMessage(SeckillOrderEvent event) {
        Long userId = event.getUserId();
        Long activityId = event.getActivityId();
        String eventId = event.getEventId();

        log.info("收到秒杀订单消息, userId: {}, activityId: {}, eventId: {}", userId, activityId, eventId);

        String idempotentKey = SECKILL_ORDER_KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(success)) {
            log.info("秒杀订单已处理，跳过, eventId: {}", eventId);
            return;
        }

        try {
            deductDatabaseStock(activityId);
            
            createSeckillOrder(event);
            
            recordMetrics(activityId, true);
            
            log.info("秒杀订单创建成功, userId: {}, activityId: {}", userId, activityId);
        } catch (Exception e) {
            log.error("秒杀订单创建失败, userId: {}, activityId: {}", userId, activityId, e);
            
            redisTemplate.delete(idempotentKey);
            
            rollbackRedis(activityId, userId);
            
            recordMetrics(activityId, false);
            
            resetMessageStatus(eventId);
            
            throw e;
        }
    }
    
    /**
     * 重置消息状态为待重试（v4新增）
     * 让生产者的定时任务可以重新发送消息
     */
    private void resetMessageStatus(String eventId) {
        try {
            var result = marketingClient.resetMessageStatus(eventId);
            if (result != null && result.isSuccess()) {
                log.info("重置消息状态成功，等待定时任务重试: eventId={}", eventId);
            } else {
                log.warn("重置消息状态失败: eventId={}", eventId);
            }
        } catch (Exception e) {
            log.error("调用营销服务重置消息状态失败: eventId={}", eventId, e);
        }
    }

    /**
     * 扣减数据库库存（v4新增）
     */
    private void deductDatabaseStock(Long activityId) {
        try {
            var result = marketingClient.deductSeckillStock(activityId);
            if (result == null || !result.isSuccess() || !Boolean.TRUE.equals(result.getData())) {
                throw new RuntimeException("扣减数据库库存失败: activityId=" + activityId);
            }
            log.info("扣减数据库库存成功: activityId={}", activityId);
        } catch (Exception e) {
            log.error("调用营销服务扣减库存失败: activityId={}", activityId, e);
            throw new RuntimeException("扣减数据库库存失败", e);
        }
    }

    /**
     * 创建秒杀订单
     */
    private void createSeckillOrder(SeckillOrderEvent event) {
        String orderNo = generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(event.getUserId());
        order.setProductId(event.getProductId());
        order.setProductPrice(event.getSeckillPrice());
        order.setCouponDeductAmount(BigDecimal.ZERO);
        order.setActualPayAmount(event.getSeckillPrice());
        order.setPayType("SECKILL");
        order.setPayStatus(PayStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.WAIT_PICK.getCode());

        orderMapper.insert(order);

        log.info("创建秒杀订单成功: orderId={}, orderNo={}, userId={}, productId={}, seckillPrice={}",
                order.getId(), orderNo, event.getUserId(), event.getProductId(), event.getSeckillPrice());
    }

    /**
     * 回滚Redis库存和用户购买标记
     */
    private void rollbackRedis(Long activityId, Long userId) {
        try {
            String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activityId;
            String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;

            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(SECKILL_ROLLBACK_SCRIPT);
            redisScript.setResultType(Long.class);

            Long result = redisTemplate.execute(
                    redisScript,
                    List.of(stockKey, boughtKey),
                    userId.toString()
            );

            if (result != null && result == 1) {
                log.warn("秒杀订单创建失败，Redis回滚成功: activityId={}, userId={}", activityId, userId);
            } else {
                log.warn("秒杀订单创建失败，Redis回滚跳过（用户未在购买集合中）: activityId={}, userId={}", activityId, userId);
            }
        } catch (Exception e) {
            log.error("Redis回滚失败，需要人工处理: activityId={}, userId={}", activityId, userId, e);
        }
    }

    /**
     * 记录监控指标（v4新增）
     */
    private void recordMetrics(Long activityId, boolean success) {
        try {
            String today = java.time.LocalDate.now().toString();
            
            String successKey = SECKILL_METRICS_PREFIX + activityId + ":" + today + ":success";
            String totalKey = SECKILL_METRICS_PREFIX + activityId + ":" + today + ":total";
            
            redisTemplate.opsForValue().increment(totalKey);
            if (success) {
                redisTemplate.opsForValue().increment(successKey);
            }
            
            redisTemplate.expire(successKey, 7, TimeUnit.DAYS);
            redisTemplate.expire(totalKey, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("记录监控指标失败: activityId={}", activityId, e);
        }
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String key = ORDER_NO_KEY_PREFIX + dateStr;
        Long seq = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 2, TimeUnit.DAYS);
        return "XS" + dateStr + String.format("%06d", seq);
    }
}
