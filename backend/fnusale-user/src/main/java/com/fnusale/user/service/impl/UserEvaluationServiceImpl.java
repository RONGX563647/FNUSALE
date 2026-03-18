package com.fnusale.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.EvaluationAppendDTO;
import com.fnusale.common.dto.user.EvaluationReplyDTO;
import com.fnusale.common.dto.user.EvaluationReportDTO;
import com.fnusale.common.dto.user.UserEvaluationDTO;
import com.fnusale.common.entity.EvaluationReport;
import com.fnusale.common.entity.EvaluationTagStat;
import com.fnusale.common.entity.OrderEvaluation;
import com.fnusale.common.entity.UserRating;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.UserEvaluationVO;
import com.fnusale.common.vo.user.UserRatingVO;
import com.fnusale.user.mapper.EvaluationReportMapper;
import com.fnusale.user.mapper.EvaluationTagStatMapper;
import com.fnusale.user.mapper.OrderEvaluationMapper;
import com.fnusale.user.mapper.UserRatingMapper;
import com.fnusale.user.service.UserEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户评价服务实现
 * 注：评价数据来自订单模块的t_order_evaluation表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEvaluationServiceImpl implements UserEvaluationService {

    private final UserRatingMapper userRatingMapper;
    private final EvaluationTagStatMapper evaluationTagStatMapper;
    private final OrderEvaluationMapper orderEvaluationMapper;
    private final EvaluationReportMapper evaluationReportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitEvaluation(Long userId, UserEvaluationDTO dto) {
        // 验证评分范围
        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }

        // TODO: 需要调用交易服务验证订单状态
        // 1. 验证订单是否存在且属于当前用户
        // 2. 验证订单是否已完成
        // 3. 验证是否在订单完成后7天内
        // 4. 验证是否已评价
        // 5. 从订单中获取被评价者(sellerId)

        log.info("用户提交评价, userId: {}, orderId: {}, score: {}", userId, dto.getOrderId(), dto.getScore());

        // TODO: 从订单获取被评价用户ID，然后更新评分统计
        // updateRatingAfterEvaluation(sellerId, dto.getScore(), dto.getEvaluationTag());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appendEvaluation(Long userId, Long evaluationId, EvaluationAppendDTO dto) {
        // 查询评价记录
        OrderEvaluation evaluation = orderEvaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BusinessException("评价不存在");
        }

        // 验证是否是评价者本人
        if (!evaluation.getEvaluatorId().equals(userId)) {
            throw new BusinessException("无权操作此评价");
        }

        // 检查是否已追加评价
        if (orderEvaluationMapper.hasAppend(evaluationId) > 0) {
            throw new BusinessException("已追加过评价，每个评价只能追加一次");
        }

        // 检查是否在追加评价时限内（评价后30天）
        LocalDateTime deadline = evaluation.getCreateTime().plusDays(UserConstants.APPEND_EVALUATION_DEADLINE_DAYS);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException("已超过追加评价时限（评价后" + UserConstants.APPEND_EVALUATION_DEADLINE_DAYS + "天内）");
        }

        // 追加评价
        int updated = orderEvaluationMapper.updateAppend(evaluationId, dto.getAppendContent(), dto.getAppendImageUrl());
        if (updated == 0) {
            throw new BusinessException("追加评价失败");
        }

        log.info("用户追加评价成功, userId: {}, evaluationId: {}", userId, evaluationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyEvaluation(Long userId, Long evaluationId, EvaluationReplyDTO dto) {
        // 查询评价记录
        OrderEvaluation evaluation = orderEvaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BusinessException("评价不存在");
        }

        // 验证是否是被评价者本人（卖家）
        if (!evaluation.getEvaluatedId().equals(userId)) {
            throw new BusinessException("无权回复此评价");
        }

        // 检查是否已回复
        if (orderEvaluationMapper.hasReply(evaluationId) > 0) {
            throw new BusinessException("已回复过此评价，每个评价只能回复一次");
        }

        // 回复评价
        int updated = orderEvaluationMapper.updateReply(evaluationId, dto.getReplyContent());
        if (updated == 0) {
            throw new BusinessException("回复评价失败");
        }

        log.info("卖家回复评价成功, userId: {}, evaluationId: {}", userId, evaluationId);
    }

    @Override
    public PageResult<UserEvaluationVO> getUserEvaluations(Long userId, Integer pageNum, Integer pageSize) {
        List<UserEvaluationVO> allEvaluations = orderEvaluationMapper.selectByEvaluatedId(userId);

        // 分页处理
        int total = allEvaluations.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<UserEvaluationVO> pageList = fromIndex < total ?
                allEvaluations.subList(fromIndex, toIndex) : List.of();

        return new PageResult<>(pageNum, pageSize, total, pageList);
    }

    @Override
    public PageResult<UserEvaluationVO> getMyEvaluations(Long currentUserId, Integer pageNum, Integer pageSize) {
        List<UserEvaluationVO> allEvaluations = orderEvaluationMapper.selectByEvaluatorId(currentUserId);

        // 分页处理
        int total = allEvaluations.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<UserEvaluationVO> pageList = fromIndex < total ?
                allEvaluations.subList(fromIndex, toIndex) : List.of();

        return new PageResult<>(pageNum, pageSize, total, pageList);
    }

    @Override
    public UserRatingVO getUserRating(Long userId) {
        try {
            UserRating rating = userRatingMapper.selectByUserId(userId);
            if (rating == null) {
                // 初始化评分记录
                rating = initUserRating(userId);
            }

            return UserRatingVO.builder()
                    .overallRating(rating.getOverallRating())
                    .ratingLevel(rating.getRatingLevel())
                    .totalEvaluations(rating.getTotalEvaluations())
                    .positiveCount(rating.getPositiveCount())
                    .neutralCount(rating.getNeutralCount())
                    .negativeCount(rating.getNegativeCount())
                    .positiveRate(rating.getPositiveRate())
                    .build();
        } catch (Exception e) {
            log.warn("获取用户评分失败, userId: {}", userId, e);
            // 返回默认评分
            return UserRatingVO.builder()
                    .overallRating(new BigDecimal("5.00"))
                    .ratingLevel("EXCELLENT")
                    .totalEvaluations(0)
                    .positiveCount(0)
                    .neutralCount(0)
                    .negativeCount(0)
                    .positiveRate(new BigDecimal("100.00"))
                    .build();
        }
    }

    @Override
    public List<Map<String, Object>> getUserTags(Long userId) {
        List<EvaluationTagStat> tags = evaluationTagStatMapper.selectByUserId(userId);
        return tags.stream()
                .map(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("tagName", tag.getTagName());
                    map.put("tagCount", tag.getTagCount());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportEvaluation(Long userId, Long evaluationId, EvaluationReportDTO dto) {
        // 查询评价记录
        OrderEvaluation evaluation = orderEvaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BusinessException("评价不存在");
        }

        // 不能举报自己的评价
        if (evaluation.getEvaluatorId().equals(userId)) {
            throw new BusinessException("不能举报自己的评价");
        }

        // 检查是否已举报过
        if (evaluationReportMapper.countByEvaluationAndReporter(evaluationId, userId) > 0) {
            throw new BusinessException("已举报过此评价，请等待处理");
        }

        // 创建举报记录
        EvaluationReport report = EvaluationReport.builder()
                .evaluationId(evaluationId)
                .reporterId(userId)
                .reportReason(dto.getReportReason())
                .reportDesc(dto.getReportDesc())
                .status("PENDING")
                .createTime(LocalDateTime.now())
                .build();

        evaluationReportMapper.insert(report);

        log.info("用户举报评价成功, userId: {}, evaluationId: {}, reason: {}",
                userId, evaluationId, dto.getReportReason());
    }

    /**
     * 初始化用户评分
     */
    private UserRating initUserRating(Long userId) {
        UserRating rating = UserRating.builder()
                .userId(userId)
                .overallRating(new BigDecimal("5.00"))
                .ratingLevel("EXCELLENT")
                .totalEvaluations(0)
                .positiveCount(0)
                .neutralCount(0)
                .negativeCount(0)
                .positiveRate(new BigDecimal("100.00"))
                .last30dEvaluations(0)
                .last30dRating(new BigDecimal("5.00"))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userRatingMapper.insert(rating);
        return rating;
    }

    /**
     * 评价后更新评分统计
     */
    private void updateRatingAfterEvaluation(Long userId, Integer score, String tags) {
        UserRating rating = userRatingMapper.selectByUserId(userId);
        if (rating == null) {
            rating = initUserRating(userId);
        }

        // 更新统计
        rating.setTotalEvaluations(rating.getTotalEvaluations() + 1);
        if (score >= 4) {
            rating.setPositiveCount(rating.getPositiveCount() + 1);
        } else if (score == 3) {
            rating.setNeutralCount(rating.getNeutralCount() + 1);
        } else {
            rating.setNegativeCount(rating.getNegativeCount() + 1);
        }

        // 重新计算好评率
        if (rating.getTotalEvaluations() > 0) {
            BigDecimal positiveRate = BigDecimal.valueOf(rating.getPositiveCount() * 100.0 / rating.getTotalEvaluations())
                    .setScale(2, RoundingMode.HALF_UP);
            rating.setPositiveRate(positiveRate);
        }

        // 重新计算综合评分（简化实现：平均分）
        // 实际应该使用加权平均+时间衰减
        BigDecimal avgRating = calculateAverageRating(rating);
        rating.setOverallRating(avgRating);
        rating.setRatingLevel(determineRatingLevel(avgRating));
        rating.setUpdateTime(LocalDateTime.now());

        userRatingMapper.updateById(rating);

        // 更新标签统计
        if (tags != null && !tags.isEmpty()) {
            updateTagStats(userId, tags.split(","));
        }
    }

    /**
     * 计算平均评分
     */
    private BigDecimal calculateAverageRating(UserRating rating) {
        int total = rating.getTotalEvaluations();
        if (total == 0) {
            return new BigDecimal("5.00");
        }

        // 简化计算：基于好评/中评/差评加权
        int sum = rating.getPositiveCount() * 5 +
                rating.getNeutralCount() * 3 +
                rating.getNegativeCount() * 1;
        return BigDecimal.valueOf(sum * 1.0 / total)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 确定评分等级
     */
    private String determineRatingLevel(BigDecimal rating) {
        double r = rating.doubleValue();
        if (r >= 4.5) return "EXCELLENT";
        if (r >= 4.0) return "GOOD";
        if (r >= 3.0) return "NORMAL";
        if (r >= 2.0) return "POOR";
        return "BAD";
    }

    /**
     * 更新标签统计
     */
    private void updateTagStats(Long userId, String[] tags) {
        for (String tag : tags) {
            String trimmedTag = tag.trim();
            if (!trimmedTag.isEmpty()) {
                int updated = evaluationTagStatMapper.incrementTagCount(userId, trimmedTag);
                if (updated == 0) {
                    // 标签不存在，创建新记录
                    EvaluationTagStat stat = EvaluationTagStat.builder()
                            .userId(userId)
                            .tagName(trimmedTag)
                            .tagCount(1)
                            .updateTime(LocalDateTime.now())
                            .build();
                    evaluationTagStatMapper.insert(stat);
                }
            }
        }
    }
}