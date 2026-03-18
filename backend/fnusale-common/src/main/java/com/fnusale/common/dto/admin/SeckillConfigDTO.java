package com.fnusale.common.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀配置DTO
 */
@Data
@Schema(description = "秒杀配置")
public class SeckillConfigDTO implements Serializable {

    @Schema(description = "秒杀接口QPS阈值")
    private Integer qpsLimit;

    @Schema(description = "库存预热提前时间（分钟）")
    private Integer stockPreloadMinutes;

    @Schema(description = "每人限购数量")
    private Integer limitPerUser;
}