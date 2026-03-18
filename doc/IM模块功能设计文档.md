# IM模块功能设计文档

**版本**: v1.0.0

---

## 一、模块概述

### 1.1 模块定位

IM（即时通讯）模块是FNUSALE校园二手交易平台的核心沟通桥梁，承载买卖双方的即时交流职责。作为校园专属二手交易平台，IM模块的核心价值在于：

- **交易撮合**：买家通过即时沟通了解商品详情，促进交易达成
- **信任建立**：通过实时对话建立买卖双方的信任关系
- **纠纷举证**：聊天记录作为交易纠纷处理的重要依据
- **平台引导**：通过敏感词过滤引导用户在平台内完成交易

### 1.2 模块职责

| 职责领域 | 具体内容 |
|---------|---------|
| 会话管理 | 会话创建、会话列表、会话状态、会话置顶 |
| 消息收发 | 文字消息、图片消息、语音消息、消息撤回 |
| 消息存储 | 聊天记录持久化、历史消息查询、聊天记录导出 |
| 消息通知 | 新消息推送、未读消息提醒、交易节点通知 |
| 内容安全 | 敏感词检测、风险交易预警 |
| 快捷回复 | 系统预设模板、用户自定义模板 |

### 1.3 模块边界

**包含功能**：
- 点对点即时聊天（文字/图片/语音）
- 会话与消息的全生命周期管理
- 聊天记录存储与查询（保留3个月）
- 敏感词过滤与风险预警
- 快捷回复模板管理

**不包含功能**：
- 用户认证与权限（用户模块）
- 商品信息管理（商品模块）
- 订单支付流程（交易模块）
- 营销消息推送（营销模块）

---

## 二、功能架构

### 2.1 功能架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          IM模块 (fnusale-im)                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  会话服务    │  │  消息服务    │  │  通知服务    │  │  安全服务    │   │
│  ├──────────────┤  ├──────────────┤  ├──────────────┤  ├──────────────┤   │
│  │ • 会话创建   │  │ • 消息发送   │  │ • 新消息推送 │  │ • 敏感词检测 │   │
│  │ • 会话列表   │  │ • 消息接收   │  │ • 未读提醒   │  │ • 风险预警   │   │
│  │ • 会话置顶   │  │ • 消息撤回   │  │ • 交易通知   │  │ • 内容过滤   │   │
│  │ • 会话删除   │  │ • 历史查询   │  │              │  │              │   │
│  │              │  │ • 记录导出   │  │              │  │              │   │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐                                        │
│  │ 快捷回复服务 │  │  WebSocket   │                                        │
│  ├──────────────┤  ├──────────────┤                                        │
│  │ • 系统模板   │  │ • 连接管理   │                                        │
│  │ • 自定义模板 │  │ • 心跳检测   │                                        │
│  │ • 模板管理   │  │ • 消息路由   │                                        │
│  └──────────────┘  └──────────────┘                                        │
│                                                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                              基础设施层                                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐           │
│  │   MySQL    │  │   Redis    │  │ RabbitMQ   │  │  MinIO     │           │
│  │  消息存储  │  │ 在线状态    │  │ 异步推送   │  │ 图片/语音  │           │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 服务依赖关系

```
                    ┌─────────────────┐
                    │   前端应用层     │
                    │  (WebSocket连接) │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │   API Gateway   │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐
│   fnusale-im    │ │  fnusale-user  │ │ fnusale-product │
│   (IM服务)      │ │   (用户服务)   │ │   (商品服务)    │
└────────┬────────┘ └─────────────────┘ └─────────────────┘
         │
         │ 调用关系
         │
┌────────▼────────┐ ┌────────────────┐ ┌────────────────┐
│ fnusale-trade   │ │ fnusale-admin  │ │ fnusale-im     │
│   (交易服务)    │ │   (管理服务)   │ │  (融云SDK)     │
└─────────────────┘ └────────────────┘ └────────────────┘
```

---

## 三、会话服务

### 3.1 功能概述

