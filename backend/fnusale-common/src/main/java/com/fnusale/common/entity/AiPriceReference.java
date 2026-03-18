package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI价格参考表
 */
@Data
@TableName("t_ai_price_reference")
public class AiPriceReference {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 品类ID
     */
    private Long categoryId;

    /**
     * 新旧程度
     */
    private String newDegree;

    /**
     * 参考最低价格（元）
     */
    private BigDecimal minPrice;

    /**
     * 参考最高价格（元）
     */
    private BigDecimal maxPrice;

    /**
     * 参考样本数
     */
    private Integer sampleCount;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}