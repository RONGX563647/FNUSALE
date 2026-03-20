package com.fnusale.product.mq.config;

/**
 * RocketMQ Topic 和 Consumer Group 配置
 */
public class RocketMQConfig {

    // ==================== Topic 定义 ====================

    /**
     * 商品事件 Topic
     * 用于商品发布、更新、删除、上下架等事件通知
     */
    public static final String PRODUCT_EVENT_TOPIC = "product-event-topic";

    /**
     * 用户行为 Topic
     * 用于收藏、点赞、浏览等行为异步处理
     */
    public static final String USER_BEHAVIOR_TOPIC = "user-behavior-topic";

    /**
     * AI 任务 Topic
     * 用于 AI 品类识别等任务
     */
    public static final String AI_TASK_TOPIC = "ai-task-topic";

    // ==================== Consumer Group 定义 ====================

    /**
     * ES 同步消费者组
     */
    public static final String ES_SYNC_CONSUMER_GROUP = "product-es-sync-group";

    /**
     * 通知消费者组
     */
    public static final String NOTIFY_CONSUMER_GROUP = "product-notify-group";

    /**
     * 统计消费者组
     */
    public static final String STATS_CONSUMER_GROUP = "product-stats-group";

    /**
     * 行为记录消费者组
     */
    public static final String BEHAVIOR_RECORD_CONSUMER_GROUP = "product-behavior-record-group";

    /**
     * AI 任务消费者组
     */
    public static final String AI_TASK_CONSUMER_GROUP = "product-ai-task-group";

    // ==================== Tag 定义 ====================

    /**
     * 商品事件 Tags
     */
    public static final String TAG_PRODUCT_PUBLISH = "PUBLISH";
    public static final String TAG_PRODUCT_UPDATE = "UPDATE";
    public static final String TAG_PRODUCT_DELETE = "DELETE";
    public static final String TAG_PRODUCT_ON_SHELF = "ON_SHELF";
    public static final String TAG_PRODUCT_OFF_SHELF = "OFF_SHELF";
    public static final String TAG_PRODUCT_SOLD_OUT = "SOLD_OUT";

    /**
     * 用户行为 Tags
     */
    public static final String TAG_BEHAVIOR_COLLECT = "COLLECT";
    public static final String TAG_BEHAVIOR_UNCOLLECT = "UNCOLLECT";
    public static final String TAG_BEHAVIOR_LIKE = "LIKE";
    public static final String TAG_BEHAVIOR_UNLIKE = "UNLIKE";
    public static final String TAG_BEHAVIOR_BROWSE = "BROWSE";

    /**
     * AI 任务 Tags
     */
    public static final String TAG_AI_CATEGORY_RECOGNIZE = "CATEGORY_RECOGNIZE";
}