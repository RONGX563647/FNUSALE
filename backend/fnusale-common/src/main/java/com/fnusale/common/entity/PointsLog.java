package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分变动日志表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_points_log")
public class PointsLog implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 变动类型 (SIGN_REWARD/CONTINUOUS_REWARD/REPAIR_COST/COUPON_EXCHANGE/PRODUCT_TOP/TRADE_REWARD/RANK_REWARD/BIRTHDAY_REWARD)
     */
    private String changeType;

    /**
     * 变动数量（正负）
     */
    private Integer changeAmount;

    /**
     * 变动前积分
     */
    private Integer beforePoints;

    /**
     * 变动后积分
     */
    private Integer afterPoints;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}