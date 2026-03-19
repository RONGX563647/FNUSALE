package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单评价表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_evaluation")
public class OrderEvaluation {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 评价者ID
     */
    private Long evaluatorId;

    /**
     * 被评价者ID
     */
    private Long evaluatedId;

    /**
     * 评分（1-5星）
     */
    private Integer score;

    /**
     * 评价标签
     */
    private String evaluationTag;

    /**
     * 评价内容
     */
    private String evaluationContent;

    /**
     * 评价图片地址
     */
    private String evaluationImageUrl;

    /**
     * 是否匿名评价（0-否, 1-是）
     */
    private Integer isAnonymous;

    /**
     * 卖家回复内容
     */
    private String replyContent;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 追加评价内容
     */
    private String appendContent;

    /**
     * 追加评价图片地址
     */
    private String appendImageUrl;

    /**
     * 追加评价时间
     */
    private LocalDateTime appendTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}