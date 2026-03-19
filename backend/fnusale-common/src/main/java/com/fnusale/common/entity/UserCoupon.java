package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_coupon")
public class UserCoupon {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 领取时间
     */
    private LocalDateTime receiveTime;

    /**
     * 使用时间
     */
    private LocalDateTime useTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态（UNUSED/USED/EXPIRED）
     */
    private String couponStatus;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ========== 以下为关联查询字段（非数据库字段） ==========

    /**
     * 优惠券名称（关联查询）
     */
    @TableField(exist = false)
    private String couponName;

    /**
     * 优惠券类型（关联查询）
     */
    @TableField(exist = false)
    private String couponType;

    /**
     * 满减门槛金额（关联查询）
     */
    @TableField(exist = false)
    private BigDecimal fullAmount;

    /**
     * 抵扣金额（关联查询）
     */
    @TableField(exist = false)
    private BigDecimal reduceAmount;

    /**
     * 品类ID（关联查询）
     */
    @TableField(exist = false)
    private Long categoryId;
}