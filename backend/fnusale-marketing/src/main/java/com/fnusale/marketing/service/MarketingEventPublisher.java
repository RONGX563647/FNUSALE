package com.fnusale.marketing.service;

import com.fnusale.common.event.CouponExpireReminderEvent;
import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.common.event.CouponReceiveEvent;
import com.fnusale.common.event.SeckillOrderEvent;
import com.fnusale.common.event.SeckillReminderEvent;
import com.fnusale.common.event.SeckillWarmUpEvent;

import java.util.List;

/**
 * 营销事件发布服务
 */
public interface MarketingEventPublisher {

    /**
     * 发布秒杀订单事件
     */
    void publishSeckillOrderEvent(SeckillOrderEvent event);

    /**
     * 发布秒杀订单事件（顺序消息）
     * 相同 userId 的消息会发送到同一队列，保证顺序性
     *
     * @param event       事件
     * @param hashKey     哈希键（通常使用 userId）
     */
    void publishSeckillOrderEventOrdered(SeckillOrderEvent event, String hashKey);

    /**
     * 发布秒杀提醒事件
     */
    void publishSeckillReminderEvent(SeckillReminderEvent event);

    /**
     * 发布优惠券发放事件
     */
    void publishCouponGrantEvent(CouponGrantEvent event);

    /**
     * 发布优惠券发放事件（顺序消息）
     * 相同 userId 的消息会发送到同一队列，保证顺序性
     *
     * @param event       事件
     * @param hashKey     哈希键（通常使用 userId）
     */
    void publishCouponGrantEventOrdered(CouponGrantEvent event, String hashKey);

    /**
     * 批量发布优惠券发放事件
     */
    void publishCouponGrantBatch(List<CouponGrantEvent> events);

    /**
     * 批量发布优惠券发放事件（顺序消息）
     * 相同 hashKey 的消息会发送到同一队列
     *
     * @param events      事件列表
     * @param hashKeys    哈希键列表（与事件一一对应）
     */
    void publishCouponGrantBatchOrdered(List<CouponGrantEvent> events, List<String> hashKeys);

    /**
     * 发布优惠券领取事件
     */
    void publishCouponReceiveEvent(CouponReceiveEvent event);

    /**
     * 发布优惠券领取事件（顺序消息）
     *
     * @param event       事件
     * @param hashKey     哈希键
     */
    void publishCouponReceiveEventOrdered(CouponReceiveEvent event, String hashKey);

    /**
     * 发布秒杀预热事件（延迟消息）
     *
     * @param event      预热事件
     * @param delayLevel 延迟级别 (1-18)
     */
    void publishSeckillWarmUpEvent(SeckillWarmUpEvent event, int delayLevel);

    /**
     * 发布优惠券过期提醒事件
     */
    void publishCouponExpireReminderEvent(CouponExpireReminderEvent event);

    /**
     * 发送原始消息（用于本地消息表重试）
     *
     * @param topic          Topic
     * @param tag            Tag
     * @param messageContent 消息内容
     */
    void sendRawMessage(String topic, String tag, String messageContent);

    /**
     * 同步发送消息（用于事务内发送）
     *
     * @param topic   Topic
     * @param tag     Tag
     * @param message 消息对象
     * @return 是否发送成功
     */
    boolean sendSync(String topic, String tag, Object message);
}