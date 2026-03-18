package com.fnusale.user.service.impl;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.user.CampusPickPointDTO;
import com.fnusale.common.entity.CampusPickPoint;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.CampusPickPointVO;
import com.fnusale.user.mapper.CampusPickPointMapper;
import com.fnusale.user.service.CampusPickPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 校园自提点服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampusPickPointServiceImpl implements CampusPickPointService {

    private final CampusPickPointMapper campusPickPointMapper;

    /**
     * 地球半径（米）
     */
    private static final double EARTH_RADIUS = 6371000;

    @Override
    public List<CampusPickPointVO> getList() {
        List<CampusPickPoint> list = campusPickPointMapper.selectAllEnabled();
        return list.stream()
                .map(this::buildCampusPickPointVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CampusPickPointVO> getNearby(String longitude, String latitude, Integer distance) {
        List<CampusPickPoint> allPoints = campusPickPointMapper.selectAllEnabled();

        double userLng = Double.parseDouble(longitude);
        double userLat = Double.parseDouble(latitude);

        return allPoints.stream()
                .filter(point -> {
                    double pointLng = point.getLongitude().doubleValue();
                    double pointLat = point.getLatitude().doubleValue();
                    double dist = calculateDistance(userLat, userLng, pointLat, pointLng);
                    return dist <= distance;
                })
                .map(point -> {
                    CampusPickPointVO vo = buildCampusPickPointVO(point);
                    double dist = calculateDistance(userLat, userLng,
                            point.getLatitude().doubleValue(), point.getLongitude().doubleValue());
                    vo.setDistance((int) dist);
                    return vo;
                })
                .sorted((a, b) -> a.getDistance() - b.getDistance())
                .collect(Collectors.toList());
    }

    @Override
    public CampusPickPointVO getById(Long id) {
        CampusPickPoint point = campusPickPointMapper.selectById(id);
        if (point == null) {
            throw new BusinessException("自提点不存在");
        }
        return buildCampusPickPointVO(point);
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
        log.info("更新自提点状态成功, id: {}, status: {}", id, status);
    }

    @Override
    public PageResult<CampusPickPointVO> getPage(String campusArea, Integer status, Integer pageNum, Integer pageSize) {
        // 简单实现：获取所有符合条件的记录
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

    /**
     * 计算两点之间的距离（Haversine公式）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = radLat1 - radLat2;
        double deltaLng = Math.toRadians(lng1) - Math.toRadians(lng2);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(radLat1) * Math.cos(radLat2) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
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