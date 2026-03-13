package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息表
 */
@Data
@TableName("t_im_message")
public class ImMessage {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型（TEXT/IMAGE/VOICE）
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 是否已读（0-未读, 1-已读）
     */
    private Integer isRead;

    /**
     * 敏感词检测结果（PASS/FAIL）
     */
    private String sensitiveCheckResult;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 逻辑删除标记（0-未删除, 1-已删除）
     */
    private Integer isDeleted;
}