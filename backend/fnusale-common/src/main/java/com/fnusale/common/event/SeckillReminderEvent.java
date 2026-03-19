package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀提醒事件
 * 用于推送秒杀开始前的提醒通知
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillReminderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀活动ID
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 需要提醒的用户ID列表
     */
    private List<Long> userIds;

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 提醒时间
     */
    private LocalDateTime remindTime;
}