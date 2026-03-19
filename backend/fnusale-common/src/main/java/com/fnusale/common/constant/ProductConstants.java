package com.fnusale.common.constant;

import java.math.BigDecimal;

/**
 * 商品相关常量
 */
public class ProductConstants {

    private ProductConstants() {
    }

    // ==================== 图片相关 ====================

    /**
     * 最少图片数量
     */
    public static final int MIN_IMAGE_COUNT = 1;

    /**
     * 最多图片数量
     */
    public static final int MAX_IMAGE_COUNT = 9;

    // ==================== 信誉分相关 ====================

    /**
     * 发布商品最低信誉分
     */
    public static final int MIN_CREDIT_SCORE_TO_PUBLISH = 60;

    // ==================== 商品名称相关 ====================

    /**
     * 商品名称最小长度
     */
    public static final int PRODUCT_NAME_MIN_LENGTH = 2;

    /**
     * 商品名称最大长度
     */
    public static final int PRODUCT_NAME_MAX_LENGTH = 100;

    /**
     * 商品描述最大长度
     */
    public static final int PRODUCT_DESC_MAX_LENGTH = 500;

    // ==================== 价格相关 ====================

    /**
     * 商品最低价格（元）
     */
    public static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    /**
     * 商品最高价格（元）
     */
    public static final BigDecimal MAX_PRICE = new BigDecimal("999999.99");

    // ==================== 分页相关 ====================

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页数量
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页数量
     */
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== 缓存相关 ====================

    /**
     * 商品详情缓存前缀
     */
    public static final String PRODUCT_DETAIL_CACHE_PREFIX = "product:detail:";

    /**
     * 商品列表缓存前缀
     */
    public static final String PRODUCT_LIST_CACHE_PREFIX = "product:list:";

    /**
     * 品类树缓存
     */
    public static final String CATEGORY_TREE_CACHE = "product:category:tree";

    /**
     * 品类列表缓存
     */
    public static final String CATEGORY_LIST_CACHE = "product:category:list";

    /**
     * 热门品类缓存
     */
    public static final String HOT_CATEGORY_CACHE = "product:category:hot";

    /**
     * 用户收藏列表缓存前缀
     */
    public static final String USER_FAVORITE_CACHE_PREFIX = "user:favorite:";

    /**
     * 缓存过期时间（秒）- 30分钟
     */
    public static final long CACHE_EXPIRE_SECONDS = 1800;

    /**
     * 品类缓存过期时间（秒）- 1小时
     */
    public static final long CATEGORY_CACHE_EXPIRE_SECONDS = 3600;
}