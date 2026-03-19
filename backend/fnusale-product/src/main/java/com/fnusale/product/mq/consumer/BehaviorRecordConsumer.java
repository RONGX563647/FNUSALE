package com.fnusale.product.mq.consumer;

import com.fnusale.product.mq.config.RabbitMQConfig;
import com.fnusale.product.mq.message.UserBehaviorMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户行为记录消费者
 * 使用内存队列批量处理浏览记录，提高写入性能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BehaviorRecordConsumer {

    // 内存批量队列
    private final Map<String, UserBehaviorMessage> browseQueue = new ConcurrentHashMap<>();
    private final AtomicInteger queueSize = new AtomicInteger(0);

    // 批量处理阈值
    private static final int BATCH_SIZE = 100;
    private static final String BROWSE_KEY_PREFIX = "behavior:browse:";

    /**
     * 消费用户行为消息 - 行为记录
     */
    @RabbitListener(queues = RabbitMQConfig.BEHAVIOR_RECORD_QUEUE)
    public void handleUserBehavior(UserBehaviorMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            // 浏览行为使用批量处理
            if (UserBehaviorMessage.BEHAVIOR_BROWSE.equals(message.getBehaviorType())) {
                addToBatchQueue(message);
            } else {
                // 收藏/点赞直接处理
                processBehaviorDirectly(message);
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("行为记录处理失败: messageId={}", message.getMessageId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 添加到批量队列
     */
    private void addToBatchQueue(UserBehaviorMessage message) {
        String key = message.getUserId() + ":" + message.getProductId();
        browseQueue.put(key, message);
        int size = queueSize.incrementAndGet();

        // 达到批量阈值时触发写入
        if (size >= BATCH_SIZE) {
            flushBrowseQueue();
        }
    }

    /**
     * 定时刷新批量队列（每5秒）
     */
    @Scheduled(fixedRate = 5000)
    public void scheduledFlush() {
        if (!browseQueue.isEmpty()) {
            flushBrowseQueue();
        }
    }

    /**
     * 批量写入浏览记录
     */
    private synchronized void flushBrowseQueue() {
        if (browseQueue.isEmpty()) {
            return;
        }

        List<UserBehaviorMessage> batch = new ArrayList<>(browseQueue.values());
        browseQueue.clear();
        queueSize.set(0);

        log.info("批量写入浏览记录: size={}", batch.size());

        // TODO: 批量写入数据库
        // userBehaviorMapper.batchInsert(batch);
    }

    /**
     * 直接处理行为（收藏/点赞等）
     */
    private void processBehaviorDirectly(UserBehaviorMessage message) {
        log.debug("直接处理用户行为: userId={}, productId={}, behaviorType={}",
                message.getUserId(), message.getProductId(), message.getBehaviorType());

        // 实际的数据库写入在 UserBehaviorServiceImpl 中已完成
        // 这里可以做额外的处理，如发送通知等
    }
}