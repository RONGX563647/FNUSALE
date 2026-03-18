package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券表
 */
@Data
@TableName("t_coupon")
public class Coupon {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 类型（FULL_REDUCE/DIRECT_REDUCE/CATEGORY）
     */
    private String couponType;

    /**
     * 满减金额
     */
    private BigDecimal fullAmount;

    /**
     * 抵扣金额
     */
    private BigDecimal reduceAmount;

    /**
     * 品类ID
     */
    private Long categoryId;

    /**
     * 发放总数
     */
    private Integer totalCount;

    /**
     * 已领数量
     */
    private Integer receivedCount;

    /**
     * 已用数量
     */
    private Integer usedCount;

    /**
     * 有效期开始时间
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endTime;

    /**
     * 启用状态（0-禁用, 1-启用）
     */
    private Integer enableStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}