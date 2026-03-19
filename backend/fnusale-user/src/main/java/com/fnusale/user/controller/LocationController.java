package com.fnusale.user.controller;

import com.fnusale.common.annotation.RateLimit;
import com.fnusale.common.common.Result;
import com.fnusale.common.config.CampusFenceConfig;
import com.fnusale.common.dto.amap.AmapGeocodeResult;
import com.fnusale.common.dto.amap.AmapLocationResult;
import com.fnusale.common.service.AmapService;
import com.fnusale.common.vo.user.LocationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 定位控制器
 * 提供IP定位、逆地理编码、围栏校验等接口
 */
@Slf4j
@Tag(name = "定位服务", description = "IP定位、逆地理编码、围栏校验等接口")
@RestController
@RequestMapping("/user/location")
@RequiredArgsConstructor
public class LocationController {

    private final AmapService amapService;
    private final CampusFenceConfig campusFenceConfig;

    @Operation(summary = "IP定位", description = "根据用户IP地址获取大致位置信息")
    @GetMapping("/ip")
    @RateLimit(key = "location:ip", maxRequests = 10, windowSeconds = 1, message = "IP定位请求过于频繁，请稍后再试")
    public Result<LocationVO> locateByIp(HttpServletRequest request) {
        String ip = getClientIp(request);
        log.info("IP定位请求，IP: {}", ip);

        AmapLocationResult locationResult = amapService.locateByIp(ip);

        LocationVO vo = new LocationVO();
        if (locationResult.isSuccess()) {
            vo.setLongitude(locationResult.getLongitude());
            vo.setLatitude(locationResult.getLatitude());
            vo.setProvince(locationResult.getProvince());
            vo.setCity(locationResult.getCity());
            vo.setDistrict(locationResult.getDistrict());
            vo.setAddress(locationResult.getAddress());

            if (locationResult.getLongitude() != null && locationResult.getLatitude() != null) {
                boolean inCampus = checkInCampus(locationResult.getLongitude(), locationResult.getLatitude());
                vo.setInCampus(inCampus);
            }
        } else {
            log.warn("IP定位失败: {}", locationResult.getInfo());
        }

        return Result.success(vo);
    }

    @Operation(summary = "逆地理编码", description = "将经纬度坐标转换为详细地址")
    @GetMapping("/geocode")
    @RateLimit(key = "location:geocode", maxRequests = 20, windowSeconds = 1, message = "逆地理编码请求过于频繁，请稍后再试")
    public Result<LocationVO> reverseGeocode(
            @Parameter(description = "经度", required = true) @RequestParam String longitude,
            @Parameter(description = "纬度", required = true) @RequestParam String latitude) {
        log.info("逆地理编码请求，经度: {}, 纬度: {}", longitude, latitude);

        AmapGeocodeResult geocodeResult = amapService.reverseGeocode(longitude, latitude);

        LocationVO vo = new LocationVO();
        vo.setLongitude(longitude);
        vo.setLatitude(latitude);

        if (geocodeResult.isSuccess()) {
            vo.setProvince(geocodeResult.getProvince());
            vo.setCity(geocodeResult.getCity());
            vo.setDistrict(geocodeResult.getDistrict());
            vo.setAddress(geocodeResult.getFormattedAddress());
        } else {
            log.warn("逆地理编码失败: {}", geocodeResult.getInfo());
        }

        boolean inCampus = checkInCampus(longitude, latitude);
        vo.setInCampus(inCampus);

        return Result.success(vo);
    }

    @Operation(summary = "校验定位是否在校园内", description = "校验用户当前定位是否在校园围栏内")
    @GetMapping("/verify")
    @RateLimit(key = "location:verify", maxRequests = 30, windowSeconds = 1, message = "围栏校验请求过于频繁，请稍后再试")
    public Result<Boolean> verifyLocation(
            @Parameter(description = "经度", required = true) @RequestParam String longitude,
            @Parameter(description = "纬度", required = true) @RequestParam String latitude) {
        log.info("围栏校验请求，经度: {}, 纬度: {}", longitude, latitude);

        boolean inCampus = checkInCampus(longitude, latitude);
        return Result.success(inCampus);
    }

    @Operation(summary = "综合定位", description = "优先使用前端传递的经纬度，否则使用IP定位")
    @GetMapping("/current")
    @RateLimit(key = "location:current", maxRequests = 10, windowSeconds = 1, message = "定位请求过于频繁，请稍后再试")
    public Result<LocationVO> getCurrentLocation(
            @Parameter(description = "经度（可选）") @RequestParam(required = false) String longitude,
            @Parameter(description = "纬度（可选）") @RequestParam(required = false) String latitude,
            HttpServletRequest request) {
        log.info("综合定位请求，经度: {}, 纬度: {}", longitude, latitude);

        LocationVO vo = new LocationVO();

        if (longitude != null && latitude != null && 
            !longitude.isEmpty() && !latitude.isEmpty()) {
            vo.setLongitude(longitude);
            vo.setLatitude(latitude);

            AmapGeocodeResult geocodeResult = amapService.reverseGeocode(longitude, latitude);
            if (geocodeResult.isSuccess()) {
                vo.setProvince(geocodeResult.getProvince());
                vo.setCity(geocodeResult.getCity());
                vo.setDistrict(geocodeResult.getDistrict());
                vo.setAddress(geocodeResult.getFormattedAddress());
            }
        } else {
            String ip = getClientIp(request);
            AmapLocationResult locationResult = amapService.locateByIp(ip);

            if (locationResult.isSuccess()) {
                vo.setLongitude(locationResult.getLongitude());
                vo.setLatitude(locationResult.getLatitude());
                vo.setProvince(locationResult.getProvince());
                vo.setCity(locationResult.getCity());
                vo.setDistrict(locationResult.getDistrict());
                vo.setAddress(locationResult.getAddress());
            }
        }

        if (vo.getLongitude() != null && vo.getLatitude() != null) {
            boolean inCampus = checkInCampus(vo.getLongitude(), vo.getLatitude());
            vo.setInCampus(inCampus);
        }

        return Result.success(vo);
    }

    /**
     * 校验坐标是否在校园围栏内
     */
    private boolean checkInCampus(String longitude, String latitude) {
        if (!campusFenceConfig.getEnabled()) {
            log.debug("围栏校验未启用");
            return true;
        }

        try {
            if (campusFenceConfig.getCenter() != null && campusFenceConfig.getRadius() != null) {
                String[] centerParts = campusFenceConfig.getCenter().split(",");
                if (centerParts.length == 2) {
                    String centerLng = centerParts[0].trim();
                    String centerLat = centerParts[1].trim();
                    return amapService.isInCircle(longitude, latitude, 
                            centerLng, centerLat, campusFenceConfig.getRadius().intValue());
                }
            }

            if (campusFenceConfig.getPolygon() != null && !campusFenceConfig.getPolygon().isEmpty()) {
                String polygon = String.join(";", campusFenceConfig.getPolygon());
                return amapService.isInPolygon(longitude, latitude, polygon);
            }
        } catch (Exception e) {
            log.error("围栏校验失败: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
