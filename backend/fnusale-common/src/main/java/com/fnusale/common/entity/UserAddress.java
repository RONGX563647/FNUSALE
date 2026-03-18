package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户地址表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_address")
public class UserAddress {

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
     * 地址类型（PICK_POINT/CUSTOM）
     */
    private String addressType;

    /**
     * 自提点ID
     */
    private Long pickPointId;

    /**
     * 自定义详细地址
     */
    private String customAddress;

    /**
     * 地址经度
     */
    private BigDecimal longitude;

    /**
     * 地址纬度
     */
    private BigDecimal latitude;

    /**
     * 是否默认地址（0-否, 1-是）
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}