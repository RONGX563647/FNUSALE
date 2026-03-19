package com.fnusale.common.service;

import com.fnusale.common.dto.amap.AmapDistanceResult;
import com.fnusale.common.dto.amap.AmapGeocodeResult;
import com.fnusale.common.dto.amap.AmapLocationResult;

import java.util.List;

/**
 * 高德地图服务接口
 */
public interface AmapService {

    /**
     * IP定位
     * 根据用户IP地址获取大致位置
     *
     * @param ip 用户IP地址，如果为空则使用请求IP
     * @return 定位结果（经纬度+省份城市）
     */
    AmapLocationResult locateByIp(String ip);

    /**
     * 逆地理编码
     * 将经纬度坐标转换为详细地址
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 详细地址信息
     */
    AmapGeocodeResult reverseGeocode(String longitude, String latitude);

    /**
     * 判断坐标是否在多边形区域内
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param polygon   多边形顶点坐标，格式：lng1,lat1;lng2,lat2;...
     * @return 是否在区域内
     */
    boolean isInPolygon(String longitude, String latitude, String polygon);

    /**
     * 判断坐标是否在圆形区域内
     *
     * @param longitude  经度
     * @param latitude   纬度
     * @param centerLng  圆心经度
     * @param centerLat  圆心纬度
     * @param radius     半径（米）
     * @return 是否在区域内
     */
    boolean isInCircle(String longitude, String latitude, 
                       String centerLng, String centerLat, int radius);

    /**
     * 计算两点间直线距离
     *
     * @param origins    起点坐标，格式：lng1,lat1|lng2,lat2（可多个）
     * @param destination 终点坐标，格式：lng,lat
     * @return 距离结果列表
     */
    List<AmapDistanceResult> calculateDistance(String origins, String destination);

    /**
     * 计算单点到单点的距离
     *
     * @param originLng      起点经度
     * @param originLat      起点纬度
     * @param destinationLng 终点经度
     * @param destinationLat 终点纬度
     * @return 距离（米），失败返回-1
     */
    long calculateDistanceSingle(String originLng, String originLat,
                                  String destinationLng, String destinationLat);
}
