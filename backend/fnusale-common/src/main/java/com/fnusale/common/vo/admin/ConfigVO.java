package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置VO
 */
@Data
@Schema(description = "系统配置")
public class ConfigVO implements Serializable {

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置描述")
    private String configDesc;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "最后修改管理员ID")
    private Long adminId;
}