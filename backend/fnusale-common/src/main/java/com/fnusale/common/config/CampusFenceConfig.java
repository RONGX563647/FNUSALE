package com.fnusale.common.config;

import com.fnusale.common.util.GeoFenceUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 校园围栏配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "campus.fence")
public class CampusFenceConfig {

    /**
     * 校园围栏多边形顶点（按顺时针或逆时针排列）
     * 格式：经度，纬度
     */
    private List<String> polygon = new ArrayList<>();

    /**
     * 校园中心点（备用圆形围栏）
     */
    private String center;

    /**
     * 校园半径（米，备用圆形围栏）
     */
    private Double radius;

    /**
     * 是否启用围栏验证
     */
    private Boolean enabled = true;

    /**
     * 获取围栏多边形点列表
     */
    public List<GeoFenceUtil.Point> getFencePoints() {
        List<GeoFenceUtil.Point> points = new ArrayList<>();
        for (String pointStr : polygon) {
            String[] parts = pointStr.split(",");
            if (parts.length == 2) {
                try {
                    double lon = Double.parseDouble(parts[0].trim());
                    double lat = Double.parseDouble(parts[1].trim());
                    points.add(new GeoFenceUtil.Point(lon, lat));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("校园围栏坐标格式错误：" + pointStr);
                }
            }
        }
        return points;
    }

    /**
     * 获取中心点
     */
    public GeoFenceUtil.Point getCenterPoint() {
        if (center == null || center.isEmpty()) {
            return null;
        }
        String[] parts = center.split(",");
        if (parts.length == 2) {
            try {
                double lon = Double.parseDouble(parts[0].trim());
                double lat = Double.parseDouble(parts[1].trim());
                return new GeoFenceUtil.Point(lon, lat);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("校园中心点坐标格式错误：" + center);
            }
        }
        return null;
    }
}
