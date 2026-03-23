# 基于 Kafka 的完整日志收集系统实现计划

## 一、需求概述

实现完整的日志收集系统，使用 Kafka 作为日志收集中间件，配合 ELK (Elasticsearch + Logstash + Kibana) 实现日志的统一收集、存储和可视化展示。

## 二、技术架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        日志收集架构 (Kafka + ELK)                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                     │
│   │  微服务应用   │  │  微服务应用   │  │  微服务应用   │                     │
│   │  (Logback)   │  │  (Logback)   │  │  (Logback)   │                     │
│   └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                     │
│          │                 │                 │                              │
│          │  JSON日志       │                 │                              │
│          ▼                 ▼                 ▼                              │
│   ┌────────────────────────────────────────────────────────────┐           │
│   │                     Kafka Cluster                          │           │
│   │              Topic: fnusale-logs                          │           │
│   └────────────────────────────┬───────────────────────────────┘           │
│                                │                                            │
│                                │ 消费日志                                   │
│                                ▼                                            │
│                         ┌──────────────┐                                    │
│                         │   Logstash   │                                    │
│                         │  (日志处理)   │                                    │
│                         └──────┬───────┘                                    │
│                                │                                            │
│                                ▼                                            │
│                         ┌──────────────┐                                    │
│                         │Elasticsearch │                                    │
│                         │  (日志存储)   │                                    │
│                         └──────┬───────┘                                    │
│                                │                                            │
│                                ▼                                            │
│                         ┌──────────────┐                                    │
│                         │    Kibana    │                                    │
│                         │  (可视化)    │                                    │
│                         └──────────────┘                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 三、实现步骤

### 步骤1：添加 Kafka 基础设施 (Docker配置)

**文件**: `backend/docker/docker-compose.yml`

添加以下服务：
- Zookeeper (Kafka依赖)
- Kafka Broker
- Kafka UI (管理界面，可选)

**配置内容**:
```yaml
# Zookeeper
zookeeper:
  image: confluentinc/cp-zookeeper:7.4.0
  container_name: fnusale-zookeeper
  ports:
    - "2181:2181"
  environment:
    ZOOKEEPER_CLIENT_PORT: 2181

# Kafka
kafka:
  image: confluentinc/cp-kafka:7.4.0
  container_name: fnusale-kafka
  ports:
    - "9092:9092"
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
  depends_on:
    - zookeeper
```

### 步骤2：添加 Kafka 相关依赖

**文件**: `backend/pom.xml`

添加版本管理：
```xml
<kafka.version>3.6.1</kafka.version>
```

添加依赖声明：
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>${kafka.version}</version>
</dependency>
```

**文件**: `backend/fnusale-common/pom.xml`

添加依赖：
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <scope>provided</scope>
</dependency>
```

### 步骤3：创建日志实体和常量类

**新建文件**: `backend/fnusale-common/src/main/java/com/fnusale/common/log/LogMessage.java`

日志消息实体类，包含：
- traceId: 链路追踪ID
- spanId: Span ID
- serviceName: 服务名称
- level: 日志级别
- message: 日志内容
- userId: 用户ID
- className: 类名
- methodName: 方法名
- threadName: 线程名
- timestamp: 时间戳
- exception: 异常信息
- requestUri: 请求URI
- requestMethod: 请求方法
- clientIp: 客户端IP
- duration: 耗时(毫秒)

**新建文件**: `backend/fnusale-common/src/main/java/com/fnusale/common/log/LogConstants.java`

日志相关常量：
- LOG_TOPIC: "fnusale-logs"
- TRACE_ID_HEADER: "X-Trace-Id"
- SPAN_ID_HEADER: "X-Span-Id"

### 步骤4：实现 TraceId 链路追踪

**新建文件**: `backend/fnusale-common/src/main/java/com/fnusale/common/log/TraceContext.java`

ThreadLocal存储TraceId和SpanId：
- setTraceId(String traceId)
- getTraceId()
- setSpanId(String spanId)
- getSpanId()
- clear()
- generateTraceId(): 生成唯一TraceId

### 步骤5：实现网关 TraceId 传递过滤器

**新建文件**: `backend/fnusale-gateway/src/main/java/com/fnusale/gateway/filter/TraceIdGatewayFilter.java`

网关过滤器功能：
- 生成或获取TraceId
- 将TraceId添加到请求头
- 传递给下游微服务

### 步骤6：实现微服务 TraceId 拦截器

**新建文件**: `backend/fnusale-common/src/main/java/com/fnusale/common/log/TraceIdInterceptor.java`

Web拦截器功能：
- 从请求头获取TraceId
- 设置到TraceContext
- 请求结束后清理

**新建文件**: `backend/fnusale-common/src/main/java/com/fnusale/common/config/TraceIdWebConfig.java`

注册拦截器到Web配置。

### 步骤7：实现请求日志切面

**新建文件**: `backend/fnusale-common/src/main/java/com/fnusale/common/aspect/RequestLogAspect.java`

AOP切面功能：
- 拦截所有Controller方法
- 记录请求路径、方法、参数
- 记录响应结果
- 计算请求耗时
- 发送日志到Kafka

### 步骤8：实现 Kafka 日志发送器

**新建文件**: `backend/fnusale-common/src/main/java/com/fnusale/common/log/KafkaLogAppender.java`

Logback自定义Appender：
- 继承 `UnsynchronizedAppenderBase<ILoggingEvent>`
- 将日志事件转换为LogMessage
- 异步发送到Kafka

