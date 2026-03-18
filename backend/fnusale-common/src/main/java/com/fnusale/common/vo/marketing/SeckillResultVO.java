package com.fnusale.common.vo.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀结果VO
 */
@Data
@Schema(description = "秒杀结果")
public class SeckillResultVO implements Serializable {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "订单ID（成功时返回）")
    private Long orderId;

    @Schema(description = "消息")
    private String message;

    public static SeckillResultVO success(Long orderId) {
        SeckillResultVO vo = new SeckillResultVO();
        vo.setSuccess(true);
        vo.setOrderId(orderId);
        vo.setMessage("秒杀成功");
        return vo;
    }

    public static SeckillResultVO fail(String message) {
        SeckillResultVO vo = new SeckillResultVO();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}