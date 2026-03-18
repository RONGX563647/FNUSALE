package com.fnusale.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.User;
import com.fnusale.common.vo.admin.UserDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 用户Mapper（Admin模块）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户列表（管理员视图）
     */
    IPage<UserDetailVO> selectUserPage(Page<UserDetailVO> page,
                                        @Param("username") String username,
                                        @Param("authStatus") String authStatus,
                                        @Param("identityType") String identityType);

    /**
     * 统计今日新增用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE DATE(create_time) = #{date} AND is_deleted = 0")
    int countTodayNewUsers(@Param("date") LocalDate date);

    /**
     * 统计待审核认证用户数
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE auth_status = 'UNDER_REVIEW' AND is_deleted = 0")
    int countPendingAuth();
}