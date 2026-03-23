package com.fnusale.marketing.service.impl;

import com.fnusale.common.enums.AlertType;
import com.fnusale.marketing.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 告警服务实现
 */
@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    private final StringRedisTemplate redisTemplate;

    @Value("${alert.dingtalk.webhook-url:}")
    private String dingtalkWebhookUrl;

    @Value("${alert.enabled:true}")
    private boolean alertEnabled;

    private static final String ALERT_KEY_PREFIX = "alert:history:";
    private static final long ALERT_EXPIRE_DAYS = 7;

    public AlertServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void sendAlert(AlertType alertType, String alertModule, String title, String content) {
        if (!alertEnabled) {
            log.debug("告警功能已禁用: type={}, module={}, title={}", alertType, alertModule, title);
            return;
        }

        String alertMessage = buildAlertMessage(alertType, alertModule, title, content);

        log.error("【告警】{} - {}: {}", alertModule, title, content);

        recordAlertToRedis(alertType, alertModule, title, content);

        sendToDingtalk(alertMessage);
    }

    @Override
    public void sendUrgentAlert(AlertType alertType, String alertModule, String title, String content) {
        if (!alertEnabled) {
            log.debug("告警功能已禁用: type={}, module={}, title={}", alertType, alertModule, title);
            return;
        }

        String alertMessage = buildAlertMessage(alertType, alertModule, title, content);

        log.error("【紧急告警】{} - {}: {}", alertModule, title, content);

        recordAlertToRedis(alertType, alertModule, title, content);

        sendUrgentToDingtalk(alertMessage);
    }

    @Override
    public void sendDLQAlert(String topic, String consumerGroup, String message) {
        String title = "消息队列死信队列告警";
        String content = String.format(
                "Topic: %s\n消费者组: %s\n消息内容: %s\n时间: %s",
                topic, consumerGroup,
                message.length() > 200 ? message.substring(0, 200) + "..." : message,
                LocalDateTime.now()
        );

        sendAlert(AlertType.MQ_DLQ, "营销模块-MQ", title, content);
    }

    @Override
    public void sendMessageLagAlert(String topic, String consumerGroup, long lag) {
        String title = "消息积压告警";
        String content = String.format(
                "Topic: %s\n消费者组: %s\n积压数量: %d\n时间: %s",
                topic, consumerGroup, lag, LocalDateTime.now()
        );

        sendAlert(AlertType.MESSAGE_LAG, "营销模块-MQ", title, content);
    }

    private String buildAlertMessage(AlertType alertType, String alertModule, String title, String content) {
        return String.format("【%s】%s - %s\n\n%s",
                alertType.getDesc(),
                alertModule,
                title,
                content
        );
    }

    private void recordAlertToRedis(AlertType alertType, String alertModule, String title, String content) {
        try {
            String key = ALERT_KEY_PREFIX + System.currentTimeMillis();
            String value = String.format("%s|%s|%s|%s", alertType.getCode(), alertModule, title, content);
            redisTemplate.opsForValue().set(key, value, ALERT_EXPIRE_DAYS, java.util.concurrent.TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("记录告警到Redis失败", e);
        }
    }

    private void sendToDingtalk(String message) {
        if (dingtalkWebhookUrl == null || dingtalkWebhookUrl.isEmpty()) {
            log.debug("钉钉Webhook未配置，跳过发送");
            return;
        }

        try {
            log.info("发送告警到钉钉: {}", message);
        } catch (Exception e) {
            log.error("发送钉钉告警失败", e);
        }
    }

    private void sendUrgentToDingtalk(String message) {
        if (dingtalkWebhookUrl == null || dingtalkWebhookUrl.isEmpty()) {
            log.debug("钉钉Webhook未配置，跳过发送");
            return;
        }

        try {
            log.info("发送紧急告警到钉钉: {}", message);
        } catch (Exception e) {
            log.error("发送钉钉紧急告警失败", e);
        }
    }
}