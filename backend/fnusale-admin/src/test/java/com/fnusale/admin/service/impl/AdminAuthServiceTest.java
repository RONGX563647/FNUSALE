package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fnusale.admin.config.AdminContext;
import com.fnusale.admin.mapper.AdminMapper;
import com.fnusale.admin.mapper.AdminPermissionMapper;
import com.fnusale.common.dto.admin.AdminLoginDTO;
import com.fnusale.common.entity.Admin;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.admin.AdminInfoVO;
import com.fnusale.common.vo.admin.AdminLoginVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 管理员认证服务测试
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private AdminPermissionMapper adminPermissionMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AdminAuthServiceImpl adminAuthService;

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("admin");
        testAdmin.setPassword("$2a$10$encodedPassword");
        testAdmin.setNickname("超级管理员");
        testAdmin.setRole("SUPER_ADMIN");
        testAdmin.setStatus(1);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        AdminContext.clear();
    }

    @Test
    @DisplayName("登录成功")
    void testLoginSuccess() {
        AdminLoginDTO dto = AdminLoginDTO.builder()
                .username("admin")
                .password("admin123")
                .build();

        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testAdmin);
        when(passwordEncoder.matches("admin123", "$2a$10$encodedPassword")).thenReturn(true);
        when(adminPermissionMapper.selectPermissionCodesByAdminId(1L))
                .thenReturn(Arrays.asList("user:manage", "product:audit"));
        when(adminMapper.updateById(any(Admin.class))).thenReturn(1);

        AdminLoginVO result = adminAuthService.login(dto, "127.0.0.1");

        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertEquals(7200L, result.getExpiresIn());
        assertNotNull(result.getAdminInfo());
        assertEquals("admin", result.getAdminInfo().getUsername());
        assertEquals("超级管理员", result.getAdminInfo().getNickname());
        assertEquals(2, result.getAdminInfo().getPermissions().size());

        verify(redisTemplate.opsForValue()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("登录失败-用户不存在")
    void testLoginFailUserNotFound() {
        AdminLoginDTO dto = AdminLoginDTO.builder()
                .username("notexist")
                .password("password")
                .build();

        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            adminAuthService.login(dto, "127.0.0.1");
        });
    }

    @Test
    @DisplayName("登录失败-密码错误")
    void testLoginFailWrongPassword() {
        AdminLoginDTO dto = AdminLoginDTO.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testAdmin);
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            adminAuthService.login(dto, "127.0.0.1");
        });
    }

    @Test
    @DisplayName("登录失败-账号被禁用")
    void testLoginFailAccountDisabled() {
        testAdmin.setStatus(0);
        AdminLoginDTO dto = AdminLoginDTO.builder()
                .username("admin")
                .password("admin123")
                .build();

        when(adminMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testAdmin);

        assertThrows(BusinessException.class, () -> {
            adminAuthService.login(dto, "127.0.0.1");
        });
    }

    @Test
    @DisplayName("登出成功")
    void testLogoutSuccess() {
        AdminContext.setAdminId(1L);

        adminAuthService.logout();

        verify(redisTemplate).delete("admin:token:1");
    }

    @Test
    @DisplayName("获取管理员信息成功")
    void testGetAdminInfoSuccess() {
        AdminContext.setAdminId(1L);

        when(adminMapper.selectById(1L)).thenReturn(testAdmin);
        when(adminPermissionMapper.selectPermissionCodesByAdminId(1L))
                .thenReturn(Arrays.asList("user:manage", "product:audit"));

        AdminInfoVO result = adminAuthService.getAdminInfo();

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("超级管理员", result.getNickname());
        assertEquals(2, result.getPermissions().size());
    }

    @Test
    @DisplayName("获取管理员信息失败-未登录")
    void testGetAdminInfoFailNotLogin() {
        assertThrows(BusinessException.class, () -> {
            adminAuthService.getAdminInfo();
        });
    }
}
