package com.fnusale.product.service;

/**
 * 用户行为服务接口
 */
public interface UserBehaviorService {

    /**
     * 收藏商品
     *
     * @param productId 商品ID
     */
    void addFavorite(Long productId);

    /**
     * 取消收藏
     *
     * @param productId 商品ID
     */
    void removeFavorite(Long productId);

    /**
     * 点赞商品
     *
     * @param productId 商品ID
     */
    void addLike(Long productId);

    /**
     * 取消点赞
     *
     * @param productId 商品ID
     */
    void removeLike(Long productId);

    /**
     * 记录浏览行为
     *
     * @param productId 商品ID
     */
    void recordBrowse(Long productId);

    /**
     * 检查用户是否收藏了商品
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return true-已收藏，false-未收藏
     */
    boolean isFavorited(Long userId, Long productId);

    /**
     * 检查用户是否点赞了商品
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return true-已点赞，false-未点赞
     */
    boolean isLiked(Long userId, Long productId);
}