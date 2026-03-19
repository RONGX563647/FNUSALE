package com.fnusale.product.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI任务消息
 * 用于异步执行AI识别任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AITaskMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务类型
     * CATEGORY_RECOGNIZE - 品类识别
     * PRICE_SUGGEST - 价格建议
     */
    private String taskType;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 商品ID（价格建议时使用）
     */
    private Long productId;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 任务类型常量
     */
    public static final String TASK_CATEGORY_RECOGNIZE = "CATEGORY_RECOGNIZE";
    public static final String TASK_PRICE_SUGGEST = "PRICE_SUGGEST";
}