会话服务提供买卖双方聊天会话的全生命周期管理，一个会话绑定一个商品，支持买卖双方针对特定商品进行沟通。

### 3.2 会话状态流转

```
┌─────────┐    用户创建    ┌─────────┐    用户关闭    ┌─────────┐
│  创建   │ ───────────▶ │  正常   │ ───────────▶ │  已关闭  │
│         │              │ NORMAL  │              │ CLOSED  │
└─────────┘              └─────────┘              └─────────┘
```

### 3.3 核心功能设计

#### 3.3.1 创建会话

**功能描述**：买家点击商品详情页"聊一聊"按钮，创建与卖家的聊天会话。

**业务规则**：
- 用户必须完成校园认证才能发起会话
- 会话必须绑定具体商品（一商品一会话）
- 同一用户对同一商品只能有一个会话
- 会话创建时，user1为发起方，user2为商品发布者
- 会话创建后自动成为双方的首条消息入口

**接口设计**：
```
POST /session/create
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
  "targetUserId": 1001,     // 对方用户ID（商品发布者）
  "productId": 2001         // 商品ID
}

Response:
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "sessionId": 3001,      // 会话ID
    "isNew": true           // 是否新创建
  }
}
```

#### 3.3.2 获取会话列表

**功能描述**：获取当前用户的所有聊天会话，按最后消息时间倒序排列。

**业务规则**：
- 会话列表按最后消息时间倒序
- 置顶会话优先显示
- 显示对方的用户信息和商品信息
- 显示未读消息数

**接口设计**：
```
GET /session/list
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "sessionId": 3001,
      "targetUser": {
        "userId": 1001,
        "username": "张三",
        "avatarUrl": "https://..."
      },
      "product": {
        "productId": 2001,
        "productName": "高等数学教材",
        "mainImageUrl": "https://...",
        "price": 25.00
      },
      "lastMessage": {
        "content": "几成新？",
        "time": "2024-06-01 14:30:00",
        "type": "TEXT"
      },
      "unreadCount": 3,
      "isPinned": true
    }
  ]
}
```

#### 3.3.3 获取未读消息数

**功能描述**：获取当前用户的未读消息总数。

**接口设计**：
```
GET /session/unread-count
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": 5
}
```

#### 3.3.4 标记会话已读

**功能描述**：用户进入会话后，标记会话内所有消息为已读。

**业务规则**：
- 进入会话详情页自动触发
- 清零该用户在会话中的未读消息数

**接口设计**：
```
PUT /session/{sessionId}/read
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

#### 3.3.5 会话置顶

**功能描述**：用户可将会话置顶，方便快速找到重要对话。

**业务规则**：
- 置顶会话在会话列表顶部显示
- 用户最多可置顶5个会话
- 置顶会话按置顶时间倒序排列

**接口设计**：
```
PUT /session/{sessionId}/pin
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "置顶成功",
  "data": null
}
```

#### 3.3.6 删除会话

**功能描述**：用户删除会话（仅删除自己的会话记录，不影响对方）。

**业务规则**：
- 删除会话仅对当前用户生效
- 对方用户的会话记录不受影响
- 删除后可重新发起会话

**接口设计**：
```
DELETE /session/{sessionId}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 3.4 会话数据模型

#### t_im_session（聊天会话表）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| user1_id | BIGINT | 用户1ID（发起方） |
| user2_id | BIGINT | 用户2ID（接收方） |
| product_id | BIGINT | 关联商品ID |
| last_message_content | VARCHAR(500) | 最后一条消息内容 |
| last_message_time | DATETIME | 最后一条消息时间 |
| unread_count_u1 | INT | 用户1未读消息数 |
| unread_count_u2 | INT | 用户2未读消息数 |
| session_status | VARCHAR(20) | 会话状态：NORMAL/CLOSED |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

---

## 四、消息服务

### 4.1 功能概述

消息服务提供即时消息的发送、接收、存储和查询功能，支持文字、图片、语音三种消息类型。

### 4.2 消息类型

