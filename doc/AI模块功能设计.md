# AI赋能模块功能设计文档

**版本**: v1.0.0
**服务名**: fnusale-ai-guide
**端口**: 8104

---

## 一、模块概述

### 1.1 模块定位

AI赋能模块是校园二手交易平台的核心提效模块，通过轻量化的AI能力，解决校园二手交易场景中的「发布繁、定价难、发现慢、咨询散」痛点。

### 1.2 设计原则

| 原则 | 说明 |
|------|------|
| **轻量化** | 调用云服务API，不自建模型，降低开发和运维成本 |
| **实用化** | 仅覆盖校园高频场景，拒绝过度智能化 |
| **可扩展** | 预留接口扩展能力，支持后续功能迭代 |

### 1.3 核心功能

| 功能 | 核心价值 | 技术方案 |
|------|----------|----------|
| AI拍照分类 | 简化发布流程，效率提升50% | 阿里云视觉AI |
| AI价格参考 | 科学定价，提升成交率 | 历史数据统计 |
| 个性化推荐 | 提升曝光效率 | 协同过滤算法 |
| 智能客服 | 降低运营成本 | 关键词匹配 + FAQ |

---

## 二、AI拍照分类

### 2.1 功能描述

用户发布商品时上传图片，系统自动识别商品品类，用户确认或微调后完成发布。

### 2.2 业务流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  上传图片   │────▶│  OSS存储    │────▶│  AI识别     │────▶│  返回品类   │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                                                                   │
┌─────────────┐     ┌─────────────┐     ┌─────────────┐            │
│  完成发布   │◀────│  用户确认   │◀────│  品类展示   │◀───────────┘
└─────────────┘     └─────────────┘     └─────────────┘
```

### 2.3 接口设计

#### 2.3.1 识别商品品类

**请求**
```http
POST /ai/category/recognize
Content-Type: application/x-www-form-urlencoded

imageUrl=https://oss.example.com/product/xxx.jpg
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "categoryId": 3,
    "categoryName": "耳机",
    "confidence": 0.92,
    "alternatives": [
      { "categoryId": 5, "categoryName": "音箱", "confidence": 0.15 },
      { "categoryId": 8, "categoryName": "充电宝", "confidence": 0.08 }
    ]
  }
}
```

#### 2.3.2 批量识别品类

**请求**
```http
POST /ai/category/batch-recognize
Content-Type: application/json

[
  "https://oss.example.com/product/1.jpg",
  "https://oss.example.com/product/2.jpg"
]
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "imageUrl": "https://oss.example.com/product/1.jpg",
      "categoryId": 3,
      "categoryName": "耳机",
      "confidence": 0.92
    },
    {
      "imageUrl": "https://oss.example.com/product/2.jpg",
      "categoryId": 1,
      "categoryName": "教材",
      "confidence": 0.88
    }
  ]
}
```

#### 2.3.3 获取识别历史

**请求**
```http
GET /ai/category/history?pageNum=1&pageSize=10
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 25,
    "list": [
      {
        "id": 1,
        "imageUrl": "https://oss.example.com/product/xxx.jpg",
        "categoryId": 3,
        "categoryName": "耳机",
        "confidence": 0.92,
        "userConfirmed": true,
        "createTime": "2024-05-01 10:30:00"
      }
    ]
  }
}
```

### 2.4 技术实现

#### 2.4.1 阿里云视觉AI对接

```java
@Service
public class AliyunVisionService {

    @Value("${aliyun.vision.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.vision.access-key-secret}")
    private String accessKeySecret;

