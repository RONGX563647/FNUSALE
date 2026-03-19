package com.fnusale.product.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品事件消息
 * 用于商品发布、更新、删除、上下架等事件通知
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（用于幂等性处理）
     */
    private String messageId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 用户ID（发布者）
     */
    private Long userId;

    /**
     * 事件类型
     * PUBLISH - 发布
     * UPDATE - 更新
     * DELETE - 删除
     * ON_SHELF - 上架
     * OFF_SHELF - 下架
     * SOLD_OUT - 售出
     */
    private String eventType;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 品类ID
     */
    private Long categoryId;

    /**
     * 品类名称
     */
    private String categoryName;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品状态
     */
    private String productStatus;

    /**
     * 旧状态（状态变更时使用）
     */
    private String oldStatus;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 事件类型常量
     */
    public static final String EVENT_PUBLISH = "PUBLISH";
    public static final String EVENT_UPDATE = "UPDATE";
    public static final String EVENT_DELETE = "DELETE";
    public static final String EVENT_ON_SHELF = "ON_SHELF";
    public static final String EVENT_OFF_SHELF = "OFF_SHELF";
    public static final String EVENT_SOLD_OUT = "SOLD_OUT";
}