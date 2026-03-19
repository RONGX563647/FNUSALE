# IM模块消息队列优化计划

## 一、现状分析

### 1.1 当前架构问题

| 问题 | 当前实现 | 影响 |
|-----|---------|-----|
| 消息发送同步阻塞 | 数据库写入 + WebSocket推送同步执行 | 接口响应慢，高峰期性能差 |
| WebSocket推送不可靠 | 推送失败只记录日志 | 消息丢失，用户体验差 |
| 外部服务调用阻塞 | 同步调用UserClient/ProductClient | 依赖服务延迟直接影响接口 |
| 敏感词检测缺失 | 当前未实现 | 消息安全性无保障 |
| 离线消息处理 | 无延迟推送机制 | 用户上线后无法及时收到消息 |
| 多端同步 | 无状态广播机制 | 撤回/已读状态同步不及时 |

### 1.2 现有MQ基础设施

- **RocketMQ**: 已配置，项目中已广泛使用
- **现有消费者**:
  - `UserAuthAuditNotifyConsumer` - 用户认证审核通知
  - `ProductAuditNotifyConsumer` - 商品审核通知
  - `SeckillReminderConsumer` - 秒杀提醒
  - `UserBanNotifyConsumer` - 用户封禁通知

> **注意**: 现有消费者大部分只是记录日志，未实现真正的消息推送

---

## 二、优化方案

### 2.1 优先级分级

| 优先级 | 说明 | 预期收益 |
|-------|-----|---------|
| **P0 - 高** | 核心流程优化，影响用户体验 | 响应时间降低50%+，消息零丢失 |
| **P1 - 中** | 功能增强，提升系统可靠性 | 功能完善，系统稳定性提升 |
| **P2 - 低** | 性能优化，锦上添花 | 高峰期吞吐量提升 |

---

### 2.2 详细优化项

#### P0-1: 消息发送异步化 ⭐⭐⭐

**当前问题**:
```
用户发送消息 → DB写入(50ms) + 会话更新(30ms) + WebSocket推送(20ms) = 100ms
```

**优化方案**:
```
用户发送消息 → DB写入(50ms) → MQ投递(5ms) = 55ms
                              ↓
                    异步消费者 → WebSocket推送
```

**实现要点**:
- 新增Topic: `im-message-send`
- 消费者消费消息后执行WebSocket推送
- 保证消息顺序性（同一会话的消息使用相同队列）

**代码变更**:
```
fnusale-im/src/main/java/com/fnusale/im/
├── mq/
│   ├── producer/
│   │   └── MessageSendProducer.java         # 消息发送生产者
│   └── consumer/
│       └── MessagePushConsumer.java         # 消息推送消费者
├── service/impl/
│   └── ImMessageServiceImpl.java            # 改为发送MQ消息
```

---

#### P0-2: 消息推送可靠性保障 ⭐⭐⭐

**当前问题**:
```java
// 推送失败不影响消息发送，仅记录日志
log.warn("消息推送失败，receiverId: {}, messageId: {}", receiverId, messageId, e);
```

**优化方案**:
```
推送失败 → 消息重新入队 → 延迟重试 (最多3次)
         ↓
    3次失败 → 死信队列 → 人工处理/定时重推
```

**实现要点**:
- 利用RocketMQ延迟消息实现重试
- 设置重试次数阈值
- 死信队列处理最终失败的消息

---

#### P0-3: 离线消息延迟推送 ⭐⭐⭐

**当前问题**:
```java
// 用户不在线，直接跳过
if (wsSessionId == null) {
    log.debug("用户不在线，userId: {}", userId);
    return;
}
```

**优化方案**:
```
用户离线 → 消息存入待推送队列
用户上线 → 批量推送离线消息（最多50条）
```

**实现要点**:
- Redis存储离线消息索引
- 用户上线时触发批量推送
- 新增Topic: `im-offline-message`

---

#### P1-1: 敏感词异步检测 ⭐⭐

**当前问题**:
```java
message.setSensitiveCheckResult("PASS");  // 直接跳过，未检测
```

**优化方案**:
```
发送消息 → MQ投递 → 敏感词检测服务 → 结果回写
         ↓
    检测失败 → 通知用户/自动撤回
```

**实现要点**:
- 新增Topic: `im-sensitive-check`
- 检测结果存储到消息表
- 敏感消息处理策略（警告/屏蔽/撤回）

---

#### P1-2: 消息撤回广播 ⭐⭐

**当前问题**:
- 撤回操作只更新数据库
- 接收方客户端不知道消息被撤回

**优化方案**:
```
撤回消息 → MQ广播 → 所有在线端同步撤回状态
```

**实现要点**:
- 新增Topic: `im-message-recall`
- 推送撤回通知给会话双方

---

#### P1-3: 已读状态同步 ⭐⭐

**当前问题**:
- 标记已读只更新数据库
- 发送方不知道消息已被读

**优化方案**:
```
标记已读 → MQ广播 → 发送方收到已读回执
```

**实现要点**:
- 新增Topic: `im-message-read`
- 推送已读回执给消息发送者

---

#### P1-4: 会话创建异步校验 ⭐

**当前问题**:
```java
// 同步调用外部服务
validateUserAuth(userId);
validateUserAuth(targetUserId);
ProductVO product = validateProduct(productId, targetUserId);
```

**优化方案**:
- 方案A: 预校验 + 缓存用户认证状态
- 方案B: 异步校验 + 结果通知

