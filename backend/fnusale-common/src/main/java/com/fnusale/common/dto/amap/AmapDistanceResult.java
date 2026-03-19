package com.fnusale.common.dto.amap;

import lombok.Data;

/**
 * 高德地图距离计算结果
 */
@Data
public class AmapDistanceResult {

    /**
     * 起点坐标
     */
    private String origin;

    /**
     * 终点坐标
     */
    private String destination;

    /**
     * 距离（米）
     */
    private Long distance;

    /**
     * 时间（秒）
     */
    private Long duration;

    /**
     * 状态码
     */
    private String status;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return "1".equals(status);
    }
}
