package com.fnusale.marketing.service;

import com.fnusale.common.enums.AlertType;

/**
 * 告警服务接口
 */
public interface AlertService {

    /**
     * 发送告警
     *
     * @param alertType  告警类型
     * @param alertModule 告警模块
     * @param title      告警标题
     * @param content    告警内容
     */
    void sendAlert(AlertType alertType, String alertModule, String title, String content);

    /**
     * 发送紧急告警
     *
     * @param alertType  告警类型
     * @param alertModule 告警模块
     * @param title      告警标题
     * @param content    告警内容
     */
    void sendUrgentAlert(AlertType alertType, String alertModule, String title, String content);

    /**
     * 发送死信队列告警
     *
     * @param topic        Topic
     * @param consumerGroup 消费者组
     * @param message      消息内容
     */
    void sendDLQAlert(String topic, String consumerGroup, String message);

    /**
     * 发送消息积压告警
     *
     * @param topic        Topic
     * @param consumerGroup 消费者组
     * @param lag          积压数量
     */
    void sendMessageLagAlert(String topic, String consumerGroup, long lag);
}