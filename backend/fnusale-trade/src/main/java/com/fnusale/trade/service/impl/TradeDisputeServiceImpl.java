package com.fnusale.trade.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.dto.trade.TradeDisputeDTO;
import com.fnusale.common.entity.Order;
import com.fnusale.common.entity.TradeDispute;
import com.fnusale.common.enums.DisputeStatus;
import com.fnusale.common.enums.OrderStatus;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.product.ProductVO;
import com.fnusale.common.vo.trade.DisputeVO;
import com.fnusale.trade.client.ProductClient;
import com.fnusale.trade.mapper.OrderMapper;
import com.fnusale.trade.mapper.TradeDisputeMapper;
import com.fnusale.trade.service.TradeDisputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交易纠纷服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeDisputeServiceImpl implements TradeDisputeService {

    private final TradeDisputeMapper tradeDisputeMapper;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDispute(TradeDisputeDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验权限：买家或卖家都能发起纠纷
        boolean isBuyer = order.getUserId().equals(userId);
        Long sellerId = null;
        var productResult = productClient.getProductById(order.getProductId());
        if (productResult.isSuccess() && productResult.getData() != null) {
            sellerId = productResult.getData().getUserId();
        }
        boolean isSeller = sellerId != null && sellerId.equals(userId);

        if (!isBuyer && !isSeller) {
            throw new BusinessException("无权对该订单发起纠纷");
        }

        // 校验订单状态：待自提或已成交可发起
        if (!OrderStatus.WAIT_PICK.getCode().equals(order.getOrderStatus())
                && !OrderStatus.SUCCESS.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态无法发起纠纷");
        }

        // 检查是否已存在未解决纠纷
        if (tradeDisputeMapper.countUnresolvedByOrderId(dto.getOrderId()) > 0) {
            throw new BusinessException("该订单已存在未解决的纠纷");
        }

        // 创建纠纷
        TradeDispute dispute = new TradeDispute();
        dispute.setOrderId(dto.getOrderId());
        dispute.setInitiatorId(userId);
        dispute.setAccusedId(isBuyer ? sellerId : order.getUserId());
        dispute.setDisputeType(dto.getDisputeType());
        dispute.setEvidenceUrl(dto.getEvidenceUrl());
        dispute.setDisputeStatus(DisputeStatus.PENDING.getCode());
        dispute.setCreateTime(LocalDateTime.now());
        dispute.setUpdateTime(LocalDateTime.now());

        tradeDisputeMapper.insert(dispute);

        log.info("纠纷创建成功，disputeId: {}, orderId: {}", dispute.getId(), dto.getOrderId());
    }

    @Override
    public DisputeVO getDisputeById(Long disputeId) {
        Long userId = UserContext.getUserIdOrThrow();
        TradeDispute dispute = tradeDisputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }

        // 校验权限
        Order order = orderMapper.selectById(dispute.getOrderId());
        var productResult = productClient.getProductById(order.getProductId());
        Long sellerId = productResult.isSuccess() && productResult.getData() != null
                ? productResult.getData().getUserId() : null;

        if (!dispute.getInitiatorId().equals(userId)
                && !dispute.getAccusedId().equals(userId)
                && !order.getUserId().equals(userId)
                && !userId.equals(sellerId)) {
            throw new BusinessException("无权查看该纠纷");
        }

        return buildDisputeVO(dispute);
    }

    @Override
    public PageResult<DisputeVO> getMyDisputes(String status, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserIdOrThrow();
        Page<TradeDispute> page = new Page<>(pageNum, pageSize);
        IPage<TradeDispute> disputePage = tradeDisputeMapper.selectPageByUserId(page, userId, status);
        List<DisputeVO> voList = disputePage.getRecords().stream()
                .map(this::buildDisputeVO)
                .collect(Collectors.toList());
        return PageResult.of(pageNum, pageSize, disputePage.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDispute(Long disputeId) {
        Long userId = UserContext.getUserIdOrThrow();
        TradeDispute dispute = tradeDisputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }

        // 只有发起者能撤销
        if (!dispute.getInitiatorId().equals(userId)) {
            throw new BusinessException("无权撤销该纠纷");
        }

        // 只有待处理状态可撤销
        if (!DisputeStatus.PENDING.getCode().equals(dispute.getDisputeStatus())) {
            throw new BusinessException("当前纠纷状态无法撤销");
        }

        tradeDisputeMapper.deleteById(disputeId);

        log.info("纠纷撤销成功，disputeId: {}", disputeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEvidence(Long disputeId, String evidenceUrl) {
        Long userId = UserContext.getUserIdOrThrow();
        TradeDispute dispute = tradeDisputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }

        // 只有当事人能补充证据
        if (!dispute.getInitiatorId().equals(userId) && !dispute.getAccusedId().equals(userId)) {
            throw new BusinessException("无权补充证据");
        }

        // 已解决状态不可补充
        if (DisputeStatus.RESOLVED.getCode().equals(dispute.getDisputeStatus())) {
            throw new BusinessException("纠纷已解决，无法补充证据");
        }

        // 追加证据
        String currentEvidence = dispute.getEvidenceUrl();
        String newEvidence = currentEvidence != null && !currentEvidence.isEmpty()
                ? currentEvidence + "," + evidenceUrl
                : evidenceUrl;

        TradeDispute update = new TradeDispute();
        update.setId(disputeId);
        update.setEvidenceUrl(newEvidence);
        update.setUpdateTime(LocalDateTime.now());
        tradeDisputeMapper.updateById(update);

        log.info("纠纷证据补充成功，disputeId: {}", disputeId);
    }

    @Override
    public List<Map<String, Object>> getDisputeRecords(Long disputeId) {
        Long userId = UserContext.getUserIdOrThrow();
        TradeDispute dispute = tradeDisputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }

        // 校验权限
        if (!dispute.getInitiatorId().equals(userId) && !dispute.getAccusedId().equals(userId)) {
            throw new BusinessException("无权查看该纠纷处理记录");
        }

        // 构建纠纷处理记录
        List<Map<String, Object>> records = new ArrayList<>();

        // 1. 创建记录
        Map<String, Object> createRecord = new HashMap<>();
        createRecord.put("id", 1L);
        createRecord.put("disputeId", disputeId);
        createRecord.put("operateType", "CREATE");
        createRecord.put("operateContent", "用户发起纠纷，类型：" + getDisputeTypeDesc(dispute.getDisputeType()));
        createRecord.put("operateTime", dispute.getCreateTime());
        createRecord.put("operatorId", dispute.getInitiatorId());
        records.add(createRecord);

        // 2. 证据补充记录（如果有多次补充）
        if (dispute.getEvidenceUrl() != null && !dispute.getEvidenceUrl().isEmpty()) {
            String[] evidences = dispute.getEvidenceUrl().split(",");
            if (evidences.length > 0) {
                Map<String, Object> evidenceRecord = new HashMap<>();
                evidenceRecord.put("id", 2L);
                evidenceRecord.put("disputeId", disputeId);
                evidenceRecord.put("operateType", "ADD_EVIDENCE");
                evidenceRecord.put("operateContent", "提交了" + evidences.length + "份举证材料");
                evidenceRecord.put("operateTime", dispute.getUpdateTime());
                evidenceRecord.put("operatorId", dispute.getInitiatorId());
                records.add(evidenceRecord);
            }
        }

        // 3. 处理中记录
        if (DisputeStatus.PROCESSING.getCode().equals(dispute.getDisputeStatus())
                || DisputeStatus.RESOLVED.getCode().equals(dispute.getDisputeStatus())) {
            Map<String, Object> processingRecord = new HashMap<>();
            processingRecord.put("id", 3L);
            processingRecord.put("disputeId", disputeId);
            processingRecord.put("operateType", "PROCESSING");
            processingRecord.put("operateContent", "管理员已受理，正在处理中");
            processingRecord.put("operateTime", dispute.getUpdateTime());
            processingRecord.put("operatorId", dispute.getAdminId());
            records.add(processingRecord);
        }

        // 4. 已解决记录
        if (DisputeStatus.RESOLVED.getCode().equals(dispute.getDisputeStatus())) {
            Map<String, Object> resolvedRecord = new HashMap<>();
            resolvedRecord.put("id", 4L);
            resolvedRecord.put("disputeId", disputeId);
            resolvedRecord.put("operateType", "RESOLVED");
            resolvedRecord.put("operateContent", "纠纷已解决" +
                    (dispute.getProcessResult() != null ? "，结果：" + dispute.getProcessResult() : ""));
            resolvedRecord.put("operateTime", dispute.getUpdateTime());
            resolvedRecord.put("operatorId", dispute.getAdminId());
            resolvedRecord.put("remark", dispute.getProcessRemark());
            records.add(resolvedRecord);
        }

        return records;
    }

    /**
     * 构建纠纷VO
     */
    private DisputeVO buildDisputeVO(TradeDispute dispute) {
        DisputeVO vo = new DisputeVO();
        BeanUtils.copyProperties(dispute, vo);

        // 获取订单信息
        Order order = orderMapper.selectById(dispute.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());

            // 获取商品信息
            var productResult = productClient.getProductById(order.getProductId());
            if (productResult.isSuccess() && productResult.getData() != null) {
                vo.setProductName(productResult.getData().getProductName());
            }
        }

        // 设置状态描述
        vo.setDisputeStatusDesc(getDisputeStatusDesc(dispute.getDisputeStatus()));
        vo.setDisputeTypeDesc(getDisputeTypeDesc(dispute.getDisputeType()));

        return vo;
    }

    private String getDisputeStatusDesc(String status) {
        if (DisputeStatus.PENDING.getCode().equals(status)) {
            return "待处理";
        } else if (DisputeStatus.PROCESSING.getCode().equals(status)) {
            return "处理中";
        } else if (DisputeStatus.RESOLVED.getCode().equals(status)) {
            return "已解决";
        }
        return status;
    }

    private String getDisputeTypeDesc(String type) {
        switch (type) {
            case "PRODUCT_NOT_MATCH":
                return "商品不符";
            case "NO_DELIVERY":
                return "未发货";
            case "PRODUCT_DAMAGED":
                return "商品损坏";
            case "OTHER":
                return "其他";
            default:
                return type;
        }
    }
}