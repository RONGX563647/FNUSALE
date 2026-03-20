# Agent模块功能设计文档

**版本**: v1.0.0
**服务名**: fnusale-agent
**端口**: 8108

---

## 一、功能概述

### 1.1 服务定位

fnusale-agent 是智能购物助手服务，通过对话式交互为用户提供个性化的购物辅助。与现有的 fnusale-ai-guide 服务形成互补：

| 服务 | 定位 | 功能 |
| --- | --- | --- |
| fnusale-ai-guide | 数据驱动 | 商品分类、价格参考、协同过滤推荐、FAQ问答 |
| fnusale-agent | 对话式交互 | 意图理解、多轮对话、综合分析、议价策略 |

### 1.2 核心功能

#### P1: 对话式购物助手

- **意图理解**: 分析用户自然语言，识别购买意图
- **多轮对话**: 维护对话上下文，提供连贯交互
- **商品筛选**: 根据用户需求自动筛选推荐商品

#### P1: 购买分析建议

- **价格分析**: 比价分析、价格合理性评估
- **卖家分析**: 信誉评分、交易历史、好评率
- **风险提醒**: 异常价格识别、风险卖家预警

#### P1: 议价辅助

- **价格建议**: 基于市场数据的议价区间
- **策略推荐**: 议价策略和技巧
- **话术生成**: 自动生成议价聊天话术

---

## 二、API接口设计

### 2.1 对话接口

#### POST /agent/chat

**请求体**:

```json
{
  "message": "我想买一个二手耳机，预算200左右",
  "sessionId": "session_123"
}
```

**响应体**:

```json
{
  "code": 200,
  "data": {
    "reply": "好的，我帮您找找200元左右的耳机。请问您对品牌有偏好吗？比如AirPods、华为FreeBuds等？",
    "recommendProducts": [
      {
        "id": 1001,
        "productName": "AirPods 2代 二手",
        "price": 180,
        "newDegree": "90_NEW",
        "mainImageUrl": "..."
      }
    ],
    "filters": {
      "categoryName": "耳机",
      "maxPrice": 250,
      "minPrice": 150
    },
    "sessionId": "session_123",
    "intentType": "SEARCH_PRODUCT",
    "needMoreInfo": true
  }
}
```

#### DELETE /agent/session/{sessionId}

清除会话上下文。

### 2.2 商品分析接口

#### GET /agent/analyze/{productId}

**响应体**:

```json
{
  "code": 200,
  "data": {
    "productId": 1001,
    "priceAnalysis": {
      "currentPrice": 180,
      "referenceRange": [150, 220],
      "priceLevel": "合理",
      "priceScore": 7
    },
    "sellerAnalysis": {
      "sellerId": 100,
      "sellerNickname": "小明",
      "rating": 4.8,
      "tradeCount": 25,
      "positiveRate": 96,
      "creditLevel": "优秀"
    },
    "recommendation": "建议购买",
    "riskAlerts": [],
    "bargainSuggestion": {
      "priceRange": [150, 170],
      "suggestedPrice": 160,
      "strategies": [
        "提到同类商品价格对比",
        "询问是否有配件",
        "表示可以自提省去快递费"
      ],
      "chatTemplates": [
        "您好，看到您发布的耳机，能小刀一点吗？",
        "您好，我看这款耳机挺喜欢的，160能出吗？"
      ]
    }
  }
}
```

#### GET /agent/bargain/{productId}

获取议价建议，返回 `bargainSuggestion` 对象。

---

## 三、数据流设计

### 3.1 对话处理流程

```text
用户消息 → 意图识别 → 上下文管理 → 商品查询 → 响应生成 → 返回用户
              ↓
         通义千问API
```

**详细步骤**:

1. **接收消息**: 接收用户发送的文本消息
2. **意图识别**: 调用通义千问API分析用户意图
3. **上下文管理**: 从Redis获取/更新会话上下文
4. **商品查询**: 根据意图调用ProductClient查询商品
5. **响应生成**: 构建Prompt，调用通义千问生成回复
6. **存储对话**: 可选存储到IM消息表
7. **返回响应**: 返回结构化响应

### 3.2 商品分析流程

```text
商品ID → 商品信息查询 → 价格分析 → 卖家分析 → 风险评估 → 议价建议 → 返回结果
            ↓              ↓           ↓
        ProductClient   价格参考表   UserClient
                        TradeClient
```

---

## 四、Prompt模板设计

### 4.1 系统提示词

```text
你是校园二手交易平台的智能购物助手。你的职责是帮助用户：
1. 理解用户的购物需求，推荐合适的二手商品
2. 分析商品的价格是否合理，提供购买建议
3. 评估卖家的信誉，提醒潜在风险
4. 提供议价建议和话术

注意：
- 保持友好、专业的语气
- 推荐商品时说明推荐理由
- 如果信息不足，主动询问用户需求
- 提醒用户注意交易安全，建议校内自提
```

### 4.2 意图识别Prompt

```text
用户消息: {userMessage}
历史对话: {chatHistory}

请分析用户意图，返回JSON格式:
{
  "intent": "SEARCH_PRODUCT | ANALYZE_PRODUCT | ASK_QUESTION | OTHER",
  "entities": {
    "category": "商品类别",
    "priceRange": [min, max],
    "keywords": ["关键词"]
  },
  "needMoreInfo": false
}
```

### 4.3 商品推荐Prompt

```text
用户需求: {userNeed}
商品列表: {productList}

请根据用户需求，从商品列表中推荐最合适的商品，并说明推荐理由。
```

---

## 五、技术实现要点

### 5.1 通义千问集成

**依赖配置**:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dashscope-sdk-java</artifactId>
    <version>2.12.0</version>
</dependency>
```

**配置项**:

```yaml
dashscope:
  api-key: ${DASHSCOPE_API_KEY}
  model: qwen-plus
  max-tokens: 2000
  temperature: 0.7
```

### 5.2 对话上下文管理

**Redis存储结构**:

```
Key: agent:session:{sessionId}
Value: {
  "messages": [{"role": "user/assistant", "content": "..."}],
  "userId": 123,
  "createdAt": "...",
  "updatedAt": "..."
}
TTL: 30分钟
```

### 5.3 服务调用关系

```text
fnusale-agent
    ├── fnusale-product (ProductClient)
    │   └── 商品查询、搜索
    ├── fnusale-user (UserClient)
    │   └── 用户信息、认证状态
    └── fnusale-trade (TradeClient)
        └── 卖家交易统计
```

### 5.4 数据依赖（无需新建表）

| 表 | 用途 |
| --- | --- |
| t_product | 商品信息 |
| t_user | 用户信息 |
| t_user_rating | 用户评分 |
| t_order_evaluation | 评价信息 |
| t_ai_price_reference | 价格参考 |
| t_im_session | 可选：存储对话会话 |
| t_im_message | 可选：存储对话消息 |

---

## 六、前端集成建议

### 6.1 对话入口

- 首页悬浮按钮，点击打开对话窗口
- 商品详情页"智能分析"按钮
- 聊天页面"议价助手"功能

### 6.2 交互流程

1. 用户输入自然语言描述需求
2. Agent返回推荐商品和分析结果
3. 用户可点击推荐商品查看详情
4. 支持多轮对话细化需求

---

## 七、后续扩展

### 7.1 Phase 2 功能

- 图片识别：支持上传图片搜索相似商品
- 语音输入：支持语音对话
- 个性化记忆：记住用户偏好

### 7.2 性能优化

- 响应缓存：相似问题缓存
- 流式输出：支持SSE流式响应
- 批量处理：批量商品分析