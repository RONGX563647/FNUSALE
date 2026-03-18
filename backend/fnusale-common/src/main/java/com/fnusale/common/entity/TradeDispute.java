package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易纠纷表
 */
@Data
@TableName("t_trade_dispute")
public class TradeDispute {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 发起者ID
     */
    private Long initiatorId;

    /**
     * 被投诉者ID
     */
    private Long accusedId;

    /**
     * 纠纷类型（PRODUCT_NOT_MATCH/NO_DELIVERY/OTHER）
     */
    private String disputeType;

    /**
     * 举证材料地址
     */
    private String evidenceUrl;

    /**
     * 状态（PENDING/PROCESSING/RESOLVED）
     */
    private String disputeStatus;

    /**
     * 处理结果
     */
    private String processResult;

    /**
     * 处理管理员ID
     */
    private Long adminId;

    /**
     * 处理备注
     */
    private String processRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}