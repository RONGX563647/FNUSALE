package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_sign_record")
public class UserSignRecord implements Serializable {

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
     * 签到日期
     */
    private LocalDate signDate;

    /**
     * 签到时间
     */
    private LocalDateTime signTime;

    /**
     * 连续签到天数
     */
    private Integer continuousDays;

    /**
     * 获得积分
     */
    private Integer rewardPoints;

    /**
     * 是否补签 (0-否, 1-是)
     */
    private Integer isRepair;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}