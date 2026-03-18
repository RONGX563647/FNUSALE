package com.fnusale.user.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.user.UserAuthDTO;
import com.fnusale.common.dto.user.UserLoginDTO;
import com.fnusale.common.dto.user.UserRegisterDTO;
import com.fnusale.common.dto.user.UserUpdateDTO;
import com.fnusale.common.vo.user.LoginVO;
import com.fnusale.common.vo.user.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 手机号注册
     */
    void registerByPhone(UserRegisterDTO dto);

    /**
     * 邮箱注册
     */
    void registerByEmail(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO dto);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 刷新Token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUserInfo();

    /**
     * 更新用户信息
     */
    void updateUserInfo(UserUpdateDTO dto);

    /**
     * 修改密码
     */
    void updatePassword(String oldPassword, String newPassword);

    /**
     * 提交校园身份认证
     */
    void submitAuth(UserAuthDTO dto);

    /**
     * 获取认证状态
     */
    UserVO getAuthStatus();

    /**
     * 获取用户详情
     */
    UserVO getUserById(Long userId);

    /**
     * 更新定位权限
     */
    void updateLocationPermission(String permission);

    /**
     * 校验定位是否在校园内
     */
    boolean verifyLocation(String longitude, String latitude);

    /**
     * 获取我的发布列表
     */
    PageResult<Object> getMyProducts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取我的订单列表
     */
    PageResult<Object> getMyOrders(Long userId, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取我的收藏列表
     */
    PageResult<Object> getMyFavorites(Long userId, Integer pageNum, Integer pageSize);
}