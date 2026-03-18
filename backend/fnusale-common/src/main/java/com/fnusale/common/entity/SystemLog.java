package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志表
 */
@Data
@TableName("t_system_log")
public class SystemLog {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long operateUserId;

    /**
     * 操作模块
     */
    private String moduleName;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 操作内容
     */
    private String operateContent;

    /**
     * 操作IP地址
     */
    private String ipAddress;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 异常信息
     */
    private String exceptionInfo;

    /**
     * 日志类型（OPERATE/EXCEPTION）
     */
    private String logType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}