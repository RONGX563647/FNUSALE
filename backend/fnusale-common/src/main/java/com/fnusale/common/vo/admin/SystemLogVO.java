package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志VO
 */
@Data
@Schema(description = "系统日志")
public class SystemLogVO implements Serializable {

    @Schema(description = "日志ID")
    private Long logId;

    @Schema(description = "操作用户ID")
    private Long operateUserId;

    @Schema(description = "操作用户名")
    private String operateUsername;

    @Schema(description = "操作模块（USER/PRODUCT/ORDER/MARKETING/SYSTEM）")
    private String moduleName;

    @Schema(description = "操作类型（ADD/UPDATE/DELETE/QUERY）")
    private String operateType;

    @Schema(description = "操作内容")
    private String operateContent;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "异常信息")
    private String exceptionInfo;

    @Schema(description = "日志类型（OPERATE/EXCEPTION）")
    private String logType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}