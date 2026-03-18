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
 * 评价举报表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_evaluation_report")
public class EvaluationReport implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评价ID
     */
    private Long evaluationId;

    /**
     * 举报者ID
     */
    private Long reporterId;

    /**
     * 举报原因
     */
    private String reportReason;

    /**
     * 举报说明
     */
    private String reportDesc;

    /**
     * 处理状态 (PENDING-待处理, APPROVED-已通过, REJECTED-已拒绝)
     */
    private String status;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}