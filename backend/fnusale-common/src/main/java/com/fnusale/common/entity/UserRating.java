package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户评价分表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_rating")
public class UserRating implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（唯一）
     */
    private Long userId;

    /**
     * 综合评分 (1.00-5.00)
     */
    private BigDecimal overallRating;

    /**
     * 评分等级 (EXCELLENT/VERY_GOOD/GOOD/AVERAGE/POOR/VERY_POOR)
     */
    private String ratingLevel;

    /**
     * 累计评价数
     */
    private Integer totalEvaluations;

    /**
     * 好评数（4-5星）
     */
    private Integer positiveCount;

    /**
     * 中评数（3星）
     */
    private Integer neutralCount;

    /**
     * 差评数（1-2星）
     */
    private Integer negativeCount;

    /**
     * 好评率（%）
     */
    private BigDecimal positiveRate;

    /**
     * 近30天评价数
     */
    private Integer last30dEvaluations;

    /**
     * 近30天评分
     */
    private BigDecimal last30dRating;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}