package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价标签统计表
 */
@Data
@TableName("t_evaluation_tag_stat")
public class EvaluationTagStat implements Serializable {

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
     * 标签名称
     */
    private String tagName;

    /**
     * 出现次数
     */
    private Integer tagCount;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}