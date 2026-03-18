package com.fnusale.user.service;

import com.fnusale.common.common.PageResult;
import com.fnusale.common.vo.user.MyRankingVO;
import com.fnusale.common.vo.user.RankingRewardVO;
import com.fnusale.common.vo.user.RankingUserVO;

import java.util.List;

/**
 * 排行榜服务接口
 */
public interface RankingService {

    /**
     * 获取活跃度排行榜
     * @param type 排行类型：daily-日榜，weekly-周榜，monthly-月榜
     * @param date 日期，格式：yyyy-MM-dd
     * @return 排行榜列表
     */
    List<RankingUserVO> getActivityRanking(String type, String date);

    /**
     * 获取交易排行榜
     * @param type 排行类型：daily-日榜，weekly-周榜，monthly-月榜
     * @param date 日期，格式：yyyy-MM-dd
     * @return 排行榜列表
     */
    List<RankingUserVO> getTradeRanking(String type, String date);

    /**
     * 获取信誉排行榜
     * @return 排行榜列表
     */
    List<RankingUserVO> getCreditRanking();

    /**
     * 获取好评排行榜
     * @return 排行榜列表
     */
    List<RankingUserVO> getRatingRanking();

    /**
     * 获取我的排名
     * @param userId 用户ID
     * @return 我的排名信息
     */
    MyRankingVO getMyRanking(Long userId);

    /**
     * 获取排行榜历史
     * @param userId 用户ID
     * @param rankType 排行类型
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 排行历史
     */
    PageResult<RankingUserVO> getRankingHistory(Long userId, String rankType, Integer pageNum, Integer pageSize);

    /**
     * 领取排行奖励
     * @param userId 用户ID
     * @param rewardId 奖励ID
     */
    void claimReward(Long userId, Long rewardId);

    /**
     * 获取我的奖励列表
     * @param userId 用户ID
     * @param isClaimed 是否已领取
     * @return 奖励列表
     */
    List<RankingRewardVO> getMyRewards(Long userId, Boolean isClaimed);
}