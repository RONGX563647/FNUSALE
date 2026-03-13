package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 校园自提点表
 */
@Data
@TableName("t_campus_pick_point")
public class CampusPickPoint {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 自提点名称
     */
    private String pickPointName;

    /**
     * 所属校区
     */
    private String campusArea;

    /**
     * 详细位置
     */
    private String detailAddress;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 启用状态（0-禁用, 1-启用）
     */
    private Integer enableStatus;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;
}