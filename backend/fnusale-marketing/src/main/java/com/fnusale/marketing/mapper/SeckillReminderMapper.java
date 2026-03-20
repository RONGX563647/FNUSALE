package com.fnusale.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.SeckillReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

/**
 * 秒杀提醒 Mapper
 */
@Mapper
public interface SeckillReminderMapper extends BaseMapper<SeckillReminder> {

    /**
     * 查询用户对某活动是否已设置提醒
     */
    @Select("SELECT COUNT(*) FROM t_seckill_reminder WHERE user_id = #{userId} AND activity_id = #{activityId}")
    int countByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /**
     * 查询活动的所有用户提醒
     */
    @Select("SELECT user_id FROM t_seckill_reminder WHERE activity_id = #{activityId} AND is_reminded = 0")
    List<Long> selectUserIdsByActivity(@Param("activityId") Long activityId);

    /**
     * 更新提醒状态为已提醒
     */
    @Update("UPDATE t_seckill_reminder SET is_reminded = 1 WHERE activity_id = #{activityId}")
    int updateReminded(@Param("activityId") Long activityId);

    /**
     * 删除用户对某活动的提醒
     */
    @Update("DELETE FROM t_seckill_reminder WHERE user_id = #{userId} AND activity_id = #{activityId}")
    int deleteByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /**
     * 批量查询用户已设置提醒的活动ID
     * 解决 N+1 查询问题
     */
    @Select("<script>" +
            "SELECT DISTINCT activity_id FROM t_seckill_reminder " +
            "WHERE user_id = #{userId} AND activity_id IN " +
            "<foreach item='id' collection='activityIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    Set<Long> selectRemindedActivityIds(@Param("userId") Long userId, @Param("activityIds") List<Long> activityIds);
}