package com.fnusale.agent.dto;

import com.fnusale.common.vo.product.ProductVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 对话响应DTO
 */
@Data
@Schema(description = "对话响应")
public class ChatResponse {

    @Schema(description = "AI回复内容")
    private String reply;

    @Schema(description = "推荐的商品列表")
    private List<ProductVO> recommendProducts;

    @Schema(description = "应用的筛选条件")
    private Map<String, Object> filters;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "意图类型", example = "SEARCH_PRODUCT")
    private String intentType;

    @Schema(description = "是否需要更多信息")
    private Boolean needMoreInfo;
}