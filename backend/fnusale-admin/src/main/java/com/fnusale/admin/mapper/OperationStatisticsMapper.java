package com.fnusale.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.OperationStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 运营数据统计Mapper
 */
@Mapper
public interface OperationStatisticsMapper extends BaseMapper<OperationStatistics> {

    /**
     * 根据日期范围查询统计数据
     */
    @Select("SELECT * FROM t_operation_statistics WHERE stat_date BETWEEN #{startDate} AND #{endDate} ORDER BY stat_date")
    List<OperationStatistics> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据日期查询统计数据
     */
    @Select("SELECT * FROM t_operation_statistics WHERE stat_date = #{date}")
    OperationStatistics selectByDate(@Param("date") LocalDate date);
}