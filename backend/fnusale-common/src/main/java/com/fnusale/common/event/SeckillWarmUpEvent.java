package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动预热事件
 * 用于活动开始前预热库存
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillWarmUpEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀活动ID
     */
    private Long activityId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 事件ID
     */
    private String eventId;
}