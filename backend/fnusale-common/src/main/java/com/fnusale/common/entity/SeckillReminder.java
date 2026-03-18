package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀提醒表
 */
@Data
@TableName("t_seckill_reminder")
public class SeckillReminder {

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
     * 活动ID
     */
    private Long activityId;

    /**
     * 提醒时间
     */
    private LocalDateTime remindTime;

    /**
     * 是否已提醒（0-否，1-是）
     */
    private Integer isReminded;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}