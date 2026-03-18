package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.CampusPickPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 校园自提点Mapper
 */
@Mapper
public interface CampusPickPointMapper extends BaseMapper<CampusPickPoint> {

    /**
     * 查询所有启用的自提点
     */
    @Select("SELECT * FROM t_campus_pick_point WHERE enable_status = 1 ORDER BY create_time")
    List<CampusPickPoint> selectAllEnabled();

    /**
     * 根据校区查询自提点
     */
    @Select("SELECT * FROM t_campus_pick_point WHERE campus_area = #{campusArea} AND enable_status = 1 ORDER BY create_time")
    List<CampusPickPoint> selectByCampusArea(@Param("campusArea") String campusArea);
}