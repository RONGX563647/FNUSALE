package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警记录表
 */
@Data
@TableName("t_alert_record")
public class AlertRecord {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 告警模块
     */
    private String alertModule;

    /**
     * 告警内容
     */
    private String alertContent;

    /**
     * 告警级别（NORMAL/URGENT）
     */
    private String alertLevel;

    /**
     * 处理状态（UNHANDLED/HANDLED）
     */
    private String handleStatus;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}