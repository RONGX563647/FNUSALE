package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户地址Mapper
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 查询用户的地址列表
     */
    @Select("SELECT * FROM t_user_address WHERE user_id = #{userId} ORDER BY is_default DESC, update_time DESC")
    List<UserAddress> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的默认地址
     */
    @Select("SELECT * FROM t_user_address WHERE user_id = #{userId} AND is_default = 1")
    UserAddress selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 统计用户地址数量
     */
    @Select("SELECT COUNT(*) FROM t_user_address WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 清除用户的默认地址
     */
    @Update("UPDATE t_user_address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefaultByUserId(@Param("userId") Long userId);
}