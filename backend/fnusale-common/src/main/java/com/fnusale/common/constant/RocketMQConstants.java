package com.fnusale.common.constant;

/**
 * RocketMQ Topic 常量
 */
public class RocketMQConstants {

    private RocketMQConstants() {
    }

    // ==================== 用户相关 Topic ====================

    /**
     * 用户注册 Topic
     */
    public static final String USER_REGISTER_TOPIC = "user-register";

    /**
     * 用户注册 - 初始化积分 Tag
     */
    public static final String USER_REGISTER_TAG_INIT_POINTS = "init-points";

    /**
     * 用户注册 - 发送欢迎通知 Tag
     */
    public static final String USER_REGISTER_TAG_WELCOME = "welcome";

    /**
     * 用户注册 - 发放新人优惠券 Tag
     */
    public static final String USER_REGISTER_TAG_NEW_USER_COUPON = "new-user-coupon";

    // ==================== 秒杀相关 Topic ====================

    /**
     * 秒杀订单 Topic
     */
    public static final String SECKILL_ORDER_TOPIC = "seckill-order";

    /**
     * 秒杀订单 - 创建订单 Tag
     */
    public static final String SECKILL_ORDER_TAG_CREATE = "create";

    /**
     * 秒杀提醒 Topic
     */
    public static final String SECKILL_REMINDER_TOPIC = "seckill-reminder";

    /**
     * 秒杀提醒 - 推送通知 Tag
     */
    public static final String SECKILL_REMINDER_TAG_PUSH = "push";

    // ==================== 优惠券相关 Topic ====================

    /**
     * 优惠券发放 Topic
     */
    public static final String COUPON_GRANT_TOPIC = "coupon-grant";

    /**
     * 优惠券发放 - 批量发放 Tag
     */
    public static final String COUPON_GRANT_TAG_BATCH = "batch";

    /**
     * 优惠券领取 Topic
     */
    public static final String COUPON_RECEIVE_TOPIC = "coupon-receive";

    /**
     * 优惠券领取 - 领取 Tag
     */
    public static final String COUPON_RECEIVE_TAG_RECEIVE = "receive";

    /**
     * 优惠券过期提醒 Topic
     */
    public static final String COUPON_EXPIRE_REMINDER_TOPIC = "coupon-expire-reminder";

    /**
     * 优惠券过期提醒 - 提醒 Tag
     */
    public static final String COUPON_EXPIRE_REMINDER_TAG_NOTIFY = "notify";

    // ==================== 秒杀预热 Topic ====================

    /**
     * 秒杀预热 Topic
     */
    public static final String SECKILL_WARMUP_TOPIC = "seckill-warmup";

    /**
     * 秒杀预热 - 预热库存 Tag
     */
    public static final String SECKILL_WARMUP_TAG_STOCK = "stock";

    // ==================== 商品审核相关 Topic ====================

    /**
     * 商品审核 Topic
     */
    public static final String PRODUCT_AUDIT_TOPIC = "product-audit";

    /**
     * 商品审核 - 审核结果通知 Tag
     */
    public static final String PRODUCT_AUDIT_TAG_NOTIFY = "notify";

    // ==================== 用户认证相关 Topic ====================

    /**
     * 用户认证审核 Topic
     */
    public static final String USER_AUTH_TOPIC = "user-auth";

    /**
     * 用户认证 - 审核结果通知 Tag
     */
    public static final String USER_AUTH_TAG_NOTIFY = "notify";

    // ==================== 用户封禁相关 Topic ====================

    /**
     * 用户封禁 Topic
     */
    public static final String USER_BAN_TOPIC = "user-ban";

    /**
     * 用户封禁 - 封禁/解封通知 Tag
     */
    public static final String USER_BAN_TAG_NOTIFY = "notify";

    // ==================== IM消息相关 Topic ====================

    /**
     * IM消息发送 Topic
     */
    public static final String IM_MESSAGE_SEND_TOPIC = "im-message-send";

    /**
     * IM消息发送 - 发送消息 Tag
     */
    public static final String IM_MESSAGE_SEND_TAG_SEND = "send";

    /**
     * IM消息推送 Topic
     */
    public static final String IM_MESSAGE_PUSH_TOPIC = "im-message-push";

    /**
     * IM消息推送 - 推送 Tag
     */
    public static final String IM_MESSAGE_PUSH_TAG_PUSH = "push";

