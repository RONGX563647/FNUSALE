package com.fnusale.user.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.user.EvaluationAppendDTO;
import com.fnusale.common.dto.user.EvaluationReplyDTO;
import com.fnusale.common.dto.user.EvaluationReportDTO;
import com.fnusale.common.dto.user.UserEvaluationDTO;
import com.fnusale.common.vo.user.UserEvaluationVO;
import com.fnusale.common.vo.user.UserRatingVO;

import java.util.List;
import java.util.Map;

/**
 * 用户评价服务接口
 */
public interface UserEvaluationService {

    /**
     * 提交评价
     */
    void submitEvaluation(Long userId, UserEvaluationDTO dto);

    /**
     * 追加评价
     */
    void appendEvaluation(Long userId, Long evaluationId, EvaluationAppendDTO dto);

    /**
     * 卖家回复
     */
    void replyEvaluation(Long userId, Long evaluationId, EvaluationReplyDTO dto);

    /**
     * 获取用户评价列表
     */
    PageResult<UserEvaluationVO> getUserEvaluations(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取我的评价
     */
    PageResult<UserEvaluationVO> getMyEvaluations(Long currentUserId, Integer pageNum, Integer pageSize);

    /**
     * 获取评价统计
     */
    UserRatingVO getUserRating(Long userId);

    /**
     * 获取评价标签统计
     */
    List<Map<String, Object>> getUserTags(Long userId);

    /**
     * 举报评价
     */
    void reportEvaluation(Long userId, Long evaluationId, EvaluationReportDTO dto);
}