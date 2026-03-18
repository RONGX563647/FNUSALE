package com.fnusale.user.service.impl;

import com.fnusale.common.constant.UserConstants;
import com.fnusale.common.dto.user.UserAddressDTO;
import com.fnusale.common.entity.CampusPickPoint;
import com.fnusale.common.entity.UserAddress;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.UserAddressVO;
import com.fnusale.user.mapper.CampusPickPointMapper;
import com.fnusale.user.mapper.UserAddressMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户地址服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserAddressServiceImplTest {

    @Mock
    private UserAddressMapper userAddressMapper;

    @Mock
    private CampusPickPointMapper campusPickPointMapper;

    @InjectMocks
    private UserAddressServiceImpl userAddressService;

    private UserAddress testAddress;
    private CampusPickPoint testPickPoint;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        testPickPoint = new CampusPickPoint();
        testPickPoint.setId(1L);
        testPickPoint.setPickPointName("图书馆自提点");
        testPickPoint.setCampusArea("东区");
        testPickPoint.setDetailAddress("图书馆一楼大厅");
        testPickPoint.setLongitude(new BigDecimal("116.404"));
        testPickPoint.setLatitude(new BigDecimal("39.915"));
        testPickPoint.setEnableStatus(1);

        testAddress = new UserAddress();
        testAddress.setId(1L);
        testAddress.setUserId(userId);
        testAddress.setAddressType("PICK_POINT");
        testAddress.setPickPointId(1L);
        testAddress.setIsDefault(1);
        testAddress.setLongitude(new BigDecimal("116.404"));
        testAddress.setLatitude(new BigDecimal("39.915"));
        testAddress.setCreateTime(LocalDateTime.now());
        testAddress.setUpdateTime(LocalDateTime.now());
    }

    @Nested
    @DisplayName("获取地址列表测试")
    class GetListTests {

        @Test
        @DisplayName("获取列表_成功")
        void getList_success() {
            when(userAddressMapper.selectByUserId(userId)).thenReturn(List.of(testAddress));
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);

            List<UserAddressVO> result = userAddressService.getList(userId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
        }

        @Test
        @DisplayName("空列表_返回空")
        void getList_empty() {
            when(userAddressMapper.selectByUserId(userId)).thenReturn(List.of());

            List<UserAddressVO> result = userAddressService.getList(userId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取地址详情测试")
    class GetByIdTests {

        @Test
        @DisplayName("获取详情_成功")
        void getById_success() {
            when(userAddressMapper.selectById(1L)).thenReturn(testAddress);
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);

            UserAddressVO result = userAddressService.getById(userId, 1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("地址不存在_抛出异常")
        void getById_notFound_throwsException() {
            when(userAddressMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userAddressService.getById(userId, 999L));
        }

        @Test
        @DisplayName("地址不属于当前用户_抛出异常")
        void getById_notOwner_throwsException() {
            testAddress.setUserId(2L);
            when(userAddressMapper.selectById(1L)).thenReturn(testAddress);

            assertThrows(BusinessException.class, () -> userAddressService.getById(userId, 1L));
        }
    }

    @Nested
    @DisplayName("新增地址测试")
    class AddTests {

        @Test
        @DisplayName("新增自提点地址_成功")
        void add_pickPoint_success() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("PICK_POINT");
            dto.setPickPointId(1L);
            dto.setIsDefault(1);

            when(userAddressMapper.countByUserId(userId)).thenReturn(0);
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);
            when(userAddressMapper.insert(any(UserAddress.class))).thenReturn(1);

            assertDoesNotThrow(() -> userAddressService.add(userId, dto));
            verify(userAddressMapper).clearDefaultByUserId(userId);
        }

        @Test
        @DisplayName("新增自定义地址_成功")
        void add_custom_success() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("CUSTOM");
            dto.setCustomAddress("宿舍楼101室");
            dto.setLongitude("116.404");
            dto.setLatitude("39.915");
            dto.setIsDefault(0);

            when(userAddressMapper.countByUserId(userId)).thenReturn(0);
            when(userAddressMapper.insert(any(UserAddress.class))).thenReturn(1);

            assertDoesNotThrow(() -> userAddressService.add(userId, dto));
        }

        @Test
        @DisplayName("地址数量达上限_抛出异常")
        void add_maxCountReached_throwsException() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("CUSTOM");
            dto.setCustomAddress("宿舍楼101室");

            when(userAddressMapper.countByUserId(userId)).thenReturn(UserConstants.MAX_ADDRESS_COUNT);

            assertThrows(BusinessException.class, () -> userAddressService.add(userId, dto));
        }

        @Test
        @DisplayName("自提点地址缺少自提点ID_抛出异常")
        void add_pickPointMissingId_throwsException() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("PICK_POINT");
            dto.setIsDefault(0);

            when(userAddressMapper.countByUserId(userId)).thenReturn(0);

            assertThrows(BusinessException.class, () -> userAddressService.add(userId, dto));
        }

        @Test
        @DisplayName("自提点不存在_抛出异常")
        void add_pickPointNotFound_throwsException() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("PICK_POINT");
            dto.setPickPointId(999L);
            dto.setIsDefault(0);

            when(userAddressMapper.countByUserId(userId)).thenReturn(0);
            when(campusPickPointMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userAddressService.add(userId, dto));
        }

        @Test
        @DisplayName("自提点已禁用_抛出异常")
        void add_pickPointDisabled_throwsException() {
            testPickPoint.setEnableStatus(0);
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("PICK_POINT");
            dto.setPickPointId(1L);
            dto.setIsDefault(0);

            when(userAddressMapper.countByUserId(userId)).thenReturn(0);
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);

            assertThrows(BusinessException.class, () -> userAddressService.add(userId, dto));
        }

        @Test
        @DisplayName("自定义地址缺少地址内容_抛出异常")
        void add_customMissingAddress_throwsException() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("CUSTOM");
            dto.setIsDefault(0);

            when(userAddressMapper.countByUserId(userId)).thenReturn(0);

            assertThrows(BusinessException.class, () -> userAddressService.add(userId, dto));
        }

        @Test
        @DisplayName("地址类型不正确_抛出异常")
        void add_invalidAddressType_throwsException() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("INVALID");
            dto.setIsDefault(0);

            when(userAddressMapper.countByUserId(userId)).thenReturn(0);

            assertThrows(BusinessException.class, () -> userAddressService.add(userId, dto));
        }
    }

    @Nested
    @DisplayName("更新地址测试")
    class UpdateTests {

        @Test
        @DisplayName("更新地址_成功")
        void update_success() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("CUSTOM");
            dto.setCustomAddress("新地址");
            dto.setIsDefault(1);

            when(userAddressMapper.selectById(1L)).thenReturn(testAddress);
            when(userAddressMapper.updateById(any(UserAddress.class))).thenReturn(1);

            assertDoesNotThrow(() -> userAddressService.update(userId, 1L, dto));
        }

        @Test
        @DisplayName("地址不存在_抛出异常")
        void update_notFound_throwsException() {
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("CUSTOM");
            dto.setCustomAddress("新地址");

            when(userAddressMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userAddressService.update(userId, 999L, dto));
        }

        @Test
        @DisplayName("地址不属于当前用户_抛出异常")
        void update_notOwner_throwsException() {
            testAddress.setUserId(2L);
            UserAddressDTO dto = new UserAddressDTO();
            dto.setAddressType("CUSTOM");
            dto.setCustomAddress("新地址");

            when(userAddressMapper.selectById(1L)).thenReturn(testAddress);

            assertThrows(BusinessException.class, () -> userAddressService.update(userId, 1L, dto));
        }
    }

    @Nested
    @DisplayName("删除地址测试")
    class DeleteTests {

        @Test
        @DisplayName("删除地址_成功")
        void delete_success() {
            when(userAddressMapper.selectById(1L)).thenReturn(testAddress);
            when(userAddressMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> userAddressService.delete(userId, 1L));
        }

        @Test
        @DisplayName("地址不存在_抛出异常")
        void delete_notFound_throwsException() {
            when(userAddressMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userAddressService.delete(userId, 999L));
        }

        @Test
        @DisplayName("地址不属于当前用户_抛出异常")
        void delete_notOwner_throwsException() {
            testAddress.setUserId(2L);
            when(userAddressMapper.selectById(1L)).thenReturn(testAddress);

            assertThrows(BusinessException.class, () -> userAddressService.delete(userId, 1L));
        }
    }

    @Nested
    @DisplayName("设置默认地址测试")
    class SetDefaultTests {

        @Test
        @DisplayName("设置默认_成功")
        void setDefault_success() {
            when(userAddressMapper.selectById(1L)).thenReturn(testAddress);
            when(userAddressMapper.updateById(any(UserAddress.class))).thenReturn(1);

            assertDoesNotThrow(() -> userAddressService.setDefault(userId, 1L));
            verify(userAddressMapper).clearDefaultByUserId(userId);
        }

        @Test
        @DisplayName("地址不存在_抛出异常")
        void setDefault_notFound_throwsException() {
            when(userAddressMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> userAddressService.setDefault(userId, 999L));
        }
    }

    @Nested
    @DisplayName("获取默认地址测试")
    class GetDefaultTests {

        @Test
        @DisplayName("获取默认地址_成功")
        void getDefault_success() {
            when(userAddressMapper.selectDefaultByUserId(userId)).thenReturn(testAddress);
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);

            UserAddressVO result = userAddressService.getDefault(userId);

            assertNotNull(result);
            assertTrue(result.getIsDefault());
        }

        @Test
        @DisplayName("无默认地址_返回null")
        void getDefault_notFound() {
            when(userAddressMapper.selectDefaultByUserId(userId)).thenReturn(null);

            UserAddressVO result = userAddressService.getDefault(userId);

            assertNull(result);
        }
    }
}