package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能客服问题表
 */
@Data
@TableName("t_ai_service_question")
public class AiServiceQuestion {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 问题内容
     */
    private String questionContent;

    /**
     * 回答内容
     */
    private String answerContent;

    /**
     * 匹配关键词
     */
    private String keyword;

    /**
     * 启用状态（0-禁用, 1-启用）
     */
    private Integer enableStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}