    /**
     * 调用阿里云商品分类接口
     */
    public CategoryRecognizeResult recognizeCategory(String imageUrl) {
        // 1. 构建请求参数
        // 2. 调用阿里云API
        // 3. 解析返回结果，映射到本地品类
        // 4. 返回识别结果
    }
}
```

#### 2.4.2 品类映射配置

数据库表 `t_product_category` 中的 `ai_mapping_value` 字段存储AI返回值的映射：

| id | category_name | ai_mapping_value |
|----|---------------|------------------|
| 1 | 教材 | book,textbook,教材 |
| 2 | 电子产品 | electronics,digital |
| 3 | 耳机 | headphone,earphone,耳机 |
| 4 | 篮球 | basketball,sports |

### 2.5 校园高频品类

仅识别以下校园高频品类，提升准确率：

| 一级品类 | 二级品类 |
|----------|----------|
| 教材 | 专业教材、公共课教材、考研资料、考证资料 |
| 电子产品 | 耳机、充电宝、键盘、鼠标、U盘 |
| 生活用品 | 台灯、风扇、热水壶、收纳盒 |
| 体育器材 | 篮球、足球、羽毛球拍、瑜伽垫 |
| 服饰配件 | 书包、鞋子、衣服、配饰 |

### 2.6 性能指标

| 指标 | 目标值 |
|------|--------|
| 识别准确率 | ≥85% |
| 接口响应时间 | ≤500ms |
| 支持图片格式 | JPG、PNG、WEBP |
| 图片大小限制 | ≤5MB |

---

## 三、AI价格参考

### 3.1 功能描述

基于校园历史成交数据，为用户发布商品时提供合理的定价区间参考，避免乱定价导致的成交率低问题。

### 3.2 业务流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  选择品类   │────▶│  选择新旧度 │────▶│  价格参考   │
└─────────────┘     └─────────────┘     └─────────────┘
```

### 3.3 接口设计

#### 3.3.1 获取价格参考

**请求**
```http
GET /ai/price/reference?categoryId=1&newDegree=90_NEW
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "categoryId": 1,
    "categoryName": "教材",
    "newDegree": "90_NEW",
    "minPrice": 15.00,
    "maxPrice": 35.00,
    "avgPrice": 25.00,
    "sampleCount": 128,
    "suggestedPrice": 25.00,
    "priceDistribution": [
      { "range": "0-20", "count": 20, "percent": 15.6 },
      { "range": "20-40", "count": 85, "percent": 66.4 },
      { "range": "40-60", "count": 18, "percent": 14.1 },
      { "range": "60+", "count": 5, "percent": 3.9 }
    ]
  }
}
```

#### 3.3.2 获取商品定价建议

**请求**
```http
POST /ai/price/suggest
Content-Type: application/json

{
  "categoryId": 1,
  "newDegree": "90_NEW",
  "productName": "大学物理教材"
}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "suggestedPrice": 28.00,
    "priceRange": {
      "min": 20.00,
      "max": 35.00
    },
    "similarProducts": [
      {
        "productId": 1001,
        "productName": "大学物理（第七版）",
        "price": 25.00,
        "newDegree": "90_NEW",
        "status": "SOLD_OUT"
      }
    ],
    "analysis": {
      "marketDemand": "HIGH",
      "competitionLevel": "MEDIUM",
      "sellProbability": 0.85
    }
  }
}
```

#### 3.3.3 获取价格趋势

**请求**
```http
GET /ai/price/trend?categoryId=1
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "categoryId": 1,
    "categoryName": "教材",
    "trend": [
      { "month": "2024-01", "avgPrice": 22.50 },
      { "month": "2024-02", "avgPrice": 23.80 },
      { "month": "2024-03", "avgPrice": 25.00 }
    ],
    "trendType": "UP",
    "changePercent": 11.1
  }
}
```

#### 3.3.4 比价

**请求**
```http
GET /ai/price/compare?productId=1001&price=30.00
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "productId": 1001,
    "inputPrice": 30.00,
    "avgPrice": 25.00,
    "priceLevel": "ABOVE_AVERAGE",
    "pricePercentile": 75,
    "comparison": {
      "lowerCount": 96,
      "equalCount": 8,
      "higherCount": 24
    },
    "suggestion": "您的定价略高于同类商品均价，建议定价25-28元可提升成交率"
  }
}
```

### 3.4 数据模型

