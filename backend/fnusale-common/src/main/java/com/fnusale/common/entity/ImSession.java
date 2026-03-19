package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话表
 */
@Data
@TableName("t_im_session")
public class ImSession {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户1ID
     */
    private Long user1Id;

    /**
     * 用户2ID
     */
    private Long user2Id;

    /**
     * 关联商品ID
     */
    private Long productId;

    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 用户1未读消息数
     */
    private Integer unreadCountU1;

    /**
     * 用户2未读消息数
     */
    private Integer unreadCountU2;

    /**
     * 会话状态（NORMAL/CLOSED）
     */
    private String sessionStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户1是否置顶（0-否, 1-是）
     */
    private Integer isPinnedU1;

    /**
     * 用户2是否置顶（0-否, 1-是）
     */
    private Integer isPinnedU2;

    /**
     * 用户1置顶时间
     */
    private LocalDateTime pinnedTimeU1;

    /**
     * 用户2置顶时间
     */
    private LocalDateTime pinnedTimeU2;

    /**
     * 用户1是否删除会话（0-否, 1-是）
     */
    private Integer isDeletedU1;

    /**
     * 用户2是否删除会话（0-否, 1-是）
     */
    private Integer isDeletedU2;
}