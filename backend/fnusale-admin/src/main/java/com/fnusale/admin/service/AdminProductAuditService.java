package com.fnusale.admin.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.vo.admin.AuditRecordVO;
import com.fnusale.common.vo.admin.AuditStatisticsVO;
import com.fnusale.common.vo.admin.PendingProductVO;

import java.util.List;

/**
 * 商品审核服务接口
 */
public interface AdminProductAuditService {

    /**
     * 获取待审核商品列表
     */
    PageResult<PendingProductVO> getPendingList(Integer pageNum, Integer pageSize);

    /**
     * 审核通过
     */
    void auditPass(Long productId, Long adminId);

    /**
     * 审核驳回
     */
    void auditReject(Long productId, Long adminId, String reason);

    /**
     * 批量审核通过
     */
    Integer batchAuditPass(List<Long> productIds, Long adminId);

    /**
     * 强制下架
     */
    void forceOffShelf(Long productId, Long adminId, String reason);

    /**
     * 获取审核记录
     */
    List<AuditRecordVO> getAuditRecords(Long productId);

    /**
     * 获取审核统计
     */
    AuditStatisticsVO getAuditStatistics();
}