#### 3.4.1 AI价格参考表

```sql
CREATE TABLE t_ai_price_reference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL COMMENT '品类ID',
    new_degree VARCHAR(20) NOT NULL COMMENT '新旧程度',
    min_price DECIMAL(10,2) NOT NULL COMMENT '参考最低价格',
    max_price DECIMAL(10,2) NOT NULL COMMENT '参考最高价格',
    avg_price DECIMAL(10,2) COMMENT '平均价格',
    sample_count INT DEFAULT 0 COMMENT '参考样本数',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_degree (category_id, new_degree)
) COMMENT 'AI价格参考表';
```

### 3.5 价格计算算法

```java
@Service
public class PriceReferenceService {

    /**
     * 计算价格参考
     * 基于历史成交数据，按品类和新旧程度统计
     */
    public PriceReference calculateReference(Long categoryId, String newDegree) {
        // 1. 查询该品类、该新旧程度的已成交商品价格
        // 2. 计算价格分布（去除异常值）
        // 3. 计算最低价、最高价、平均价
        // 4. 更新价格参考表
    }

    /**
     * 定时任务：每周更新价格参考数据
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    public void updatePriceReference() {
        // 批量更新所有品类的价格参考
    }
}
```

---

## 四、个性化推荐

### 4.1 功能描述

基于用户行为数据，推荐用户可能感兴趣的商品，提升商品曝光效率和用户发现效率。

### 4.2 推荐策略

| 策略 | 说明 | 权重 |
|------|------|------|
| 协同过滤 | 基于用户行为相似度推荐 | 40% |
| 同专业推荐 | 推荐同专业同学发布的商品 | 25% |
| 同宿舍区推荐 | 推荐同宿舍区用户的商品（便于线下自提） | 20% |
| 热门推荐 | 推荐近期热门商品 | 15% |

### 4.3 接口设计

#### 4.3.1 获取首页推荐

**请求**
```http
GET /ai/recommend/home?pageNum=1&pageSize=10
Authorization: Bearer {token}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "productId": 1001,
        "productName": "大学物理教材",
        "price": 25.00,
        "mainImageUrl": "https://oss.example.com/xxx.jpg",
        "newDegree": "90_NEW",
        "categoryId": 1,
        "categoryName": "教材",
        "distance": 500,
        "recommendReason": "同专业同学发布"
      }
    ]
  }
}
```

#### 4.3.2 获取猜你喜欢

**请求**
```http
GET /ai/recommend/guess-like?pageNum=1&pageSize=10
Authorization: Bearer {token}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "list": [
      {
        "productId": 1002,
        "productName": "蓝牙耳机",
        "price": 80.00,
        "mainImageUrl": "https://oss.example.com/xxx.jpg",
        "newDegree": "NEW",
        "recommendReason": "根据您的浏览记录推荐"
      }
    ]
  }
}
```

#### 4.3.3 获取相似商品

**请求**
```http
GET /ai/recommend/similar/1001?pageNum=1&pageSize=10
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "list": [
      {
        "productId": 1003,
        "productName": "大学物理习题集",
        "price": 15.00,
        "mainImageUrl": "https://oss.example.com/xxx.jpg",
        "similarity": 0.92
      }
    ]
  }
}
```

#### 4.3.4 获取同专业推荐

**请求**
```http
GET /ai/recommend/same-major?pageNum=1&pageSize=10
Authorization: Bearer {token}
```

#### 4.3.5 获取同宿舍区推荐

**请求**
```http
GET /ai/recommend/same-dorm?pageNum=1&pageSize=10
Authorization: Bearer {token}
```

#### 4.3.6 刷新推荐

**请求**
```http
POST /ai/recommend/refresh
Authorization: Bearer {token}
```

#### 4.3.7 反馈推荐结果

**请求**
```http
POST /ai/recommend/feedback?productId=1001&feedbackType=like
Authorization: Bearer {token}
```

### 4.4 数据模型

#### 4.4.1 用户行为表

