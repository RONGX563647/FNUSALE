package com.fnusale.marketing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.dto.marketing.CouponDTO;
import com.fnusale.common.entity.Coupon;
import com.fnusale.common.entity.UserCoupon;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.marketing.CouponVO;
import com.fnusale.common.vo.marketing.UserCouponVO;
import com.fnusale.marketing.mapper.CouponMapper;
import com.fnusale.marketing.mapper.UserCouponMapper;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 优惠券服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponMapper couponMapper;

    @Mock
    private UserCouponMapper userCouponMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon testCoupon;
    private CouponDTO testCouponDTO;

    @BeforeEach
    void setUp() {
        testCoupon = Coupon.builder()
                .id(1L)
                .couponName("测试优惠券")
                .couponType("FULL_REDUCE")
                .fullAmount(new BigDecimal("100"))
                .reduceAmount(new BigDecimal("10"))
                .totalCount(100)
                .receivedCount(50)
                .usedCount(10)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(30))
                .enableStatus(1)
                .createTime(LocalDateTime.now())
                .build();

        testCouponDTO = new CouponDTO();
        testCouponDTO.setCouponName("测试满减券");
        testCouponDTO.setCouponType("FULL_REDUCE");
        testCouponDTO.setFullAmount(new BigDecimal("100"));
        testCouponDTO.setReduceAmount(new BigDecimal("10"));
        testCouponDTO.setTotalCount(100);
        testCouponDTO.setStartTime(LocalDateTime.now().plusDays(1));
        testCouponDTO.setEndTime(LocalDateTime.now().plusDays(30));
    }

    @Nested
    @DisplayName("获取可领取优惠券列表测试")
    class GetAvailableCouponsTests {

        @Test
        @DisplayName("成功获取可领取优惠券列表")
        void shouldReturnAvailableCoupons() {
            when(couponMapper.selectAvailableCoupons(any(LocalDateTime.class)))
                    .thenReturn(List.of(testCoupon));
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(0);

            List<CouponVO> result = couponService.getAvailableCoupons(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试优惠券", result.get(0).getCouponName());
            assertFalse(result.get(0).getReceived());
            verify(couponMapper).selectAvailableCoupons(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("用户已领取的优惠券应标记为已领取")
        void shouldMarkReceivedCoupons() {
            when(couponMapper.selectAvailableCoupons(any(LocalDateTime.class)))
                    .thenReturn(List.of(testCoupon));
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(1);

            List<CouponVO> result = couponService.getAvailableCoupons(1L);

            assertTrue(result.get(0).getReceived());
        }

        @Test
        @DisplayName("无用户ID时返回未领取状态")
        void shouldReturnNotReceivedWhenNoUserId() {
            when(couponMapper.selectAvailableCoupons(any(LocalDateTime.class)))
                    .thenReturn(List.of(testCoupon));

            List<CouponVO> result = couponService.getAvailableCoupons(null);

            assertFalse(result.get(0).getReceived());
        }

        @Test
        @DisplayName("无可用优惠券时返回空列表")
        void shouldReturnEmptyListWhenNoCoupons() {
            when(couponMapper.selectAvailableCoupons(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            List<CouponVO> result = couponService.getAvailableCoupons(1L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("领取优惠券测试")
    class ReceiveCouponTests {

        @Test
        @DisplayName("成功领取优惠券")
        void shouldReceiveCouponSuccessfully() {
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(0);
            when(couponMapper.incrementReceivedCount(1L)).thenReturn(1);
            when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.receiveCoupon(1L, 1L));

            verify(couponMapper).incrementReceivedCount(1L);
            verify(userCouponMapper).insert(any(UserCoupon.class));
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void shouldThrowExceptionWhenCouponNotFound() {
            when(couponMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.receiveCoupon(1L, 1L));

            assertEquals(4001, exception.getCode());
            assertEquals("优惠券不存在", exception.getMessage());
        }

        @Test
        @DisplayName("优惠券已禁用时抛出异常")
        void shouldThrowExceptionWhenCouponDisabled() {
            testCoupon.setEnableStatus(0);
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.receiveCoupon(1L, 1L));

            assertEquals(4006, exception.getCode());
            assertEquals("优惠券已禁用", exception.getMessage());
        }

        @Test
        @DisplayName("优惠券未开始时抛出异常")
        void shouldThrowExceptionWhenCouponNotStarted() {
            testCoupon.setStartTime(LocalDateTime.now().plusDays(1));
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.receiveCoupon(1L, 1L));

            assertEquals(4003, exception.getCode());
            assertEquals("优惠券尚未开始", exception.getMessage());
        }

        @Test
        @DisplayName("优惠券已过期时抛出异常")
        void shouldThrowExceptionWhenCouponExpired() {
            testCoupon.setEndTime(LocalDateTime.now().minusDays(1));
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.receiveCoupon(1L, 1L));

            assertEquals(4003, exception.getCode());
            assertEquals("优惠券已过期", exception.getMessage());
        }

        @Test
        @DisplayName("已领取过的优惠券抛出异常")
        void shouldThrowExceptionWhenAlreadyReceived() {
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(1);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.receiveCoupon(1L, 1L));

            assertEquals(4005, exception.getCode());
            assertEquals("您已领取过该优惠券", exception.getMessage());
        }

        @Test
        @DisplayName("优惠券库存不足时抛出异常")
        void shouldThrowExceptionWhenCouponOutOfStock() {
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(0);
            when(couponMapper.incrementReceivedCount(1L)).thenReturn(0);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.receiveCoupon(1L, 1L));

            assertEquals(4002, exception.getCode());
            assertEquals("优惠券已领完", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("获取我的优惠券列表测试")
    class GetMyCouponsTests {

        @Test
        @DisplayName("成功获取我的优惠券列表")
        void shouldReturnMyCoupons() {
            UserCoupon userCoupon = UserCoupon.builder()
                    .id(1L)
                    .userId(1L)
                    .couponId(1L)
                    .couponStatus("UNUSED")
                    .expireTime(LocalDateTime.now().plusDays(30))
                    .receiveTime(LocalDateTime.now())
                    .build();

            when(userCouponMapper.selectByUserId(1L, null)).thenReturn(List.of(userCoupon));
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);

            List<UserCouponVO> result = couponService.getMyCoupons(1L, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getUsable());
        }

        @Test
        @DisplayName("已使用优惠券不可用")
        void shouldMarkUsedCouponAsNotUsable() {
            UserCoupon userCoupon = UserCoupon.builder()
                    .id(1L)
                    .userId(1L)
                    .couponId(1L)
                    .couponStatus("USED")
                    .expireTime(LocalDateTime.now().plusDays(30))
                    .receiveTime(LocalDateTime.now())
                    .build();

            when(userCouponMapper.selectByUserId(1L, null)).thenReturn(List.of(userCoupon));
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);

            List<UserCouponVO> result = couponService.getMyCoupons(1L, null);

            assertFalse(result.get(0).getUsable());
        }

        @Test
        @DisplayName("已过期优惠券不可用")
        void shouldMarkExpiredCouponAsNotUsable() {
            UserCoupon userCoupon = UserCoupon.builder()
                    .id(1L)
                    .userId(1L)
                    .couponId(1L)
                    .couponStatus("UNUSED")
                    .expireTime(LocalDateTime.now().minusDays(1))
                    .receiveTime(LocalDateTime.now())
                    .build();

            when(userCouponMapper.selectByUserId(1L, null)).thenReturn(List.of(userCoupon));
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);

            List<UserCouponVO> result = couponService.getMyCoupons(1L, null);

            assertFalse(result.get(0).getUsable());
        }
    }

    @Nested
    @DisplayName("获取优惠券详情测试")
    class GetCouponDetailTests {

        @Test
        @DisplayName("成功获取优惠券详情")
        void shouldReturnCouponDetail() {
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);

            CouponVO result = couponService.getCouponDetail(1L);

            assertNotNull(result);
            assertEquals("测试优惠券", result.getCouponName());
            assertEquals(50, result.getRemainCount());
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void shouldThrowExceptionWhenCouponNotFound() {
            when(couponMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.getCouponDetail(1L));

            assertEquals(4001, exception.getCode());
        }
    }

    @Nested
    @DisplayName("创建优惠券测试")
    class CreateCouponTests {

        @Test
        @DisplayName("成功创建满减券")
        void shouldCreateFullReduceCoupon() {
            when(couponMapper.insert(any(Coupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.createCoupon(testCouponDTO));

            verify(couponMapper).insert(any(Coupon.class));
        }

        @Test
        @DisplayName("满减券未配置门槛金额时抛出异常")
        void shouldThrowExceptionWhenFullReduceWithoutThreshold() {
            testCouponDTO.setFullAmount(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.createCoupon(testCouponDTO));

            assertEquals(400, exception.getCode());
            assertEquals("满减券必须配置满减门槛金额", exception.getMessage());
        }

        @Test
        @DisplayName("品类券未关联品类时抛出异常")
        void shouldThrowExceptionWhenCategoryCouponWithoutCategory() {
            testCouponDTO.setCouponType("CATEGORY");
            testCouponDTO.setCategoryId(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.createCoupon(testCouponDTO));

            assertEquals(400, exception.getCode());
            assertEquals("品类券必须关联商品品类", exception.getMessage());
        }

        @Test
        @DisplayName("有效期超过90天时抛出异常")
        void shouldThrowExceptionWhenValidityExceeds90Days() {
            testCouponDTO.setEndTime(LocalDateTime.now().plusDays(100));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.createCoupon(testCouponDTO));

            assertEquals(400, exception.getCode());
            assertEquals("有效期不能超过90天", exception.getMessage());
        }

        @Test
        @DisplayName("发放总数超过10000时抛出异常")
        void shouldThrowExceptionWhenTotalCountExceedsLimit() {
            testCouponDTO.setTotalCount(15000);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.createCoupon(testCouponDTO));

            assertEquals(400, exception.getCode());
            assertEquals("发放总数最大不超过10000张", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("更新优惠券测试")
    class UpdateCouponTests {

        @Test
        @DisplayName("成功更新优惠券")
        void shouldUpdateCoupon() {
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);
            when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.updateCoupon(1L, testCouponDTO));

            verify(couponMapper).updateById(any(Coupon.class));
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void shouldThrowExceptionWhenCouponNotFound() {
            when(couponMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.updateCoupon(1L, testCouponDTO));

            assertEquals(4001, exception.getCode());
        }
    }

    @Nested
    @DisplayName("删除优惠券测试")
    class DeleteCouponTests {

        @Test
        @DisplayName("成功删除优惠券")
        void shouldDeleteCoupon() {
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);
            when(couponMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> couponService.deleteCoupon(1L));

            verify(couponMapper).deleteById(1L);
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void shouldThrowExceptionWhenCouponNotFound() {
            when(couponMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.deleteCoupon(1L));

            assertEquals(4001, exception.getCode());
        }
    }

    @Nested
    @DisplayName("使用优惠券测试")
    class UseCouponTests {

        private UserCoupon userCoupon;

        @BeforeEach
        void setUpUserCoupon() {
            userCoupon = UserCoupon.builder()
                    .id(1L)
                    .userId(1L)
                    .couponId(1L)
                    .couponStatus(MarketingConstants.COUPON_STATUS_UNUSED)
                    .expireTime(LocalDateTime.now().plusDays(30))
                    .receiveTime(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("成功使用优惠券")
        void shouldUseCouponSuccessfully() {
            when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);
            when(userCouponMapper.updateById(any(UserCoupon.class))).thenReturn(1);
            when(couponMapper.incrementUsedCount(1L)).thenReturn(1);

            assertDoesNotThrow(() -> couponService.useCoupon(1L, 100L, 1L));

            verify(userCouponMapper).updateById(any(UserCoupon.class));
            verify(couponMapper).incrementUsedCount(1L);
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void shouldThrowExceptionWhenCouponNotFound() {
            when(userCouponMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.useCoupon(1L, 100L, 1L));

            assertEquals(4001, exception.getCode());
        }

        @Test
        @DisplayName("非本人优惠券抛出异常")
        void shouldThrowExceptionWhenNotOwner() {
            when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.useCoupon(1L, 100L, 2L));

            assertEquals(4001, exception.getCode());
            assertEquals("优惠券不存在", exception.getMessage());
        }

        @Test
        @DisplayName("已使用优惠券抛出异常")
        void shouldThrowExceptionWhenCouponAlreadyUsed() {
            userCoupon.setCouponStatus(MarketingConstants.COUPON_STATUS_USED);
            when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.useCoupon(1L, 100L, 1L));

            assertEquals(4004, exception.getCode());
            assertEquals("优惠券不可用", exception.getMessage());
        }

        @Test
        @DisplayName("已过期优惠券抛出异常")
        void shouldThrowExceptionWhenCouponExpired() {
            userCoupon.setExpireTime(LocalDateTime.now().minusDays(1));
            when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.useCoupon(1L, 100L, 1L));

            assertEquals(4003, exception.getCode());
            assertEquals("优惠券已过期", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("分页查询优惠券测试")
    class GetCouponPageTests {

        @Test
        @DisplayName("成功分页查询优惠券")
        void shouldReturnCouponPage() {
            Page<Coupon> page = new Page<>(1, 10);
            page.setRecords(List.of(testCoupon));
            page.setTotal(1);

            when(couponMapper.selectCouponPage(any(Page.class), isNull(), isNull(), isNull()))
                    .thenReturn(page);

            IPage<CouponVO> result = couponService.getCouponPage(null, null, null, 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1, result.getTotal());
        }
    }

    @Nested
    @DisplayName("发放优惠券测试")
    class GrantCouponTests {

        @Test
        @DisplayName("成功发放优惠券给多个用户")
        void shouldGrantCouponToUsers() {
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);
            when(userCouponMapper.countByUserAndCoupon(anyLong(), anyLong())).thenReturn(0);
            when(couponMapper.incrementReceivedCount(1L)).thenReturn(1);
            when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.grantCoupon(1L, List.of(1L, 2L, 3L)));

            verify(userCouponMapper, times(3)).insert(any(UserCoupon.class));
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void shouldThrowExceptionWhenCouponNotFound() {
            when(couponMapper.selectById(1L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.grantCoupon(1L, List.of(1L)));

            assertEquals(4001, exception.getCode());
        }

        @Test
        @DisplayName("优惠券已禁用时抛出异常")
        void shouldThrowExceptionWhenCouponDisabled() {
            testCoupon.setEnableStatus(0);
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> couponService.grantCoupon(1L, List.of(1L)));

            assertEquals(4006, exception.getCode());
        }

        @Test
        @DisplayName("用户已领取过则跳过")
        void shouldSkipUserWhoAlreadyReceived() {
            when(couponMapper.selectById(1L)).thenReturn(testCoupon);
            when(userCouponMapper.countByUserAndCoupon(1L, 1L)).thenReturn(1);
            when(userCouponMapper.countByUserAndCoupon(2L, 1L)).thenReturn(0);
            when(couponMapper.incrementReceivedCount(1L)).thenReturn(1);
            when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);

            couponService.grantCoupon(1L, List.of(1L, 2L));

            verify(userCouponMapper, times(1)).insert(any(UserCoupon.class));
        }
    }
}