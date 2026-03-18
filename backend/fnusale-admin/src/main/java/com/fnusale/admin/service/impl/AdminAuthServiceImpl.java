package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fnusale.admin.config.AdminContext;
import com.fnusale.admin.mapper.AdminMapper;
import com.fnusale.admin.mapper.AdminPermissionMapper;
import com.fnusale.admin.service.AdminAuthService;
import com.fnusale.common.dto.admin.AdminLoginDTO;
import com.fnusale.common.entity.Admin;
import com.fnusale.common.enums.ResultCode;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.JwtUtil;
import com.fnusale.common.vo.admin.AdminInfoVO;
import com.fnusale.common.vo.admin.AdminLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 管理员认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminMapper adminMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final String ADMIN_TOKEN_PREFIX = "admin:token:";
    private static final long TOKEN_EXPIRE_HOURS = 2;

    @Override
    @Transactional
    public AdminLoginVO login(AdminLoginDTO dto, String ip) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, dto.getUsername())
        );

        if (admin == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        List<String> permissions = adminPermissionMapper.selectPermissionCodesByAdminId(admin.getId());

        String accessToken = JwtUtil.generateAccessToken(admin.getId(), admin.getUsername(), admin.getRole());
        String refreshToken = JwtUtil.generateRefreshToken(admin.getId());

        redisTemplate.opsForValue().set(
                ADMIN_TOKEN_PREFIX + admin.getId(),
                accessToken,
                TOKEN_EXPIRE_HOURS,
                TimeUnit.HOURS
        );

        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        adminMapper.updateById(admin);

        AdminInfoVO adminInfo = AdminInfoVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .avatar(admin.getAvatarUrl())
                .role(admin.getRole())
                .permissions(permissions)
                .createTime(admin.getCreateTime())
                .build();

        return AdminLoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(JwtUtil.getAccessTokenExpirationSeconds())
                .adminInfo(adminInfo)
                .build();
    }

    @Override
    public void logout() {
        Long adminId = AdminContext.getAdminId();
        if (adminId != null) {
            redisTemplate.delete(ADMIN_TOKEN_PREFIX + adminId);
        }
    }

    @Override
    public AdminInfoVO getAdminInfo() {
        Long adminId = AdminContext.getAdminId();
        if (adminId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }

        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "管理员不存在");
        }

        List<String> permissions = adminPermissionMapper.selectPermissionCodesByAdminId(admin.getId());

        return AdminInfoVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .avatar(admin.getAvatarUrl())
                .role(admin.getRole())
                .permissions(permissions)
                .createTime(admin.getCreateTime())
                .build();
    }

    @Override
    public AdminLoginVO refreshToken(String refreshToken) {
        if (!JwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }

        String tokenType = JwtUtil.getTokenType(refreshToken);
        if (!JwtUtil.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "不是有效的刷新令牌");
        }

        Long adminId = JwtUtil.getUserId(refreshToken);
        if (adminId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新令牌无效");
        }

        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "管理员不存在");
        }

        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        List<String> permissions = adminPermissionMapper.selectPermissionCodesByAdminId(admin.getId());

        String newAccessToken = JwtUtil.generateAccessToken(admin.getId(), admin.getUsername(), admin.getRole());
        String newRefreshToken = JwtUtil.generateRefreshToken(admin.getId());

        redisTemplate.opsForValue().set(
                ADMIN_TOKEN_PREFIX + admin.getId(),
                newAccessToken,
                TOKEN_EXPIRE_HOURS,
                TimeUnit.HOURS
        );

        AdminInfoVO adminInfo = AdminInfoVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .avatar(admin.getAvatarUrl())
                .role(admin.getRole())
                .permissions(permissions)
                .createTime(admin.getCreateTime())
                .build();

        return AdminLoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(JwtUtil.getAccessTokenExpirationSeconds())
                .adminInfo(adminInfo)
                .build();
    }
}
