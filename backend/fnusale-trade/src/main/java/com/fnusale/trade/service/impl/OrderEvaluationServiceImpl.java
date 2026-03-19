package com.fnusale.trade.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.trade.OrderEvaluationDTO;
import com.fnusale.common.entity.Order;
import com.fnusale.common.entity.OrderEvaluation;
import com.fnusale.common.enums.OrderStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.common.vo.trade.EvaluationVO;
import com.fnusale.trade.client.ProductClient;
import com.fnusale.trade.client.UserClient;
import com.fnusale.trade.event.OrderEvaluationEvent;
import com.fnusale.trade.mapper.OrderEvaluationMapper;
import com.fnusale.trade.mapper.OrderMapper;
import com.fnusale.trade.mq.producer.OrderMessageProducer;
import com.fnusale.trade.service.OrderEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单评价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEvaluationServiceImpl implements OrderEvaluationService {

    private final OrderEvaluationMapper orderEvaluationMapper;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final OrderMessageProducer orderMessageProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitEvaluation(OrderEvaluationDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验权限：只有买家能评价
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权评价该订单");
        }

        // 校验订单状态：只有已成交的订单能评价
        if (!OrderStatus.SUCCESS.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("订单未完成，暂不可评价");
        }

        // 校验是否已评价
        if (orderEvaluationMapper.countByOrderId(dto.getOrderId()) > 0) {
            throw new BusinessException("该订单已评价");
        }

        // 获取卖家ID
        var productResult = productClient.getProductById(order.getProductId());
        Long sellerId = null;
        if (productResult.isSuccess() && productResult.getData() != null) {
            sellerId = productResult.getData().getUserId();
        }

        // 创建评价
        OrderEvaluation evaluation = OrderEvaluation.builder()
                .orderId(dto.getOrderId())
                .evaluatorId(userId)
                .evaluatedId(sellerId)
                .score(dto.getScore())
                .evaluationTag(dto.getEvaluationTag())
                .evaluationContent(dto.getEvaluationContent())
                .evaluationImageUrl(dto.getEvaluationImageUrl())
                .createTime(LocalDateTime.now())
                .build();

        orderEvaluationMapper.insert(evaluation);

        // 发送评价消息（异步更新卖家评分统计）
        OrderEvaluationEvent evaluationEvent = OrderEvaluationEvent.builder()
                .orderId(dto.getOrderId())
                .orderNo(order.getOrderNo())
                .buyerId(userId)
                .sellerId(sellerId)
                .productId(order.getProductId())
                .evaluationId(evaluation.getId())
                .score(dto.getScore())
                .content(dto.getEvaluationContent())
                .eventId(java.util.UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now())
                .build();
        orderMessageProducer.sendEvaluationMessage(evaluationEvent);

        log.info("评价提交成功，orderId: {}, score: {}", dto.getOrderId(), dto.getScore());
    }

    @Override
    public EvaluationVO getByOrderId(Long orderId) {
        OrderEvaluation evaluation = orderEvaluationMapper.selectByOrderId(orderId);
        if (evaluation == null) {
            return null;
        }
        return buildEvaluationVO(evaluation);
    }

    @Override
    public PageResult<EvaluationVO> getByProductId(Long productId, Integer pageNum, Integer pageSize) {
        Page<OrderEvaluation> page = new Page<>(pageNum, pageSize);
        IPage<OrderEvaluation> evaluationPage = orderEvaluationMapper.selectPageByProductId(page, productId);
        List<EvaluationVO> voList = evaluationPage.getRecords().stream()
                .map(this::buildEvaluationVO)
                .collect(Collectors.toList());
        return PageResult.of(pageNum, pageSize, evaluationPage.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyEvaluation(Long evaluationId, String content) {
        Long userId = UserContext.getUserIdOrThrow();
        OrderEvaluation evaluation = orderEvaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BusinessException("评价不存在");
        }

        // 校验权限：只有被评价者能回复
        if (!evaluation.getEvaluatedId().equals(userId)) {
            throw new BusinessException("无权回复该评价");
        }

        // 校验是否已回复
        if (evaluation.getReplyContent() != null) {
            throw new BusinessException("该评价已回复");
        }

        // 更新回复
        OrderEvaluation update = new OrderEvaluation();
        update.setId(evaluationId);
        update.setReplyContent(content);
        update.setReplyTime(LocalDateTime.now());
        orderEvaluationMapper.updateById(update);

        log.info("评价回复成功，evaluationId: {}", evaluationId);
    }

    @Override
    public PageResult<EvaluationVO> getMyEvaluations(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserIdOrThrow();
        Page<OrderEvaluation> page = new Page<>(pageNum, pageSize);
        IPage<OrderEvaluation> evaluationPage = orderEvaluationMapper.selectPageByEvaluatorId(page, userId);
        List<EvaluationVO> voList = evaluationPage.getRecords().stream()
                .map(this::buildEvaluationVO)
                .collect(Collectors.toList());
        return PageResult.of(pageNum, pageSize, evaluationPage.getTotal(), voList);
    }

    @Override
    public PageResult<EvaluationVO> getReceivedEvaluations(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserIdOrThrow();
        Page<OrderEvaluation> page = new Page<>(pageNum, pageSize);
        IPage<OrderEvaluation> evaluationPage = orderEvaluationMapper.selectPageByEvaluatedId(page, userId);
        List<EvaluationVO> voList = evaluationPage.getRecords().stream()
                .map(this::buildEvaluationVO)
                .collect(Collectors.toList());
        return PageResult.of(pageNum, pageSize, evaluationPage.getTotal(), voList);
    }

    /**
     * 构建评价VO
     */
    private EvaluationVO buildEvaluationVO(OrderEvaluation evaluation) {
        EvaluationVO vo = new EvaluationVO();
        BeanUtils.copyProperties(evaluation, vo);
        return vo;
    }
}