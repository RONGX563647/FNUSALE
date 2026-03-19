package com.fnusale.trade.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.trade.OrderEvaluationDTO;
import com.fnusale.common.vo.trade.EvaluationVO;

/**
 * 订单评价服务接口
 */
public interface OrderEvaluationService {

    /**
     * 提交评价
     */
    void submitEvaluation(OrderEvaluationDTO dto);

    /**
     * 根据订单ID获取评价
     */
    EvaluationVO getByOrderId(Long orderId);

    /**
     * 根据商品ID获取评价列表
     */
    PageResult<EvaluationVO> getByProductId(Long productId, Integer pageNum, Integer pageSize);

    /**
     * 卖家回复评价
     */
    void replyEvaluation(Long evaluationId, String content);

    /**
     * 获取我发出的评价列表
     */
    PageResult<EvaluationVO> getMyEvaluations(Integer pageNum, Integer pageSize);

    /**
     * 获取我收到的评价列表
     */
    PageResult<EvaluationVO> getReceivedEvaluations(Integer pageNum, Integer pageSize);
}