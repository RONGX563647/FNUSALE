package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.EvaluationTagStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 评价标签统计Mapper
 */
@Mapper
public interface EvaluationTagStatMapper extends BaseMapper<EvaluationTagStat> {

    /**
     * 查询用户的评价标签统计
     */
    @Select("SELECT * FROM t_evaluation_tag_stat WHERE user_id = #{userId} ORDER BY tag_count DESC")
    List<EvaluationTagStat> selectByUserId(@Param("userId") Long userId);

    /**
     * 增加标签统计
     */
    @Update("UPDATE t_evaluation_tag_stat SET tag_count = tag_count + 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND tag_name = #{tagName}")
    int incrementTagCount(@Param("userId") Long userId, @Param("tagName") String tagName);
}