    /**
     * IM消息推送 - 重试 Tag
     */
    public static final String IM_MESSAGE_PUSH_TAG_RETRY = "retry";

    /**
     * IM消息推送 - 死信队列 Tag
     */
    public static final String IM_MESSAGE_PUSH_TAG_DLQ = "dlq";

    /**
     * IM离线消息 Topic
     */
    public static final String IM_OFFLINE_MESSAGE_TOPIC = "im-offline-message";

    /**
     * IM离线消息 - 存储 Tag
     */
    public static final String IM_OFFLINE_MESSAGE_TAG_STORE = "store";

    /**
     * IM离线消息 - 推送 Tag
     */
    public static final String IM_OFFLINE_MESSAGE_TAG_PUSH = "push";

    /**
     * IM消息撤回 Topic
     */
    public static final String IM_MESSAGE_RECALL_TOPIC = "im-message-recall";

    /**
     * IM消息撤回 - 撤回 Tag
     */
    public static final String IM_MESSAGE_RECALL_TAG_RECALL = "recall";

    /**
     * IM消息已读 Topic
     */
    public static final String IM_MESSAGE_READ_TOPIC = "im-message-read";

    /**
     * IM消息已读 - 已读 Tag
     */
    public static final String IM_MESSAGE_READ_TAG_READ = "read";

    /**
     * IM敏感词检测 Topic
     */
    public static final String IM_SENSITIVE_CHECK_TOPIC = "im-sensitive-check";

    /**
     * IM敏感词检测 - 检测 Tag
     */
    public static final String IM_SENSITIVE_CHECK_TAG_CHECK = "check";

    /**
     * IM敏感词检测 - 结果 Tag
     */
    public static final String IM_SENSITIVE_CHECK_TAG_RESULT = "result";

    // ==================== 交易订单相关 Topic ====================

    /**
     * 订单超时 Topic
     */
    public static final String ORDER_TIMEOUT_TOPIC = "order-timeout";

    /**
     * 订单超时 - 取消订单 Tag
     */
    public static final String ORDER_TIMEOUT_TAG_CANCEL = "cancel";

    /**
     * 订单支付 Topic
     */
    public static final String ORDER_PAY_TOPIC = "order-pay";

    /**
     * 订单支付 - 支付成功 Tag
     */
    public static final String ORDER_PAY_TAG_SUCCESS = "success";

    /**
     * 订单完成 Topic
     */
    public static final String ORDER_COMPLETE_TOPIC = "order-complete";

    /**
     * 订单完成 - 更新商品 Tag
     */
    public static final String ORDER_COMPLETE_TAG_UPDATE_PRODUCT = "update-product";

    /**
     * 订单完成 - 通知卖家 Tag
     */
    public static final String ORDER_COMPLETE_TAG_NOTIFY_SELLER = "notify-seller";

    /**
     * 订单退款 Topic
     */
    public static final String ORDER_REFUND_TOPIC = "order-refund";

    /**
     * 订单退款 - 处理退款 Tag
     */
    public static final String ORDER_REFUND_TAG_PROCESS = "process";

    /**
     * 订单退款 - 退款成功 Tag
     */
    public static final String ORDER_REFUND_TAG_SUCCESS = "success";

    /**
     * 订单评价 Topic
     */
    public static final String ORDER_EVALUATION_TOPIC = "order-evaluation";

    /**
     * 订单评价 - 更新评分 Tag
     */
    public static final String ORDER_EVALUATION_TAG_UPDATE_RATING = "update-rating";

    /**
     * 交易纠纷 Topic
     */
    public static final String TRADE_DISPUTE_TOPIC = "trade-dispute";

    /**
     * 交易纠纷 - 创建纠纷 Tag
     */
    public static final String TRADE_DISPUTE_TAG_CREATE = "create";

    /**
     * 交易纠纷 - 处理纠纷 Tag
     */
    public static final String TRADE_DISPUTE_TAG_PROCESS = "process";

    /**
     * 交易纠纷 - 解决纠纷 Tag
     */
    public static final String TRADE_DISPUTE_TAG_RESOLVE = "resolve";

    /**
     * 订单创建通知 Topic
     */
    public static final String ORDER_CREATE_TOPIC = "order-create";

    /**
     * 订单创建 - 通知卖家 Tag
     */
    public static final String ORDER_CREATE_TAG_NOTIFY_SELLER = "notify-seller";
}