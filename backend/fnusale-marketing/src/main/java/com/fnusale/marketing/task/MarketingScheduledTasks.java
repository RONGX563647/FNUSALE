package com.fnusale.marketing.task;

import com.fnusale.marketing.mapper.UserCouponMapper;
import com.fnusale.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 营销模块定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketingScheduledTasks {

    private final SeckillService seckillService;
    private final UserCouponMapper userCouponMapper;

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
}