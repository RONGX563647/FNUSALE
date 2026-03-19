package com.fnusale.common.constant;

/**
 * Redis Key 常量
 */
public class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    // ==================== 签到相关 ====================

    /**
     * 用户签到 Bitmap
     * 格式: sign:{userId}:{yyyyMM}
     */
    public static final String SIGN_BITMAP_KEY = "sign:%d:%s";

    /**
     * 全局签到统计 Bitmap
     * 格式: sign:stat:{yyyyMM}
     */
    public static final String SIGN_STAT_KEY = "sign:stat:%s";

    /**
     * 签到排行榜 (Sorted Set)
     * 格式: sign:rank:{yyyyMM}
     */
    public static final String SIGN_RANK_KEY = "sign:rank:%s";

    /**
     * 用户连续签到天数缓存
     * 格式: sign:continuous:{userId}
     */
    public static final String SIGN_CONTINUOUS_KEY = "sign:continuous:%d";

    /**
     * 构建签到 Bitmap Key
     */
    public static String buildSignBitmapKey(Long userId, String yearMonth) {
        return String.format(SIGN_BITMAP_KEY, userId, yearMonth);
    }

    /**
     * 构建签到统计 Key
     */
    public static String buildSignStatKey(String yearMonth) {
        return String.format(SIGN_STAT_KEY, yearMonth);
    }

    /**
     * 构建签到排行 Key
     */
    public static String buildSignRankKey(String yearMonth) {
        return String.format(SIGN_RANK_KEY, yearMonth);
    }

    /**
     * 构建连续签到 Key
     */
    public static String buildSignContinuousKey(Long userId) {
        return String.format(SIGN_CONTINUOUS_KEY, userId);
    }

    // ==================== 用户注册相关 ====================

    /**
     * 用户注册分布式锁
     * 格式: register:lock:{phone} 或 register:lock:{email}
     */
    public static final String REGISTER_LOCK_KEY = "register:lock:%s";

    /**
     * 构建注册锁 Key
     */
    public static String buildRegisterLockKey(String account) {
        return String.format(REGISTER_LOCK_KEY, account);
    }

    // ==================== 校园自提点相关 ====================

    /**
     * 自提点 GEO 集合
     */
    public static final String PICK_POINT_GEO_KEY = "campus:pickpoint:geo";

    /**
     * 自提点详情 Hash
     * 格式: campus:pickpoint:{id}
     */
    public static final String PICK_POINT_DETAIL_KEY = "campus:pickpoint:%d";

    /**
     * 自提点列表缓存
     */
    public static final String PICK_POINT_LIST_KEY = "campus:pickpoint:list";

    /**
     * 构建自提点详情 Key
     */
    public static String buildPickPointDetailKey(Long id) {
        return String.format(PICK_POINT_DETAIL_KEY, id);
    }
}