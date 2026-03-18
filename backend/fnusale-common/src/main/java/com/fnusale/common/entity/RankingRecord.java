package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排行榜记录表
 */
@Data
@TableName("t_ranking_record")
public class RankingRecord implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 排行类型 (ACTIVITY/TRADE/CREDIT/RATING/NEW_SELLER)
     */
    private String rankType;

    /**
     * 排行日期
     */
    private LocalDate rankDate;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 排名
     */
    private Integer rankPosition;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}