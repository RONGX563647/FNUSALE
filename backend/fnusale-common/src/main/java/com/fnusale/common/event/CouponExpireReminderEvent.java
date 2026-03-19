package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券过期提醒事件
 * 用于通知用户优惠券即将过期
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponExpireReminderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户优惠券ID
     */
    private Long userCouponId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠金额
     */
    private java.math.BigDecimal reduceAmount;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 需要提醒的用户列表（批量提醒时使用）
     */
    private List<UserCouponInfo> userCouponList;

    /**
     * 用户优惠券信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCouponInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userCouponId;
        private Long userId;
        private String couponName;
        private java.math.BigDecimal reduceAmount;
        private LocalDateTime expireTime;
    }
}