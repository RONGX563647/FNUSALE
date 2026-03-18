package com.fnusale.common.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 校园围栏DTO
 */
@Data
@Schema(description = "校园围栏配置")
public class CampusFenceDTO implements Serializable {

    @Schema(description = "围栏顶点坐标列表")
    private List<FencePoint> fencePoints;

    @Data
    @Schema(description = "围栏坐标点")
    public static class FencePoint implements Serializable {
        @Schema(description = "经度")
        private Double lng;

        @Schema(description = "纬度")
        private Double lat;
    }
}