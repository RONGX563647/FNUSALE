package com.fnusale.product.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户行为消息
 * 用于收藏、点赞、浏览等行为异步处理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（用于幂等性处理）
     */
    private String messageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 行为类型
     * COLLECT - 收藏
     * UNCOLLECT - 取消收藏
     * LIKE - 点赞
     * UNLIKE - 取消点赞
     * BROWSE - 浏览
     */
    private String behaviorType;

    /**
     * 行为时间
     */
    private Long behaviorTime;

    /**
     * 额外信息（JSON格式）
     */
    private String extra;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 行为类型常量
     */
    public static final String BEHAVIOR_COLLECT = "COLLECT";
    public static final String BEHAVIOR_UNCOLLECT = "UNCOLLECT";
    public static final String BEHAVIOR_LIKE = "LIKE";
    public static final String BEHAVIOR_UNLIKE = "UNLIKE";
    public static final String BEHAVIOR_BROWSE = "BROWSE";
}