| 类型 | 编码 | 说明 | 校园场景示例 |
|-----|------|------|------------|
| 文字消息 | TEXT | 纯文本消息 | "这本书还有吗？" |
| 图片消息 | IMAGE | 图片消息（OSS存储） | 商品细节图片 |
| 语音消息 | VOICE | 语音消息（最长60秒） | 快速回复（静音场景） |

### 4.3 核心功能设计

#### 4.3.1 发送文字消息

**功能描述**：用户发送文字消息。

**业务规则**：
- 消息内容不超过500字
- 发送前进行敏感词检测
- 检测到敏感词时进行标记，但仍发送（用于风险预警）
- 消息发送成功后更新会话最后消息

**接口设计**：
```
POST /message/text
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
  "sessionId": 3001,
  "content": "这本书还有吗？什么时间方便自提？"
}

Response:
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "messageId": 4001,
    "sendTime": "2024-06-01 14:30:00"
  }
}
```

#### 4.3.2 发送图片消息

**功能描述**：用户发送图片消息。

**业务规则**：
- 图片必须先上传至OSS，获取URL
- 图片大小不超过5MB
- 支持jpg、png、gif格式
- 图片进行涉黄涉暴检测

**接口设计**：
```
POST /message/image
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
  "sessionId": 3001,
  "imageUrl": "https://oss.example.com/im/image/xxx.jpg"
}

Response:
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "messageId": 4002,
    "sendTime": "2024-06-01 14:31:00"
  }
}
```

#### 4.3.3 发送语音消息

**功能描述**：用户发送语音消息。

**业务规则**：
- 语音时长最长60秒
- 语音格式支持amr、mp3、aac
- 语音必须先上传至OSS

**接口设计**：
```
POST /message/voice
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
  "sessionId": 3001,
  "voiceUrl": "https://oss.example.com/im/voice/xxx.amr",
  "duration": 15     // 语音时长（秒）
}

Response:
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "messageId": 4003,
    "sendTime": "2024-06-01 14:32:00"
  }
}
```

#### 4.3.4 消息撤回

**功能描述**：用户撤回已发送的消息。

**业务规则**：
- 消息发送后2分钟内可撤回
- 撤回后消息内容显示为"对方已撤回一条消息"
- 撤回操作不可撤销

**接口设计**：
```
DELETE /message/{messageId}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "撤回成功",
  "data": null
}

Error Response:
{
  "code": 6003,
  "message": "消息已超过撤回时限",
  "data": null
}
```

#### 4.3.5 获取历史消息

**功能描述**：分页获取会话的历史消息记录。

**业务规则**：
- 按发送时间倒序分页
- 默认每页20条
- 支持上拉加载更多

**接口设计**：
```
GET /session/{sessionId}/messages?pageNum=1&pageSize=20
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "list": [
      {
        "messageId": 4001,
        "senderId": 1001,
        "receiverId": 1002,
        "messageType": "TEXT",
        "content": "这本书还有吗？",
        "sendTime": "2024-06-01 14:30:00",
        "isRead": 1
      }
    ]
  }
}
```

#### 4.3.6 搜索消息

**功能描述**：在会话中搜索包含关键词的消息。

**业务规则**：
- 支持模糊搜索
- 返回匹配的消息列表
- 高亮显示关键词

**接口设计**：
```
GET /message/search?sessionId=3001&keyword=价格
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "messageId": 4001,
      "content": "这个价格可以再优惠吗？",
      "sendTime": "2024-06-01 14:30:00"
    }
  ]
}
```

#### 4.3.7 导出聊天记录

**功能描述**：导出指定会话的聊天记录。

**业务规则**：
- 用户只能导出自己的聊天记录
- 导出格式为TXT或PDF
- 可作为纠纷举证材料

**接口设计**：
```
GET /message/{sessionId}/export
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": "https://oss.example.com/export/chat_3001.txt"
}
```

### 4.4 消息数据模型

