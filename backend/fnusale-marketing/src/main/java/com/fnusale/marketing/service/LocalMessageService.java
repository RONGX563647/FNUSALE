package com.fnusale.marketing.service;

import com.fnusale.common.entity.LocalMessage;

/**
 * 本地消息服务接口
 */
public interface LocalMessageService {

    /**
     * 保存消息（用于事务内调用）
     *
     * @param messageId      消息唯一ID
     * @param messageType    消息类型
     * @param topic          目标Topic
     * @param tag            目标Tag
     * @param messageContent 消息内容（JSON）
     * @return 本地消息实体
     */
    LocalMessage saveMessage(String messageId, String messageType, String topic, String tag, String messageContent);

    /**
     * 处理待重试的消息
     */
    void processPendingMessages();

    /**
     * 清理已发送的旧消息
     */
    void cleanOldMessages();

    /**
     * 检查消息是否已处理（幂等性检查）
     *
     * @param messageId 消息ID
     * @return 是否已处理
     */
    boolean isProcessed(String messageId);
}