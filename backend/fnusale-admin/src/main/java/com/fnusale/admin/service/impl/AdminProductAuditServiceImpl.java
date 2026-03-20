package com.fnusale.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.admin.mapper.ProductAuditMapper;
import com.fnusale.admin.mapper.ProductMapper;
import com.fnusale.admin.mapper.UserMapper;
import com.fnusale.admin.service.AdminEventPublisher;
import com.fnusale.admin.service.AdminProductAuditService;
import com.fnusale.admin.service.SystemLogService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.entity.Product;
import com.fnusale.common.entity.ProductAudit;
import com.fnusale.common.entity.User;
import com.fnusale.common.enums.AuditResult;
import com.fnusale.common.enums.LogModule;
import com.fnusale.common.enums.OperateType;
import com.fnusale.common.enums.ProductStatus;
import com.fnusale.common.event.ProductAuditEvent;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.admin.AuditRecordVO;
import com.fnusale.common.vo.admin.AuditStatisticsVO;
import com.fnusale.common.vo.admin.PendingProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 商品审核服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductAuditServiceImpl implements AdminProductAuditService {

    private final ProductMapper productMapper;
    private final ProductAuditMapper productAuditMapper;
    private final UserMapper userMapper;
    private final SystemLogService systemLogService;
    private final AdminEventPublisher eventPublisher;

    @Override
    public PageResult<PendingProductVO> getPendingList(Integer pageNum, Integer pageSize) {
        Page<PendingProductVO> page = new Page<>(pageNum, pageSize);
        Page<PendingProductVO> result = productMapper.selectPendingProducts(page);
        return new PageResult<>(pageNum, pageSize, result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(Long productId, Long adminId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!ProductStatus.DRAFT.getCode().equals(product.getProductStatus())) {
            throw new BusinessException("商品状态不允许审核");
        }

        // 更新商品状态
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setProductStatus(ProductStatus.ON_SHELF.getCode());
        productMapper.updateById(updateProduct);

        // 记录审核记录
        ProductAudit audit = new ProductAudit();
        audit.setProductId(productId);
        audit.setAdminId(adminId);
        audit.setAuditResult(AuditResult.PASS.getCode());
        audit.setAuditTime(LocalDateTime.now());
        audit.setCreateTime(LocalDateTime.now());
        productAuditMapper.insert(audit);

        // 记录日志
        systemLogService.log(adminId, LogModule.PRODUCT.getCode(), OperateType.UPDATE.getCode(),
                "审核通过商品ID:" + productId, null, null);

        // 发布审核通知事件
        ProductAuditEvent event = ProductAuditEvent.builder()
                .productId(productId)
                .productName(product.getProductName())
                .userId(product.getUserId())
                .auditResult("PASS")
                .auditTime(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();
        eventPublisher.publishProductAuditEvent(event);

        log.info("商品审核通过, productId: {}, adminId: {}", productId, adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReject(Long productId, Long adminId, String reason) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!ProductStatus.DRAFT.getCode().equals(product.getProductStatus())) {
            throw new BusinessException("商品状态不允许审核");
        }

        // 更新商品状态
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setProductStatus(ProductStatus.OFF_SHELF.getCode());
        productMapper.updateById(updateProduct);

        // 记录审核记录
        ProductAudit audit = new ProductAudit();
        audit.setProductId(productId);
        audit.setAdminId(adminId);
        audit.setAuditResult(AuditResult.REJECT.getCode());
        audit.setRejectReason(reason);
        audit.setAuditTime(LocalDateTime.now());
        audit.setCreateTime(LocalDateTime.now());
        productAuditMapper.insert(audit);

        // 记录日志
        systemLogService.log(adminId, LogModule.PRODUCT.getCode(), OperateType.UPDATE.getCode(),
                "审核驳回商品ID:" + productId + ", 原因:" + reason, null, null);

        // 发布审核通知事件
        ProductAuditEvent event = ProductAuditEvent.builder()
                .productId(productId)
                .productName(product.getProductName())
                .userId(product.getUserId())
                .auditResult("REJECT")
                .rejectReason(reason)
                .auditTime(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .build();
        eventPublisher.publishProductAuditEvent(event);

        log.info("商品审核驳回, productId: {}, adminId: {}, reason: {}", productId, adminId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchAuditPass(List<Long> productIds, Long adminId) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        if (productIds.size() > 50) {
            throw new BusinessException("一次最多批量审核50个商品");
        }

        int successCount = 0;
        for (Long productId : productIds) {
            try {
                auditPass(productId, adminId);
                successCount++;
            } catch (Exception e) {
                log.warn("批量审核失败, productId: {}", productId, e);
            }
        }

        log.info("批量审核完成, total: {}, success: {}", productIds.size(), successCount);
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceOffShelf(Long productId, Long adminId, String reason) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 更新商品状态
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setProductStatus(ProductStatus.ILLEGAL.getCode());
        updateProduct.setIllegalReason(reason);
        productMapper.updateById(updateProduct);

        // 扣减发布者信誉分
        Long userId = product.getUserId();
        User user = userMapper.selectById(userId);
        if (user != null) {
            int newScore = Math.max(0, user.getCreditScore() - 10);
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setCreditScore(newScore);
            userMapper.updateById(updateUser);
        }

        // 记录审核记录
        ProductAudit audit = new ProductAudit();
        audit.setProductId(productId);
        audit.setAdminId(adminId);
        audit.setAuditResult(AuditResult.REJECT.getCode());
        audit.setRejectReason("强制下架:" + reason);
        audit.setAuditTime(LocalDateTime.now());
        audit.setCreateTime(LocalDateTime.now());
        productAuditMapper.insert(audit);

        // 记录日志
        systemLogService.log(adminId, LogModule.PRODUCT.getCode(), OperateType.UPDATE.getCode(),
                "强制下架商品ID:" + productId + ", 原因:" + reason, null, null);

        log.info("强制下架商品, productId: {}, adminId: {}, reason: {}", productId, adminId, reason);
    }

    @Override
    public List<AuditRecordVO> getAuditRecords(Long productId) {
        LambdaQueryWrapper<ProductAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductAudit::getProductId, productId)
                .orderByDesc(ProductAudit::getAuditTime);
        List<ProductAudit> records = productAuditMapper.selectList(wrapper);

        return records.stream().map(audit -> {
            AuditRecordVO vo = new AuditRecordVO();
            vo.setId(audit.getId());
            vo.setProductId(audit.getProductId());
            vo.setAdminId(audit.getAdminId());
            vo.setAuditResult(audit.getAuditResult());
            vo.setRejectReason(audit.getRejectReason());
            vo.setAuditTime(audit.getAuditTime());

            // 获取管理员名称
            User admin = userMapper.selectById(audit.getAdminId());
            if (admin != null) {
                vo.setAdminName(admin.getUsername());
            }
            return vo;
        }).toList();
    }

    @Override
    public AuditStatisticsVO getAuditStatistics() {
        AuditStatisticsVO vo = new AuditStatisticsVO();
        LocalDate today = LocalDate.now();

        vo.setPendingCount(productMapper.countPendingAudit());
        vo.setTodayPassCount(productAuditMapper.countTodayPass(today));
        vo.setTodayRejectCount(productAuditMapper.countTodayReject(today));

        int total = vo.getTodayPassCount() + vo.getTodayRejectCount();
        if (total > 0) {
            vo.setTodayPassRate((double) vo.getTodayPassCount() / total * 100);
        } else {
            vo.setTodayPassRate(0.0);
        }

        return vo;
    }
}