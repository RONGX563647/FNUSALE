package com.fnusale.agent.service;

import com.fnusale.agent.dto.AnalysisResult;

/**
 * 商品分析服务接口
 *
 * 提供购买分析功能：
 * - 价格分析：比价、价格合理性评估
 * - 卖家分析：信誉评分、交易历史
 * - 风险提醒：异常价格、风险卖家识别
 * - 议价建议：价格区间、议价策略
 */
public interface ProductAnalyzeService {

    /**
     * 分析商品
     *
     * @param productId 商品ID
     * @param userId    用户ID（可选，用于个性化分析）
     * @return 分析结果
     */
    AnalysisResult analyze(Long productId, Long userId);

    /**
     * 获取议价建议
     *
     * @param productId 商品ID
     * @return 议价建议
     */
    AnalysisResult.BargainSuggestion getBargainSuggestion(Long productId);
}