```sql
CREATE TABLE t_user_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    behavior_type VARCHAR(20) NOT NULL COMMENT '行为类型：BROWSE/COLLECT/LIKE/BUY',
    behavior_time DATETIME NOT NULL COMMENT '行为时间',
    behavior_weight DECIMAL(3,2) DEFAULT 1.0 COMMENT '行为权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_behavior_time (behavior_time)
) COMMENT '用户行为表';
```

### 4.5 推荐算法实现

#### 4.5.1 协同过滤算法

```java
@Service
public class CollaborativeFilteringService {

    /**
     * 基于用户的协同过滤推荐
     * User-based Collaborative Filtering
     */
    public List<ProductVO> recommendByUserBehavior(Long userId, int limit) {
        // 1. 找出与当前用户行为相似的用户
        // 2. 获取这些用户浏览/收藏但当前用户未浏览的商品
        // 3. 按相似度排序返回推荐结果
    }

    /**
     * 计算用户相似度（余弦相似度）
     */
    private double calculateUserSimilarity(Long userId1, Long userId2) {
        // 获取两个用户的行为向量
        // 计算余弦相似度
    }
}
```

#### 4.5.2 Elasticsearch商品检索

```java
@Service
public class ProductSearchService {

    /**
     * 搜索相似商品
     */
    public List<ProductVO> searchSimilarProducts(Long productId, int limit) {
        // 1. 获取商品特征（品类、价格区间、关键词）
        // 2. 构建ES查询条件
        // 3. 返回相似商品列表
    }
}
```

### 4.6 性能优化

| 优化策略 | 说明 |
|----------|------|
| Redis缓存 | 推荐结果缓存30分钟，减少计算压力 |
| 异步计算 | 推荐结果预计算，用户请求直接返回缓存 |
| 降级策略 | 缓存失效时返回热门商品 |

---

## 五、智能客服

### 5.1 功能描述

解答用户高频问题，复杂问题转接人工客服，降低运营成本。

### 5.2 问题分类

| 分类 | 问题示例 |
|------|----------|
| 账号认证 | 如何认证校园身份？认证失败怎么办？ |
| 商品发布 | 如何发布商品？图片上传失败？ |
| 交易流程 | 如何下单？如何确认收货？ |
| 秒杀活动 | 秒杀规则？秒杀失败？ |
| 纠纷处理 | 如何申请退款？如何投诉？ |
| 其他 | 平台规则、功能建议 |

### 5.3 接口设计

#### 5.3.1 智能问答

**请求**
```http
POST /ai/service/ask
Content-Type: application/x-www-form-urlencoded

question=如何认证校园身份？
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "answerId": 1,
    "question": "如何认证校园身份？",
    "answer": "您可以在「我的-认证中心」上传学生证或校园卡照片，系统会在1-3个工作日内完成审核。",
    "confidence": 0.95,
    "matchedQuestion": "如何认证校园身份？",
    "relatedQuestions": [
      "认证失败怎么办？",
      "学生证信息不清晰怎么办？"
    ]
  }
}
```

#### 5.3.2 获取常见问题列表

**请求**
```http
GET /ai/service/faq/list
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "categoryName": "账号认证",
      "questions": [
        { "id": 101, "question": "如何认证校园身份？" },
        { "id": 102, "question": "认证失败怎么办？" }
      ]
    },
    {
      "id": 2,
      "categoryName": "商品发布",
      "questions": [
        { "id": 201, "question": "如何发布商品？" },
        { "id": 202, "question": "图片上传失败怎么办？" }
      ]
    }
  ]
}
```

#### 5.3.3 搜索问题

**请求**
```http
GET /ai/service/search?keyword=认证
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 101,
      "question": "如何认证校园身份？",
      "answer": "您可以在「我的-认证中心」上传学生证...",
      "matchScore": 0.95
    }
  ]
}
```

#### 5.3.4 获取问题分类

