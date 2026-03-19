package com.fnusale.common.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 地理围栏工具类
 * 使用射线法判断点是否在多边形内
 */
@Slf4j
public class GeoFenceUtil {

    /**
     * 判断点是否在多边形内（射线法）
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param polygon   多边形顶点列表（按顺时针或逆时针排列）
     * @return 是否在多边形内
     */
    public static boolean isPointInPolygon(double longitude, double latitude, List<Point> polygon) {
        if (polygon == null || polygon.size() < 3) {
            log.warn("多边形顶点不足，无法形成闭合区域");
            return false;
        }

        int n = polygon.size();
        boolean inside = false;

        double lon = longitude;
        double lat = latitude;

        int j = n - 1;
        for (int i = 0; i < n; i++) {
            double xi = polygon.get(i).getLongitude();
            double yi = polygon.get(i).getLatitude();
            double xj = polygon.get(j).getLongitude();
            double yj = polygon.get(j).getLatitude();

            // 射线法核心逻辑
            boolean intersect = ((yi > lat) != (yj > lat))
                    && (lon < (xj - xi) * (lat - yi) / (yj - yi) + xi);

            if (intersect) {
                inside = !inside;
            }

            j = i;
        }

        return inside;
    }

    /**
     * 判断点是否在圆形区域内
     *
     * @param longitude  经度
     * @param latitude  纬度
     * @param centerLon 圆心经度
     * @param centerLat 圆心纬度
     * @param radius    半径（米）
     * @return 是否在圆形区域内
     */
    public static boolean isPointInCircle(double longitude, double latitude,
                                          double centerLon, double centerLat, double radius) {
        double distance = haversineDistance(latitude, longitude, centerLat, centerLon);
        return distance <= radius;
    }

    /**
     * 使用 Haversine 公式计算两点之间的距离
     *
     * @param lat1 纬度1
     * @param lon1 经度1
     * @param lat2 纬度2
     * @param lon2 经度2
     * @return 距离（米）
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // 地球半径（米）

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 计算点到线段的最短距离
     *
     * @param pointLon   点经度
     * @param pointLat   点纬度
     * @param segmentLon1 线段起点经度
     * @param segmentLat1 线段起点纬度
     * @param segmentLon2 线段终点经度
     * @param segmentLat2 线段终点纬度
     * @return 最短距离（米）
     */
    public static double pointToSegmentDistance(double pointLon, double pointLat,
                                                double segmentLon1, double segmentLat1,
                                                double segmentLon2, double segmentLat2) {
        double distance1 = haversineDistance(pointLat, pointLon, segmentLat1, segmentLon1);
        double distance2 = haversineDistance(pointLat, pointLon, segmentLat2, segmentLon2);

        double segmentLength = haversineDistance(segmentLat1, segmentLon1, segmentLat2, segmentLon2);

        if (segmentLength == 0) {
            return distance1;
        }

        // 计算投影点比例
        double t = Math.max(0, Math.min(1,
                ((pointLon - segmentLon1) * (segmentLon2 - segmentLon1)
                        + (pointLat - segmentLat1) * (segmentLat2 - segmentLat1))
                        / (segmentLength * segmentLength)));

        double projectionLat = segmentLat1 + t * (segmentLat2 - segmentLat1);
        double projectionLon = segmentLon1 + t * (segmentLon2 - segmentLon1);

        return haversineDistance(pointLat, pointLon, projectionLat, projectionLon);
    }

    /**
     * 地理坐标点
     */
    @Data
    public static class Point {
        private double longitude;
        private double latitude;

        public Point() {
        }

        public Point(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public static Point of(double longitude, double latitude) {
            return new Point(longitude, latitude);
        }
    }
}
