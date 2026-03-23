package com.fnusale.user.service.impl;

import com.fnusale.common.config.CampusFenceConfig;
import com.fnusale.common.constant.RedisKeyConstants;
import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.UserAuthDTO;
import com.fnusale.common.dto.user.UserLoginDTO;
import com.fnusale.common.dto.user.UserRegisterDTO;
import com.fnusale.common.dto.user.UserUpdateDTO;
import com.fnusale.common.entity.User;
import com.fnusale.common.entity.UserPoints;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.event.UserRegisterEvent;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.common.PageResult;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.fnusale.common.util.GeoFenceUtil;
import com.fnusale.common.util.JwtUtil;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.user.LoginVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.user.mapper.UserMapper;
import com.fnusale.user.mapper.UserPointsMapper;
import com.fnusale.user.service.LoginAttemptService;
import com.fnusale.user.service.OssService;
import com.fnusale.user.service.UserRegisterEventPublisher;
import com.fnusale.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserPointsMapper userPointsMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedissonClient redissonClient;
    private final UserRegisterEventPublisher eventPublisher;
    private final CampusFenceConfig campusFenceConfig;
    private final OssService ossService;
    private final LoginAttemptService loginAttemptService;

    private static final long LOCK_WAIT_TIME = 0;
    private static final long LOCK_LEASE_TIME = 30;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerByPhone(UserRegisterDTO dto) {
        validatePhone(dto.getPhone());
        validateUsername(dto.getUsername());
        validatePassword(dto.getPassword());

        executeWithLock(
            RedisKeyConstants.buildRegisterLockKey(dto.getPhone()),
            "请勿重复提交注册请求",
            () -> {
                if (userMapper.countByPhone(dto.getPhone()) > 0) {
                    throw new BusinessException("该手机号已被注册");
                }
                User user = buildUserForRegistration(dto);
                user.setPhone(dto.getPhone());
                userMapper.insert(user);
                initUserPoints(user.getId());
                publishRegisterEvent(user, "PHONE");
                log.info("用户注册成功，userId: {}, phone: {}", user.getId(), DesensitizedUtil.mobilePhone(dto.getPhone()));
            }
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerByEmail(UserRegisterDTO dto) {
        validateEmail(dto.getEmail());
        validateUsername(dto.getUsername());
        validatePassword(dto.getPassword());

        executeWithLock(
            RedisKeyConstants.buildRegisterLockKey(dto.getEmail()),
            "请勿重复提交注册请求",
            () -> {
                if (userMapper.countByEmail(dto.getEmail()) > 0) {
                    throw new BusinessException("该邮箱已被注册");
                }
                User user = buildUserForRegistration(dto);
                user.setCampusEmail(dto.getEmail());
                userMapper.insert(user);
                initUserPoints(user.getId());
                publishRegisterEvent(user, "EMAIL");
                log.info("用户注册成功，userId: {}, email: {}", user.getId(), DesensitizedUtil.email(dto.getEmail()));
            }
        );
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        String account = "PHONE".equals(dto.getLoginType()) ? dto.getPhone() : dto.getEmail();

        // 检查是否被锁定
        if (loginAttemptService.isLocked(account)) {
            Long lockTime = UserConstants.LOGIN_LOCK_TIME_MINUTES;
            throw new BusinessException("登录失败次数过多，请" + lockTime + "分钟后再试");
        }

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
            loginAttemptService.recordLoginAttempt(account);
            Long remaining = loginAttemptService.getRemainingAttempts(account);
            throw new BusinessException("用户不存在，剩余尝试次数：" + remaining);
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.recordLoginAttempt(account);
            Long remaining = loginAttemptService.getRemainingAttempts(account);
            throw new BusinessException("密码错误，剩余尝试次数：" + remaining);
        }

        // 登录成功，清除失败记录
        loginAttemptService.clearLoginAttempts(account);

        String accessToken = JwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getIdentityType());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId());

        String tokenKey = UserConstants.TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, accessToken, UserConstants.CAPTCHA_EXPIRATION * 24, TimeUnit.SECONDS);

        LoginVO loginVO = LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(JwtUtil.getAccessTokenExpirationSeconds())
                .userInfo(buildUserVO(user))
                .build();

        log.info("用户登录成功，userId: {}", user.getId());
        return loginVO;
    }

    @Override
    public void logout() {
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) {
            String tokenKey = UserConstants.TOKEN_KEY_PREFIX + userId;
            redisTemplate.delete(tokenKey);
            log.info("用户登出成功，userId: {}", userId);
        }
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
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

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String newAccessToken = JwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getIdentityType());
        String newRefreshToken = JwtUtil.generateRefreshToken(user.getId());

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
        Long userId = UserContext.getUserIdOrThrow();
        return getUserVOById(userId);
    }

    /**
     * 根据用户ID获取用户VO（带缓存）
     */
    @Cacheable(value = "userInfo", key = "#userId", unless = "#result == null")
    public UserVO getUserVOById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserUpdateDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();

        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException("用户不存在");
        }

        User user = new User();
        user.setId(userId);

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            validateUsername(dto.getUsername());
            user.setUsername(dto.getUsername());
        }

        if (dto.getAvatarUrl() != null) {
            if (oldUser.getAvatarUrl() != null && !oldUser.getAvatarUrl().equals(dto.getAvatarUrl())) {
                ossService.deleteFile(oldUser.getAvatarUrl());
            }
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        if (dto.getBirthday() != null) {
            user.setBirthday(dto.getBirthday());
        }

        if (dto.getLocationPermission() != null) {
            user.setLocationPermission(dto.getLocationPermission());
        }

        userMapper.updateById(user);

        // 清除缓存
        evictUserCache(userId);

        log.info("用户信息更新成功，userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPassword, String newPassword) {
        Long userId = UserContext.getUserIdOrThrow();

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        validatePassword(newPassword);

        User updateUser = User.builder()
                .id(userId)
                .password(passwordEncoder.encode(newPassword))
                .build();

        userMapper.updateById(updateUser);

        // 清除缓存
        evictUserCache(userId);

        log.info("用户密码修改成功，userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuth(UserAuthDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();

        // 使用学号/工号作为锁，防止并发占用
        String lockKey = "auth:studentTeacherId:" + dto.getStudentTeacherId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            try {
                User user = userMapper.selectById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }

                if (AuthStatus.AUTH_SUCCESS.getCode().equals(user.getAuthStatus())) {
                    throw new BusinessException("您已完成身份认证");
                }
                if (AuthStatus.UNDER_REVIEW.getCode().equals(user.getAuthStatus())) {
                    throw new BusinessException("认证申请审核中，请耐心等待");
                }

                if (userMapper.countByStudentTeacherId(dto.getStudentTeacherId()) > 0) {
                    User existUser = userMapper.selectByStudentTeacherId(dto.getStudentTeacherId());
                    if (existUser != null && !existUser.getId().equals(userId)) {
                        throw new BusinessException("该学号/工号已被其他用户使用");
                    }
                }

                User updateUser = User.builder()
                        .id(userId)
                        .studentTeacherId(dto.getStudentTeacherId())
                        .identityType(dto.getIdentityType())
                        .authImageUrl(dto.getAuthImageUrl())
                        .authStatus(AuthStatus.UNDER_REVIEW.getCode())
                        .build();

                userMapper.updateById(updateUser);

                // 清除缓存
                evictUserCache(userId);

                log.info("用户提交身份认证，userId: {}, studentTeacherId: {}", userId,
                        maskStudentTeacherId(dto.getStudentTeacherId()));
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("请求被中断，请重试");
        }
    }

    @Override
    public UserVO getAuthStatus() {
        Long userId = UserContext.getUserIdOrThrow();

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return buildUserVO(user);
    }

    @Override
    public UserVO getUserById(Long userId) {
        return getUserVOById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLocationPermission(String permission) {
        Long userId = UserContext.getUserIdOrThrow();

        validateLocationPermission(permission);

        User user = User.builder()
                .id(userId)
                .locationPermission(permission)
                .build();

        userMapper.updateById(user);

        // 清除缓存
        evictUserCache(userId);

        log.info("用户更新定位权限，userId: {}, permission: {}", userId, permission);
    }

    @Override
    public boolean verifyLocation(String longitude, String latitude) {
        // 检查配置是否启用
        if (!Boolean.TRUE.equals(campusFenceConfig.getEnabled())) {
            log.warn("校园围栏验证未启用，默认通过");
            return true;
        }

        try {
            double lon = Double.parseDouble(longitude);
            double lat = Double.parseDouble(latitude);

            // 1. 优先使用多边形围栏验证
            List<GeoFenceUtil.Point> fencePoints = campusFenceConfig.getFencePoints();
            if (fencePoints != null && !fencePoints.isEmpty()) {
                boolean inPolygon = GeoFenceUtil.isPointInPolygon(lon, lat, fencePoints);
                log.info("校园围栏验证结果：经度={}, 纬度={}, 是否在多边形内={}", lon, lat, inPolygon);
                return inPolygon;
            }

            // 2. 备用圆形围栏验证
            GeoFenceUtil.Point center = campusFenceConfig.getCenterPoint();
            Double radius = campusFenceConfig.getRadius();
            if (center != null && radius != null) {
                boolean inCircle = GeoFenceUtil.isPointInCircle(lon, lat, 
                    center.getLongitude(), center.getLatitude(), radius);
                log.info("校园圆形围栏验证结果：经度={}, 纬度={}, 是否在圆形内={}", lon, lat, inCircle);
                return inCircle;
            }

            log.warn("未配置校园围栏坐标，默认通过验证");
            return true;
        } catch (NumberFormatException e) {
            log.error("经纬度格式错误：longitude={}, latitude={}", longitude, latitude);
            throw new BusinessException("经纬度格式不正确");
        } catch (Exception e) {
            log.error("校园围栏验证失败", e);
            throw new BusinessException("定位验证失败，请重试");
        }
    }

    @Override
    public PageResult<Object> getMyProducts(Long userId, Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    @Override
    public PageResult<Object> getMyOrders(Long userId, String status, Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    @Override
    public PageResult<Object> getMyFavorites(Long userId, Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    @Override
    public Map<Long, UserVO> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getId, this::buildUserVO, (v1, v2) -> v1));
    }

    @Override
    public Map<Long, String> getAuthStatusByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getId, User::getAuthStatus, (v1, v2) -> v1));
    }

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

    private void publishRegisterEvent(User user, String registerSource) {
        UserRegisterEvent event = UserRegisterEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getCampusEmail())
                .identityType(user.getIdentityType())
                .registerSource(registerSource)
                .registerTime(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();

        eventPublisher.publishRegisterEvent(event);
    }

    private UserVO buildUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getStudentTeacherId() != null) {
            vo.setStudentTeacherId(maskStudentTeacherId(user.getStudentTeacherId()));
        }
        if (user.getPhone() != null) {
            vo.setPhone(DesensitizedUtil.mobilePhone(user.getPhone()));
        }
        if (user.getCampusEmail() != null) {
            vo.setCampusEmail(DesensitizedUtil.email(user.getCampusEmail()));
        }
        return vo;
    }

    /**
     * 学号/工号脱敏
     * 2021001001 -> ****0001
     */
    private String maskStudentTeacherId(String id) {
        if (id == null || id.length() <= 4) {
            return id;
        }
        return StrUtil.hide(id, 0, id.length() - 4);
    }

    /**
     * 清除用户缓存
     */
    @CacheEvict(value = "userInfo", key = "#userId")
    public void evictUserCache(Long userId) {
        log.debug("清除用户缓存，userId: {}", userId);
    }

    /**
     * 分布式锁执行模板
     */
    private void executeWithLock(String lockKey, String lockFailMessage, Runnable task) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(lockFailMessage);
            }
            try {
                task.run();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("请求被中断，请重试");
        }
    }

    /**
     * 构建注册用户实体（公共逻辑）
     */
    private User buildUserForRegistration(UserRegisterDTO dto) {
        return User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .identityType(dto.getIdentityType() != null ? dto.getIdentityType() : UserConstants.IDENTITY_TYPE_STUDENT)
                .authStatus(AuthStatus.UNAUTH.getCode())
                .creditScore(UserConstants.DEFAULT_CREDIT_SCORE)
                .locationPermission(UserConstants.LOCATION_PERMISSION_DENY)
                .build();
    }

    // ========== 验证方法（使用 Hutool Validator）==========

    private void validatePhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            throw new BusinessException("手机号不能为空");
        }
        if (!Validator.isMobile(phone)) {
            throw new BusinessException("手机号格式不正确");
        }
    }

    private void validateEmail(String email) {
        if (StrUtil.isBlank(email)) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!Validator.isEmail(email)) {
            throw new BusinessException("邮箱格式不正确");
        }
    }

    private void validateUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        int length = username.length();
        if (length < UserConstants.USERNAME_MIN_LENGTH || length > UserConstants.USERNAME_MAX_LENGTH) {
            throw new BusinessException("用户名长度应在" + UserConstants.USERNAME_MIN_LENGTH + "-" +
                UserConstants.USERNAME_MAX_LENGTH + "个字符之间");
        }
    }

    private void validatePassword(String password) {
        if (StrUtil.isBlank(password)) {
            return;
        }
        int length = password.length();
        if (length < UserConstants.PASSWORD_MIN_LENGTH || length > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new BusinessException("密码长度应在" + UserConstants.PASSWORD_MIN_LENGTH + "-" +
                UserConstants.PASSWORD_MAX_LENGTH + "位之间");
        }
    }

    private void validateLocationPermission(String permission) {
        if (!"ALLOW".equals(permission) && !"DENY".equals(permission)) {
            throw new BusinessException("定位权限状态不正确");
        }
    }
}
