package com.fnusale.admin.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.admin.DisputeProcessDTO;
import com.fnusale.common.vo.admin.DisputeVO;

/**
 * 纠纷处理服务接口
 */
public interface AdminDisputeService {

    /**
     * 分页查询纠纷列表
     */
    PageResult<DisputeVO> getDisputePage(String status, Integer pageNum, Integer pageSize);

    /**
     * 获取纠纷详情
     */
    DisputeVO getDisputeDetail(Long disputeId);

    /**
     * 处理纠纷
     */
    void processDispute(Long disputeId, DisputeProcessDTO dto, Long adminId);
}