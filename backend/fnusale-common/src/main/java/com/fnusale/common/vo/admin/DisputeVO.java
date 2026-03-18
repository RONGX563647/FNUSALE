package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 纠纷详情VO
 */
@Data
@Schema(description = "纠纷详情")
public class DisputeVO implements Serializable {

    @Schema(description = "纠纷ID")
    private Long disputeId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "纠纷类型（PRODUCT_NOT_MATCH/NO_DELIVERY/PRODUCT_DAMAGED/OTHER）")
    private String disputeType;

    @Schema(description = "纠纷状态（PENDING/PROCESSING/RESOLVED）")
    private String disputeStatus;

    @Schema(description = "举证材料URL列表")
    private List<String> evidenceUrls;

    @Schema(description = "处理结果")
    private String processResult;

    @Schema(description = "处理备注")
    private String processRemark;

    @Schema(description = "发起者信息")
    private DisputeUser initiator;

    @Schema(description = "被投诉者信息")
    private DisputeUser accused;

    @Schema(description = "订单信息")
    private DisputeOrder orderInfo;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Data
    @Schema(description = "纠纷用户信息")
    public static class DisputeUser implements Serializable {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "描述")
        private String description;
    }

    @Data
    @Schema(description = "纠纷订单信息")
    public static class DisputeOrder implements Serializable {
        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品价格")
        private String productPrice;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;
    }
}