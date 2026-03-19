package com.fnusale.product.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // ==================== Exchange定义 ====================

    /**
     * 商品事件Exchange
     */
    public static final String PRODUCT_EVENT_EXCHANGE = "product.event.exchange";

    /**
     * 用户行为Exchange
     */
    public static final String USER_BEHAVIOR_EXCHANGE = "user.behavior.exchange";

    /**
     * AI任务Exchange
     */
    public static final String AI_TASK_EXCHANGE = "ai.task.exchange";

    /**
     * 死信Exchange
     */
    public static final String DEAD_LETTER_EXCHANGE = "product.dlx.exchange";

    // ==================== Queue定义 ====================

    /**
     * 商品事件队列 - ES同步
     */
    public static final String PRODUCT_ES_QUEUE = "product.es.queue";

    /**
     * 商品事件队列 - 通知服务
     */
    public static final String PRODUCT_NOTIFY_QUEUE = "product.notify.queue";

    /**
     * 商品事件队列 - 统计服务
     */
    public static final String PRODUCT_STATS_QUEUE = "product.stats.queue";

    /**
     * 用户行为队列 - 记录服务
     */
    public static final String BEHAVIOR_RECORD_QUEUE = "behavior.record.queue";

    /**
     * 用户行为队列 - 统计服务
     */
    public static final String BEHAVIOR_STATS_QUEUE = "behavior.stats.queue";

    /**
     * 用户行为队列 - 推荐服务
     */
    public static final String BEHAVIOR_RECOMMEND_QUEUE = "behavior.recommend.queue";

    /**
     * AI任务队列
     */
    public static final String AI_TASK_QUEUE = "ai.task.queue";

    /**
     * 死信队列
     */
    public static final String DEAD_LETTER_QUEUE = "product.dlx.queue";

    // ==================== Routing Key定义 ====================

    public static final String PRODUCT_EVENT_ROUTING_KEY = "product.event.#";
    public static final String USER_BEHAVIOR_ROUTING_KEY = "user.behavior.#";
    public static final String AI_TASK_ROUTING_KEY = "ai.task.#";
    public static final String DEAD_LETTER_ROUTING_KEY = "product.dlx.#";

    // ==================== 消息转换器 ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }

    // ==================== Exchange Bean ====================

    @Bean
    public TopicExchange productEventExchange() {
        return ExchangeBuilder.topicExchange(PRODUCT_EVENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange userBehaviorExchange() {
        return ExchangeBuilder.topicExchange(USER_BEHAVIOR_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange aiTaskExchange() {
        return ExchangeBuilder.topicExchange(AI_TASK_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== Queue Bean ====================

    @Bean
    public Queue productEsQueue() {
        return QueueBuilder.durable(PRODUCT_ES_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("product.dlx.es")
                .build();
    }

    @Bean
    public Queue productNotifyQueue() {
        return QueueBuilder.durable(PRODUCT_NOTIFY_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("product.dlx.notify")
                .build();
    }

    @Bean
    public Queue productStatsQueue() {
        return QueueBuilder.durable(PRODUCT_STATS_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("product.dlx.stats")
                .build();
    }

    @Bean
    public Queue behaviorRecordQueue() {
        return QueueBuilder.durable(BEHAVIOR_RECORD_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("product.dlx.behavior.record")
                .build();
    }

    @Bean
    public Queue behaviorStatsQueue() {
        return QueueBuilder.durable(BEHAVIOR_STATS_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("product.dlx.behavior.stats")
                .build();
    }

    @Bean
    public Queue behaviorRecommendQueue() {
        return QueueBuilder.durable(BEHAVIOR_RECOMMEND_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("product.dlx.behavior.recommend")
                .build();
    }

    @Bean
    public Queue aiTaskQueue() {
        return QueueBuilder.durable(AI_TASK_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("product.dlx.ai")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    // ==================== Binding Bean ====================

    @Bean
    public Binding productEsBinding() {
        return BindingBuilder.bind(productEsQueue())
                .to(productEventExchange())
                .with("product.event.#");
    }

    @Bean
    public Binding productNotifyBinding() {
        return BindingBuilder.bind(productNotifyQueue())
                .to(productEventExchange())
                .with("product.event.#");
    }

    @Bean
    public Binding productStatsBinding() {
        return BindingBuilder.bind(productStatsQueue())
                .to(productEventExchange())
                .with("product.event.#");
    }

    @Bean
    public Binding behaviorRecordBinding() {
        return BindingBuilder.bind(behaviorRecordQueue())
                .to(userBehaviorExchange())
                .with("user.behavior.#");
    }

    @Bean
    public Binding behaviorStatsBinding() {
        return BindingBuilder.bind(behaviorStatsQueue())
                .to(userBehaviorExchange())
                .with("user.behavior.#");
    }

    @Bean
    public Binding behaviorRecommendBinding() {
        return BindingBuilder.bind(behaviorRecommendQueue())
                .to(userBehaviorExchange())
                .with("user.behavior.#");
    }

    @Bean
    public Binding aiTaskBinding() {
        return BindingBuilder.bind(aiTaskQueue())
                .to(aiTaskExchange())
                .with("ai.task.#");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("product.dlx.#");
    }
}