#### t_im_message（聊天消息表）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| session_id | BIGINT | 会话ID |
| sender_id | BIGINT | 发送者ID |
| receiver_id | BIGINT | 接收者ID |
| message_type | VARCHAR(20) | 消息类型：TEXT/IMAGE/VOICE |
| message_content | VARCHAR(500) | 消息内容（文字内容或OSS地址） |
| is_read | TINYINT | 是否已读：0-未读，1-已读 |
| sensitive_check_result | VARCHAR(20) | 敏感词检测结果：PASS/FAIL |
| send_time | DATETIME | 发送时间 |
| is_deleted | TINYINT | 逻辑删除标记：0-未删除，1-已删除 |

---

## 五、快捷回复服务

### 5.1 功能概述

快捷回复服务提供预设的校园交易高频话术模板，帮助用户快速回复，提升沟通效率。

### 5.2 核心功能设计

#### 5.2.1 系统预设模板

系统预设的校园高频话术模板：

| 序号 | 模板内容 | 场景 |
|-----|---------|------|
| 1 | 几成新？ | 询问商品成色 |
| 2 | 能小刀吗？ | 议价 |
| 3 | 什么时候方便自提？ | 约定自提时间 |
| 4 | 在哪里自提？ | 询问自提地点 |
| 5 | 这个还在吗？ | 确认商品状态 |
| 6 | 可以先看看实物吗？ | 约定看货 |
| 7 | 价格可以再优惠吗？ | 议价 |
| 8 | 好的，我这就过去 | 确认交易 |
| 9 | 不好意思，已经出掉了 | 商品已售 |
| 10 | 谢谢，已收到 | 交易完成 |

#### 5.2.2 获取快捷回复列表

**功能描述**：获取用户可用的快捷回复模板列表。

**接口设计**：
```
GET /message/quick-reply/list
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "systemReplies": [
      { "id": 1, "content": "几成新？" },
      { "id": 2, "content": "能小刀吗？" }
    ],
    "userReplies": [
      { "id": 101, "content": "我在图书馆门口等您" }
    ]
  }
}
```

#### 5.2.3 添加自定义快捷回复

**功能描述**：用户添加自己的快捷回复模板。

**业务规则**：
- 每个用户最多添加10条自定义模板
- 模板内容不超过50字
- 模板内容进行敏感词检测

**接口设计**：
```
POST /message/quick-reply
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
  "content": "我在图书馆门口等您"
}

Response:
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

### 5.3 数据模型

#### t_im_quick_reply（快捷回复模板表）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| reply_content | VARCHAR(100) | 模板内容 |
| enable_status | TINYINT | 启用状态：0-禁用，1-启用 |
| sort | INT | 排序 |
| create_time | DATETIME | 创建时间 |

---

## 六、安全服务

### 6.1 功能概述

安全服务对聊天内容进行安全检测，过滤敏感信息，引导用户在平台内完成交易，防范交易风险。

### 6.2 敏感词过滤

#### 6.2.1 敏感词类别

| 类别 | 示例 | 处理方式 |
|-----|------|---------|
| 线下交易引导 | "加我微信"、"私聊"、"转账" | 标记预警，不拦截 |
| 联系方式 | 手机号、微信号、QQ号 | 脱敏处理 |
| 违禁品 | 管制刀具、违禁药品 | 拦截并警告 |
| 诈骗关键词 | "先付款"、"保证金"、"验证码" | 标记预警，提示风险 |

#### 6.2.2 处理流程

```
消息发送 → DFA算法敏感词检测 → 判断敏感词类别
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ▼                   ▼                   ▼
               违禁品类别          诈骗/引导类别          正常消息
               拦截并警告          标记预警               正常发送
                    │                   │
                    │                   └─────▶ 记录日志
                    │                          发送安全提醒
                    ▼
               返回错误信息
               "消息包含敏感内容"
