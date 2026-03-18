package com.fnusale.user.service.impl;

import com.fnusale.common.dto.user.CaptchaDTO;
import com.fnusale.common.dto.user.CaptchaLoginDTO;
import com.fnusale.common.entity.User;
import com.fnusale.common.enums.ResultCode;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.LoginVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.user.mapper.UserMapper;
import com.fnusale.user.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
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

        // TODO: 根据account类型（手机号或邮箱）调用相应的发送服务
        // 这里暂时只记录日志，实际项目中需要集成短信/邮件服务
        log.info("验证码已发送 - account: {}, type: {}, captcha: {}", account, type, captcha);
    }

    @Override
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
            // 用户不存在，自动注册
            isNewUser = true;
            user = createNewUser(account);
            log.info("验证码登录自动创建用户: {}", account);
        }

        // 删除已使用的验证码
        String captchaKey = CAPTCHA_PREFIX + "LOGIN:" + account;
        redisTemplate.delete(captchaKey);

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
        // TODO: 实际生成JWT token
        return LoginVO.builder()
                .accessToken("generated_token")
                .refreshToken("generated_refresh_token")
                .tokenType("Bearer")
                .expiresIn(7200L)
                .userInfo(userVO)
                .isNewUser(isNewUser)
                .build();
    }

    @Override
    public boolean verifyCaptcha(String account, String captcha, String type) {
        String captchaKey = CAPTCHA_PREFIX + type + ":" + account;
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);

        if (storedCaptcha == null) {
            return false;
        }

        return storedCaptcha.equals(captcha);
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
        User user = new User();
        user.setUsername(generateDefaultUsername());
        user.setPassword(""); // 验证码登录用户无密码
        user.setCreditScore(100);
        user.setAuthStatus("UNAUTH");

        // 判断是手机号还是邮箱
        if (account.contains("@")) {
            user.setCampusEmail(account);
        } else {
            user.setPhone(account);
        }

        userMapper.insert(user);

        return user;
    }

    /**
     * 生成默认用户名
     */
    private String generateDefaultUsername() {
        return "用户" + System.currentTimeMillis() % 100000000;
    }
}