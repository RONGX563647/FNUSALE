package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 优惠券发放事件
 * 用于批量异步发放优惠券
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponGrantEvent {

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 批次ID（用于统计发放进度）
     */
    private String batchId;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 事件ID（用于幂等性）
     */
    private String eventId;

    /**
     * 发放时间
     */
    private LocalDateTime grantTime;
}