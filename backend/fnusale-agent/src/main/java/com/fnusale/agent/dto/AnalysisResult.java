package com.fnusale.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商品分析结果DTO
 */
@Data
@Schema(description = "商品分析结果")
public class AnalysisResult {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "价格分析")
    private PriceAnalysis priceAnalysis;

    @Schema(description = "卖家分析")
    private SellerAnalysis sellerAnalysis;

    @Schema(description = "购买建议", example = "建议购买")
    private String recommendation;

    @Schema(description = "风险提醒列表")
    private List<String> riskAlerts;

    @Schema(description = "议价建议")
    private BargainSuggestion bargainSuggestion;

    /**
     * 价格分析
     */
    @Data
    @Schema(description = "价格分析")
    public static class PriceAnalysis {
        @Schema(description = "当前价格")
        private Integer currentPrice;

        @Schema(description = "参考价格区间")
        private List<Integer> referenceRange;

        @Schema(description = "价格水平", example = "合理/偏高/偏低")
        private String priceLevel;

        @Schema(description = "价格评分(1-10)")
        private Integer priceScore;
    }

    /**
     * 卖家分析
     */
    @Data
    @Schema(description = "卖家分析")
    public static class SellerAnalysis {
        @Schema(description = "卖家ID")
        private Long sellerId;

        @Schema(description = "卖家昵称")
        private String sellerNickname;

        @Schema(description = "综合评分")
        private Double rating;

        @Schema(description = "交易次数")
        private Integer tradeCount;

        @Schema(description = "好评率(%)")
        private Integer positiveRate;

        @Schema(description = "信誉等级", example = "优秀/良好/一般")
        private String creditLevel;
    }

    /**
     * 议价建议
     */
    @Data
    @Schema(description = "议价建议")
    public static class BargainSuggestion {
        @Schema(description = "建议议价区间")
        private List<Integer> priceRange;

        @Schema(description = "建议价格")
        private Integer suggestedPrice;

        @Schema(description = "议价策略")
        private List<String> strategies;

        @Schema(description = "聊天话术模板")
        private List<String> chatTemplates;
    }
}