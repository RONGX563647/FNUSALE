package com.fnusale.common.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnusale.common.config.AmapConfig;
import com.fnusale.common.dto.amap.AmapDistanceResult;
import com.fnusale.common.dto.amap.AmapGeocodeResult;
import com.fnusale.common.dto.amap.AmapLocationResult;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.service.AmapService;
import com.fnusale.common.util.GeoFenceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 高德地图服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmapServiceImpl implements AmapService {

    private final AmapConfig amapConfig;
    private final ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    private static final String STATUS_SUCCESS = "1";
    private static final String STATUS_FAIL = "0";
    private static final String API_NOT_ENABLED_MSG = "高德地图API未启用";

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(amapConfig.getConnectTimeout());
        factory.setReadTimeout(amapConfig.getReadTimeout());
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public AmapLocationResult locateByIp(String ip) {
        if (!isApiEnabled()) {
            return createFailedLocationResult(API_NOT_ENABLED_MSG);
        }

        validateIp(ip);

        String url = buildUrl("/ip", "ip", ip);
        return executeApiCall(
                () -> {
                    String response = restTemplate.getForObject(url, String.class);
                    logResponse("IP定位", response);
                    try {
                        return objectMapper.readValue(response, AmapLocationResult.class);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        throw new RuntimeException("JSON解析失败", e);
                    }
                },
                () -> createFailedLocationResult("请求失败"),
                "IP定位"
        );
    }

    @Override
    public AmapGeocodeResult reverseGeocode(String longitude, String latitude) {
        if (!isApiEnabled()) {
            return createFailedGeocodeResult(API_NOT_ENABLED_MSG);
        }

        validateCoordinates(longitude, latitude);

        String location = longitude + "," + latitude;
        String url = buildUrl("/geocode/regeo", 
                "location", location,
                "radius", "1000",
                "extensions", "base");

        return executeApiCall(
                () -> {
                    String response = restTemplate.getForObject(url, String.class);
                    logResponse("逆地理编码", response);
                    try {
                        return objectMapper.readValue(response, AmapGeocodeResult.class);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        throw new RuntimeException("JSON解析失败", e);
                    }
                },
                () -> createFailedGeocodeResult("请求失败"),
                "逆地理编码"
        );
    }

    @Override
    public boolean isInPolygon(String longitude, String latitude, String polygon) {
        if (!isApiEnabled()) {
            log.warn("高德地图API未启用，使用本地计算");
            return isInPolygonLocal(longitude, latitude, polygon);
        }

        validateCoordinates(longitude, latitude);
        validatePolygon(polygon);

        String location = longitude + "," + latitude;
        String url = buildUrl("/geocode/regeo",
                "location", location,
                "polygon", polygon);

        try {
            String response = restTemplate.getForObject(url, String.class);
            logResponse("多边形围栏校验", response);

            JsonNode root = objectMapper.readTree(response);
            if (STATUS_SUCCESS.equals(root.path("status").asText())) {
                JsonNode regeocode = root.path("regeocode");
                return !regeocode.path("formatted_address").asText().isEmpty();
            }
            return false;
        } catch (Exception e) {
            log.error("多边形围栏校验失败，使用本地计算", e);
            return isInPolygonLocal(longitude, latitude, polygon);
        }
    }

    @Override
    public boolean isInCircle(String longitude, String latitude,
                              String centerLng, String centerLat, int radius) {
        if (!isApiEnabled()) {
            log.warn("高德地图API未启用，使用本地计算");
            return isInCircleLocal(longitude, latitude, centerLng, centerLat, radius);
        }

        validateCoordinates(longitude, latitude);
        validateCoordinates(centerLng, centerLat);
        validateRadius(radius);

        try {
            String origins = longitude + "," + latitude;
            String destination = centerLng + "," + centerLat;

            List<AmapDistanceResult> results = calculateDistance(origins, destination);
            if (!results.isEmpty() && results.get(0).isSuccess()) {
                return results.get(0).getDistance() <= radius;
            }
            return false;
        } catch (Exception e) {
            log.error("圆形围栏校验失败，使用本地计算", e);
            return isInCircleLocal(longitude, latitude, centerLng, centerLat, radius);
        }
    }

    @Override
    public List<AmapDistanceResult> calculateDistance(String origins, String destination) {
        List<AmapDistanceResult> results = new ArrayList<>();

        if (!isApiEnabled()) {
            log.warn("高德地图API未启用，使用本地计算");
            return calculateDistanceLocal(origins, destination);
        }

        validateOrigins(origins);
        validateDestination(destination);

        String url = buildUrl("/distance",
                "origins", origins,
                "destination", destination,
                "type", "0");

        try {
            String response = restTemplate.getForObject(url, String.class);
            logResponse("距离计算", response);

            JsonNode root = objectMapper.readTree(response);
            String status = root.path("status").asText();

            JsonNode resultsArray = root.path("results");
            if (resultsArray.isArray()) {
                for (JsonNode node : resultsArray) {
                    AmapDistanceResult result = new AmapDistanceResult();
                    result.setOrigin(node.path("origin").asText());
                    result.setDestination(destination);
                    result.setDistance(node.path("distance").asLong());
                    result.setDuration(node.path("duration").asLong());
                    result.setStatus(status);
                    results.add(result);
                }
            }
        } catch (Exception e) {
            log.error("距离计算失败", e);
        }

        return results;
    }

    @Override
    public long calculateDistanceSingle(String originLng, String originLat,
                                         String destinationLng, String destinationLat) {
        String origins = originLng + "," + originLat;
        String destination = destinationLng + "," + destinationLat;
        List<AmapDistanceResult> results = calculateDistance(origins, destination);
        if (!results.isEmpty() && results.get(0).isSuccess()) {
            return results.get(0).getDistance();
        }
        return -1;
    }

    // ==================== 私有方法 ====================

    /**
     * 检查API是否启用
     */
    private boolean isApiEnabled() {
        return Boolean.TRUE.equals(amapConfig.getEnabled());
    }

    /**
     * 构建API URL（自动添加key和output参数）
     */
    private String buildUrl(String path, String... params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(amapConfig.getBaseUrl() + path)
                .queryParam("key", amapConfig.getKey())
                .queryParam("output", "json");

        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                builder.queryParam(params[i], params[i + 1]);
            }
        }

        return builder.build().toUriString();
    }

    /**
     * 记录响应日志（脱敏处理）
     */
    private void logResponse(String apiName, String response) {
        if (response != null) {
            String maskedResponse = response.replaceAll("key\":\"[^\"]+\"", "key\":\"***\"");
            log.debug("{}响应: {}", apiName, maskedResponse);
        }
    }

    /**
     * 统一API调用处理
     */
    private <T> T executeApiCall(Supplier<T> apiCall, Supplier<T> fallback, String apiName) {
        try {
            return apiCall.get();
        } catch (RestClientException e) {
            log.error("{}请求失败", apiName, e);
            return fallback.get();
        } catch (Exception e) {
            log.error("{}解析失败", apiName, e);
            return fallback.get();
        }
    }

    /**
     * 创建失败的定位结果
     */
    private AmapLocationResult createFailedLocationResult(String message) {
        AmapLocationResult result = new AmapLocationResult();
        result.setStatus(STATUS_FAIL);
        result.setInfo(message);
        return result;
    }

    /**
     * 创建失败的逆地理编码结果
     */
    private AmapGeocodeResult createFailedGeocodeResult(String message) {
        AmapGeocodeResult result = new AmapGeocodeResult();
        result.setStatus(STATUS_FAIL);
        result.setInfo(message);
        return result;
    }

    // ==================== 参数校验方法 ====================

    /**
     * 校验IP地址格式
     */
    private void validateIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            throw new BusinessException("IP地址不能为空");
        }
        String ipPattern = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        if (!ip.matches(ipPattern)) {
            log.warn("IP地址格式不正确: {}", ip);
        }
    }

    /**
     * 校验坐标格式
     */
    private void validateCoordinates(String longitude, String latitude) {
        if (longitude == null || longitude.isEmpty()) {
            throw new BusinessException("经度不能为空");
        }
        if (latitude == null || latitude.isEmpty()) {
            throw new BusinessException("纬度不能为空");
        }
        String coordPattern = "^-?\\d+(\\.\\d+)?$";
        if (!longitude.matches(coordPattern)) {
            throw new BusinessException("经度格式错误: " + longitude);
        }
        if (!latitude.matches(coordPattern)) {
            throw new BusinessException("纬度格式错误: " + latitude);
        }
        double lng = Double.parseDouble(longitude);
        double lat = Double.parseDouble(latitude);
        if (lng < -180 || lng > 180) {
            throw new BusinessException("经度范围错误，应在-180到180之间");
        }
        if (lat < -90 || lat > 90) {
            throw new BusinessException("纬度范围错误，应在-90到90之间");
        }
    }

    /**
     * 校验多边形围栏参数
     */
    private void validatePolygon(String polygon) {
        if (polygon == null || polygon.isEmpty()) {
            throw new BusinessException("多边形围栏参数不能为空");
        }
    }

    /**
     * 校验半径
     */
    private void validateRadius(int radius) {
        if (radius <= 0) {
            throw new BusinessException("半径必须大于0");
        }
    }

    /**
     * 校验起点坐标
     */
    private void validateOrigins(String origins) {
        if (origins == null || origins.isEmpty()) {
            throw new BusinessException("起点坐标不能为空");
        }
    }

    /**
     * 校验终点坐标
     */
    private void validateDestination(String destination) {
        if (destination == null || destination.isEmpty()) {
            throw new BusinessException("终点坐标不能为空");
        }
    }

    // ==================== 本地计算方法（降级方案） ====================

    /**
     * 本地多边形围栏计算
     */
    private boolean isInPolygonLocal(String longitude, String latitude, String polygon) {
        try {
            List<GeoFenceUtil.Point> points = new ArrayList<>();
            String[] coords = polygon.split(";");
            for (String coord : coords) {
                String[] parts = coord.split(",");
                if (parts.length == 2) {
                    points.add(new GeoFenceUtil.Point(
                            Double.parseDouble(parts[0].trim()),
                            Double.parseDouble(parts[1].trim())
                    ));
                }
            }
            return GeoFenceUtil.isPointInPolygon(
                    Double.parseDouble(longitude),
                    Double.parseDouble(latitude),
                    points
            );
        } catch (Exception e) {
            log.error("本地多边形围栏计算失败", e);
            return false;
        }
    }

    /**
     * 本地圆形围栏计算
     */
    private boolean isInCircleLocal(String longitude, String latitude,
                                    String centerLng, String centerLat, int radius) {
        try {
            return GeoFenceUtil.isPointInCircle(
                    Double.parseDouble(longitude),
                    Double.parseDouble(latitude),
                    Double.parseDouble(centerLng),
                    Double.parseDouble(centerLat),
                    radius
            );
        } catch (Exception e) {
            log.error("本地圆形围栏计算失败", e);
            return false;
        }
    }

    /**
     * 本地距离计算
     */
    private List<AmapDistanceResult> calculateDistanceLocal(String origins, String destination) {
        List<AmapDistanceResult> results = new ArrayList<>();
        String[] originArray = origins.split("\\|");
        String[] destCoords = destination.split(",");

        if (destCoords.length != 2) {
            return results;
        }

        for (String origin : originArray) {
            String[] coords = origin.split(",");
            if (coords.length == 2) {
                double distance = GeoFenceUtil.haversineDistance(
                        Double.parseDouble(coords[1]),
                        Double.parseDouble(coords[0]),
                        Double.parseDouble(destCoords[1]),
                        Double.parseDouble(destCoords[0])
                );
                AmapDistanceResult result = new AmapDistanceResult();
                result.setOrigin(origin);
                result.setDestination(destination);
                result.setDistance((long) distance);
                result.setStatus(STATUS_SUCCESS);
                results.add(result);
            }
        }
        return results;
    }
}
