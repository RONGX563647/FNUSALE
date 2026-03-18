package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品基础表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
public class Product extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 品类ID
     */
    private Long categoryId;

    /**
     * 新旧程度（NEW/90_NEW/80_NEW/70_NEW/OLD）
     */
    private String newDegree;

    /**
     * 售价（元）
     */
    private BigDecimal price;

    /**
     * 原价（元）
     */
    private BigDecimal originalPrice;

    /**
     * 商品描述
     */
    private String productDesc;

    /**
     * 是否秒杀商品（0-否, 1-是）
     */
    private Integer isSeckill;

    /**
     * 秒杀库存
     */
    private Integer seckillStock;

    /**
     * 自提点ID
     */
    private Long pickPointId;

    /**
     * 发布时定位经度
     */
    private BigDecimal longitude;

    /**
     * 发布时定位纬度
     */
    private BigDecimal latitude;

    /**
     * 状态（DRAFT/ON_SHELF/SOLD_OUT/OFF_SHELF/ILLEGAL）
     */
    private String productStatus;

    /**
     * 违规原因
     */
    private String illegalReason;

    /**
     * AI分类结果
     */
    private String aiCategoryResult;
}