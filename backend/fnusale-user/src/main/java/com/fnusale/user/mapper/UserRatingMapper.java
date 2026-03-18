package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.UserRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户评分Mapper
 */
@Mapper
public interface UserRatingMapper extends BaseMapper<UserRating> {

    /**
     * 根据用户ID查询评分
     */
    @Select("SELECT * FROM t_user_rating WHERE user_id = #{userId}")
    UserRating selectByUserId(@Param("userId") Long userId);
}