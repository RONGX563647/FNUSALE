package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置历史记录表
 */
@Data
@TableName("t_system_config_history")
public class SystemConfigHistory {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 修改前值
     */
    private String oldValue;

    /**
     * 修改后值
     */
    private String newValue;

    /**
     * 操作管理员ID
     */
    private Long adminId;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 操作IP
     */
    private String operateIp;

    /**
     * 备注
     */
    private String remark;
}