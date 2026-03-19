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
 * 本地消息表 - 用于分布式事务最终一致性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_local_message")
public class LocalMessage {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一ID（用于幂等性）
     */
    private String messageId;

    /**
     * 消息类型（SECKILL_ORDER/COUPON_GRANT等）
     */
    private String messageType;

    /**
     * 目标Topic
     */
    private String topic;

    /**
     * 目标Tag
     */
    private String tag;

    /**
     * 消息内容（JSON格式）
     */
    private String messageContent;

    /**
     * 状态（PENDING/SENT/FAILED）
     */
    private String status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注信息
     */
    private String remark;
}