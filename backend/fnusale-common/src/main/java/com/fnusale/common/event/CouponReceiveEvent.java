package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券领取事件
 * 用于异步处理用户领券
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponReceiveEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 事件ID（用于幂等性）
     */
    private String eventId;

    /**
     * 领取时间
     */
    private LocalDateTime receiveTime;
}