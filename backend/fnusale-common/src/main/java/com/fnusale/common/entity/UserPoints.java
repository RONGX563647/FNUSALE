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
 * 用户积分表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_points")
public class UserPoints implements Serializable {

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
     * 累计获得积分
     */
    private Integer totalPoints;

    /**
     * 可用积分
     */
    private Integer availablePoints;

    /**
     * 已使用积分
     */
    private Integer usedPoints;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}