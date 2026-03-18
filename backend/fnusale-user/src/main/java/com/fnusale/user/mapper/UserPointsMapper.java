package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户积分Mapper
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {

    /**
     * 根据用户ID查询积分
     */
    @Select("SELECT * FROM t_user_points WHERE user_id = #{userId}")
    UserPoints selectByUserId(@Param("userId") Long userId);

    /**
     * 增加积分（原子操作）
     */
    @Update("UPDATE t_user_points SET available_points = available_points + #{points}, " +
            "total_points = total_points + #{points}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND available_points + #{points} >= 0")
    int addPoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 扣减积分（原子操作）
     */
    @Update("UPDATE t_user_points SET available_points = available_points - #{points}, " +
            "used_points = used_points + #{points}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND available_points >= #{points}")
    int deductPoints(@Param("userId") Long userId, @Param("points") Integer points);
}