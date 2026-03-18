package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动表
 */
@Data
@TableName("t_seckill_activity")
public class SeckillActivity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀价格（元）
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀总库存
     */
    private Integer totalStock;

    /**
     * 剩余库存
     */
    private Integer remainStock;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态（NOT_START/ON_GOING/END）
     */
    private String activityStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}