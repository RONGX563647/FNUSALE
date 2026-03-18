package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.RankingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 排行榜记录Mapper
 */
@Mapper
public interface RankingRecordMapper extends BaseMapper<RankingRecord> {

    /**
     * 按类型和日期查询排行列表
     */
    @Select("SELECT * FROM t_ranking_record WHERE rank_type = #{rankType} AND rank_date = #{rankDate} ORDER BY rank_position LIMIT #{limit}")
    List<RankingRecord> selectByTypeAndDate(@Param("rankType") String rankType, @Param("rankDate") LocalDate rankDate, @Param("limit") int limit);

    /**
     * 查询用户在指定排行中的记录
     */
    @Select("SELECT * FROM t_ranking_record WHERE rank_type = #{rankType} AND user_id = #{userId} ORDER BY rank_date DESC LIMIT 1")
    RankingRecord selectLatestByUserAndType(@Param("rankType") String rankType, @Param("userId") Long userId);

    /**
     * 查询用户指定日期的排行记录
     */
    @Select("SELECT * FROM t_ranking_record WHERE rank_type = #{rankType} AND rank_date = #{rankDate} AND user_id = #{userId}")
    RankingRecord selectByTypeDateAndUser(@Param("rankType") String rankType, @Param("rankDate") LocalDate rankDate, @Param("userId") Long userId);

    /**
     * 查询排行历史
     */
    @Select("SELECT * FROM t_ranking_record WHERE rank_type = #{rankType} AND user_id = #{userId} ORDER BY rank_date DESC")
    List<RankingRecord> selectHistoryByTypeAndUser(@Param("rankType") String rankType, @Param("userId") Long userId);

    /**
     * 查询用户排名
     */
    @Select("SELECT rank_position FROM t_ranking_record WHERE rank_type = #{rankType} AND rank_date = #{rankDate} AND user_id = #{userId}")
    Integer selectUserRank(@Param("rankType") String rankType, @Param("rankDate") LocalDate rankDate, @Param("userId") Long userId);
}