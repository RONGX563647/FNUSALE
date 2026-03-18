package com.fnusale.admin.controller;

import com.fnusale.admin.service.AdminDisputeService;
import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.dto.admin.DisputeProcessDTO;
import com.fnusale.common.vo.admin.DisputeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 纠纷处理控制器
 */
@Tag(name = "纠纷处理", description = "交易纠纷处理接口（管理员）")
@RestController
@RequestMapping("/admin/dispute")
@RequiredArgsConstructor
public class AdminDisputeController {

    private final AdminDisputeService adminDisputeService;

    @Operation(summary = "获取纠纷列表", description = "分页获取纠纷列表")
    @GetMapping("/page")
    public Result<PageResult<DisputeVO>> getDisputePage(
            @Parameter(description = "纠纷状态") @RequestParam(required = false) String disputeStatus,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminDisputeService.getDisputePage(disputeStatus, pageNum, pageSize));
    }

    @Operation(summary = "获取纠纷详情", description = "获取纠纷详细信息")
    @GetMapping("/{disputeId}")
    public Result<DisputeVO> getDisputeDetail(
            @Parameter(description = "纠纷ID") @PathVariable Long disputeId) {
        return Result.success(adminDisputeService.getDisputeDetail(disputeId));
    }

    @Operation(summary = "处理纠纷", description = "处理交易纠纷")
    @PutMapping("/{disputeId}/process")
    public Result<Void> processDispute(
            @Parameter(description = "纠纷ID") @PathVariable Long disputeId,
            @Valid @RequestBody DisputeProcessDTO dto) {
        Long adminId = 1L;
        adminDisputeService.processDispute(disputeId, dto, adminId);
        return Result.success();
    }
}