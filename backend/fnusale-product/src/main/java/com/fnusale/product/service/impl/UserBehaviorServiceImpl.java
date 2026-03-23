package com.fnusale.product.service.impl;

import com.fnusale.common.entity.Product;
import com.fnusale.common.entity.UserBehavior;
import com.fnusale.common.enums.BehaviorType;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.product.mapper.ProductMapper;
import com.fnusale.product.mapper.UserBehaviorMapper;
import com.fnusale.product.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户行为服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorServiceImpl implements UserBehaviorService {

    private final UserBehaviorMapper userBehaviorMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long productId) {
        Long userId = UserContext.getUserIdOrThrow();

        // 校验商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否已收藏
        if (userBehaviorMapper.checkFavorite(userId, productId) > 0) {
            throw new BusinessException("已收藏该商品");
        }

        // 添加收藏记录
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(BehaviorType.COLLECT.getCode());
        behavior.setCreateTime(LocalDateTime.now());

        userBehaviorMapper.insert(behavior);

        log.info("用户收藏商品成功，userId: {}, productId: {}", userId, productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long productId) {
        Long userId = UserContext.getUserIdOrThrow();

        // 删除收藏记录
        UserBehavior behavior = userBehaviorMapper.selectByUserProductBehavior(
                userId, productId, BehaviorType.COLLECT.getCode());

        if (behavior == null) {
            throw new BusinessException("未收藏该商品");
        }

        userBehaviorMapper.deleteById(behavior.getId());

        log.info("用户取消收藏商品成功，userId: {}, productId: {}", userId, productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addLike(Long productId) {
        Long userId = UserContext.getUserIdOrThrow();

        // 校验商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否已点赞
        if (userBehaviorMapper.checkLike(userId, productId) > 0) {
            throw new BusinessException("已点赞该商品");
        }

        // 添加点赞记录
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(BehaviorType.LIKE.getCode());
        behavior.setCreateTime(LocalDateTime.now());

        userBehaviorMapper.insert(behavior);

        log.info("用户点赞商品成功，userId: {}, productId: {}", userId, productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeLike(Long productId) {
        Long userId = UserContext.getUserIdOrThrow();

        // 删除点赞记录
        UserBehavior behavior = userBehaviorMapper.selectByUserProductBehavior(
                userId, productId, BehaviorType.LIKE.getCode());

        if (behavior == null) {
            throw new BusinessException("未点赞该商品");
        }

        userBehaviorMapper.deleteById(behavior.getId());

        log.info("用户取消点赞商品成功，userId: {}, productId: {}", userId, productId);
    }

    @Override
    public void recordBrowse(Long productId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            // 未登录用户不记录浏览行为
            return;
        }

        // 校验商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return;
        }

        // 添加浏览记录
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(BehaviorType.BROWSE.getCode());
        behavior.setCreateTime(LocalDateTime.now());

        userBehaviorMapper.insert(behavior);

        log.debug("用户浏览商品记录，userId: {}, productId: {}", userId, productId);
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return userBehaviorMapper.checkFavorite(userId, productId) > 0;
    }

    @Override
    public boolean isLiked(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return userBehaviorMapper.checkLike(userId, productId) > 0;
    }
}