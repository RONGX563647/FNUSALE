package com.fnusale.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND is_deleted = 0")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM t_user WHERE campus_email = #{email} AND is_deleted = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据学号/工号查询用户
     */
    @Select("SELECT * FROM t_user WHERE student_teacher_id = #{studentTeacherId} AND is_deleted = 0")
    User selectByStudentTeacherId(@Param("studentTeacherId") String studentTeacherId);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE phone = #{phone} AND is_deleted = 0")
    int countByPhone(@Param("phone") String phone);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE campus_email = #{email} AND is_deleted = 0")
    int countByEmail(@Param("email") String email);

    /**
     * 检查学号/工号是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE student_teacher_id = #{studentTeacherId} AND is_deleted = 0")
    int countByStudentTeacherId(@Param("studentTeacherId") String studentTeacherId);
}