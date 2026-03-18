package com.fnusale.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.entity.PointsLog;
import com.fnusale.common.entity.UserPoints;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.UserPointsVO;
import com.fnusale.user.mapper.PointsLogMapper;
import com.fnusale.user.mapper.UserPointsMapper;
import com.fnusale.user.service.UserPointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 用户积分服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointsServiceImpl implements UserPointsService {

    private final UserPointsMapper userPointsMapper;
    private final PointsLogMapper pointsLogMapper;

    @Override
    public UserPointsVO getUserPoints(Long userId) {
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null) {
            // 如果不存在，初始化积分记录
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setAvailablePoints(0);
            userPoints.setUsedPoints(0);
            userPoints.setCreateTime(LocalDateTime.now());
            userPoints.setUpdateTime(LocalDateTime.now());
            userPointsMapper.insert(userPoints);
        }

        UserPointsVO vo = new UserPointsVO();
        vo.setTotalPoints(userPoints.getTotalPoints());
        vo.setAvailablePoints(userPoints.getAvailablePoints());
        vo.setUsedPoints(userPoints.getUsedPoints());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, Integer points, String changeType, String remark) {
        if (points <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }

        // 增加积分
        int updated = userPointsMapper.addPoints(userId, points);
        if (updated == 0) {
            throw new BusinessException("积分增加失败");
        }

        // 记录日志
        PointsLog pointsLog = new PointsLog();
        pointsLog.setUserId(userId);
        pointsLog.setChangeType(changeType);
        pointsLog.setChangeAmount(points);
        pointsLog.setRemark(remark);
        pointsLog.setCreateTime(LocalDateTime.now());
        pointsLogMapper.insert(pointsLog);

        log.info("用户积分增加, userId: {}, points: {}, type: {}", userId, points, changeType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long userId, Integer points, String changeType, String remark) {
        if (points <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }

        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null || userPoints.getAvailablePoints() < points) {
            throw new BusinessException("积分不足");
        }

        int beforePoints = userPoints.getAvailablePoints();

        // 扣减积分
        int updated = userPointsMapper.deductPoints(userId, points);
        if (updated == 0) {
            throw new BusinessException("积分扣减失败");
        }

        // 记录日志
        PointsLog pointsLog = new PointsLog();
        pointsLog.setUserId(userId);
        pointsLog.setChangeType(changeType);
        pointsLog.setChangeAmount(-points);
        pointsLog.setBeforePoints(beforePoints);
        pointsLog.setAfterPoints(beforePoints - points);
        pointsLog.setRemark(remark);
        pointsLog.setCreateTime(LocalDateTime.now());
        pointsLogMapper.insert(pointsLog);

        log.info("用户积分扣减, userId: {}, points: {}, type: {}", userId, points, changeType);
    }

    @Override
    public PageResult<Object> getPointsLogs(Long userId, Integer pageNum, Integer pageSize) {
        List<PointsLog> logs = pointsLogMapper.selectByUserId(userId);

        // 简单分页处理
        int total = logs.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Object> pageList = fromIndex < total ?
                logs.subList(fromIndex, toIndex).stream().map(this::buildPointsLogVO).toList() :
                Collections.emptyList();

        return new PageResult<>(pageNum, pageSize, total, pageList);
    }

    /**
     * 构建积分日志VO
     */
    private Object buildPointsLogVO(PointsLog pointsLog) {
        // 返回Map或创建专门的VO类
        return new java.util.HashMap<String, Object>() {{
            put("changeType", pointsLog.getChangeType());
            put("changeAmount", pointsLog.getChangeAmount());
            put("beforePoints", pointsLog.getBeforePoints());
            put("afterPoints", pointsLog.getAfterPoints());
            put("remark", pointsLog.getRemark());
            put("createTime", pointsLog.getCreateTime());
        }};
    }
}