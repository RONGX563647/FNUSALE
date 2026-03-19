package com.fnusale.user.controller;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.common.Result;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.user.MyRankingVO;
import com.fnusale.common.vo.user.RankingRewardVO;
import com.fnusale.common.vo.user.RankingUserVO;
import com.fnusale.user.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排行榜控制器
 * 提供活跃度排行、交易排行、信誉排行等接口
 */
@Tag(name = "排行榜管理", description = "活跃度排行、交易排行、信誉排行等接口")
@RestController
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "活跃度排行榜", description = "获取平台活跃用户排行，基于签到、浏览、发布、交易等行为计算")
    @GetMapping("/activity")
    public Result<List<RankingUserVO>> getActivityRanking(
            @Parameter(description = "排行类型：daily-日榜，weekly-周榜，monthly-月榜") @RequestParam(defaultValue = "daily") String type,
            @Parameter(description = "日期，格式：yyyy-MM-dd") @RequestParam(required = false) String date) {
        List<RankingUserVO> list = rankingService.getActivityRanking(type, date);
        return Result.success(list);
    }

    @Operation(summary = "交易排行榜", description = "获取交易量/交易额排行")
    @GetMapping("/trade")
    public Result<List<RankingUserVO>> getTradeRanking(
            @Parameter(description = "排行类型：daily-日榜，weekly-周榜，monthly-月榜") @RequestParam(defaultValue = "daily") String type,
            @Parameter(description = "日期，格式：yyyy-MM-dd") @RequestParam(required = false) String date) {
        List<RankingUserVO> list = rankingService.getTradeRanking(type, date);
        return Result.success(list);
    }

    @Operation(summary = "信誉排行榜", description = "获取信誉分排行，综合信誉分和评价分计算")
    @GetMapping("/credit")
    public Result<List<RankingUserVO>> getCreditRanking() {
        List<RankingUserVO> list = rankingService.getCreditRanking();
        return Result.success(list);
    }

    @Operation(summary = "好评排行榜", description = "获取好评率排行")
    @GetMapping("/rating")
    public Result<List<RankingUserVO>> getRatingRanking() {
        List<RankingUserVO> list = rankingService.getRatingRanking();
        return Result.success(list);
    }

    @Operation(summary = "我的排名", description = "获取当前用户在各榜单的排名和得分")
    @GetMapping("/my")
    public Result<MyRankingVO> getMyRanking() {
        Long userId = UserContext.getCurrentUserId();
        MyRankingVO vo = rankingService.getMyRanking(userId);
        return Result.success(vo);
    }

    @Operation(summary = "排行榜历史", description = "获取历史排行记录")
    @GetMapping("/history")
    public Result<PageResult<RankingUserVO>> getRankingHistory(
            @Parameter(description = "排行类型：ACTIVITY-活跃度，TRADE-交易，CREDIT-信誉，RATING-好评", required = true) @RequestParam String rankType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        PageResult<RankingUserVO> result = rankingService.getRankingHistory(userId, rankType, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "领取排行奖励", description = "领取排行榜奖励积分或优惠券")
    @PostMapping("/reward/{id}")
    public Result<Void> claimReward(
            @Parameter(description = "奖励ID", required = true) @PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        rankingService.claimReward(userId, id);
        return Result.success("领取成功", null);
    }

    @Operation(summary = "我的奖励列表", description = "获取当前用户的排行榜奖励列表")
    @GetMapping("/rewards")
    public Result<List<RankingRewardVO>> getMyRewards(
            @Parameter(description = "是否已领取") @RequestParam(required = false) Boolean isClaimed) {
        Long userId = UserContext.getCurrentUserId();
        List<RankingRewardVO> list = rankingService.getMyRewards(userId, isClaimed);
        return Result.success(list);
    }
}