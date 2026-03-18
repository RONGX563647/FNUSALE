package com.fnusale.admin.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.admin.UserQueryDTO;
import com.fnusale.common.vo.admin.UserDetailVO;

/**
 * 用户管理服务接口
 */
public interface AdminUserService {

    /**
     * 分页查询用户列表
     */
    PageResult<UserDetailVO> getUserPage(UserQueryDTO query);

    /**
     * 获取用户详情
     */
    UserDetailVO getUserDetail(Long userId);

    /**
     * 获取待审核认证列表
     */
    PageResult<UserDetailVO> getPendingAuthList(Integer pageNum, Integer pageSize);

    /**
     * 认证通过
     */
    void authPass(Long userId, Long adminId);

    /**
     * 认证驳回
     */
    void authReject(Long userId, Long adminId, String reason);

    /**
     * 封禁用户
     */
    void banUser(Long userId, Long adminId, String reason);

    /**
     * 解封用户
     */
    void unbanUser(Long userId, Long adminId);

    /**
     * 调整信誉分
     */
    Integer adjustCredit(Long userId, Integer score, String reason, Long adminId);
}