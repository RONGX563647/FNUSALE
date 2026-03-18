package com.fnusale.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.AdminPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 管理员权限Mapper
 */
@Mapper
public interface AdminPermissionMapper extends BaseMapper<AdminPermission> {

    /**
     * 根据管理员ID查询权限代码列表
     */
    List<String> selectPermissionCodesByAdminId(Long adminId);
}
