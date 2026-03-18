package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.admin.mapper.TradeDisputeMapper;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.admin.service.AdminDisputeService;
import com.fnusale.admin.service.SystemLogService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.admin.DisputeProcessDTO;
import com.fnusale.common.entity.TradeDispute;
import com.fnusale.common.entity.User;
import com.fnusale.common.enums.DisputeStatus;
import com.fnusale.common.enums.LogModule;
import com.fnusale.common.enums.OperateType;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.admin.DisputeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 纠纷处理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDisputeServiceImpl implements AdminDisputeService {

    private final TradeDisputeMapper tradeDisputeMapper;
    private final UserMapper userMapper;
    private final SystemLogService systemLogService;

    @Override
    public PageResult<DisputeVO> getDisputePage(String status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TradeDispute> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(TradeDispute::getDisputeStatus, status);
        }
        wrapper.orderByDesc(TradeDispute::getCreateTime);

        Page<TradeDispute> page = new Page<>(pageNum, pageSize);
        Page<TradeDispute> result = tradeDisputeMapper.selectPage(page, wrapper);

        java.util.List<DisputeVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return new PageResult<>(pageNum, pageSize, result.getTotal(), voList);
    }

    @Override
    public DisputeVO getDisputeDetail(Long disputeId) {
        TradeDispute dispute = tradeDisputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }
        return convertToDetailVO(dispute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processDispute(Long disputeId, DisputeProcessDTO dto, Long adminId) {
        TradeDispute dispute = tradeDisputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }
        if (DisputeStatus.RESOLVED.getCode().equals(dispute.getDisputeStatus())) {
            throw new BusinessException("纠纷已解决，不能重复处理");
        }

        // 更新纠纷状态
        TradeDispute updateDispute = new TradeDispute();
        updateDispute.setId(disputeId);
        updateDispute.setDisputeStatus(DisputeStatus.RESOLVED.getCode());
        updateDispute.setProcessResult(dto.getProcessResult());
        updateDispute.setProcessRemark(dto.getProcessRemark());
        updateDispute.setAdminId(adminId);
        updateDispute.setUpdateTime(LocalDateTime.now());
        tradeDisputeMapper.updateById(updateDispute);

        // 调整相关方信誉分
        if (dto.getBuyerCreditChange() != null && dto.getBuyerCreditChange() != 0) {
            adjustUserCredit(dispute.getInitiatorId(), dto.getBuyerCreditChange());
        }
        if (dto.getSellerCreditChange() != null && dto.getSellerCreditChange() != 0) {
            adjustUserCredit(dispute.getAccusedId(), dto.getSellerCreditChange());
        }

        // 记录日志
        systemLogService.log(adminId, LogModule.ORDER.getCode(), OperateType.UPDATE.getCode(),
                "处理纠纷ID:" + disputeId + ", 结果:" + dto.getProcessResult(), null, null);

        log.info("处理纠纷, disputeId: {}, result: {}, adminId: {}", disputeId, dto.getProcessResult(), adminId);
    }

    private void adjustUserCredit(Long userId, Integer change) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            int newScore = Math.max(0, Math.min(100, user.getCreditScore() + change));
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setCreditScore(newScore);
            userMapper.updateById(updateUser);
        }
    }

    private DisputeVO convertToVO(TradeDispute dispute) {
        DisputeVO vo = new DisputeVO();
        vo.setDisputeId(dispute.getId());
        vo.setOrderId(dispute.getOrderId());
        vo.setDisputeType(dispute.getDisputeType());
        vo.setDisputeStatus(dispute.getDisputeStatus());
        vo.setProcessResult(dispute.getProcessResult());
        vo.setCreateTime(dispute.getCreateTime());

        // 获取发起者信息
        User initiator = userMapper.selectById(dispute.getInitiatorId());
        if (initiator != null) {
            DisputeVO.DisputeUser initiatorVo = new DisputeVO.DisputeUser();
            initiatorVo.setUserId(initiator.getId());
            initiatorVo.setUsername(initiator.getUsername());
            vo.setInitiator(initiatorVo);
        }

        // 获取被投诉者信息
        User accused = userMapper.selectById(dispute.getAccusedId());
        if (accused != null) {
            DisputeVO.DisputeUser accusedVo = new DisputeVO.DisputeUser();
            accusedVo.setUserId(accused.getId());
            accusedVo.setUsername(accused.getUsername());
            vo.setAccused(accusedVo);
        }

        return vo;
    }

    private DisputeVO convertToDetailVO(TradeDispute dispute) {
        DisputeVO vo = convertToVO(dispute);
        vo.setProcessRemark(dispute.getProcessRemark());

        // 解析举证材料
        if (dispute.getEvidenceUrl() != null && !dispute.getEvidenceUrl().isEmpty()) {
            vo.setEvidenceUrls(Arrays.asList(dispute.getEvidenceUrl().split(",")));
        }

        return vo;
    }
}