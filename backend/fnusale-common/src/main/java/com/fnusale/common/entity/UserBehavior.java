package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为表
 */
@Data
@TableName("t_user_behavior")
public class UserBehavior {

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
     * 商品ID
     */
    private Long productId;

    /**
     * 行为类型（BROWSE/COLLECT/LIKE）
     */
    private String behaviorType;

    /**
     * 行为时间
     */
    private LocalDateTime createTime;
}