package com.fnusale.marketing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.constant.MarketingConstants;
import com.fnusale.common.dto.marketing.CouponDTO;
import com.fnusale.common.entity.Coupon;
import com.fnusale.common.entity.UserCoupon;
import com.fnusale.common.event.CouponGrantEvent;
import com.fnusale.common.event.CouponReceiveEvent;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.vo.marketing.CouponVO;
import com.fnusale.common.vo.marketing.UserCouponVO;
import com.fnusale.marketing.mapper.CouponMapper;
import com.fnusale.marketing.mapper.UserCouponMapper;
import com.fnusale.marketing.service.CouponService;
import com.fnusale.marketing.service.MarketingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final MarketingEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    /**
     * 优惠券库存 Redis Key 前缀
     */
    private static final String COUPON_STOCK_KEY_PREFIX = "coupon:stock:";
    private static final String COUPON_RECEIVED_KEY_PREFIX = "coupon:received:";

    @Override
    public List<CouponVO> getAvailableCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> coupons = couponMapper.selectAvailableCoupons(now);

        List<CouponVO> voList = new ArrayList<>();
        for (Coupon coupon : coupons) {
            CouponVO vo = convertToVO(coupon);
            // 检查用户是否已领取
            if (userId != null) {
                int count = userCouponMapper.countByUserAndCoupon(userId, coupon.getId());
                vo.setReceived(count > 0);
            } else {
                vo.setReceived(false);
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public void receiveCoupon(Long userId, Long couponId) {
        // 检查优惠券是否存在
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(4001, "优惠券不存在");
        }

        // 检查优惠券是否启用
        if (coupon.getEnableStatus() != 1) {
            throw new BusinessException(4006, "优惠券已禁用");
        }

        // 检查优惠券是否在有效期内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime())) {
            throw new BusinessException(4003, "优惠券尚未开始");
        }
        if (now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(4003, "优惠券已过期");
        }

        // 检查是否已领取（先查 Redis，再查 DB）
        String receivedKey = COUPON_RECEIVED_KEY_PREFIX + couponId;
        Boolean hasReceived = redisTemplate.opsForSet().isMember(receivedKey, userId.toString());
        if (Boolean.TRUE.equals(hasReceived)) {
            throw new BusinessException(4005, "您已领取过该优惠券");
        }

        // 双重检查：查 DB 确保未领取
        int count = userCouponMapper.countByUserAndCoupon(userId, couponId);
        if (count > 0) {
            // 同步到 Redis
            redisTemplate.opsForSet().add(receivedKey, userId.toString());
            throw new BusinessException(4005, "您已领取过该优惠券");
        }

        // ========== Redis 预扣库存 ==========
        String stockKey = COUPON_STOCK_KEY_PREFIX + couponId;
        String stockStr = redisTemplate.opsForValue().get(stockKey);

        // 如果 Redis 中没有库存缓存，从 DB 加载
        if (stockStr == null) {
            int remainCount = coupon.getTotalCount() - coupon.getReceivedCount();
            redisTemplate.opsForValue().set(stockKey, String.valueOf(remainCount));
            stockStr = String.valueOf(remainCount);
        }

        // 检查库存
        long remainStock = Long.parseLong(stockStr);
        if (remainStock <= 0) {
            throw new BusinessException(4002, "优惠券已领完");
        }

        // Redis 原子扣减库存
        Long newStock = redisTemplate.opsForValue().decrement(stockKey);
        if (newStock == null || newStock < 0) {
            // 库存不足，回滚
            redisTemplate.opsForValue().increment(stockKey);
            throw new BusinessException(4002, "优惠券已领完");
        }

        // 标记用户已领取（防止重复领取）
        redisTemplate.opsForSet().add(receivedKey, userId.toString());

        // ========== 发送 MQ 消息异步写入数据库 ==========
        String eventId = UUID.randomUUID().toString();
        CouponReceiveEvent event = CouponReceiveEvent.builder()
                .couponId(couponId)
                .userId(userId)
                .expireTime(coupon.getEndTime())
                .eventId(eventId)
                .receiveTime(now)
                .build();

        eventPublisher.publishCouponReceiveEvent(event);

        log.info("用户 {} 领取优惠券 {} 成功（异步入库中），eventId: {}", userId, couponId, eventId);
    }

    @Override
    public List<UserCouponVO> getMyCoupons(Long userId, String status) {
        List<UserCoupon> userCoupons = userCouponMapper.selectByUserId(userId, status);
        List<UserCouponVO> voList = new ArrayList<>();
        for (UserCoupon uc : userCoupons) {
            UserCouponVO vo = convertToUserCouponVO(uc);
            // 检查是否可用
            vo.setUsable(MarketingConstants.COUPON_STATUS_UNUSED.equals(uc.getCouponStatus())
                    && uc.getExpireTime().isAfter(LocalDateTime.now()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<UserCouponVO> getUsableCoupons(Long userId, Long productId, BigDecimal price) {
        List<UserCoupon> userCoupons = userCouponMapper.selectUsableCoupons(userId, productId, price);
        List<UserCouponVO> voList = new ArrayList<>();
        for (UserCoupon uc : userCoupons) {
            voList.add(convertToUserCouponVO(uc));
        }
        return voList;
    }

    @Override
    public CouponVO getCouponDetail(Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(4001, "优惠券不存在");
        }
        return convertToVO(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCoupon(CouponDTO dto) {
        // 校验优惠券类型
        validateCouponDTO(dto);

        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        coupon.setReceivedCount(0);
        coupon.setUsedCount(0);
        coupon.setCreateTime(LocalDateTime.now());

        couponMapper.insert(coupon);
        log.info("创建优惠券 {} 成功", coupon.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoupon(Long couponId, CouponDTO dto) {
        Coupon existing = couponMapper.selectById(couponId);
        if (existing == null) {
            throw new BusinessException(4001, "优惠券不存在");
        }

        // 校验优惠券类型
        validateCouponDTO(dto);

        BeanUtils.copyProperties(dto, existing);
        existing.setId(couponId);
        couponMapper.updateById(existing);
        log.info("更新优惠券 {} 成功", couponId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(4001, "优惠券不存在");
        }

        couponMapper.deleteById(couponId);
        log.info("删除优惠券 {} 成功", couponId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCouponStatus(Long couponId, Integer status) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(4001, "优惠券不存在");
        }

        coupon.setEnableStatus(status);
        couponMapper.updateById(coupon);
        log.info("更新优惠券 {} 状态为 {}", couponId, status);
    }

    @Override
    public IPage<CouponVO> getCouponPage(String name, String type, Integer status, Integer pageNum, Integer pageSize) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        IPage<Coupon> couponPage = couponMapper.selectCouponPage(page, name, type, status);

        return couponPage.convert(this::convertToVO);
    }

    @Override
    public void grantCoupon(Long couponId, List<Long> userIds) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(4001, "优惠券不存在");
        }

        if (coupon.getEnableStatus() != 1) {
            throw new BusinessException(4006, "优惠券已禁用");
        }

        // 生成批次ID
        String batchId = UUID.randomUUID().toString();

        // 构建批量消息
        List<CouponGrantEvent> events = new ArrayList<>();
        for (Long userId : userIds) {
            CouponGrantEvent event = CouponGrantEvent.builder()
                    .couponId(couponId)
                    .userId(userId)
                    .batchId(batchId)
                    .expireTime(coupon.getEndTime())
                    .eventId(UUID.randomUUID().toString())
                    .grantTime(LocalDateTime.now())
                    .build();
            events.add(event);
        }

        // 批量发送 MQ 消息
        eventPublisher.publishCouponGrantBatch(events);

        log.info("优惠券发放任务已提交, couponId: {}, 批次ID: {}, 用户数: {}", couponId, batchId, userIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userCouponId, Long orderId, Long userId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new BusinessException(4001, "优惠券不存在");
        }

        // 检查是否属于当前用户
        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException(4001, "优惠券不存在");
        }

        // 检查优惠券状态
        if (!MarketingConstants.COUPON_STATUS_UNUSED.equals(userCoupon.getCouponStatus())) {
            throw new BusinessException(4004, "优惠券不可用");
        }

        // 检查是否过期
        if (userCoupon.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(4003, "优惠券已过期");
        }

        // 更新优惠券状态
        userCoupon.setCouponStatus(MarketingConstants.COUPON_STATUS_USED);
        userCoupon.setUseTime(LocalDateTime.now());
        userCoupon.setOrderId(orderId);
        userCouponMapper.updateById(userCoupon);

        // 增加已使用数量
        couponMapper.incrementUsedCount(userCoupon.getCouponId());

        log.info("用户 {} 使用优惠券 {} 成功，订单ID: {}", userId, userCouponId, orderId);
    }

    /**
     * 校验优惠券DTO
     */
    private void validateCouponDTO(CouponDTO dto) {
        // 满减券必须配置满减门槛
        if ("FULL_REDUCE".equals(dto.getCouponType()) && dto.getFullAmount() == null) {
            throw new BusinessException(400, "满减券必须配置满减门槛金额");
        }

        // 品类券必须关联品类
        if ("CATEGORY".equals(dto.getCouponType()) && dto.getCategoryId() == null) {
            throw new BusinessException(400, "品类券必须关联商品品类");
        }

        // 有效期不能超过90天
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            long days = java.time.Duration.between(dto.getStartTime(), dto.getEndTime()).toDays();
            if (days > 90) {
                throw new BusinessException(400, "有效期不能超过90天");
            }
        }

        // 发放总数限制
        if (dto.getTotalCount() != null && dto.getTotalCount() > 10000) {
            throw new BusinessException(400, "发放总数最大不超过10000张");
        }
    }

    /**
     * 转换为VO
     */
    private CouponVO convertToVO(Coupon coupon) {
        CouponVO vo = new CouponVO();
        BeanUtils.copyProperties(coupon, vo);
        vo.setRemainCount(coupon.getTotalCount() - coupon.getReceivedCount());
        return vo;
    }

    /**
     * 转换为用户优惠券VO
     * 优化：优先使用JOIN查询返回的优惠券信息，避免N+1问题
     */
    private UserCouponVO convertToUserCouponVO(UserCoupon userCoupon) {
        UserCouponVO.UserCouponVOBuilder builder = UserCouponVO.builder()
                .id(userCoupon.getId())
                .couponId(userCoupon.getCouponId())
                .couponStatus(userCoupon.getCouponStatus())
                .receiveTime(userCoupon.getReceiveTime())
                .useTime(userCoupon.getUseTime())
                .expireTime(userCoupon.getExpireTime())
                .orderId(userCoupon.getOrderId());

        // 优先使用JOIN查询返回的优惠券信息（避免N+1问题）
        if (userCoupon.getCouponName() != null) {
            builder.couponName(userCoupon.getCouponName())
                    .couponType(userCoupon.getCouponType())
                    .fullAmount(userCoupon.getFullAmount())
                    .reduceAmount(userCoupon.getReduceAmount())
                    .categoryId(userCoupon.getCategoryId());
        } else {
            // 仅在JOIN未返回信息时单独查询（兜底）
            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            if (coupon != null) {
                builder.couponName(coupon.getCouponName())
                        .couponType(coupon.getCouponType())
                        .fullAmount(coupon.getFullAmount())
                        .reduceAmount(coupon.getReduceAmount())
                        .categoryId(coupon.getCategoryId());
            }
        }

        return builder.build();
    }

    /**
     * 创建用户优惠券记录
     */
    private UserCoupon createUserCoupon(Long userId, Long couponId, LocalDateTime expireTime, LocalDateTime receiveTime) {
        return UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .receiveTime(receiveTime)
                .expireTime(expireTime)
                .couponStatus(MarketingConstants.COUPON_STATUS_UNUSED)
                .createTime(receiveTime)
                .build();
    }
}