package com.fnusale.marketing.task;

import com.fnusale.common.entity.UserCoupon;
import com.fnusale.common.event.CouponExpireReminderEvent;
import com.fnusale.marketing.mapper.UserCouponMapper;
import com.fnusale.marketing.service.LocalMessageService;
import com.fnusale.marketing.service.MarketingEventPublisher;
import com.fnusale.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 优惠券过期提醒 Key 前缀
     */
    private static final String COUPON_EXPIRE_REMIND_KEY_PREFIX = "coupon:expire:remind:";

    /**
     * 优惠券过期处理 - 每日01:00执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processExpiredCoupons() {
        log.info("开始处理过期优惠券...");
        try {
            int count = userCouponMapper.updateExpiredCoupons();
            log.info("处理过期优惠券完成，共 {} 张", count);
        } catch (Exception e) {
            log.error("处理过期优惠券失败", e);
        }
    }

    /**
     * 秒杀活动状态更新 - 每分钟执行
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateActivityStatus() {
        try {
            seckillService.updateActivityStatus();
        } catch (Exception e) {
            log.error("更新秒杀活动状态失败", e);
        }
    }

    /**
     * 秒杀库存预热 - 每5分钟检查一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void preloadSeckillStock() {
        log.debug("检查秒杀库存预热...");
        try {
            seckillService.preloadStock();
        } catch (Exception e) {
            log.error("秒杀库存预热失败", e);
        }
    }

    /**
     * 秒杀提醒推送 - 每5分钟检查一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void pushSeckillReminders() {
        log.debug("检查秒杀提醒推送...");
        try {
            seckillService.pushReminders();
        } catch (Exception e) {
            log.error("秒杀提醒推送失败", e);
        }
    }

    /**
     * 本地消息表重试 - 每分钟执行
     * 确保分布式事务最终一致性
     */
    @Scheduled(cron = "0 * * * * ?")
    public void retryPendingMessages() {
        log.debug("检查待重试的本地消息...");
        try {
            localMessageService.processPendingMessages();
        } catch (Exception e) {
            log.error("本地消息重试失败", e);
        }
    }

    /**
     * 清理已发送的旧消息 - 每日02:00执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldMessages() {
        log.info("开始清理已发送的旧消息...");
        try {
            localMessageService.cleanOldMessages();
        } catch (Exception e) {
            log.error("清理旧消息失败", e);
        }
    }

    /**
     * 优惠券过期提醒 - 每日09:00执行
     * 扫描3天内即将过期的优惠券，发送提醒
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendCouponExpireReminders() {
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
}