```

#### 6.2.3 安全提醒

当检测到"校外交易引导"类敏感词时，系统自动发送安全提醒：

```
【安全提醒】为保障您的交易安全，请在平台内完成交易。平台外交易可能存在风险，请谨慎操作。
```

### 6.3 风险预警

#### 6.3.1 预警规则

| 规则 | 触发条件 | 处理方式 |
|-----|---------|---------|
| 敏感词触发 | 单次会话敏感词触发≥3次 | 管理员后台预警 |
| 异常行为 | 用户被多人举报 | 自动标记高风险 |
| 诈骗嫌疑 | 包含"先付款"等关键词 | 通知买家风险 |

---

## 七、消息通知服务

### 7.1 功能概述

消息通知服务负责将新消息推送给接收方，并提供未读消息提醒功能。

### 7.2 推送方式

| 场景 | 推送方式 | 说明 |
|-----|---------|------|
| 用户在线 | WebSocket实时推送 | 即时送达 |
| 用户离线 | 站内信+APP推送 | 离线消息存储 |
| 交易节点 | IM系统消息 | 订单状态变更通知 |

### 7.3 交易节点通知

系统在交易关键节点自动发送IM通知：

| 节点 | 通知内容 |
|-----|---------|
| 买家下单 | "您有新的订单，商品：[商品名称]，请及时处理" |
| 卖家发货（约定自提） | "买家已约定自提，时间：[时间]，地点：[地点]" |
| 交易完成 | "订单已完成，请对本次交易进行评价" |
| 订单取消 | "订单已取消，原因：[原因]" |

### 7.4 未读消息角标

- 显示在应用图标上的未读消息总数
- 进入会话后自动清除该会话未读数
- 支持设置免打扰的会话

---

## 八、技术方案

### 8.1 技术选型

#### 8.1.1 即时通讯方案

**方案对比**：

| 方案 | 优点 | 缺点 | 适用场景 |
|-----|------|------|---------|
| WebSocket（自建） | 完全自主可控、无额外费用 | 开发成本高、需维护集群 | 小规模用户 |
| 融云IM SDK | 功能完善、稳定可靠 | 按量付费、数据不在本地 | 中大规模用户 |

**选型决策**：
- 初期用户规模≤1万：采用WebSocket自建方案
- 用户规模>1万：切换至融云IM SDK

#### 8.1.2 WebSocket架构

```
┌─────────────────────────────────────────────────────────────┐
│                      WebSocket服务                           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ WebSocket    │    │   消息路由    │    │   连接管理    │  │
│  │ Handler      │───▶│   Service    │───▶│   Service    │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│         │                   │                    │          │
│         ▼                   ▼                    ▼          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │  心跳检测    │    │  消息持久化   │    │  在线状态     │  │
│  │  (30s间隔)   │    │  (MySQL)     │    │  (Redis)     │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 消息存储策略

#### 8.2.1 存储周期

- 聊天记录保留3个月
- 超过3个月的记录自动归档或删除
- 纠纷相关的聊天记录永久保留

#### 8.2.2 分库分表策略

当消息量较大时，按用户ID进行分表：

```sql
-- 消息表按用户ID取模分表
t_im_message_0  -- user_id % 8 = 0
t_im_message_1  -- user_id % 8 = 1
...
t_im_message_7  -- user_id % 8 = 7
```

### 8.3 在线状态管理

```java
// Redis存储用户在线状态
// Key: im:online:{userId}
// Value: serverId (标识用户连接的服务器节点)
// TTL: 60秒 (心跳续期)

public void updateUserOnline(Long userId, String serverId) {
    String key = "im:online:" + userId;
    redisTemplate.opsForValue().set(key, serverId, 60, TimeUnit.SECONDS);
}

public boolean isUserOnline(Long userId) {
    String key = "im:online:" + userId;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
}
```

---

## 九、接口清单

