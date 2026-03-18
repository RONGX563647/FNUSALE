package com.fnusale.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.UserAuthDTO;
import com.fnusale.common.dto.user.UserLoginDTO;
import com.fnusale.common.dto.user.UserRegisterDTO;
import com.fnusale.common.dto.user.UserUpdateDTO;
import com.fnusale.common.entity.User;
import com.fnusale.common.entity.UserPoints;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.util.DesensitizeUtil;
import com.fnusale.common.util.JwtUtil;
import com.fnusale.common.vo.user.LoginVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.user.mapper.UserMapper;
import com.fnusale.user.mapper.UserPointsMapper;
import com.fnusale.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserPointsMapper userPointsMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    // 当前登录用户ID（通过ThreadLocal或请求上下文获取）
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    public static void clearCurrentUserId() {
        currentUserId.remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerByPhone(UserRegisterDTO dto) {
        // 参数校验
        if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (!dto.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        // 用户名长度校验
        if (dto.getUsername() != null && (dto.getUsername().length() < UserConstants.USERNAME_MIN_LENGTH ||
            dto.getUsername().length() > UserConstants.USERNAME_MAX_LENGTH)) {
            throw new BusinessException("用户名长度应在" + UserConstants.USERNAME_MIN_LENGTH + "-" + UserConstants.USERNAME_MAX_LENGTH + "个字符之间");
        }
        // 密码长度校验
        if (dto.getPassword() != null && (dto.getPassword().length() < UserConstants.PASSWORD_MIN_LENGTH ||
            dto.getPassword().length() > UserConstants.PASSWORD_MAX_LENGTH)) {
            throw new BusinessException("密码长度应在" + UserConstants.PASSWORD_MIN_LENGTH + "-" + UserConstants.PASSWORD_MAX_LENGTH + "位之间");
        }

        // 检查手机号是否已注册
        if (userMapper.countByPhone(dto.getPhone()) > 0) {
            throw new BusinessException("该手机号已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(dto.getUsername())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword()))
                .identityType(dto.getIdentityType() != null ? dto.getIdentityType() : "STUDENT")
                .authStatus(AuthStatus.UNAUTH.getCode())
                .creditScore(UserConstants.DEFAULT_CREDIT_SCORE)
                .locationPermission("DENY")
                .build();

        userMapper.insert(user);

        // 初始化积分
        initUserPoints(user.getId());

        log.info("用户注册成功, userId: {}, phone: {}", user.getId(), DesensitizeUtil.phone(dto.getPhone()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerByEmail(UserRegisterDTO dto) {
        // 参数校验
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!dto.getEmail().matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new BusinessException("邮箱格式不正确");
        }
        // 用户名长度校验
        if (dto.getUsername() != null && (dto.getUsername().length() < UserConstants.USERNAME_MIN_LENGTH ||
            dto.getUsername().length() > UserConstants.USERNAME_MAX_LENGTH)) {
            throw new BusinessException("用户名长度应在" + UserConstants.USERNAME_MIN_LENGTH + "-" + UserConstants.USERNAME_MAX_LENGTH + "个字符之间");
        }
        // 密码长度校验
        if (dto.getPassword() != null && (dto.getPassword().length() < UserConstants.PASSWORD_MIN_LENGTH ||
            dto.getPassword().length() > UserConstants.PASSWORD_MAX_LENGTH)) {
            throw new BusinessException("密码长度应在" + UserConstants.PASSWORD_MIN_LENGTH + "-" + UserConstants.PASSWORD_MAX_LENGTH + "位之间");
        }

        // 检查邮箱是否已注册
        if (userMapper.countByEmail(dto.getEmail()) > 0) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(dto.getUsername())
                .campusEmail(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .identityType(dto.getIdentityType() != null ? dto.getIdentityType() : "STUDENT")
                .authStatus(AuthStatus.UNAUTH.getCode())
                .creditScore(UserConstants.DEFAULT_CREDIT_SCORE)
                .locationPermission("DENY")
                .build();

        userMapper.insert(user);

        // 初始化积分
        initUserPoints(user.getId());

        log.info("用户注册成功, userId: {}, email: {}", user.getId(), DesensitizeUtil.email(dto.getEmail()));
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        User user = null;

        if ("PHONE".equals(dto.getLoginType())) {
            if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
                throw new BusinessException("手机号不能为空");
            }
            user = userMapper.selectByPhone(dto.getPhone());
        } else if ("EMAIL".equals(dto.getLoginType())) {
            if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
                throw new BusinessException("邮箱不能为空");
            }
            user = userMapper.selectByEmail(dto.getEmail());
        } else {
            throw new BusinessException("登录类型不正确");
        }

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 生成Token
        String accessToken = JwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getIdentityType());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId());

        // 缓存Token到Redis
        String tokenKey = UserConstants.TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, accessToken, UserConstants.CAPTCHA_EXPIRATION * 24, TimeUnit.SECONDS);

        // 构建返回对象
        LoginVO loginVO = LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(JwtUtil.getAccessTokenExpirationSeconds())
                .userInfo(buildUserVO(user))
                .build();

        log.info("用户登录成功, userId: {}", user.getId());
        return loginVO;
    }

    @Override
    public void logout() {
        Long userId = getCurrentUserId();
        if (userId != null) {
            String tokenKey = UserConstants.TOKEN_KEY_PREFIX + userId;
            redisTemplate.delete(tokenKey);
            log.info("用户登出成功, userId: {}", userId);
        }
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 验证refreshToken
        if (!JwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效或已过期");
        }

        String tokenType = JwtUtil.getTokenType(refreshToken);
        if (!JwtUtil.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw new BusinessException("令牌类型不正确");
        }

        Long userId = JwtUtil.getUserId(refreshToken);
        if (userId == null) {
            throw new BusinessException("令牌解析失败");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 生成新的Token
        String newAccessToken = JwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getIdentityType());
        String newRefreshToken = JwtUtil.generateRefreshToken(user.getId());

        // 更新Redis缓存
        String tokenKey = UserConstants.TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, newAccessToken, UserConstants.CAPTCHA_EXPIRATION * 24, TimeUnit.SECONDS);

        LoginVO loginVO = LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(JwtUtil.getAccessTokenExpirationSeconds())
                .build();

        return loginVO;
    }

    @Override
    public UserVO getCurrentUserInfo() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return buildUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserUpdateDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = new User();
        user.setId(userId);

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            if (dto.getUsername().length() < UserConstants.USERNAME_MIN_LENGTH ||
                dto.getUsername().length() > UserConstants.USERNAME_MAX_LENGTH) {
                throw new BusinessException("用户名长度应在" + UserConstants.USERNAME_MIN_LENGTH + "-" + UserConstants.USERNAME_MAX_LENGTH + "个字符之间");
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        if (dto.getBirthday() != null) {
            user.setBirthday(dto.getBirthday());
        }

        if (dto.getLocationPermission() != null) {
            user.setLocationPermission(dto.getLocationPermission());
        }

        userMapper.updateById(user);

        log.info("用户信息更新成功, userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPassword, String newPassword) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 验证新密码格式
        if (newPassword.length() < UserConstants.PASSWORD_MIN_LENGTH ||
            newPassword.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new BusinessException("密码长度应在" + UserConstants.PASSWORD_MIN_LENGTH + "-" + UserConstants.PASSWORD_MAX_LENGTH + "位之间");
        }

        // 更新密码
        User updateUser = User.builder()
                .id(userId)
                .password(passwordEncoder.encode(newPassword))
                .build();

        userMapper.updateById(updateUser);

        log.info("用户密码修改成功, userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuth(UserAuthDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查是否已认证或审核中
        if (AuthStatus.AUTH_SUCCESS.getCode().equals(user.getAuthStatus())) {
            throw new BusinessException("您已完成身份认证");
        }
        if (AuthStatus.UNDER_REVIEW.getCode().equals(user.getAuthStatus())) {
            throw new BusinessException("认证申请审核中，请耐心等待");
        }

        // 检查学号/工号是否已被使用
        if (userMapper.countByStudentTeacherId(dto.getStudentTeacherId()) > 0) {
            User existUser = userMapper.selectByStudentTeacherId(dto.getStudentTeacherId());
            if (existUser != null && !existUser.getId().equals(userId)) {
                throw new BusinessException("该学号/工号已被其他用户使用");
            }
        }

        // 更新认证信息
        User updateUser = User.builder()
                .id(userId)
                .studentTeacherId(dto.getStudentTeacherId())
                .identityType(dto.getIdentityType())
                .authImageUrl(dto.getAuthImageUrl())
                .authStatus(AuthStatus.UNDER_REVIEW.getCode())
                .build();

        userMapper.updateById(updateUser);

        log.info("用户提交身份认证, userId: {}, studentTeacherId: {}", userId,
                DesensitizeUtil.studentTeacherId(dto.getStudentTeacherId()));
    }

    @Override
    public UserVO getAuthStatus() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return buildUserVO(user);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .identityType(user.getIdentityType())
                .authStatus(user.getAuthStatus())
                .creditScore(user.getCreditScore())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLocationPermission(String permission) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        if (!"ALLOW".equals(permission) && !"DENY".equals(permission)) {
            throw new BusinessException("定位权限状态不正确");
        }

        User user = User.builder()
                .id(userId)
                .locationPermission(permission)
                .build();

        userMapper.updateById(user);

        log.info("用户更新定位权限, userId: {}, permission: {}", userId, permission);
    }

    @Override
    public boolean verifyLocation(String longitude, String latitude) {
        // TODO: 实现校园围栏验证
        // 1. 从t_system_config读取校园围栏坐标
        // 2. 使用Haversine公式或高德地图API验证
        // 当前默认返回true
        log.info("校验定位, longitude: {}, latitude: {}", longitude, latitude);
        return true;
    }

    @Override
    public PageResult<Object> getMyProducts(Long userId, Integer pageNum, Integer pageSize) {
        // TODO: 调用商品服务获取用户发布的商品
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    @Override
    public PageResult<Object> getMyOrders(Long userId, String status, Integer pageNum, Integer pageSize) {
        // TODO: 调用交易服务获取用户订单
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    @Override
    public PageResult<Object> getMyFavorites(Long userId, Integer pageNum, Integer pageSize) {
        // TODO: 调用商品服务获取用户收藏
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    /**
     * 初始化用户积分
     */
    private void initUserPoints(Long userId) {
        UserPoints userPoints = UserPoints.builder()
                .userId(userId)
                .totalPoints(0)
                .availablePoints(0)
                .usedPoints(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userPointsMapper.insert(userPoints);
    }

    /**
     * 构建用户VO（脱敏）
     */
    private UserVO buildUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        // 处理需要脱敏的字段
        if (user.getStudentTeacherId() != null) {
            vo.setStudentTeacherId(DesensitizeUtil.studentTeacherId(user.getStudentTeacherId()));
        }
        if (user.getPhone() != null) {
            vo.setPhone(DesensitizeUtil.phone(user.getPhone()));
        }
        if (user.getCampusEmail() != null) {
            vo.setCampusEmail(DesensitizeUtil.email(user.getCampusEmail()));
        }
        return vo;
    }
}