package com.fnusale.marketing.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.common.constant.LocalMessageStatus;
import com.fnusale.common.entity.LocalMessage;
import com.fnusale.marketing.mapper.LocalMessageMapper;
import com.fnusale.marketing.service.LocalMessageService;
import com.fnusale.marketing.service.MarketingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalMessageServiceImpl implements LocalMessageService {

    private final LocalMessageMapper localMessageMapper;
    private final MarketingEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public LocalMessage saveMessage(String messageId, String messageType, String topic, String tag, String messageContent) {
        LocalMessage message = LocalMessage.builder()
                .messageId(messageId)
                .messageType(messageType)
                .topic(topic)
                .tag(tag)
                .messageContent(messageContent)
                .status(LocalMessageStatus.PENDING)
                .retryCount(0)
                .maxRetryCount(LocalMessageStatus.DEFAULT_MAX_RETRY_COUNT)
                .nextRetryTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        localMessageMapper.insert(message);
        log.debug("保存本地消息: messageId={}, type={}", messageId, messageType);
        return message;
    }

    @Override
    public void processPendingMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<LocalMessage> messages = localMessageMapper.selectPendingMessages(now, 100);

        for (LocalMessage message : messages) {
            try {
                // 幂等性检查：避免重复发送
                LocalMessage latestMessage = localMessageMapper.selectByMessageId(message.getMessageId());
                if (latestMessage == null) {
                    log.warn("消息已被删除，跳过: messageId={}", message.getMessageId());
                    continue;
                }

                // 如果消息已发送，跳过
                if (LocalMessageStatus.SENT.equals(latestMessage.getStatus())) {
                    log.debug("消息已发送，跳过: messageId={}", message.getMessageId());
                    continue;
                }

                // 发送消息到MQ
                try {
                    eventPublisher.sendRawMessage(message.getTopic(), message.getTag(), message.getMessageContent());
                    // 发送成功，更新为已发送
                    localMessageMapper.updateToSent(message.getId());
                    log.info("消息发送成功: messageId={}", message.getMessageId());
                } catch (Exception sendEx) {
                    // 发送失败，计算下次重试时间
                    log.warn("消息发送失败: messageId={}, error={}", message.getMessageId(), sendEx.getMessage());
                    int retryIndex = Math.min(message.getRetryCount(), LocalMessageStatus.RETRY_INTERVALS.length - 1);
                    LocalDateTime nextRetryTime = LocalDateTime.now().plusMinutes(LocalMessageStatus.RETRY_INTERVALS[retryIndex]);

                    // 更新为失败状态
                    localMessageMapper.updateToFailed(message.getId(), nextRetryTime);

                    // 如果超过最大重试次数，记录错误日志
                    if (message.getRetryCount() + 1 >= message.getMaxRetryCount()) {
                        log.error("消息超过最大重试次数，需要人工处理: messageId={}, content={}",
                                message.getMessageId(), message.getMessageContent());
                    }
                }
            } catch (Exception e) {
                log.error("消息处理异常: messageId={}, error={}", message.getMessageId(), e.getMessage());

                // 计算下次重试时间
                int retryIndex = Math.min(message.getRetryCount(), LocalMessageStatus.RETRY_INTERVALS.length - 1);
                LocalDateTime nextRetryTime = LocalDateTime.now().plusMinutes(LocalMessageStatus.RETRY_INTERVALS[retryIndex]);

                // 更新为失败状态
                localMessageMapper.updateToFailed(message.getId(), nextRetryTime);

                // 如果超过最大重试次数，记录错误日志
                if (message.getRetryCount() + 1 >= message.getMaxRetryCount()) {
                    log.error("消息超过最大重试次数，需要人工处理: messageId={}, content={}",
                            message.getMessageId(), message.getMessageContent());
                }
            }
        }
    }

    @Override
    public void cleanOldMessages() {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(7);
        int count = localMessageMapper.cleanOldMessages(beforeTime);
        if (count > 0) {
            log.info("清理已发送的旧消息: {} 条", count);
        }
    }

    @Override
    public boolean isProcessed(String messageId) {
        LocalMessage message = localMessageMapper.selectByMessageId(messageId);
        return message != null && LocalMessageStatus.SENT.equals(message.getStatus());
    }
}