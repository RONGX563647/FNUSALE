package com.fnusale.marketing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fnusale.common.dto.marketing.CouponDTO;
import com.fnusale.common.vo.marketing.CouponVO;
import com.fnusale.common.vo.marketing.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口
 */
public interface CouponService {

    /**
     * 获取可领取的优惠券列表
     *
     * @param userId 当前用户ID（可为null）
     * @return 优惠券列表
     */
    List<CouponVO> getAvailableCoupons(Long userId);

    /**
     * 领取优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     */
    void receiveCoupon(Long userId, Long couponId);

    /**
     * 获取我的优惠券列表
     *
     * @param userId 用户ID
     * @param status 状态筛选
     * @return 优惠券列表
     */
    List<UserCouponVO> getMyCoupons(Long userId, String status);

    /**
     * 获取可用优惠券
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param price     商品价格
     * @return 可用优惠券列表
     */
    List<UserCouponVO> getUsableCoupons(Long userId, Long productId, BigDecimal price);

    /**
     * 获取优惠券详情
     *
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    CouponVO getCouponDetail(Long couponId);

    /**
     * 创建优惠券
     *
     * @param dto 优惠券DTO
     */
    void createCoupon(CouponDTO dto);

    /**
     * 更新优惠券
     *
     * @param couponId 优惠券ID
     * @param dto      优惠券DTO
     */
    void updateCoupon(Long couponId, CouponDTO dto);

    /**
     * 删除优惠券
     *
     * @param couponId 优惠券ID
     */
    void deleteCoupon(Long couponId);

    /**
     * 更新优惠券状态
     *
     * @param couponId 优惠券ID
     * @param status   状态
     */
    void updateCouponStatus(Long couponId, Integer status);

    /**
     * 分页查询优惠券
     *
     * @param name     名称
     * @param type     类型
     * @param status   状态
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<CouponVO> getCouponPage(String name, String type, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 发放优惠券
     *
     * @param couponId 优惠券ID
     * @param userIds  用户ID列表
     */
    void grantCoupon(Long couponId, List<Long> userIds);

    /**
     * 核销优惠券
     *
     * @param userCouponId 用户优惠券ID
     * @param orderId      订单ID
     * @param userId       用户ID
     */
    void useCoupon(Long userCouponId, Long orderId, Long userId);
}