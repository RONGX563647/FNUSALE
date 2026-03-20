package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单事件
 * 用于异步创建秒杀订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderEvent {

    /**
     * 用户ID
     */
    private Long userId;

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
     * 购买数量
     */
    private Integer quantity;

    /**
     * 事件ID（用于幂等性处理）
     */
    private String eventId;

    /**
     * 秒杀时间
     */
    private LocalDateTime seckillTime;
}