**实现要点**:
- Redis缓存用户认证状态（TTL 5分钟）
- 减少对外部服务的直接依赖

---

#### P2-1: 批量操作合并 ⭐

**当前问题**:
```
标记已读 → 逐条更新数据库
```

**优化方案**:
```
标记已读请求 → MQ缓冲 → 100ms窗口合并 → 批量更新DB
```

**实现要点**:
- 利用RocketMQ批量消费
- Redis临时存储待更新数据
- 定时任务批量刷盘

---

#### P2-2: 消息写入批量优化 ⭐

**当前问题**:
```
每条消息单独insert
```

**优化方案**:
```
消息先入MQ → 消费者批量insert (100条/批)
```

**实现要点**:
- 高峰期削峰填谷
- MyBatis批量插入优化

---

## 三、新增Topic规划

| Topic | Tag | 用途 | 生产者 | 消费者 |
|-------|-----|-----|-------|-------|
| `im-message-send` | `send` | 消息发送异步化 | ImMessageService | MessagePushConsumer |
| `im-message-push` | `push`, `retry`, `dlq` | 消息推送 | MessagePushConsumer | MessagePushConsumer |
| `im-offline-message` | `store`, `push` | 离线消息 | MessagePushConsumer | OfflineMessageConsumer |
| `im-message-recall` | `recall` | 消息撤回广播 | ImMessageService | MessageRecallConsumer |
| `im-message-read` | `read` | 已读状态同步 | ImSessionService | MessageReadConsumer |
| `im-sensitive-check` | `check`, `result` | 敏感词检测 | ImMessageService | SensitiveCheckConsumer |

---

## 四、实施计划

### Phase 1: 核心优化 (2周)

| 任务 | 预计工时 | 依赖 |
|-----|---------|-----|
| P0-1 消息发送异步化 | 3天 | RocketMQ配置 |
| P0-2 消息推送可靠性 | 2天 | P0-1 |
| P0-3 离线消息推送 | 2天 | P0-1, Redis |
| 单元测试 | 2天 | - |
| 集成测试 | 1天 | - |

### Phase 2: 功能增强 (1.5周)

| 任务 | 预计工时 | 依赖 |
|-----|---------|-----|
| P1-1 敏感词异步检测 | 2天 | 敏感词词库 |
| P1-2 消息撤回广播 | 1天 | - |
| P1-3 已读状态同步 | 1天 | - |
| P1-4 会话创建优化 | 1天 | Redis缓存 |
| 测试 | 1天 | - |

### Phase 3: 性能优化 (1周)

| 任务 | 预计工时 | 依赖 |
|-----|---------|-----|
| P2-1 批量操作合并 | 2天 | - |
| P2-2 消息写入批量优化 | 2天 | - |
| 性能测试 | 1天 | - |

---

## 五、技术实现细节

### 5.1 消息顺序性保证

```java
// RocketMQ顺序消息 - 同一会话的消息使用相同队列
@RocketMQMessageListener(
    topic = "im-message-send",
    consumerGroup = "message-push-consumer-group",
    messageModel = MessageModel.ORDERLY  // 顺序消费
)
public class MessagePushConsumer implements RocketMQListener<MessageSendEvent> {
    // ...
}
```

### 5.2 幂等性设计

```java
// 消费者幂等处理
String idempotentKey = "im:message:push:" + event.getMessageId();
Boolean success = redisTemplate.opsForValue()
    .setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);
if (Boolean.FALSE.equals(success)) {
    log.info("消息已处理，跳过: {}", event.getMessageId());
    return;
}
```

### 5.3 重试策略

```java
// 延迟重试配置
@RocketMQMessageListener(
    topic = "im-message-push",
    selectorExpression = "push || retry",
    delayLevel = 3  // 10秒后重试
)
```

### 5.4 死信队列处理

```java
// 超过最大重试次数，转入死信队列
if (retryCount >= MAX_RETRY) {
    rocketMQTemplate.syncSend("im-message-push:dlq", message);
    log.warn("消息推送失败，转入死信队列: {}", messageId);
}
```

---

## 六、监控指标

| 指标 | 阈值 | 告警级别 |
|-----|-----|---------|
| 消息发送延迟 | > 100ms | Warning |
| MQ消息堆积 | > 10000条 | Warning |
| 推送失败率 | > 1% | Warning |
| 推送失败率 | > 5% | Critical |
| 消费者延迟 | > 5s | Warning |
| 死信队列消息数 | > 100条 | Critical |

---

## 七、风险评估

| 风险 | 影响 | 缓解措施 |
|-----|-----|---------|
| MQ不可用 | 消息无法发送 | 降级为同步发送 |
| 消息丢失 | 用户收不到消息 | 持久化 + ACK机制 |
| 顺序错乱 | 消息显示异常 | 顺序消息队列 |
| 重复消费 | 消息重复推送 | 幂等性设计 |
| 消费延迟 | 推送不及时 | 监控 + 告警 |

---

## 八、总结

### 预期收益

| 指标 | 优化前 | 优化后 | 提升 |
|-----|-------|-------|-----|
| 消息发送响应时间 | ~100ms | ~55ms | 45% |
| 高峰期吞吐量 | 100 TPS | 500 TPS | 400% |
| 消息推送成功率 | ~95% | ~99.9% | 5% |
| 离线消息触达率 | 0% | 100% | 新功能 |

### 下一步行动

1. 评审此优化计划
2. 确认实施优先级
3. 分配开发资源
4. 开始Phase 1实现