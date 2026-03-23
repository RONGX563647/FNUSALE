package com.fnusale.marketing.service.core;

import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.constant.RocketMQConstants;
import com.fnusale.common.entity.LocalMessage;
import com.fnusale.common.entity.SeckillActivity;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.marketing.event.SeckillSuccessAppEvent;
import com.fnusale.marketing.mapper.LocalMessageMapper;
import com.fnusale.marketing.metrics.SeckillMetrics;
import com.fnusale.marketing.script.SeckillLuaScript;
import com.fnusale.marketing.service.SeckillAntiFraudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀核心服务（v4优化：解耦分离）
 * 
 * 职责：
 * 1. 秒杀核心流程
 * 2. Redis原子操作
 * 3. 分布式锁
 * 4. 数据库事务
 * 
 * 依赖：6个（符合单一职责原则）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillCoreService {

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final LocalMessageMapper localMessageMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SeckillAntiFraudService antiFraudService;
    private final SeckillMetrics seckillMetrics;

    private static final String SECKILL_TIME_KEY_PREFIX = "seckill:time:";
    private static final String SECKILL_TIME_SET_PREFIX = "seckill:time:set:";
    
    private DefaultRedisScript<Long> seckillAtomicScript;
    private DefaultRedisScript<Long> seckillRollbackScript;

    @PostConstruct
    public void init() {
        seckillAtomicScript = new DefaultRedisScript<>();
        seckillAtomicScript.setScriptText(SeckillLuaScript.SECKILL_ATOMIC_SCRIPT);
        seckillAtomicScript.setResultType(Long.class);

        seckillRollbackScript = new DefaultRedisScript<>();
        seckillRollbackScript.setScriptText(SeckillLuaScript.SECKILL_ROLLBACK_SCRIPT);
        seckillRollbackScript.setResultType(Long.class);
    }

    /**
     * 执行秒杀核心流程
     */
    public Long executeSeckill(Long userId, Long activityId, SeckillActivity activity, String ip) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            // 先记录IP访问
            antiFraudService.recordIpAccess(ip, activityId);
            
            // 再检查是否被限流
            if (antiFraudService.isIpLimited(ip, activityId)) {
                log.warn("IP被限流: ip={}, activityId={}", ip, activityId);
                throw new BusinessException(429, "请求过于频繁，请稍后再试");
            }

            long expireSeconds = Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
            if (expireSeconds <= 0) {
                expireSeconds = MarketingConstants.SECKILL_STOCK_PRELOAD_MINUTES * 60L;
            }

            // 分布式锁
            String lockKey = "seckill:lock:" + activityId + ":" + userId;
            RLock lock = redissonClient.getLock(lockKey);
            
            try {
                if (!lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                    log.warn("获取分布式锁失败: userId={}, activityId={}", userId, activityId);
                    throw new BusinessException(429, "请求处理中，请勿重复提交");
                }
                
                try {
                    Long result = executeRedisAtomic(userId, activityId, activity, expireSeconds);
                    
                    if (result == null || result < 0) {
                        handleSeckillFail(result);
                    }

                    Long queueNumber = result;
                    Long finalResult = doJoinSeckillInTransaction(userId, activityId, activity, expireSeconds, queueNumber);
                    success = true;
                    return finalResult;
                } catch (Exception e) {
                    rollbackRedis(activityId, userId);
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取分布式锁被中断: userId={}, activityId={}", userId, activityId, e);
                throw new BusinessException(500, "系统繁忙，请稍后重试");
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } finally {
            long latencyMs = System.currentTimeMillis() - startTime;
            seckillMetrics.recordSeckillRequest(success, latencyMs);
        }
    }

    /**
     * 执行Redis原子操作
     */
    private Long executeRedisAtomic(Long userId, Long activityId, SeckillActivity activity, long expireSeconds) {
        String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activityId;
        String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;

        return redisTemplate.execute(
                seckillAtomicScript,
                List.of(stockKey, boughtKey),
                userId.toString(),
                activity.getRemainStock().toString(),
                String.valueOf(expireSeconds)
        );
    }

    /**
     * 处理秒杀失败
     */
    private void handleSeckillFail(Long result) {
        String errorMessage = getSeckillErrorMessage(result);
        if (result != null && result == -2) {
            seckillMetrics.recordSeckillFail("DUPLICATE_PURCHASE");
            throw new BusinessException(5005, errorMessage);
        }
        if (result != null && result == -4) {
            seckillMetrics.recordSeckillFail("STOCK_INSUFFICIENT");
        }
        throw new BusinessException(5004, errorMessage);
    }

    /**
     * 在事务中执行数据库操作
     */
    @Transactional(rollbackFor = Exception.class)
    public Long doJoinSeckillInTransaction(Long userId, Long activityId, SeckillActivity activity, 
                                           long expireSeconds, Long queueNumber) {
        antiFraudService.clearUserFail(userId, activityId);

        String eventId = UUID.randomUUID().toString();

        SeckillOrderEvent orderEvent = SeckillOrderEvent.builder()
                .userId(userId)
                .activityId(activityId)
                .productId(activity.getProductId())
                .seckillPrice(activity.getSeckillPrice())
                .quantity(1)
                .eventId(eventId)
                .seckillTime(LocalDateTime.now())
                .build();

        String messageContent;
        try {
            messageContent = objectMapper.writeValueAsString(orderEvent);
        } catch (Exception e) {
            log.error("序列化秒杀订单事件失败", e);
            throw new BusinessException(500, "系统繁忙，请稍后重试");
        }

        try {
            LocalMessage localMessage = LocalMessage.builder()
                    .messageId(eventId)
                    .messageType(com.fnusale.common.constant.LocalMessageStatus.MessageType.SECKILL_ORDER)
                    .topic(RocketMQConstants.SECKILL_ORDER_TOPIC)
                    .tag(RocketMQConstants.SECKILL_ORDER_TAG_CREATE)
                    .messageContent(messageContent)
                    .status(com.fnusale.common.constant.LocalMessageStatus.PENDING)
                    .retryCount(0)
                    .maxRetryCount(com.fnusale.common.constant.LocalMessageStatus.DEFAULT_MAX_RETRY_COUNT)
                    .nextRetryTime(LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            localMessageMapper.insert(localMessage);
        } catch (Exception e) {
            log.error("保存本地消息失败: userId={}, activityId={}", userId, activityId, e);
            throw new BusinessException(500, "系统繁忙，请稍后重试");
        }

        try {
            String timeKey = SECKILL_TIME_KEY_PREFIX + activityId + ":" + userId;
            String setKey = SECKILL_TIME_SET_PREFIX + activityId;
            
            redisTemplate.opsForValue().set(timeKey, 
                    String.valueOf(System.currentTimeMillis()), 
                    Duration.ofSeconds(expireSeconds));
            
            redisTemplate.opsForSet().add(setKey, userId.toString());
            redisTemplate.expire(setKey, Duration.ofSeconds(expireSeconds));
        } catch (Exception e) {
            log.warn("记录秒杀时间戳失败: userId={}, activityId={}", userId, activityId, e);
        }

        applicationEventPublisher.publishEvent(
                new SeckillSuccessAppEvent(this, orderEvent, null)
        );

        log.info("用户 {} 参与秒杀活动 {} 成功，排队号: {}", userId, activityId, queueNumber);
        return queueNumber;
    }

    /**
     * 回滚Redis
     */
    private void rollbackRedis(Long activityId, Long userId) {
        try {
            String stockKey = MarketingConstants.SECKILL_STOCK_KEY_PREFIX + activityId;
            String boughtKey = MarketingConstants.SECKILL_USER_BOUGHT_PREFIX + activityId;

            Long result = redisTemplate.execute(
                    seckillRollbackScript,
                    List.of(stockKey, boughtKey),
                    userId.toString()
            );

            if (result != null && result == 1) {
                log.warn("Redis 回滚成功: activityId={}, userId={}", activityId, userId);
            }
        } catch (Exception e) {
            log.error("Redis 回滚失败: activityId={}, userId={}", activityId, userId, e);
        }
    }

    private String getSeckillErrorMessage(Long resultCode) {
        if (resultCode == null) {
            return "系统繁忙，请稍后重试";
        }
        return switch (resultCode.intValue()) {
            case -2 -> "您已参与过该秒杀";
            case -1, -3 -> "系统繁忙，请稍后重试";
            case -4 -> "秒杀库存不足";
            default -> "系统繁忙，请稍后重试";
        };
    }
}
