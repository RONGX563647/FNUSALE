package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.RankingRewardLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 排行榜奖励记录Mapper
 */
@Mapper
public interface RankingRewardLogMapper extends BaseMapper<RankingRewardLog> {

    /**
     * 查询用户的奖励列表
     */
    @Select("SELECT * FROM t_ranking_reward_log WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<RankingRewardLog> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户未领取的奖励
     */
    @Select("SELECT * FROM t_ranking_reward_log WHERE user_id = #{userId} AND is_claimed = 0 ORDER BY create_time DESC")
    List<RankingRewardLog> selectUnclaimedByUserId(@Param("userId") Long userId);

    /**
     * 查询用户已领取的奖励
     */
    @Select("SELECT * FROM t_ranking_reward_log WHERE user_id = #{userId} AND is_claimed = 1 ORDER BY create_time DESC")
    List<RankingRewardLog> selectClaimedByUserId(@Param("userId") Long userId);

    /**
     * 领取奖励
     */
    @Update("UPDATE t_ranking_reward_log SET is_claimed = 1 WHERE id = #{id} AND user_id = #{userId} AND is_claimed = 0")
    int claimReward(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询奖励详情
     */
    @Select("SELECT * FROM t_ranking_reward_log WHERE id = #{id}")
    RankingRewardLog selectById(@Param("id") Long id);
}