package com.fnusale.user.service.impl;

import com.fnusale.common.cache.RedisService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.constant.RedisKeyConstants;
import com.fnusale.common.dto.amap.AmapLocationResult;
import com.fnusale.common.dto.user.CampusPickPointDTO;
import com.fnusale.common.entity.CampusPickPoint;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.service.AmapService;
import com.fnusale.common.vo.user.CampusPickPointVO;
import com.fnusale.user.mapper.CampusPickPointMapper;
import com.fnusale.user.service.CampusPickPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 校园自提点服务实现
 * 使用高德地图API进行地理位置计算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampusPickPointServiceImpl implements CampusPickPointService {

    private final CampusPickPointMapper campusPickPointMapper;
    private final RedisService redisService;
    private final AmapService amapService;

    /**
     * 自提点详情缓存过期时间（小时）
     */
    private static final long DETAIL_CACHE_HOURS = 24;

    /**
     * 自提点列表缓存过期时间（小时）
     */
    private static final long LIST_CACHE_HOURS = 1;

    /**
     * IP定位结果缓存过期时间（分钟）
     */
    private static final long IP_LOCATION_CACHE_MINUTES = 30;

    @Override
    public List<CampusPickPointVO> getList() {
        String listKey = RedisKeyConstants.PICK_POINT_LIST_KEY;
        Map<String, String> cachedList = redisService.hGetAll(listKey);

        if (!cachedList.isEmpty()) {
            return cachedList.values().stream()
                    .map(this::parseCachedPickPoint)
                    .collect(Collectors.toList());
        }

        List<CampusPickPoint> list = campusPickPointMapper.selectAllEnabled();
        cachePickPointList(list);

        return list.stream()
                .map(this::buildCampusPickPointVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CampusPickPointVO> getNearby(String longitude, String latitude, Integer distance, String ip) {
        String userLng = longitude;
        String userLat = latitude;

        if ((userLng == null || userLng.isEmpty()) || (userLat == null || userLat.isEmpty())) {
            AmapLocationResult locationResult = getLocationByIp(ip);
            if (locationResult.isSuccess()) {
                userLng = locationResult.getLongitude();
                userLat = locationResult.getLatitude();
                log.info("IP定位成功，IP: {}, 位置: {},{}", ip, userLng, userLat);
            } else {
                log.warn("IP定位失败，返回所有自提点: {}", locationResult.getInfo());
                return getList();
            }
        }

        final String finalUserLng = userLng;
        final String finalUserLat = userLat;

        List<CampusPickPoint> allPoints = campusPickPointMapper.selectAllEnabled();

        return allPoints.stream()
                .filter(point -> point.getLongitude() != null && point.getLatitude() != null)
                .map(point -> {
                    CampusPickPointVO vo = buildCampusPickPointVO(point);
                    long dist = amapService.calculateDistanceSingle(
                            finalUserLng, finalUserLat,
                            point.getLongitude().toString(), point.getLatitude().toString()
                    );
                    vo.setDistance(dist > 0 ? (int) dist : Integer.MAX_VALUE);
                    return vo;
                })
                .filter(vo -> vo.getDistance() <= distance)
                .sorted((a, b) -> a.getDistance() - b.getDistance())
                .collect(Collectors.toList());
    }

    @Override
    public CampusPickPointVO getById(Long id) {
        return getPickPointDetailWithCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CampusPickPointDTO dto) {
        CampusPickPoint point = new CampusPickPoint();
        point.setPickPointName(dto.getPickPointName());
        point.setCampusArea(dto.getCampusArea());
        point.setDetailAddress(dto.getDetailAddress());
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            point.setLongitude(new BigDecimal(dto.getLongitude()));
            point.setLatitude(new BigDecimal(dto.getLatitude()));
        }
        point.setEnableStatus(1);

        campusPickPointMapper.insert(point);

        cachePickPointDetail(point);
        invalidateListCache();

        log.info("新增自提点成功, id: {}, name: {}", point.getId(), point.getPickPointName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CampusPickPointDTO dto) {
        CampusPickPoint existPoint = campusPickPointMapper.selectById(id);
        if (existPoint == null) {
            throw new BusinessException("自提点不存在");
        }

        CampusPickPoint point = new CampusPickPoint();
        point.setId(id);
        point.setPickPointName(dto.getPickPointName());
        point.setCampusArea(dto.getCampusArea());
        point.setDetailAddress(dto.getDetailAddress());
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            point.setLongitude(new BigDecimal(dto.getLongitude()));
            point.setLatitude(new BigDecimal(dto.getLatitude()));
        }

        campusPickPointMapper.updateById(point);

        CampusPickPoint updatedPoint = campusPickPointMapper.selectById(id);
        cachePickPointDetail(updatedPoint);
        invalidateListCache();

        log.info("更新自提点成功, id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CampusPickPoint point = campusPickPointMapper.selectById(id);
        if (point == null) {
            throw new BusinessException("自提点不存在");
        }

        campusPickPointMapper.deleteById(id);

        String detailKey = RedisKeyConstants.buildPickPointDetailKey(id);
        redisService.delete(detailKey);
        invalidateListCache();

        log.info("删除自提点成功, id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        CampusPickPoint point = campusPickPointMapper.selectById(id);
        if (point == null) {
            throw new BusinessException("自提点不存在");
        }

        CampusPickPoint updatePoint = new CampusPickPoint();
        updatePoint.setId(id);
        updatePoint.setEnableStatus(status);

        campusPickPointMapper.updateById(updatePoint);

        point.setEnableStatus(status);
        cachePickPointDetail(point);
        invalidateListCache();

        log.info("更新自提点状态成功, id: {}, status: {}", id, status);
    }

    @Override
    public PageResult<CampusPickPointVO> getPage(String campusArea, Integer status, Integer pageNum, Integer pageSize) {
        List<CampusPickPoint> allPoints;

        if (campusArea != null && !campusArea.isEmpty()) {
            allPoints = campusPickPointMapper.selectByCampusArea(campusArea);
        } else {
            allPoints = campusPickPointMapper.selectList(null);
        }

        if (status != null) {
            allPoints = allPoints.stream()
                    .filter(p -> p.getEnableStatus().equals(status))
                    .collect(Collectors.toList());
        }

        int total = allPoints.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<CampusPickPointVO> pageList = fromIndex < total ?
                allPoints.subList(fromIndex, toIndex).stream()
                        .map(this::buildCampusPickPointVO)
                        .collect(Collectors.toList()) :
                List.of();

        return new PageResult<>(pageNum, pageSize, total, pageList);
    }

    // ==================== 私有方法 ====================

    /**
     * 通过IP获取位置（带缓存）
     */
    private AmapLocationResult getLocationByIp(String ip) {
        String cacheKey = RedisKeyConstants.PICK_POINT_GEO_KEY + ":ip:" + ip;
        String cachedLocation = redisService.get(cacheKey);

        if (cachedLocation != null && !cachedLocation.isEmpty()) {
            String[] parts = cachedLocation.split(",");
            if (parts.length >= 2) {
                AmapLocationResult result = new AmapLocationResult();
                result.setStatus("1");
                result.setLongitude(parts[0]);
                result.setLatitude(parts[1]);
                if (parts.length > 2) {
                    result.setProvince(parts[2]);
                }
                if (parts.length > 3) {
                    result.setCity(parts[3]);
                }
                return result;
            }
        }

        AmapLocationResult result = amapService.locateByIp(ip);

        if (result.isSuccess() && result.getLongitude() != null && result.getLatitude() != null) {
            String locationCache = String.format("%s,%s,%s,%s",
                    result.getLongitude(),
                    result.getLatitude(),
                    result.getProvince() != null ? result.getProvince() : "",
                    result.getCity() != null ? result.getCity() : "");
            redisService.set(cacheKey, locationCache, IP_LOCATION_CACHE_MINUTES, TimeUnit.MINUTES);
        }

        return result;
    }

    /**
     * 获取自提点详情（带缓存）
     */
    private CampusPickPointVO getPickPointDetailWithCache(Long id) {
        String detailKey = RedisKeyConstants.buildPickPointDetailKey(id);

        Map<String, String> cached = redisService.hGetAll(detailKey);
        if (!cached.isEmpty()) {
            return parseCachedPickPointFromHash(cached);
        }

        CampusPickPoint point = campusPickPointMapper.selectById(id);
        if (point == null) {
            return null;
        }

        cachePickPointDetail(point);

        return buildCampusPickPointVO(point);
    }

    /**
     * 缓存自提点详情
     */
    private void cachePickPointDetail(CampusPickPoint point) {
        if (point == null || point.getId() == null) {
            return;
        }

        String detailKey = RedisKeyConstants.buildPickPointDetailKey(point.getId());
        Map<String, String> map = new HashMap<>();
        map.put("id", point.getId().toString());
        map.put("pickPointName", point.getPickPointName() != null ? point.getPickPointName() : "");
        map.put("campusArea", point.getCampusArea() != null ? point.getCampusArea() : "");
        map.put("detailAddress", point.getDetailAddress() != null ? point.getDetailAddress() : "");
        map.put("longitude", point.getLongitude() != null ? point.getLongitude().toString() : "");
        map.put("latitude", point.getLatitude() != null ? point.getLatitude().toString() : "");
        map.put("enableStatus", point.getEnableStatus() != null ? point.getEnableStatus().toString() : "0");

        redisService.hSetAll(detailKey, map);
        redisService.expire(detailKey, DETAIL_CACHE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 缓存自提点列表
     */
    private void cachePickPointList(List<CampusPickPoint> points) {
        if (points == null || points.isEmpty()) {
            return;
        }

        String listKey = RedisKeyConstants.PICK_POINT_LIST_KEY;

        for (CampusPickPoint point : points) {
            cachePickPointDetail(point);

            String listValue = String.format("%d|%s|%s|%s|%s|%s|%d",
                    point.getId(),
                    point.getPickPointName() != null ? point.getPickPointName() : "",
                    point.getCampusArea() != null ? point.getCampusArea() : "",
                    point.getDetailAddress() != null ? point.getDetailAddress() : "",
                    point.getLongitude() != null ? point.getLongitude().toString() : "",
                    point.getLatitude() != null ? point.getLatitude().toString() : "",
                    point.getEnableStatus() != null ? point.getEnableStatus() : 0);
            redisService.hSet(listKey, point.getId().toString(), listValue);
        }

        redisService.expire(listKey, LIST_CACHE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 从 Hash 解析缓存的自提点详情
     */
    private CampusPickPointVO parseCachedPickPointFromHash(Map<String, String> cached) {
        CampusPickPointVO vo = new CampusPickPointVO();
        vo.setId(Long.parseLong(cached.get("id")));
        vo.setPickPointName(cached.get("pickPointName"));
        vo.setCampusArea(cached.get("campusArea"));
        vo.setDetailAddress(cached.get("detailAddress"));

        String lng = cached.get("longitude");
        String lat = cached.get("latitude");
        if (lng != null && !lng.isEmpty()) {
            vo.setLongitude(new BigDecimal(lng));
        }
        if (lat != null && !lat.isEmpty()) {
            vo.setLatitude(new BigDecimal(lat));
        }

        String status = cached.get("enableStatus");
        vo.setEnableStatus(status != null && "1".equals(status));
        return vo;
    }

    /**
     * 解析缓存的自提点（列表用）
     */
    private CampusPickPointVO parseCachedPickPoint(String json) {
        CampusPickPointVO vo = new CampusPickPointVO();
        String[] parts = json.split("\\|");
        if (parts.length >= 7) {
            vo.setId(Long.parseLong(parts[0]));
            vo.setPickPointName(parts[1]);
            vo.setCampusArea(parts[2]);
            vo.setDetailAddress(parts[3]);
            if (!parts[4].isEmpty()) {
                vo.setLongitude(new BigDecimal(parts[4]));
            }
            if (!parts[5].isEmpty()) {
                vo.setLatitude(new BigDecimal(parts[5]));
            }
            vo.setEnableStatus("1".equals(parts[6]));
        }
        return vo;
    }

    /**
     * 使列表缓存失效
     */
    private void invalidateListCache() {
        redisService.delete(RedisKeyConstants.PICK_POINT_LIST_KEY);
    }

    /**
     * 构建自提点VO
     */
    private CampusPickPointVO buildCampusPickPointVO(CampusPickPoint point) {
        CampusPickPointVO vo = new CampusPickPointVO();
        vo.setId(point.getId());
        vo.setPickPointName(point.getPickPointName());
        vo.setCampusArea(point.getCampusArea());
        vo.setDetailAddress(point.getDetailAddress());
        vo.setLongitude(point.getLongitude());
        vo.setLatitude(point.getLatitude());
        vo.setEnableStatus(point.getEnableStatus() == 1);
        return vo;
    }
}
