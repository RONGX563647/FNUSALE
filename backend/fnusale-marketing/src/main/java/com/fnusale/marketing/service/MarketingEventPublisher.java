package com.fnusale.marketing.service;

import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.common.event.SeckillReminderEvent;

/**
 * 营销事件发布服务
 */
public interface MarketingEventPublisher {

    /**
     * 发布秒杀订单事件
     */
    void publishSeckillOrderEvent(SeckillOrderEvent event);

    /**
     * 发布秒杀提醒事件
     */
    void publishSeckillReminderEvent(SeckillReminderEvent event);

    /**
     * 发布优惠券发放事件
     */
    void publishCouponGrantEvent(CouponGrantEvent event);
}