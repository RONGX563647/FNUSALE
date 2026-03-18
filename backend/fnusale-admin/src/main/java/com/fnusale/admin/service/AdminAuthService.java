package com.fnusale.admin.service;

import com.fnusale.common.dto.admin.AdminLoginDTO;
import com.fnusale.common.vo.admin.AdminInfoVO;
import com.fnusale.common.vo.admin.AdminLoginVO;

/**
 * 管理员认证服务接口
 */
public interface AdminAuthService {

    /**
     * 管理员登录
     *
     * @param dto 登录请求
     * @param ip  登录IP
     * @return 登录响应
     */
    AdminLoginVO login(AdminLoginDTO dto, String ip);

    /**
     * 管理员登出
     */
    void logout();

    /**
     * 获取当前管理员信息
     *
     * @return 管理员信息
     */
    AdminInfoVO getAdminInfo();

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    AdminLoginVO refreshToken(String refreshToken);
}
