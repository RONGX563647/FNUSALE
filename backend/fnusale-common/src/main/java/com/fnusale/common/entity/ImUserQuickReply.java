package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户自定义快捷回复表
 */
@Data
@TableName("t_im_user_quick_reply")
public class ImUserQuickReply {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}