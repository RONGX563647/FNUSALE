package com.fnusale.user.service.impl;

import com.fnusale.common.cache.RedisService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.constant.RedisKeyConstants;
import com.fnusale.common.dto.user.CampusPickPointDTO;
import com.fnusale.common.entity.CampusPickPoint;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.CampusPickPointVO;
import com.fnusale.user.mapper.CampusPickPointMapper;
import com.fnusale.user.service.CampusPickPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
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
 * 使用 Redis GEO 优化地理位置查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampusPickPointServiceImpl implements CampusPickPointService {

    private final CampusPickPointMapper campusPickPointMapper;
    private final RedisService redisService;

    /**
     * 自提点详情缓存过期时间（小时）
     */
    private static final long DETAIL_CACHE_HOURS = 24;

    /**
     * 自提点列表缓存过期时间（小时）
     */
    private static final long LIST_CACHE_HOURS = 1;

    @Override
    public List<CampusPickPointVO> getList() {
        // 尝试从缓存获取
        String listKey = RedisKeyConstants.PICK_POINT_LIST_KEY;
        Map<String, String> cachedList = redisService.hGetAll(listKey);

        if (!cachedList.isEmpty()) {
            return cachedList.values().stream()
                    .map(this::parseCachedPickPoint)
                    .collect(Collectors.toList());
        }

        // 从数据库获取
        List<CampusPickPoint> list = campusPickPointMapper.selectAllEnabled();

        // 同步到 GEO 和缓存
        syncToGeoAndCache(list);

        return list.stream()
                .map(this::buildCampusPickPointVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CampusPickPointVO> getNearby(String longitude, String latitude, Integer distance) {
        double userLng = Double.parseDouble(longitude);
        double userLat = Double.parseDouble(latitude);

        // 使用 Redis GEO 查询附近自提点
        List<RedisService.GeoResult> geoResults = redisService.geoRadius(
                RedisKeyConstants.PICK_POINT_GEO_KEY,
                userLng, userLat, distance);

        // 如果 GEO 数据为空，先同步
        if (geoResults.isEmpty()) {
            syncGeoData();
            geoResults = redisService.geoRadius(
                    RedisKeyConstants.PICK_POINT_GEO_KEY,
                    userLng, userLat, distance);
        }

        // 构建返回结果
        return geoResults.stream()
                .map(geoResult -> {
                    Long pointId = Long.parseLong(geoResult.getMember());
                    CampusPickPointVO vo = getPickPointDetailWithCache(pointId);
                    if (vo != null) {
                        vo.setDistance(geoResult.getDistance().intValue());
                    }
                    return vo;
                })
                .filter(vo -> vo != null && vo.getEnableStatus())
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

        // 同步到 GEO 和缓存
        if (point.getLongitude() != null && point.getLatitude() != null) {
            redisService.geoAdd(
                    RedisKeyConstants.PICK_POINT_GEO_KEY,
                    point.getLongitude().doubleValue(),
                    point.getLatitude().doubleValue(),
                    point.getId().toString());
        }
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

        // 更新 GEO 数据
        if (point.getLongitude() != null && point.getLatitude() != null) {
            // 先删除旧的 GEO 数据
            redisService.geoRemove(RedisKeyConstants.PICK_POINT_GEO_KEY, id.toString());
            // 添加新的 GEO 数据
            redisService.geoAdd(
                    RedisKeyConstants.PICK_POINT_GEO_KEY,
                    point.getLongitude().doubleValue(),
                    point.getLatitude().doubleValue(),
                    id.toString());
        }

        // 更新详情缓存
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

        // 删除 GEO 和缓存数据
        redisService.geoRemove(RedisKeyConstants.PICK_POINT_GEO_KEY, id.toString());
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

        // 更新详情缓存
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

        // 过滤状态
        if (status != null) {
            allPoints = allPoints.stream()
                    .filter(p -> p.getEnableStatus().equals(status))
                    .collect(Collectors.toList());
        }

        // 分页
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
     * 获取自提点详情（带缓存）
     */
    private CampusPickPointVO getPickPointDetailWithCache(Long id) {
        String detailKey = RedisKeyConstants.buildPickPointDetailKey(id);

        // 尝试从缓存获取
        Map<String, String> cached = redisService.hGetAll(detailKey);
        if (!cached.isEmpty()) {
            return parseCachedPickPointFromHash(cached);
        }

        // 从数据库获取
        CampusPickPoint point = campusPickPointMapper.selectById(id);
        if (point == null) {
            return null;
        }

        // 缓存
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
        // 简化处理，实际可以使用 JSON 解析
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
     * 同步 GEO 数据
     */
    private void syncGeoData() {
        List<CampusPickPoint> points = campusPickPointMapper.selectAllEnabled();
        syncToGeoAndCache(points);
    }

    /**
     * 同步到 GEO 和缓存
     */
    private void syncToGeoAndCache(List<CampusPickPoint> points) {
        if (points == null || points.isEmpty()) {
            return;
        }

        Map<String, Point> geoMembers = new HashMap<>();
        String listKey = RedisKeyConstants.PICK_POINT_LIST_KEY;

        for (CampusPickPoint point : points) {
            if (point.getLongitude() != null && point.getLatitude() != null) {
                geoMembers.put(
                        point.getId().toString(),
                        new Point(point.getLongitude().doubleValue(), point.getLatitude().doubleValue()));
            }

            // 缓存详情
            cachePickPointDetail(point);

            // 缓存列表
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

        // 批量添加 GEO 数据
        if (!geoMembers.isEmpty()) {
            redisService.geoAdd(RedisKeyConstants.PICK_POINT_GEO_KEY, geoMembers);
        }

        redisService.expire(listKey, LIST_CACHE_HOURS, TimeUnit.HOURS);
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