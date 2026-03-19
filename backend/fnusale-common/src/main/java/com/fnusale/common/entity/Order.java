package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单基础表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品原价（元）
     */
    private BigDecimal productPrice;

    /**
     * 优惠券抵扣金额（元）
     */
    private BigDecimal couponDeductAmount;

    /**
     * 实付金额（元）
     */
    private BigDecimal actualPayAmount;

    /**
     * 自提点ID
     */
    private Long pickPointId;

    /**
     * 支付方式（WECHAT/ALIPAY/CAMPUS_CARD）
     */
    private String payType;

    /**
     * 支付状态（UNPAID/PAID/REFUNDED）
     */
    private String payStatus;

    /**
     * 订单状态（UNPAID/WAIT_PICK/SUCCESS/CANCEL）
     */
    private String orderStatus;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 成交时间
     */
    private LocalDateTime successTime;

    /**
     * 商品备好时间（卖家标记）
     */
    private LocalDateTime readyTime;

    /**
     * 延长收货天数
     */
    private Integer extendReceiveDays;

    /**
     * 关联的优惠券ID
     */
    private Long couponId;
}