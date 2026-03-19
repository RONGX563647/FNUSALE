package com.fnusale.common.constant;

/**
 * 交易模块常量
 */
public class TradeConstants {

    private TradeConstants() {
    }

    // ==================== 订单相关 ====================

    /**
     * 订单编号前缀
     */
    public static final String ORDER_NO_PREFIX = "XS";

    /**
     * 订单编号Key前缀（Redis）
     */
    public static final String ORDER_NO_KEY_PREFIX = "order:no:";

    /**
     * 未支付订单超时时间（小时）
     */
    public static final int UNPAID_TIMEOUT_HOURS = 24;

    /**
     * 订单创建幂等Key前缀
     */
    public static final String ORDER_CREATE_KEY_PREFIX = "order:create:";

    /**
     * 订单详情缓存Key前缀
     */
    public static final String ORDER_DETAIL_KEY_PREFIX = "order:detail:";

    // ==================== 支付相关 ====================

    /**
     * 支付方式 - 微信
     */
    public static final String PAY_TYPE_WECHAT = "WECHAT";

    /**
     * 支付方式 - 支付宝
     */
    public static final String PAY_TYPE_ALIPAY = "ALIPAY";

    /**
     * 支付方式 - 校园卡
     */
    public static final String PAY_TYPE_CAMPUS_CARD = "CAMPUS_CARD";

    /**
     * 支付回调Key前缀（Redis，用于幂等性）
     */
    public static final String PAY_CALLBACK_KEY_PREFIX = "pay:callback:";

    /**
     * 退款编号前缀
     */
    public static final String REFUND_NO_PREFIX = "RF";

    // ==================== 评价相关 ====================

    /**
     * 评价有效期（天）
     */
    public static final int EVALUATION_EXPIRE_DAYS = 15;

    /**
     * 好评最低分数
     */
    public static final int POSITIVE_SCORE_MIN = 4;

    /**
     * 中评分数
     */
    public static final int NEUTRAL_SCORE = 3;

    // ==================== 纠纷相关 ====================

    /**
     * 纠纷处理时限（天）
     */
    public static final int DISPUTE_PROCESS_DAYS = 3;

    // ==================== 错误消息 ====================

    public static final String MSG_ORDER_NOT_FOUND = "订单不存在";
    public static final String MSG_ORDER_STATUS_ERROR = "订单状态异常";
    public static final String MSG_PRODUCT_NOT_FOUND = "商品不存在";
    public static final String MSG_PRODUCT_OFF_SHELF = "商品已下架或已售出";
    public static final String MSG_CANNOT_BUY_SELF = "不能购买自己发布的商品";
    public static final String MSG_NOT_AUTHENTICATED = "请先完成校园身份认证";
    public static final String MSG_NO_PERMISSION = "无权操作该订单";
    public static final String MSG_DISPUTE_EXISTS = "该订单已存在未解决的纠纷";
    public static final String MSG_EVALUATION_EXISTS = "该订单已评价";
}