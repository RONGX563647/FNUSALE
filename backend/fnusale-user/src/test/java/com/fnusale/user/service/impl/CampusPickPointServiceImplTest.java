package com.fnusale.user.service.impl;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.user.CampusPickPointDTO;
import com.fnusale.common.entity.CampusPickPoint;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.user.CampusPickPointVO;
import com.fnusale.user.mapper.CampusPickPointMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 校园自提点服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class CampusPickPointServiceImplTest {

    @Mock
    private CampusPickPointMapper campusPickPointMapper;

    @InjectMocks
    private CampusPickPointServiceImpl campusPickPointService;

    private CampusPickPoint testPickPoint;

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
    }

    @Nested
    @DisplayName("获取自提点列表测试")
    class GetListTests {

        @Test
        @DisplayName("获取所有启用的自提点_成功")
        void getList_success() {
            when(campusPickPointMapper.selectAllEnabled()).thenReturn(List.of(testPickPoint));

            List<CampusPickPointVO> result = campusPickPointService.getList();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("图书馆自提点", result.get(0).getPickPointName());
            assertTrue(result.get(0).getEnableStatus());
        }

        @Test
        @DisplayName("无自提点_返回空列表")
        void getList_empty() {
            when(campusPickPointMapper.selectAllEnabled()).thenReturn(List.of());

            List<CampusPickPointVO> result = campusPickPointService.getList();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取附近自提点测试")
    class GetNearbyTests {

        @Test
        @DisplayName("获取附近自提点_按距离排序")
        void getNearby_sortedByDistance() {
            CampusPickPoint nearPoint = new CampusPickPoint();
            nearPoint.setId(1L);
            nearPoint.setPickPointName("近的自提点");
            nearPoint.setLongitude(new BigDecimal("116.405"));
            nearPoint.setLatitude(new BigDecimal("39.916"));
            nearPoint.setEnableStatus(1);

            CampusPickPoint farPoint = new CampusPickPoint();
            farPoint.setId(2L);
            farPoint.setPickPointName("远的自提点");
            farPoint.setLongitude(new BigDecimal("116.500"));
            farPoint.setLatitude(new BigDecimal("40.000"));
            farPoint.setEnableStatus(1);

            when(campusPickPointMapper.selectAllEnabled()).thenReturn(List.of(nearPoint, farPoint));

            // 使用近自提点的坐标
            List<CampusPickPointVO> result = campusPickPointService.getNearby("116.405", "39.916", 10000);

            assertNotNull(result);
            // 近的应该排在前面
            assertEquals("近的自提点", result.get(0).getPickPointName());
        }

        @Test
        @DisplayName("超出距离范围_返回空列表")
        void getNearby_outOfRange() {
            CampusPickPoint farPoint = new CampusPickPoint();
            farPoint.setId(1L);
            farPoint.setPickPointName("远的自提点");
            farPoint.setLongitude(new BigDecimal("120.000"));
            farPoint.setLatitude(new BigDecimal("40.000"));
            farPoint.setEnableStatus(1);

            when(campusPickPointMapper.selectAllEnabled()).thenReturn(List.of(farPoint));

            // 使用北京坐标，自提点在上海附近
            List<CampusPickPointVO> result = campusPickPointService.getNearby("116.404", "39.915", 1000);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("距离计算正确")
        void getNearby_distanceCalculation() {
            // 创建一个距离约100米的自提点
            CampusPickPoint point = new CampusPickPoint();
            point.setId(1L);
            point.setPickPointName("测试自提点");
            // 约100米距离的坐标偏移
            point.setLongitude(new BigDecimal("116.405")); // 约0.001度经度
            point.setLatitude(new BigDecimal("39.915"));
            point.setEnableStatus(1);

            when(campusPickPointMapper.selectAllEnabled()).thenReturn(List.of(point));

            List<CampusPickPointVO> result = campusPickPointService.getNearby("116.404", "39.915", 1000);

            assertEquals(1, result.size());
            assertNotNull(result.get(0).getDistance());
            // 验证距离计算在合理范围内（约100米）
            assertTrue(result.get(0).getDistance() < 200);
        }
    }

    @Nested
    @DisplayName("获取自提点详情测试")
    class GetByIdTests {

        @Test
        @DisplayName("获取详情_成功")
        void getById_success() {
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);

            CampusPickPointVO result = campusPickPointService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("图书馆自提点", result.getPickPointName());
            assertEquals("东区", result.getCampusArea());
        }

        @Test
        @DisplayName("自提点不存在_抛出异常")
        void getById_notFound_throwsException() {
            when(campusPickPointMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> campusPickPointService.getById(999L));
        }
    }

    @Nested
    @DisplayName("新增自提点测试")
    class AddTests {

        @Test
        @DisplayName("新增自提点_成功")
        void add_success() {
            CampusPickPointDTO dto = new CampusPickPointDTO();
            dto.setPickPointName("新自提点");
            dto.setCampusArea("西区");
            dto.setDetailAddress("食堂门口");
            dto.setLongitude("116.400");
            dto.setLatitude("39.920");

            when(campusPickPointMapper.insert(any(CampusPickPoint.class))).thenReturn(1);

            assertDoesNotThrow(() -> campusPickPointService.add(dto));
        }

        @Test
        @DisplayName("新增无坐标自提点_成功")
        void add_withoutCoordinates_success() {
            CampusPickPointDTO dto = new CampusPickPointDTO();
            dto.setPickPointName("新自提点");
            dto.setCampusArea("西区");
            dto.setDetailAddress("食堂门口");

            when(campusPickPointMapper.insert(any(CampusPickPoint.class))).thenReturn(1);

            assertDoesNotThrow(() -> campusPickPointService.add(dto));
        }
    }

    @Nested
    @DisplayName("更新自提点测试")
    class UpdateTests {

        @Test
        @DisplayName("更新自提点_成功")
        void update_success() {
            CampusPickPointDTO dto = new CampusPickPointDTO();
            dto.setPickPointName("更新后的名称");
            dto.setCampusArea("东区");
            dto.setDetailAddress("新地址");

            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);
            when(campusPickPointMapper.updateById(any(CampusPickPoint.class))).thenReturn(1);

            assertDoesNotThrow(() -> campusPickPointService.update(1L, dto));
        }

        @Test
        @DisplayName("自提点不存在_抛出异常")
        void update_notFound_throwsException() {
            CampusPickPointDTO dto = new CampusPickPointDTO();
            dto.setPickPointName("更新后的名称");

            when(campusPickPointMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> campusPickPointService.update(999L, dto));
        }
    }

    @Nested
    @DisplayName("删除自提点测试")
    class DeleteTests {

        @Test
        @DisplayName("删除自提点_成功")
        void delete_success() {
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);
            when(campusPickPointMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> campusPickPointService.delete(1L));
        }

        @Test
        @DisplayName("自提点不存在_抛出异常")
        void delete_notFound_throwsException() {
            when(campusPickPointMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> campusPickPointService.delete(999L));
        }
    }

    @Nested
    @DisplayName("更新自提点状态测试")
    class UpdateStatusTests {

        @Test
        @DisplayName("禁用自提点_成功")
        void updateStatus_disable_success() {
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);
            when(campusPickPointMapper.updateById(any(CampusPickPoint.class))).thenReturn(1);

            assertDoesNotThrow(() -> campusPickPointService.updateStatus(1L, 0));
        }

        @Test
        @DisplayName("启用自提点_成功")
        void updateStatus_enable_success() {
            testPickPoint.setEnableStatus(0);
            when(campusPickPointMapper.selectById(1L)).thenReturn(testPickPoint);
            when(campusPickPointMapper.updateById(any(CampusPickPoint.class))).thenReturn(1);

            assertDoesNotThrow(() -> campusPickPointService.updateStatus(1L, 1));
        }

        @Test
        @DisplayName("自提点不存在_抛出异常")
        void updateStatus_notFound_throwsException() {
            when(campusPickPointMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> campusPickPointService.updateStatus(999L, 0));
        }
    }

    @Nested
    @DisplayName("分页查询自提点测试")
    class GetPageTests {

        @Test
        @DisplayName("分页查询_成功")
        void getPage_success() {
            when(campusPickPointMapper.selectList(null)).thenReturn(List.of(testPickPoint));

            PageResult<CampusPickPointVO> result = campusPickPointService.getPage(null, null, 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
        }

        @Test
        @DisplayName("按校区筛选_成功")
        void getPage_filterByCampusArea() {
            when(campusPickPointMapper.selectByCampusArea("东区")).thenReturn(List.of(testPickPoint));

            PageResult<CampusPickPointVO> result = campusPickPointService.getPage("东区", null, 1, 10);

            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("按状态筛选_成功")
        void getPage_filterByStatus() {
            when(campusPickPointMapper.selectList(null)).thenReturn(List.of(testPickPoint));

            PageResult<CampusPickPointVO> result = campusPickPointService.getPage(null, 1, 1, 10);

            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("分页正确_第二页为空")
        void getPage_secondPageEmpty() {
            when(campusPickPointMapper.selectList(null)).thenReturn(List.of(testPickPoint));

            PageResult<CampusPickPointVO> result = campusPickPointService.getPage(null, null, 2, 10);

            assertEquals(1, result.getTotal());
            assertTrue(result.getList().isEmpty());
        }
    }

    @Nested
    @DisplayName("距离计算测试")
    class DistanceCalculationTests {

        @Test
        @DisplayName("相同坐标_距离为0")
        void sameCoordinates_distanceZero() {
            CampusPickPoint point = new CampusPickPoint();
            point.setId(1L);
            point.setPickPointName("测试点");
            point.setLongitude(new BigDecimal("116.404"));
            point.setLatitude(new BigDecimal("39.915"));
            point.setEnableStatus(1);

            when(campusPickPointMapper.selectAllEnabled()).thenReturn(List.of(point));

            List<CampusPickPointVO> result = campusPickPointService.getNearby("116.404", "39.915", 100);

            assertEquals(1, result.size());
            assertEquals(0, result.get(0).getDistance());
        }

        @Test
        @DisplayName("地球赤道距离计算_合理范围")
        void equatorDistance_reasonableRange() {
            CampusPickPoint point = new CampusPickPoint();
            point.setId(1L);
            point.setPickPointName("测试点");
            // 约1度经度在赤道约111公里
            point.setLongitude(new BigDecimal("117.404"));
            point.setLatitude(new BigDecimal("0"));
            point.setEnableStatus(1);

            when(campusPickPointMapper.selectAllEnabled()).thenReturn(List.of(point));

            List<CampusPickPointVO> result = campusPickPointService.getNearby("116.404", "0", 120000);

            assertEquals(1, result.size());
            // 验证距离在合理范围（约111km）
            assertTrue(result.get(0).getDistance() > 100000);
            assertTrue(result.get(0).getDistance() < 120000);
        }
    }
}