**请求**
```http
GET /ai/service/categories
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 1, "categoryName": "账号认证", "icon": "verify" },
    { "id": 2, "categoryName": "商品发布", "icon": "publish" },
    { "id": 3, "categoryName": "交易流程", "icon": "trade" },
    { "id": 4, "categoryName": "秒杀活动", "icon": "seckill" },
    { "id": 5, "categoryName": "纠纷处理", "icon": "dispute" }
  ]
}
```

#### 5.3.5 转人工客服

**请求**
```http
POST /ai/service/transfer
Authorization: Bearer {token}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "ticketId": "TK20240501001",
    "status": "WAITING",
    "estimatedWaitTime": 5
  }
}
```

#### 5.3.6 反馈问答结果

**请求**
```http
POST /ai/service/feedback?answerId=1&helpful=true
Authorization: Bearer {token}
```

#### 5.3.7 获取聊天历史

**请求**
```http
GET /ai/service/history?pageNum=1&pageSize=10
Authorization: Bearer {token}
```

### 5.4 数据模型

#### 5.4.1 智能客服问题表

```sql
CREATE TABLE t_ai_service_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL COMMENT '问题分类ID',
    question_content VARCHAR(500) NOT NULL COMMENT '问题内容',
    answer_content TEXT NOT NULL COMMENT '回答内容',
    keyword VARCHAR(200) COMMENT '匹配关键词，多个用逗号分隔',
    enable_status TINYINT DEFAULT 1 COMMENT '启用状态',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    helpful_count INT DEFAULT 0 COMMENT '有帮助次数',
    unhelpful_count INT DEFAULT 0 COMMENT '无帮助次数',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '智能客服问题表';
```

### 5.5 匹配算法

```java
@Service
public class QuestionMatchService {

    /**
     * 问题匹配算法
     * 1. 关键词精确匹配
     * 2. 模糊匹配（相似度计算）
     * 3. 返回最佳匹配结果
     */
    public MatchResult matchQuestion(String userQuestion) {
        // 1. 分词处理
        List<String> keywords = segment(userQuestion);

        // 2. 关键词匹配
        List<Question> candidates = findByKeywords(keywords);

        // 3. 计算相似度，返回最佳匹配
        return findBestMatch(userQuestion, candidates);
    }
}
```

### 5.6 转人工规则

| 场景 | 规则 |
|------|------|
| 匹配失败 | 未找到相关问题（相似度<0.6） |
| 用户主动 | 用户点击「转人工」按钮 |
| 连续失败 | 连续3次问答用户反馈无帮助 |
| 敏感问题 | 检测到敏感关键词（投诉、举报等） |

---

## 六、技术架构

### 6.1 技术选型

| 组件 | 技术 | 说明 |
|------|------|------|
| 开发框架 | Spring Boot 3.2+ | 主框架 |
| 服务注册 | Nacos | 服务发现与配置中心 |
| 网关路由 | Spring Cloud Gateway | API网关 |
| 数据库 | MySQL 8.0 | 主数据存储 |
| 缓存 | Redis 7.0 | 推荐结果缓存、会话缓存 |
| 搜索引擎 | Elasticsearch 8.0 | 商品检索、相似商品推荐 |
| 消息队列 | RocketMQ 5.0 | 异步处理、削峰填谷 |
| AI能力 | 阿里云视觉AI | 拍照分类 |
| 容器化 | Docker | 部署容器化 |

### 6.2 服务依赖

```
┌─────────────────────────────────────────────────────────┐
│                     fnusale-gateway                      │
│                        (8080)                           │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                   fnusale-ai-guide                       │
│                        (8104)                           │
├─────────────────────────────────────────────────────────┤
│  AiCategoryController  │  AiPriceController             │
│  AiRecommendController │  AiServiceController           │
└───────────┬─────────────┬───────────────┬───────────────┘
            │             │               │
     ┌──────▼──────┐ ┌────▼────┐   ┌──────▼──────┐
     │   MySQL     │ │  Redis  │   │Elasticsearch│
     │   (3307)    │ │  (6379) │   │   (9200)    │
     └─────────────┘ └─────────┘   └─────────────┘
            │
     ┌──────▼──────┐
     │  RocketMQ   │
     │   (5672)    │
     └─────────────┘
```

