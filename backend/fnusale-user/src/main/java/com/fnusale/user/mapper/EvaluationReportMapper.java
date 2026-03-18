package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.EvaluationReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评价举报Mapper
 */
@Mapper
public interface EvaluationReportMapper extends BaseMapper<EvaluationReport> {

    /**
     * 检查是否已举报
     */
    @Select("SELECT COUNT(*) FROM t_evaluation_report WHERE evaluation_id = #{evaluationId} AND reporter_id = #{reporterId}")
    int countByEvaluationAndReporter(@Param("evaluationId") Long evaluationId, @Param("reporterId") Long reporterId);
}