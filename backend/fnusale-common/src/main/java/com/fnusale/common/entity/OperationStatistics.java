package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运营数据统计表
 */
@Data
@TableName("t_operation_statistics")
public class OperationStatistics {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 当日商品发布数
     */
    private Integer productPublishCount;

    /**
     * 当日成交订单数
     */
    private Integer orderSuccessCount;

    /**
     * 当日秒杀参与数
     */
    private Integer seckillParticipateCount;

    /**
     * AI分类准确率（%）
     */
    private BigDecimal aiCategoryAccuracy;

    /**
     * 当日优惠券使用数
     */
    private Integer couponUseCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}