### 6.3 目录结构

```
fnusale-ai-guide/
├── src/main/java/com/fnusale/aiguide/
│   ├── controller/
│   │   ├── AiCategoryController.java      # AI拍照分类
│   │   ├── AiPriceController.java         # AI价格参考
│   │   ├── AiRecommendController.java     # 个性化推荐
│   │   └── AiServiceController.java       # 智能客服
│   ├── service/
│   │   ├── AliyunVisionService.java       # 阿里云视觉AI
│   │   ├── PriceReferenceService.java     # 价格参考
│   │   ├── RecommendService.java          # 推荐服务
│   │   └── QuestionMatchService.java      # 问题匹配
│   ├── mapper/
│   │   ├── UserBehaviorMapper.java        # 用户行为
│   │   ├── PriceReferenceMapper.java      # 价格参考
│   │   └── ServiceQuestionMapper.java     # 客服问题
│   ├── dto/
│   │   └── ...                            # 数据传输对象
│   ├── vo/
│   │   └── ...                            # 视图对象
│   └── FnusaleAiGuideApplication.java     # 启动类
└── src/main/resources/
    ├── application.yml                    # 配置文件
    └── mapper/                            # MyBatis映射文件
```

---

## 七、性能指标

### 7.1 接口性能

| 接口 | 响应时间 | QPS |
|------|----------|-----|
| AI分类识别 | ≤500ms | 100 |
| 价格参考 | ≤100ms | 500 |
| 首页推荐 | ≤100ms | 500 |
| 智能问答 | ≤200ms | 200 |

### 7.2 AI准确率

| 功能 | 准确率目标 |
|------|------------|
| 拍照分类 | ≥85% |
| 问题匹配 | ≥90% |
| 推荐点击率 | ≥5% |

### 7.3 服务可用性

| 指标 | 目标 |
|------|------|
| 服务可用性 | ≥99.5% |
| 错误率 | ≤0.1% |

---

## 八、监控告警

### 8.1 监控指标

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| AI分类接口成功率 | 阿里云API调用成功率 | <95% |
| AI分类接口延迟 | 接口响应时间 | >1s |
| 推荐缓存命中率 | Redis缓存命中情况 | <80% |
| 智能客服匹配率 | 问题匹配成功率 | <70% |

### 8.2 Prometheus指标

```yaml
# AI分类相关
ai_category_request_total{status="success|failed"}
ai_category_request_duration_seconds
ai_category_accuracy_rate

# 价格参考相关
ai_price_request_total
ai_price_cache_hit_rate

# 推荐相关
ai_recommend_request_total
ai_recommend_click_rate
ai_recommend_cache_hit_rate

# 智能客服相关
ai_service_request_total
ai_service_match_rate
ai_service_transfer_rate
```

---

## 九、数据安全

### 9.1 数据脱敏

| 数据类型 | 脱敏规则 |
|----------|----------|
| 用户ID | 推荐日志中脱敏存储 |
| 行为数据 | 仅存储必要字段，定期清理 |

### 9.2 隐私保护

- 用户行为数据仅用于推荐，不对外暴露
- 用户可关闭个性化推荐功能
- 聊天记录定期清理（保留3个月）

---

## 十、扩展规划

### 10.1 短期规划（v1.1）

- [ ] 推荐算法优化（引入深度学习模型）
- [ ] 智能客服引入大语言模型
- [ ] 价格预测功能

### 10.2 中期规划（v1.2）

- [ ] 图像识别能力扩展（成色识别、瑕疵检测）
- [ ] 智能议价助手
- [ ] 商品描述智能生成

### 10.3 长期规划（v2.0）

- [ ] 多模态推荐（图像+文本+行为）
- [ ] 智能定价系统
- [ ] AI客服全流程自动化