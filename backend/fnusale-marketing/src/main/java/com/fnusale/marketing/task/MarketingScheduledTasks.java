package com.fnusale.marketing.task;

import com.fnusale.common.entity.SeckillActivity;
import com.fnusale.common.entity.UserCoupon;
import com.fnusale.common.event.CouponExpireReminderEvent;
import com.fnusale.marketing.mapper.SeckillActivityMapper;
import com.fnusale.marketing.mapper.UserCouponMapper;
import com.fnusale.marketing.service.LocalMessageService;
import com.fnusale.marketing.service.MarketingEventPublisher;
import com.fnusale.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 营销模块定时任务
 * 
 * v4优化：
 * - 添加分布式锁，避免多实例重复执行
 * - 使用Redisson的看门狗机制自动续期
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketingScheduledTasks {

    private final SeckillService seckillService;
    private final UserCouponMapper userCouponMapper;
    private final LocalMessageService localMessageService;
    private final MarketingEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final SeckillActivityMapper activityMapper;
    private final RedissonClient redissonClient;

    /**
     * 优惠券过期提醒 Key 前缀
     */
    private static final String COUPON_EXPIRE_REMIND_KEY_PREFIX = "coupon:expire:remind:";
    
    /**
     * 秒杀监控指标 Key 前缀
     */
    private static final String SECKILL_METRICS_PREFIX = "seckill:metrics:";
    
    /**
     * 秒杀成功率告警阈值
     */
    private static final double SUCCESS_RATE_THRESHOLD = 0.5;
    
    /**
     * 秒杀最低请求量（低于此值不告警）
     */
    private static final int MIN_REQUEST_COUNT = 10;

    /**
     * 优惠券过期处理 - 每日01:00执行
     * 使用分布式锁避免多实例重复执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processExpiredCoupons() {
        RLock lock = redissonClient.getLock("seckill:task:processExpiredCoupons");
        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                log.info("开始处理过期优惠券...");
                int count = userCouponMapper.updateExpiredCoupons();
                log.info("处理过期优惠券完成，共 {} 张", count);
            } else {
                log.debug("未获取到锁，跳过本次执行: processExpiredCoupons");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
        } catch (Exception e) {
            log.error("处理过期优惠券失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 秒杀活动状态更新 - 每分钟执行
     * 使用分布式锁避免多实例重复执行
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateActivityStatus() {
        RLock lock = redissonClient.getLock("seckill:task:updateActivityStatus");
        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                seckillService.updateActivityStatus();
            } else {
                log.debug("未获取到锁，跳过本次执行: updateActivityStatus");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
        } catch (Exception e) {
            log.error("更新秒杀活动状态失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 秒杀库存预热 - 每5分钟检查一次
     * 使用分布式锁避免多实例重复执行
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void preloadSeckillStock() {
        RLock lock = redissonClient.getLock("seckill:task:preloadSeckillStock");
        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                log.debug("检查秒杀库存预热...");
                seckillService.preloadStock();
            } else {
                log.debug("未获取到锁，跳过本次执行: preloadSeckillStock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
        } catch (Exception e) {
            log.error("秒杀库存预热失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 秒杀提醒推送 - 每5分钟检查一次
     * 使用分布式锁避免多实例重复执行
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void pushSeckillReminders() {
        RLock lock = redissonClient.getLock("seckill:task:pushSeckillReminders");
        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                log.debug("检查秒杀提醒推送...");
                seckillService.pushReminders();
            } else {
                log.debug("未获取到锁，跳过本次执行: pushSeckillReminders");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
        } catch (Exception e) {
            log.error("秒杀提醒推送失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 本地消息表重试 - 每分钟执行
     * 确保分布式事务最终一致性
     * 使用分布式锁避免多实例重复执行
     */
    @Scheduled(cron = "0 * * * * ?")
    public void retryPendingMessages() {
        RLock lock = redissonClient.getLock("seckill:task:retryPendingMessages");
        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                log.debug("检查待重试的本地消息...");
                localMessageService.processPendingMessages();
            } else {
                log.debug("未获取到锁，跳过本次执行: retryPendingMessages");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
        } catch (Exception e) {
            log.error("本地消息重试失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 清理已发送的旧消息 - 每日02:00执行
     * 使用分布式锁避免多实例重复执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldMessages() {
        RLock lock = redissonClient.getLock("seckill:task:cleanOldMessages");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                log.info("开始清理已发送的旧消息...");
                localMessageService.cleanOldMessages();
            } else {
                log.debug("未获取到锁，跳过本次执行: cleanOldMessages");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
        } catch (Exception e) {
            log.error("清理旧消息失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 优惠券过期提醒 - 每日09:00执行
     * 扫描3天内即将过期的优惠券，发送提醒
     * 使用分布式锁避免多实例重复执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendCouponExpireReminders() {
        RLock lock = redissonClient.getLock("seckill:task:sendCouponExpireReminders");
        try {
            if (lock.tryLock(0, 60, TimeUnit.SECONDS)) {
                doSendCouponExpireReminders();
            } else {
                log.debug("未获取到锁，跳过本次执行: sendCouponExpireReminders");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
        } catch (Exception e) {
            log.error("优惠券过期提醒失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    private void doSendCouponExpireReminders() {
        log.info("开始扫描即将过期的优惠券...");
        try {
            // 查询3天内即将过期的优惠券
            List<UserCoupon> expiringCoupons = userCouponMapper.selectExpiringSoon(3);
            if (expiringCoupons.isEmpty()) {
                log.info("没有即将过期的优惠券");
                return;
            }

            log.info("发现 {} 张即将过期的优惠券，开始发送提醒...", expiringCoupons.size());

            // 按用户分组发送提醒
            int reminderCount = 0;
            for (UserCoupon uc : expiringCoupons) {
                try {
                    // 幂等性检查（当天只提醒一次）
                    String remindKey = COUPON_EXPIRE_REMIND_KEY_PREFIX + uc.getId() + ":" + LocalDate.now();
                    Boolean sent = redisTemplate.opsForValue().setIfAbsent(remindKey, "1", 1, TimeUnit.DAYS);
                    if (Boolean.FALSE.equals(sent)) {
                        continue;
                    }

                    // 发送过期提醒事件
                    CouponExpireReminderEvent.UserCouponInfo info = CouponExpireReminderEvent.UserCouponInfo.builder()
                            .userCouponId(uc.getId())
                            .userId(uc.getUserId())
                            .couponName(uc.getCouponName())
                            .reduceAmount(uc.getReduceAmount())
                            .expireTime(uc.getExpireTime())
                            .build();

                    CouponExpireReminderEvent event = CouponExpireReminderEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .userCouponId(uc.getId())
                            .userId(uc.getUserId())
                            .couponId(uc.getCouponId())
                            .couponName(uc.getCouponName())
                            .reduceAmount(uc.getReduceAmount())
                            .expireTime(uc.getExpireTime())
                            .build();

                    eventPublisher.publishCouponExpireReminderEvent(event);
                    reminderCount++;
                } catch (Exception e) {
                    log.warn("发送优惠券过期提醒失败, userCouponId: {}", uc.getId(), e);
                }
            }

            log.info("优惠券过期提醒发送完成，共发送 {} 条提醒", reminderCount);
        } catch (Exception e) {
            log.error("扫描即将过期优惠券失败", e);
        }
    }
    
    /**
     * 秒杀监控告警 - 每5分钟执行
     * 检查秒杀成功率，低于阈值则告警
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkSeckillMetrics() {
        log.debug("检查秒杀监控指标...");
        try {
            String today = LocalDate.now().toString();
            
            List<SeckillActivity> activities = activityMapper.selectOngoingActivities();
            if (activities == null || activities.isEmpty()) {
                return;
            }
            
            for (SeckillActivity activity : activities) {
                Long activityId = activity.getId();
                String successKey = SECKILL_METRICS_PREFIX + activityId + ":" + today + ":success";
                String totalKey = SECKILL_METRICS_PREFIX + activityId + ":" + today + ":total";
                
                String successCount = redisTemplate.opsForValue().get(successKey);
                String totalCount = redisTemplate.opsForValue().get(totalKey);
                
                if (totalCount != null) {
                    long total = Long.parseLong(totalCount);
                    if (total >= MIN_REQUEST_COUNT) {
                        long success = successCount != null ? Long.parseLong(successCount) : 0;
                        double successRate = (double) success / total;
                        
                        if (successRate < SUCCESS_RATE_THRESHOLD) {
                            log.warn("秒杀成功率告警: activityId={}, activityName={}, successRate={}, success={}, total={}", 
                                    activityId, activity.getActivityName(), 
                                    String.format("%.2f%%", successRate * 100), success, total);
                            sendAlert(activity, successRate, success, total);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查秒杀监控指标失败", e);
        }
    }
    
    /**
     * 发送告警通知
     */
    private void sendAlert(SeckillActivity activity, double successRate, long success, long total) {
        String alertMessage = String.format(
                "秒杀活动异常告警\n活动ID: %d\n活动名称: %s\n成功率: %.2f%%\n成功数: %d\n总请求数: %d",
                activity.getId(),
                activity.getActivityName(),
                successRate * 100,
                success,
                total
        );
        
        log.warn("秒杀告警通知: {}", alertMessage);
    }
}