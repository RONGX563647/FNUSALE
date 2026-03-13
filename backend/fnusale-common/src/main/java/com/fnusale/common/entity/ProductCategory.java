package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品品类表
 */
@Data
@TableName("t_product_category")
public class ProductCategory {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 品类名称
     */
    private String categoryName;

    /**
     * 父品类ID
     */
    private Long parentCategoryId;

    /**
     * AI分类映射值
     */
    private String aiMappingValue;

    /**
     * 启用状态（0-禁用, 1-启用）
     */
    private Integer enableStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}