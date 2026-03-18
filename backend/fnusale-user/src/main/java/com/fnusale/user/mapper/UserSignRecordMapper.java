package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.UserSignRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户签到记录Mapper
 */
@Mapper
public interface UserSignRecordMapper extends BaseMapper<UserSignRecord> {

    /**
     * 查询用户某天的签到记录
     */
    @Select("SELECT * FROM t_user_sign_record WHERE user_id = #{userId} AND sign_date = #{signDate}")
    UserSignRecord selectByUserIdAndDate(@Param("userId") Long userId, @Param("signDate") LocalDate signDate);

    /**
     * 查询用户某月的签到记录
     */
    @Select("SELECT * FROM t_user_sign_record WHERE user_id = #{userId} " +
            "AND YEAR(sign_date) = #{year} AND MONTH(sign_date) = #{month} " +
            "ORDER BY sign_date DESC")
    List<UserSignRecord> selectByUserIdAndMonth(@Param("userId") Long userId,
            @Param("year") int year, @Param("month") int month);

    /**
     * 查询用户最近一次签到记录
     */
    @Select("SELECT * FROM t_user_sign_record WHERE user_id = #{userId} ORDER BY sign_date DESC LIMIT 1")
    UserSignRecord selectLatestByUserId(@Param("userId") Long userId);

    /**
     * 统计用户总签到天数
     */
    @Select("SELECT COUNT(*) FROM t_user_sign_record WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户某月补签次数
     */
    @Select("SELECT COUNT(*) FROM t_user_sign_record WHERE user_id = #{userId} " +
            "AND is_repair = 1 AND YEAR(sign_date) = #{year} AND MONTH(sign_date) = #{month}")
    int countRepairByUserIdAndMonth(@Param("userId") Long userId,
            @Param("year") int year, @Param("month") int month);
}