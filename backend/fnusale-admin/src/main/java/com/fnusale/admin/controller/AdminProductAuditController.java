package com.fnusale.admin.controller;

import com.fnusale.admin.service.AdminProductAuditService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.admin.BatchAuditDTO;
import com.fnusale.common.vo.admin.AuditRecordVO;
import com.fnusale.common.vo.admin.AuditStatisticsVO;
import com.fnusale.common.vo.admin.PendingProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品审核控制器
 */
@Tag(name = "商品审核管理", description = "商品审核、违规处理等接口（管理员）")
@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AdminProductAuditController {

    private final AdminProductAuditService adminProductAuditService;

    @Operation(summary = "获取待审核商品列表", description = "分页获取待审核商品列表")
    @GetMapping("/pending")
    public Result<PageResult<PendingProductVO>> getPendingList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminProductAuditService.getPendingList(pageNum, pageSize));
    }

    @Operation(summary = "审核通过", description = "审核通过商品")
    @PutMapping("/{productId}/pass")
    public Result<Void> auditPass(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        // TODO: 从上下文获取管理员ID
        Long adminId = 1L;
        adminProductAuditService.auditPass(productId, adminId);
        return Result.success();
    }

    @Operation(summary = "审核驳回", description = "驳回商品并填写原因")
    @PutMapping("/{productId}/reject")
    public Result<Void> auditReject(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(description = "驳回原因") @RequestParam String reason) {
        Long adminId = 1L;
        adminProductAuditService.auditReject(productId, adminId, reason);
        return Result.success();
    }

    @Operation(summary = "批量审核通过", description = "批量审核通过商品")
    @PutMapping("/batch/pass")
    public Result<Map<String, Integer>> batchAuditPass(
            @Parameter(description = "商品ID列表") @Valid @RequestBody BatchAuditDTO dto) {
        Long adminId = 1L;
        Integer successCount = adminProductAuditService.batchAuditPass(dto.getProductIds(), adminId);
        Map<String, Integer> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", dto.getProductIds().size() - successCount);
        return Result.success(result);
    }

    @Operation(summary = "获取审核记录", description = "获取商品审核记录")
    @GetMapping("/{productId}/records")
    public Result<List<AuditRecordVO>> getAuditRecords(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(adminProductAuditService.getAuditRecords(productId));
    }

    @Operation(summary = "强制下架", description = "强制下架违规商品")
    @PutMapping("/{productId}/force-off")
    public Result<Void> forceOffShelf(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(description = "下架原因") @RequestParam String reason) {
        Long adminId = 1L;
        adminProductAuditService.forceOffShelf(productId, adminId, reason);
        return Result.success();
    }

    @Operation(summary = "获取审核统计", description = "获取审核统计数据")
    @GetMapping("/statistics")
    public Result<AuditStatisticsVO> getAuditStatistics() {
        return Result.success(adminProductAuditService.getAuditStatistics());
    }
}