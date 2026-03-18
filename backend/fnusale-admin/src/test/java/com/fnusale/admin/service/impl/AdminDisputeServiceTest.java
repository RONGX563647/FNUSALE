package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.admin.mapper.TradeDisputeMapper;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.admin.service.SystemLogService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.admin.DisputeProcessDTO;
import com.fnusale.common.entity.TradeDispute;
import com.fnusale.common.entity.User;
import com.fnusale.common.enums.DisputeStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.admin.DisputeVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdminDisputeService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminDisputeServiceTest {

    @Mock
    private TradeDisputeMapper tradeDisputeMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private AdminDisputeServiceImpl adminDisputeService;

    private TradeDispute createTestDispute(Long id, String status, Long initiatorId, Long accusedId) {
        TradeDispute dispute = new TradeDispute();
        dispute.setId(id);
        dispute.setOrderId(100L);
        dispute.setInitiatorId(initiatorId);
        dispute.setAccusedId(accusedId);
        dispute.setDisputeType("PRODUCT_NOT_MATCH");
        dispute.setDisputeStatus(status);
        dispute.setProcessResult(null);
        dispute.setProcessRemark(null);
        return dispute;
    }

    private User createTestUser(Long id, String username, Integer creditScore) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setCreditScore(creditScore);
        return user;
    }

    @Nested
    @DisplayName("纠纷列表测试")
    class GetDisputePageTest {

        @Test
        @DisplayName("查询纠纷列表成功")
        void getDisputePage_success() {
            // Arrange
            TradeDispute dispute = createTestDispute(1L, DisputeStatus.PENDING.getCode(), 1L, 2L);
            Page<TradeDispute> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.setRecords(List.of(dispute));

            User initiator = createTestUser(1L, "buyer", 80);
            User accused = createTestUser(2L, "seller", 90);

            when(tradeDisputeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);
            when(userMapper.selectById(1L)).thenReturn(initiator);
            when(userMapper.selectById(2L)).thenReturn(accused);

            // Act
            PageResult<DisputeVO> result = adminDisputeService.getDisputePage(null, 1, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
        }

        @Test
        @DisplayName("按状态查询纠纷列表")
        void getDisputePage_withStatus() {
            // Arrange
            TradeDispute dispute = createTestDispute(1L, DisputeStatus.PENDING.getCode(), 1L, 2L);
            Page<TradeDispute> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.setRecords(List.of(dispute));

            User initiator = createTestUser(1L, "buyer", 80);
            User accused = createTestUser(2L, "seller", 90);

            when(tradeDisputeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);
            when(userMapper.selectById(1L)).thenReturn(initiator);
            when(userMapper.selectById(2L)).thenReturn(accused);

            // Act
            PageResult<DisputeVO> result = adminDisputeService.getDisputePage("PENDING", 1, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getList().size());
        }
    }

    @Nested
    @DisplayName("纠纷详情测试")
    class GetDisputeDetailTest {

        @Test
        @DisplayName("获取纠纷详情成功")
        void getDisputeDetail_success() {
            // Arrange
            TradeDispute dispute = createTestDispute(1L, DisputeStatus.PENDING.getCode(), 1L, 2L);
            dispute.setEvidenceUrl("http://example.com/evidence1.jpg,http://example.com/evidence2.jpg");

            User initiator = createTestUser(1L, "buyer", 80);
            User accused = createTestUser(2L, "seller", 90);

            when(tradeDisputeMapper.selectById(1L)).thenReturn(dispute);
            when(userMapper.selectById(1L)).thenReturn(initiator);
            when(userMapper.selectById(2L)).thenReturn(accused);

            // Act
            DisputeVO result = adminDisputeService.getDisputeDetail(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getDisputeId());
            assertNotNull(result.getInitiator());
            assertNotNull(result.getAccused());
            assertEquals("buyer", result.getInitiator().getUsername());
            assertEquals("seller", result.getAccused().getUsername());
            assertNotNull(result.getEvidenceUrls());
            assertEquals(2, result.getEvidenceUrls().size());
        }

        @Test
        @DisplayName("纠纷不存在抛出异常")
        void getDisputeDetail_notFound_throwsException() {
            when(tradeDisputeMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> adminDisputeService.getDisputeDetail(999L));
        }
    }

    @Nested
    @DisplayName("处理纠纷测试")
    class ProcessDisputeTest {

        @Test
        @DisplayName("处理纠纷成功")
        void processDispute_success() {
            // Arrange
            TradeDispute dispute = createTestDispute(1L, DisputeStatus.PENDING.getCode(), 1L, 2L);
            User buyer = createTestUser(1L, "buyer", 80);
            User seller = createTestUser(2L, "seller", 90);

            DisputeProcessDTO dto = new DisputeProcessDTO();
            dto.setProcessResult("BUYER_WIN");
            dto.setProcessRemark("商品与描述不符");
            dto.setBuyerCreditChange(5);
            dto.setSellerCreditChange(-10);

            when(tradeDisputeMapper.selectById(1L)).thenReturn(dispute);
            when(tradeDisputeMapper.updateById(any(TradeDispute.class))).thenReturn(1);
            when(userMapper.selectById(1L)).thenReturn(buyer);
            when(userMapper.selectById(2L)).thenReturn(seller);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            adminDisputeService.processDispute(1L, dto, 1L);

            // Assert
            verify(tradeDisputeMapper).updateById(argThat(d ->
                    d.getId().equals(1L) &&
                    d.getDisputeStatus().equals(DisputeStatus.RESOLVED.getCode()) &&
                    d.getProcessResult().equals("BUYER_WIN")
            ));
            // 验证信誉分调整
            verify(userMapper).updateById(argThat(u -> u.getId().equals(1L) && u.getCreditScore().equals(85)));
            verify(userMapper).updateById(argThat(u -> u.getId().equals(2L) && u.getCreditScore().equals(80)));
        }

        @Test
        @DisplayName("处理纠纷-无信誉分变化")
        void processDispute_noCreditChange() {
            // Arrange
            TradeDispute dispute = createTestDispute(1L, DisputeStatus.PENDING.getCode(), 1L, 2L);

            DisputeProcessDTO dto = new DisputeProcessDTO();
            dto.setProcessResult("NEGOTIATE");
            dto.setProcessRemark("双方协商解决");
            dto.setBuyerCreditChange(null);
            dto.setSellerCreditChange(null);

            when(tradeDisputeMapper.selectById(1L)).thenReturn(dispute);
            when(tradeDisputeMapper.updateById(any(TradeDispute.class))).thenReturn(1);

            // Act
            adminDisputeService.processDispute(1L, dto, 1L);

            // Assert
            verify(tradeDisputeMapper).updateById(any(TradeDispute.class));
            verify(userMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("处理纠纷-纠纷不存在抛出异常")
        void processDispute_notFound_throwsException() {
            when(tradeDisputeMapper.selectById(1L)).thenReturn(null);

            DisputeProcessDTO dto = new DisputeProcessDTO();
            dto.setProcessResult("BUYER_WIN");
            dto.setProcessRemark("测试");

            assertThrows(BusinessException.class, () -> adminDisputeService.processDispute(1L, dto, 1L));
        }

        @Test
        @DisplayName("处理纠纷-已解决的纠纷抛出异常")
        void processDispute_alreadyResolved_throwsException() {
            // Arrange
            TradeDispute dispute = createTestDispute(1L, DisputeStatus.RESOLVED.getCode(), 1L, 2L);
            when(tradeDisputeMapper.selectById(1L)).thenReturn(dispute);

            DisputeProcessDTO dto = new DisputeProcessDTO();
            dto.setProcessResult("BUYER_WIN");
            dto.setProcessRemark("测试");

            // Act & Assert
            assertThrows(BusinessException.class, () -> adminDisputeService.processDispute(1L, dto, 1L));
        }

        @Test
        @DisplayName("处理纠纷-信誉分下限为0")
        void processDispute_creditFloor() {
            // Arrange
            TradeDispute dispute = createTestDispute(1L, DisputeStatus.PENDING.getCode(), 1L, 2L);
            User buyer = createTestUser(1L, "buyer", 5);
            User seller = createTestUser(2L, "seller", 90);

            DisputeProcessDTO dto = new DisputeProcessDTO();
            dto.setProcessResult("SELLER_WIN");
            dto.setProcessRemark("买家恶意投诉");
            dto.setBuyerCreditChange(-10);

            when(tradeDisputeMapper.selectById(1L)).thenReturn(dispute);
            when(tradeDisputeMapper.updateById(any(TradeDispute.class))).thenReturn(1);
            when(userMapper.selectById(1L)).thenReturn(buyer);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            adminDisputeService.processDispute(1L, dto, 1L);

            // Assert
            verify(userMapper).updateById(argThat(u -> u.getCreditScore().equals(0)));
        }

        @Test
        @DisplayName("处理纠纷-信誉分上限为100")
        void processDispute_creditCeiling() {
            // Arrange
            TradeDispute dispute = createTestDispute(1L, DisputeStatus.PENDING.getCode(), 1L, 2L);
            User buyer = createTestUser(1L, "buyer", 98);
            User seller = createTestUser(2L, "seller", 90);

            DisputeProcessDTO dto = new DisputeProcessDTO();
            dto.setProcessResult("BUYER_WIN");
            dto.setProcessRemark("商品与描述不符");
            dto.setBuyerCreditChange(10);

            when(tradeDisputeMapper.selectById(1L)).thenReturn(dispute);
            when(tradeDisputeMapper.updateById(any(TradeDispute.class))).thenReturn(1);
            when(userMapper.selectById(1L)).thenReturn(buyer);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // Act
            adminDisputeService.processDispute(1L, dto, 1L);

            // Assert
            verify(userMapper).updateById(argThat(u -> u.getCreditScore().equals(100)));
        }
    }
}