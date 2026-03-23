package com.fnusale.common.vo.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "秒杀结果")
public class SeckillResultVO implements Serializable {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "订单ID（成功时返回）")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单状态")
    private String orderStatus;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "时间戳")
    private Long timestamp;

    @Schema(description = "排队号（秒杀成功时返回，用于查询结果）")
    private Long queueNumber;

    @Schema(description = "活动ID")
    private Long activityId;

    public static SeckillResultVO success(Long orderId) {
        return SeckillResultVO.builder()
                .success(true)
                .orderId(orderId)
                .message("秒杀成功")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 秒杀成功（异步创建订单）
     * @param queueNumber 排队号
     * @param activityId 活动ID
     * @return 秒杀结果
     */
    public static SeckillResultVO pending(Long queueNumber, Long activityId) {
        return SeckillResultVO.builder()
                .success(true)
                .queueNumber(queueNumber)
                .activityId(activityId)
                .message("秒杀成功，订单创建中")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static SeckillResultVO fail(String message) {
        return SeckillResultVO.builder()
                .success(false)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}