### 9.1 会话接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|-----|------|------|------|------|
| 获取会话列表 | GET | /session/list | 用户 | 获取当前用户的所有聊天会话 |
| 获取会话详情 | GET | /session/{sessionId} | 用户 | 根据会话ID获取详细信息 |
| 创建会话 | POST | /session/create | 用户 | 与指定用户创建新会话 |
| 获取或创建会话 | GET | /session/get-or-create | 用户 | 获取与指定用户的会话，不存在则创建 |
| 删除会话 | DELETE | /session/{sessionId} | 用户 | 删除指定会话 |
| 获取未读消息数 | GET | /session/unread-count | 用户 | 获取当前用户的未读消息总数 |
| 标记会话已读 | PUT | /session/{sessionId}/read | 用户 | 将会话标记为已读 |
| 获取会话消息列表 | GET | /session/{sessionId}/messages | 用户 | 分页获取会话的消息记录 |
| 置顶会话 | PUT | /session/{sessionId}/pin | 用户 | 将指定会话置顶 |
| 取消置顶 | DELETE | /session/{sessionId}/pin | 用户 | 取消会话置顶 |

### 9.2 消息接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|-----|------|------|------|------|
| 发送文字消息 | POST | /message/text | 用户 | 发送文字消息 |
| 发送图片消息 | POST | /message/image | 用户 | 发送图片消息 |
| 发送语音消息 | POST | /message/voice | 用户 | 发送语音消息 |
| 撤回消息 | DELETE | /message/{messageId} | 用户 | 撤回已发送的消息 |
| 搜索消息 | GET | /message/search | 用户 | 在会话中搜索消息 |
| 导出聊天记录 | GET | /message/{sessionId}/export | 用户 | 导出指定会话的聊天记录 |

### 9.3 快捷回复接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|-----|------|------|------|------|
| 获取快捷回复列表 | GET | /message/quick-reply/list | 用户 | 获取系统预设的快捷回复模板 |
| 添加快捷回复 | POST | /message/quick-reply | 用户 | 添加自定义快捷回复 |
| 删除快捷回复 | DELETE | /message/quick-reply/{id} | 用户 | 删除自定义快捷回复 |

---

## 十、异常处理

### 10.1 错误码定义

| 错误码 | 说明 | 处理建议 |
|-------|------|---------|
| 6001 | 会话不存在 | 检查会话ID是否正确 |
| 6002 | 消息发送失败 | 检查网络连接 |
| 6003 | 消息已超过撤回时限 | 消息发送2分钟后无法撤回 |
| 6004 | 用户不在线 | 切换至离线消息推送 |
| 6005 | 消息包含敏感内容 | 修改消息内容后重发 |
| 6006 | 会话已关闭 | 重新发起会话 |
| 6007 | 快捷回复数量已达上限 | 删除部分快捷回复后添加 |
| 6008 | 图片上传失败 | 检查图片格式和大小 |
| 6009 | 语音上传失败 | 检查语音格式和时长 |

### 10.2 异常处理策略

```java
@RestControllerAdvice
public class ImExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    public Result<Void> handleSessionNotFound(SessionNotFoundException e) {
        return Result.error(6001, "会话不存在");
    }

    @ExceptionHandler(MessageRecallTimeoutException.class)
    public Result<Void> handleRecallTimeout(MessageRecallTimeoutException e) {
        return Result.error(6003, "消息已超过撤回时限");
    }

    @ExceptionHandler(SensitiveContentException.class)
    public Result<Void> handleSensitiveContent(SensitiveContentException e) {
        return Result.error(6005, "消息包含敏感内容");
    }
}
```

---

## 十一、性能优化

### 11.1 缓存策略

| 缓存项 | Key格式 | 过期时间 | 说明 |
|-------|---------|---------|------|
| 用户在线状态 | im:online:{userId} | 60秒 | WebSocket心跳续期 |
| 会话信息 | im:session:{sessionId} | 30分钟 | 热点会话缓存 |
| 未读消息数 | im:unread:{userId} | 30分钟 | 实时更新 |
| 敏感词列表 | im:sensitive:words | 1小时 | DFA算法使用 |

### 11.2 数据库优化

- 会话表：`user1_id`、`user2_id`、`product_id` 字段加索引
- 消息表：`session_id`、`send_time` 字段加联合索引
- 消息表按时间分区，便于清理历史数据

### 11.3 消息推送优化

- 批量推送：多条消息合并推送
- 消息压缩：大文本消息压缩后传输
- 离线消息分页加载：避免一次加载过多消息

