package com.fnusale.trade.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.trade.TradeDisputeDTO;
import com.fnusale.common.vo.trade.DisputeVO;

import java.util.List;
import java.util.Map;

/**
 * 交易纠纷服务接口
 */
public interface TradeDisputeService {

    /**
     * 申请纠纷
     */
    void createDispute(TradeDisputeDTO dto);

    /**
     * 获取纠纷详情
     */
    DisputeVO getDisputeById(Long disputeId);

    /**
     * 获取我的纠纷列表
     */
    PageResult<DisputeVO> getMyDisputes(String status, Integer pageNum, Integer pageSize);

    /**
     * 撤销纠纷
     */
    void cancelDispute(Long disputeId);

    /**
     * 补充证据
     */
    void addEvidence(Long disputeId, String evidenceUrl);

    /**
     * 获取纠纷处理记录
     */
    List<Map<String, Object>> getDisputeRecords(Long disputeId);
}