**新建文件**: `backend/fnusale-common/src/main/java/com/fnusale/common/log/KafkaLogSender.java`

Kafka日志发送服务：
- 使用KafkaTemplate发送日志
- 异步发送，不阻塞业务线程
- 批量发送优化

### 步骤9：配置 Logback 集成 Kafka

**修改文件**: `backend/fnusale-common/src/main/resources/logback-spring.xml`

添加Kafka Appender配置：
```xml
<appender name="KAFKA" class="com.fnusale.common.log.KafkaLogAppender">
    <topic>fnusale-logs</topic>
    <bootstrapServers>${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}</bootstrapServers>
</appender>

<appender name="ASYNC_KAFKA" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>2048</queueSize>
    <discardingThreshold>20</discardingThreshold>
    <neverBlock>true</neverBlock>
    <appender-ref ref="KAFKA"/>
</appender>
```

### 步骤10：配置 Logstash 消费 Kafka

**修改文件**: `backend/docker/logstash/logstash.conf`

```ruby
input {
  kafka {
    bootstrap_servers => "kafka:9092"
    topics => ["fnusale-logs"]
    codec => json
    consumer_threads => 3
  }
}

filter {
  # 解析时间戳
  date {
    match => [ "timestamp", "ISO8601", "yyyy-MM-dd HH:mm:ss.SSS" ]
    target => "@timestamp"
    timezone => "Asia/Shanghai"
  }
}

output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "fnusale-logs-%{[serviceName]}-%{+YYYY.MM.dd}"
  }
}
```

### 步骤11：添加 Kafka 配置到各微服务

**修改各微服务的 application.yml**，添加Kafka配置：
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
    consumer:
      group-id: ${spring.application.name}
      auto-offset-reset: earliest
      enable-auto-commit: false
```

### 步骤12：更新 Logstash Docker 配置

**修改文件**: `backend/docker/docker-compose.yml`

更新Logstash配置，添加Kafka依赖：
```yaml
logstash:
  depends_on:
    - elasticsearch
    - kafka
```

### 步骤13：创建 Kibana 索引模板

在Kibana中创建索引模式：
- 索引模式: `fnusale-logs-*`
- 时间字段: `@timestamp`

### 步骤14：更新项目文档

**修改文件**: `backend/docker/README.md`

添加Kafka相关说明。

## 四、文件清单

### 新建文件 (10个)

| 序号 | 文件路径 | 说明 |
|------|----------|------|
| 1 | `fnusale-common/src/main/java/com/fnusale/common/log/LogMessage.java` | 日志消息实体类 |
| 2 | `fnusale-common/src/main/java/com/fnusale/common/log/LogConstants.java` | 日志常量定义 |
| 3 | `fnusale-common/src/main/java/com/fnusale/common/log/TraceContext.java` | TraceId上下文 |
| 4 | `fnusale-common/src/main/java/com/fnusale/common/log/TraceIdInterceptor.java` | Web拦截器 |
| 5 | `fnusale-common/src/main/java/com/fnusale/common/config/TraceIdWebConfig.java` | Web配置 |
| 6 | `fnusale-common/src/main/java/com/fnusale/common/aspect/RequestLogAspect.java` | 请求日志切面 |
| 7 | `fnusale-common/src/main/java/com/fnusale/common/log/KafkaLogAppender.java` | Logback Appender |
| 8 | `fnusale-common/src/main/java/com/fnusale/common/log/KafkaLogSender.java` | Kafka发送器 |
| 9 | `fnusale-gateway/src/main/java/com/fnusale/gateway/filter/TraceIdGatewayFilter.java` | 网关过滤器 |
| 10 | `fnusale-gateway/src/main/java/com/fnusale/gateway/config/GatewayConfig.java` | 网关配置类 |

### 修改文件 (8个)

| 序号 | 文件路径 | 修改内容 |
|------|----------|----------|
| 1 | `backend/pom.xml` | 添加Kafka版本管理和依赖声明 |
| 2 | `backend/fnusale-common/pom.xml` | 添加Kafka依赖 |
| 3 | `backend/fnusale-common/src/main/resources/logback-spring.xml` | 添加Kafka Appender |
| 4 | `backend/docker/docker-compose.yml` | 添加Zookeeper、Kafka服务 |
| 5 | `backend/docker/logstash/logstash.conf` | 修改为Kafka输入 |
| 6 | `backend/fnusale-gateway/src/main/resources/application.yml` | 添加Kafka配置 |
| 7 | `backend/fnusale-common/src/main/java/com/fnusale/common/util/UserContext.java` | 添加TraceId支持 |
| 8 | `backend/docker/README.md` | 添加Kafka说明 |

## 五、验证步骤

1. 启动Docker基础设施：`docker-compose up -d`
2. 检查Kafka状态：访问 `http://localhost:9092`
3. 启动微服务应用
4. 发送测试请求，验证TraceId生成和传递
5. 检查Kafka Topic：`fnusale-logs` 是否有消息
6. 检查Elasticsearch索引是否创建
7. 访问Kibana `http://localhost:5601` 查看日志

## 六、注意事项

1. Kafka与RocketMQ共存：项目已有RocketMQ，Kafka仅用于日志收集，两者互不影响
2. 异步发送：日志发送采用异步方式，不影响业务性能
3. 容错处理：Kafka不可用时，日志降级到控制台输出
4. 日志脱敏：敏感信息（密码、手机号等）需要脱敏处理
5. 索引管理：Elasticsearch索引按日期分片，需要定期清理历史数据