---

## 十二、安全设计

### 12.1 接口安全

- 所有接口需登录认证
- 会话操作验证用户身份（会话参与者）
- WebSocket连接需Token验证

### 12.2 内容安全

- 敏感词实时检测
- 图片涉黄涉暴检测
- 语音内容审核（可选）

### 12.3 数据安全

- 聊天记录加密存储
- 敏感信息脱敏显示
- 用户删除会话仅逻辑删除

---

## 十三、监控告警

### 13.1 监控指标

| 指标名称 | 阈值 | 告警级别 | 处理方式 |
|---------|------|---------|---------|
| WebSocket连接数 | >1000 | 警告 | 检查是否有异常连接 |
| 消息发送延迟 | >500ms | 警告 | 检查服务器负载 |
| 消息发送失败率 | >5% | 紧急 | 检查网络和服务状态 |
| 敏感词触发次数 | >100/小时 | 警告 | 检查是否有异常用户 |

### 13.2 告警通知

- 普通告警：站内消息
- 紧急告警：短信 + 站内消息

---

## 十四、扩展设计

### 14.1 群聊功能

预留群聊功能，支持多人群组聊天。

**数据模型**：
```sql
CREATE TABLE t_im_group (
    id BIGINT PRIMARY KEY,
    group_name VARCHAR(100) COMMENT '群组名称',
    owner_id BIGINT COMMENT '群主ID',
    group_type VARCHAR(20) COMMENT '群类型：PRODUCT/COMMUNITY',
    product_id BIGINT COMMENT '关联商品ID',
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE t_im_group_member (
    id BIGINT PRIMARY KEY,
    group_id BIGINT COMMENT '群组ID',
    user_id BIGINT COMMENT '成员ID',
    join_time DATETIME
);
```

### 14.2 消息已读回执

支持消息级别的已读回执功能。

**数据模型**：
```sql
ALTER TABLE t_im_message ADD COLUMN read_time DATETIME COMMENT '已读时间';
```

### 14.3 消息引用回复

支持引用历史消息进行回复。

**数据模型**：
```sql
ALTER TABLE t_im_message ADD COLUMN reply_to_id BIGINT COMMENT '引用消息ID';
```

---

## 附录

### A. 枚举值定义

#### A.1 消息类型（messageType）

| 值 | 说明 |
|----|------|
| TEXT | 文字消息 |
| IMAGE | 图片消息 |
| VOICE | 语音消息 |

#### A.2 会话状态（sessionStatus）

| 值 | 说明 |
|----|------|
| NORMAL | 正常 |
| CLOSED | 已关闭 |

#### A.3 敏感词检测结果（sensitiveCheckResult）

| 值 | 说明 |
|----|------|
| PASS | 检测通过 |
| FAIL | 包含敏感词 |

### B. 配置项说明

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| im.message.recall.timeout | 120 | 消息撤回时限（秒） |
| im.message.max.length | 500 | 消息最大长度 |
| im.voice.max.duration | 60 | 语音最大时长（秒） |
| im.session.pin.max | 5 | 最大置顶会话数 |
| im.quick-reply.user.max | 10 | 用户自定义快捷回复上限 |
| im.message.retention.days | 90 | 消息保留天数 |

### C. WebSocket消息格式

#### C.1 连接认证

```json
{
  "type": "auth",
  "token": "Bearer xxx"
}
```

#### C.2 心跳消息

```json
{
  "type": "ping",
  "timestamp": 1717228800000
}
```

#### C.3 聊天消息

```json
{
  "type": "message",
  "data": {
    "sessionId": 3001,
    "messageId": 4001,
    "senderId": 1001,
    "messageType": "TEXT",
    "content": "你好，这本书还有吗？",
    "sendTime": "2024-06-01 14:30:00"
  }
}
```

### D. 相关文档

- [功能分析与架构选型](功能分析与架构选型.md)
- [数据库设计](数据库设计.md)
- [API接口文档](API接口文档.md)
- [用户模块功能设计文档](用户模块功能设计文档.md)