package com.fnusale.user.service.impl;

import com.fnusale.common.constant.RedisKeyConstants;
import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.CaptchaDTO;
import com.fnusale.common.dto.user.CaptchaLoginDTO;
import com.fnusale.common.entity.User;
import com.fnusale.common.entity.UserPoints;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.enums.ResultCode;
import com.fnusale.common.event.UserRegisterEvent;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.DesensitizeUtil;
import com.fnusale.common.util.JwtUtil;
import com.fnusale.common.vo.user.LoginVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.user.mapper.UserMapper;
import com.fnusale.user.mapper.UserPointsMapper;
import com.fnusale.user.service.CaptchaService;
import com.fnusale.user.service.EmailService;
import com.fnusale.user.service.UserRegisterEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final UserPointsMapper userPointsMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedissonClient redissonClient;
    private final UserRegisterEventPublisher eventPublisher;
    private final EmailService emailService;

    /**
     * 验证码前缀
     */
    private static final String CAPTCHA_PREFIX = "captcha:";

    /**
     * 验证码有效期（分钟）
     */
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    /**
     * 发送间隔（秒）
     */
    private static final long SEND_INTERVAL_SECONDS = 60;

    /**
     * 注册分布式锁持有时间（秒）
     */
    private static final long LOCK_LEASE_TIME = 30;

    /**
     * 验证码最大尝试次数
     */
    private static final int MAX_CAPTCHA_ATTEMPTS = 5;

    /**
     * 验证码尝试锁定时间（分钟）
     */
    private static final long CAPTCHA_LOCK_MINUTES = 30;

    @Override
    public void sendCaptcha(CaptchaDTO dto) {
        String account = dto.getAccount();
        String type = dto.getType();

        // 检查发送频率
        String intervalKey = CAPTCHA_PREFIX + "interval:" + account;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(intervalKey))) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "发送频率过高，请60秒后再试");
        }

        // 生成6位验证码
        String captcha = generateCaptcha();

        // 存储验证码
        String captchaKey = CAPTCHA_PREFIX + type + ":" + account;
        redisTemplate.opsForValue().set(captchaKey, captcha, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置发送间隔限制
        redisTemplate.opsForValue().set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 根据 account 类型（手机号或邮箱）调用相应的发送服务
        if (account.contains("@")) {
            // 邮箱地址，发送邮件验证码
            emailService.sendVerificationCode(account, captcha);
            log.info("验证码邮件发送成功 - 邮箱：{}", DesensitizeUtil.email(account));
        } else {
            // 手机号，发送短信验证码（待集成短信服务）
            log.info("短信验证码已生成 - 手机号：{}, 验证码：{}", DesensitizeUtil.phone(account), captcha);
            // TODO: 集成短信服务（阿里云 SMS / 腾讯云 SMS）
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByCaptcha(CaptchaLoginDTO dto) {
        String account = dto.getAccount();
        String captcha = dto.getCaptcha();

        // 验证验证码
        if (!verifyCaptcha(account, captcha, "LOGIN")) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "验证码错误或已过期");
        }

        // 查询用户是否存在
        User user = userMapper.selectByPhoneOrEmail(account);
        boolean isNewUser = false;

        if (user == null) {
            // 用户不存在，自动注册（需要分布式锁防重）
            String lockKey = RedisKeyConstants.buildRegisterLockKey(account);
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean locked = lock.tryLock(0, LOCK_LEASE_TIME, TimeUnit.SECONDS);
                if (!locked) {
                    throw new BusinessException("请勿重复提交");
                }

                try {
                    // 双重检查，防止并发重复注册
                    user = userMapper.selectByPhoneOrEmail(account);
                    if (user == null) {
                        isNewUser = true;
                        user = createNewUser(account);
                        // 发布注册事件（异步处理后续操作）
                        publishRegisterEvent(user, account);
                        log.info("验证码登录自动创建用户: {}", account);
                    }
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

        // 删除已使用的验证码
        String captchaKey = CAPTCHA_PREFIX + "LOGIN:" + account;
        redisTemplate.delete(captchaKey);

        // 生成JWT Token
        String accessToken = JwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getIdentityType());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId());

        // 缓存Token到Redis
        String tokenKey = UserConstants.TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, accessToken, UserConstants.CAPTCHA_EXPIRATION * 24, TimeUnit.SECONDS);

        // 构建用户信息
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setPhone(user.getPhone());
        userVO.setCampusEmail(user.getCampusEmail());
        userVO.setIdentityType(user.getIdentityType());
        userVO.setAuthStatus(user.getAuthStatus());
        userVO.setCreditScore(user.getCreditScore());

        // 返回登录信息
        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(JwtUtil.getAccessTokenExpirationSeconds())
                .userInfo(userVO)
                .isNewUser(isNewUser)
                .build();
    }

    @Override
    public boolean verifyCaptcha(String account, String captcha, String type) {
        // 检查是否被锁定
        String lockKey = CAPTCHA_PREFIX + "lock:" + type + ":" + account;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BusinessException("验证码验证次数过多，请" + CAPTCHA_LOCK_MINUTES + "分钟后再试");
        }

        String captchaKey = CAPTCHA_PREFIX + type + ":" + account;
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);

        if (storedCaptcha == null) {
            return false;
        }

        // 验证成功
        if (storedCaptcha.equals(captcha)) {
            // 清除尝试次数
            String attemptsKey = CAPTCHA_PREFIX + "attempts:" + type + ":" + account;
            redisTemplate.delete(attemptsKey);
            return true;
        }

        // 验证失败，增加尝试次数
        String attemptsKey = CAPTCHA_PREFIX + "attempts:" + type + ":" + account;
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        if (attempts != null && attempts == 1) {
            // 首次失败，设置过期时间与验证码相同
            redisTemplate.expire(attemptsKey, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        // 超过最大尝试次数，锁定
        if (attempts != null && attempts >= MAX_CAPTCHA_ATTEMPTS) {
            redisTemplate.opsForValue().set(lockKey, "1", CAPTCHA_LOCK_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(captchaKey);  // 删除验证码
            redisTemplate.delete(attemptsKey); // 清除尝试次数
            throw new BusinessException("验证码验证次数过多，请" + CAPTCHA_LOCK_MINUTES + "分钟后再试");
        }

        return false;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCaptcha() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 创建新用户（验证码登录自动注册）
     */
    private User createNewUser(String account) {
        User user = User.builder()
                .username(generateDefaultUsername())
                .password(passwordEncoder.encode(generateRandomPassword())) // 随机密码并加密
                .identityType("STUDENT")
                .authStatus(AuthStatus.UNAUTH.getCode())
                .creditScore(UserConstants.DEFAULT_CREDIT_SCORE)
                .locationPermission("DENY")
                .build();

        // 判断是手机号还是邮箱
        if (account.contains("@")) {
            user.setCampusEmail(account);
        } else {
            user.setPhone(account);
        }

        userMapper.insert(user);

        // 初始化积分
        initUserPoints(user.getId());

        return user;
    }

    /**
     * 生成随机密码（验证码登录用户初始密码）
     */
    private String generateRandomPassword() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
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
     * 生成默认用户名
     */
    private String generateDefaultUsername() {
        return "用户" + System.currentTimeMillis() % 100000000;
    }

    /**
     * 发布用户注册事件
     */
    private void publishRegisterEvent(User user, String account) {
        String registerSource = account.contains("@") ? "EMAIL" : "PHONE";
        UserRegisterEvent event = UserRegisterEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getCampusEmail())
                .identityType(user.getIdentityType())
                .registerSource("CAPTCHA_LOGIN_" + registerSource)
                .registerTime(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();

        eventPublisher.publishRegisterEvent(event);
    }
}