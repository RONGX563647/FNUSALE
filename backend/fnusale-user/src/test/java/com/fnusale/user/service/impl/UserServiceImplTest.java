package com.fnusale.user.service.impl;

import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.UserAuthDTO;
import com.fnusale.common.dto.user.UserLoginDTO;
import com.fnusale.common.dto.user.UserRegisterDTO;
import com.fnusale.common.dto.user.UserUpdateDTO;
import com.fnusale.common.entity.User;
import com.fnusale.common.entity.UserPoints;
import com.fnusale.common.enums.AuthStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.LoginVO;
import com.fnusale.common.vo.user.UserVO;
import com.fnusale.user.mapper.UserMapper;
import com.fnusale.user.mapper.UserPointsMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserPointsMapper userPointsMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPhone("13800138000");
        testUser.setCampusEmail("test@campus.edu.cn");
        testUser.setPassword("password123");
        testUser.setIdentityType("STUDENT");
        testUser.setAuthStatus(AuthStatus.UNAUTH.getCode());
        testUser.setCreditScore(UserConstants.DEFAULT_CREDIT_SCORE);
        testUser.setLocationPermission("DENY");
        testUser.setCreateTime(LocalDateTime.now());

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 配置passwordEncoder的mock行为
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @AfterEach
    void tearDown() {
        UserServiceImpl.clearCurrentUserId();
    }

    @Nested
    @DisplayName("手机号注册测试")
    class RegisterByPhoneTests {

        @Test
        @DisplayName("正常注册_成功")
        void registerByPhone_success() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setPhone("13900139000");
            dto.setPassword("password123");
            dto.setIdentityType("STUDENT");

            when(userMapper.countByPhone("13900139000")).thenReturn(0);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(2L);
                return 1;
            });

            assertDoesNotThrow(() -> userService.registerByPhone(dto));
            verify(userMapper).insert(any(User.class));
            verify(userPointsMapper).insert(any(UserPoints.class));
        }

        @Test
        @DisplayName("手机号为空_抛出异常")
        void registerByPhone_emptyPhone_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setPassword("password123");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByPhone(dto));
            assertEquals("手机号不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("手机号格式错误_抛出异常")
        void registerByPhone_invalidPhoneFormat_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setPhone("1234567890");
            dto.setPassword("password123");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByPhone(dto));
            assertEquals("手机号格式不正确", exception.getMessage());
        }

        @Test
        @DisplayName("手机号已注册_抛出异常")
        void registerByPhone_phoneExists_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setPhone("13800138000");
            dto.setPassword("password123");

            when(userMapper.countByPhone("13800138000")).thenReturn(1);

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByPhone(dto));
            assertEquals("该手机号已被注册", exception.getMessage());
        }

        @Test
        @DisplayName("用户名过短_抛出异常")
        void registerByPhone_usernameTooShort_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("a");
            dto.setPhone("13900139000");
            dto.setPassword("password123");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByPhone(dto));
            assertEquals("用户名长度应在2-20个字符之间", exception.getMessage());
        }

        @Test
        @DisplayName("用户名过长_抛出异常")
        void registerByPhone_usernameTooLong_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("a".repeat(25));
            dto.setPhone("13900139000");
            dto.setPassword("password123");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByPhone(dto));
            assertEquals("用户名长度应在2-20个字符之间", exception.getMessage());
        }

        @Test
        @DisplayName("密码过短_抛出异常")
        void registerByPhone_passwordTooShort_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setPhone("13900139000");
            dto.setPassword("12345");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByPhone(dto));
            assertEquals("密码长度应在6-20位之间", exception.getMessage());
        }

        @Test
        @DisplayName("密码过长_抛出异常")
        void registerByPhone_passwordTooLong_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setPhone("13900139000");
            dto.setPassword("123456789012345678901");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByPhone(dto));
            assertEquals("密码长度应在6-20位之间", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("邮箱注册测试")
    class RegisterByEmailTests {

        @Test
        @DisplayName("正常注册_成功")
        void registerByEmail_success() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setEmail("newuser@campus.edu.cn");
            dto.setPassword("password123");
            dto.setIdentityType("TEACHER");

            when(userMapper.countByEmail("newuser@campus.edu.cn")).thenReturn(0);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(3L);
                return 1;
            });

            assertDoesNotThrow(() -> userService.registerByEmail(dto));
            verify(userMapper).insert(any(User.class));
            verify(userPointsMapper).insert(any(UserPoints.class));
        }

        @Test
        @DisplayName("邮箱为空_抛出异常")
        void registerByEmail_emptyEmail_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setPassword("password123");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByEmail(dto));
            assertEquals("邮箱不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("邮箱格式错误_抛出异常")
        void registerByEmail_invalidEmailFormat_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setEmail("invalid-email");
            dto.setPassword("password123");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByEmail(dto));
            assertEquals("邮箱格式不正确", exception.getMessage());
        }

        @Test
        @DisplayName("邮箱已注册_抛出异常")
        void registerByEmail_emailExists_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setEmail("test@campus.edu.cn");
            dto.setPassword("password123");

            when(userMapper.countByEmail("test@campus.edu.cn")).thenReturn(1);

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByEmail(dto));
            assertEquals("该邮箱已被注册", exception.getMessage());
        }

        @Test
        @DisplayName("用户名过短_抛出异常")
        void registerByEmail_usernameTooShort_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("a");
            dto.setEmail("newuser@campus.edu.cn");
            dto.setPassword("password123");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByEmail(dto));
            assertEquals("用户名长度应在2-20个字符之间", exception.getMessage());
        }

        @Test
        @DisplayName("密码过短_抛出异常")
        void registerByEmail_passwordTooShort_throwsException() {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setUsername("newuser");
            dto.setEmail("newuser@campus.edu.cn");
            dto.setPassword("12345");

            BusinessException exception = assertThrows(BusinessException.class, () -> userService.registerByEmail(dto));
            assertEquals("密码长度应在6-20位之间", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("登录测试")
    class LoginTests {

        @Test
        @DisplayName("手机号登录_成功")
        void login_phone_success() {
            UserLoginDTO dto = new UserLoginDTO();
            dto.setLoginType("PHONE");
            dto.setPhone("13800138000");
            dto.setPassword("password123");

            when(userMapper.selectByPhone("13800138000")).thenReturn(testUser);

            LoginVO result = userService.login(dto);

            assertNotNull(result);
            assertNotNull(result.getAccessToken());
            assertNotNull(result.getRefreshToken());
            assertEquals("Bearer", result.getTokenType());
            assertNotNull(result.getUserInfo());
        }

        @Test
        @DisplayName("邮箱登录_成功")
        void login_email_success() {
            UserLoginDTO dto = new UserLoginDTO();
            dto.setLoginType("EMAIL");
            dto.setEmail("test@campus.edu.cn");
            dto.setPassword("password123");

            when(userMapper.selectByEmail("test@campus.edu.cn")).thenReturn(testUser);

            LoginVO result = userService.login(dto);

            assertNotNull(result);
            assertNotNull(result.getAccessToken());
        }

        @Test
        @DisplayName("用户不存在_抛出异常")
        void login_userNotFound_throwsException() {
            UserLoginDTO dto = new UserLoginDTO();
            dto.setLoginType("PHONE");
            dto.setPhone("19900199000");
            dto.setPassword("password123");

            when(userMapper.selectByPhone("19900199000")).thenReturn(null);

            assertThrows(BusinessException.class, () -> userService.login(dto));
        }

        @Test
        @DisplayName("密码错误_抛出异常")
        void login_wrongPassword_throwsException() {
            UserLoginDTO dto = new UserLoginDTO();
            dto.setLoginType("PHONE");
            dto.setPhone("13800138000");
            dto.setPassword("wrongpassword");

            when(userMapper.selectByPhone("13800138000")).thenReturn(testUser);
            // 配置密码验证失败
            when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

            assertThrows(BusinessException.class, () -> userService.login(dto));
        }

        @Test
        @DisplayName("登录类型错误_抛出异常")
        void login_invalidLoginType_throwsException() {
            UserLoginDTO dto = new UserLoginDTO();
            dto.setLoginType("INVALID");
            dto.setPassword("password123");

            assertThrows(BusinessException.class, () -> userService.login(dto));
        }
    }

    @Nested
    @DisplayName("获取当前用户信息测试")
    class GetCurrentUserInfoTests {

        @Test
        @DisplayName("已登录_成功获取")
        void getCurrentUserInfo_success() {
            UserServiceImpl.setCurrentUserId(1L);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            UserVO result = userService.getCurrentUserInfo();

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("testuser", result.getUsername());
            // 验证脱敏
            assertNotNull(result.getPhone());
            assertTrue(result.getPhone().contains("*"));
        }

        @Test
        @DisplayName("未登录_抛出异常")
        void getCurrentUserInfo_notLogin_throwsException() {
            assertThrows(BusinessException.class, () -> userService.getCurrentUserInfo());
        }

        @Test
        @DisplayName("用户不存在_抛出异常")
        void getCurrentUserInfo_userNotFound_throwsException() {
            UserServiceImpl.setCurrentUserId(999L);
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userService.getCurrentUserInfo());
        }
    }

    @Nested
    @DisplayName("更新用户信息测试")
    class UpdateUserInfoTests {

        @Test
        @DisplayName("更新用户名_成功")
        void updateUserInfo_username_success() {
            UserServiceImpl.setCurrentUserId(1L);
            UserUpdateDTO dto = new UserUpdateDTO();
            dto.setUsername("newusername");

            when(userMapper.updateById(any(User.class))).thenReturn(1);

            assertDoesNotThrow(() -> userService.updateUserInfo(dto));
        }

        @Test
        @DisplayName("用户名过短_抛出异常")
        void updateUserInfo_usernameTooShort_throwsException() {
            UserServiceImpl.setCurrentUserId(1L);
            UserUpdateDTO dto = new UserUpdateDTO();
            dto.setUsername("a"); // 少于最小长度

            assertThrows(BusinessException.class, () -> userService.updateUserInfo(dto));
        }

        @Test
        @DisplayName("用户名过长_抛出异常")
        void updateUserInfo_usernameTooLong_throwsException() {
            UserServiceImpl.setCurrentUserId(1L);
            UserUpdateDTO dto = new UserUpdateDTO();
            dto.setUsername("a".repeat(30)); // 超过最大长度

            assertThrows(BusinessException.class, () -> userService.updateUserInfo(dto));
        }

        @Test
        @DisplayName("未登录_抛出异常")
        void updateUserInfo_notLogin_throwsException() {
            UserUpdateDTO dto = new UserUpdateDTO();
            dto.setUsername("newusername");

            assertThrows(BusinessException.class, () -> userService.updateUserInfo(dto));
        }
    }

    @Nested
    @DisplayName("修改密码测试")
    class UpdatePasswordTests {

        @Test
        @DisplayName("修改密码_成功")
        void updatePassword_success() {
            UserServiceImpl.setCurrentUserId(1L);
            String oldPassword = "password123";
            String newPassword = "newpassword123";

            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            assertDoesNotThrow(() -> userService.updatePassword(oldPassword, newPassword));
        }

        @Test
        @DisplayName("原密码错误_抛出异常")
        void updatePassword_wrongOldPassword_throwsException() {
            UserServiceImpl.setCurrentUserId(1L);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            // 配置密码验证失败
            when(passwordEncoder.matches("wrongold", testUser.getPassword())).thenReturn(false);

            assertThrows(BusinessException.class, () -> userService.updatePassword("wrongold", "newpassword123"));
        }

        @Test
        @DisplayName("新密码过短_抛出异常")
        void updatePassword_newPasswordTooShort_throwsException() {
            UserServiceImpl.setCurrentUserId(1L);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            assertThrows(BusinessException.class, () -> userService.updatePassword("password123", "123"));
        }
    }

    @Nested
    @DisplayName("校园身份认证测试")
    class SubmitAuthTests {

        @Test
        @DisplayName("提交认证_成功")
        void submitAuth_success() {
            UserServiceImpl.setCurrentUserId(1L);
            UserAuthDTO dto = new UserAuthDTO();
            dto.setStudentTeacherId("2024001");
            dto.setIdentityType("STUDENT");
            dto.setAuthImageUrl("http://example.com/auth.jpg");

            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.countByStudentTeacherId("2024001")).thenReturn(0);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            assertDoesNotThrow(() -> userService.submitAuth(dto));
        }

        @Test
        @DisplayName("已认证_抛出异常")
        void submitAuth_alreadyAuth_throwsException() {
            UserServiceImpl.setCurrentUserId(1L);
            testUser.setAuthStatus(AuthStatus.AUTH_SUCCESS.getCode());

            UserAuthDTO dto = new UserAuthDTO();
            dto.setStudentTeacherId("2024001");
            dto.setIdentityType("STUDENT");
            dto.setAuthImageUrl("http://example.com/auth.jpg");

            when(userMapper.selectById(1L)).thenReturn(testUser);

            assertThrows(BusinessException.class, () -> userService.submitAuth(dto));
        }

        @Test
        @DisplayName("审核中_抛出异常")
        void submitAuth_underReview_throwsException() {
            UserServiceImpl.setCurrentUserId(1L);
            testUser.setAuthStatus(AuthStatus.UNDER_REVIEW.getCode());

            UserAuthDTO dto = new UserAuthDTO();
            dto.setStudentTeacherId("2024001");
            dto.setIdentityType("STUDENT");
            dto.setAuthImageUrl("http://example.com/auth.jpg");

            when(userMapper.selectById(1L)).thenReturn(testUser);

            assertThrows(BusinessException.class, () -> userService.submitAuth(dto));
        }
    }

    @Nested
    @DisplayName("登出测试")
    class LogoutTests {

        @Test
        @DisplayName("登出_成功")
        void logout_success() {
            UserServiceImpl.setCurrentUserId(1L);

            assertDoesNotThrow(() -> userService.logout());
            verify(redisTemplate).delete(anyString());
        }

        @Test
        @DisplayName("未登录_不执行操作")
        void logout_notLogin_noAction() {
            assertDoesNotThrow(() -> userService.logout());
            verify(redisTemplate, never()).delete(anyString());
        }
    }

    @Nested
    @DisplayName("获取用户详情测试")
    class GetUserByIdTests {

        @Test
        @DisplayName("获取用户详情_成功")
        void getUserById_success() {
            when(userMapper.selectById(1L)).thenReturn(testUser);

            UserVO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("testuser", result.getUsername());
        }

        @Test
        @DisplayName("用户不存在_抛出异常")
        void getUserById_notFound_throwsException() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userService.getUserById(999L));
        }
    }

    @Nested
    @DisplayName("更新定位权限测试")
    class UpdateLocationPermissionTests {

        @Test
        @DisplayName("允许定位_成功")
        void updateLocationPermission_allow_success() {
            UserServiceImpl.setCurrentUserId(1L);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            assertDoesNotThrow(() -> userService.updateLocationPermission("ALLOW"));
        }

        @Test
        @DisplayName("拒绝定位_成功")
        void updateLocationPermission_deny_success() {
            UserServiceImpl.setCurrentUserId(1L);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            assertDoesNotThrow(() -> userService.updateLocationPermission("DENY"));
        }

        @Test
        @DisplayName("无效权限值_抛出异常")
        void updateLocationPermission_invalid_throwsException() {
            UserServiceImpl.setCurrentUserId(1L);

            assertThrows(BusinessException.class, () -> userService.updateLocationPermission("INVALID"));
        }
    }
}