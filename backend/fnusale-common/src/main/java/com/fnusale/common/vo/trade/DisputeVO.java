package com.fnusale.common.vo.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交易纠纷VO
 */
@Data
@Schema(description = "交易纠纷信息")
public class DisputeVO implements Serializable {

    @Schema(description = "纠纷ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "发起者ID")
    private Long initiatorId;

    @Schema(description = "发起者用户名")
    private String initiatorName;

    @Schema(description = "被投诉者ID")
    private Long accusedId;

    @Schema(description = "被投诉者用户名")
    private String accusedName;

    @Schema(description = "纠纷类型（PRODUCT_NOT_MATCH-商品不符，NO_DELIVERY-未发货，OTHER-其他）")
    private String disputeType;

    @Schema(description = "纠纷类型描述")
    private String disputeTypeDesc;

    @Schema(description = "举证材料地址")
    private String evidenceUrl;

    @Schema(description = "纠纷状态（PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决）")
    private String disputeStatus;

    @Schema(description = "纠纷状态描述")
    private String disputeStatusDesc;

    @Schema(description = "处理结果")
    private String processResult;

    @Schema(description = "处理管理员ID")
    private Long adminId;

    @Schema(description = "处理备注")
    private String processRemark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}