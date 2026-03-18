package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.PointsLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 积分变动日志Mapper
 */
@Mapper
public interface PointsLogMapper extends BaseMapper<PointsLog> {

    /**
     * 查询用户积分变动记录
     */
    @Select("SELECT * FROM t_points_log WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<PointsLog> selectByUserId(@Param("userId") Long userId);
}