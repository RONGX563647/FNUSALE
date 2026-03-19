# 校园二手交易平台 - API接口文档

## 文档说明

### 基础信息
- **项目名称**: FNUSALE校园二手交易平台
- **版本**: v1.1.1
- **基础URL**: `http://localhost:8080`
- **接口协议**: RESTful API
- **数据格式**: JSON
- **字符编码**: UTF-8

### 认证方式
系统采用JWT（JSON Web Token）进行身份认证，除登录、注册等公开接口外，其他接口均需在请求头中携带Token。

**请求头示例**:
```
Authorization: Bearer {accessToken}
```

### 通用响应格式

**说明**: 所有接口统一返回 `Result<T>` 格式的响应数据，包含以下字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 响应消息，成功时为"操作成功"，失败时为错误描述 |
| data | T | 响应数据，成功时返回业务数据，失败时为null |

#### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**说明**: 
- `code` 固定为 200
- `message` 通常为"操作成功"或具体操作的成功提示（如"发布成功"、"删除成功"等）
- `data` 包含实际业务数据，可能是对象、数组或基本类型

#### 失败响应
```json
{
  "code": 400,
  "message": "错误信息描述",
  "data": null
}
```

**说明**:
- `code` 为非200的错误码，具体见错误码说明
- `message` 包含具体的错误描述信息
- `data` 失败时固定为 null

#### 分页响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "list": []
  }
}
```

**说明**:
- `total`: 总记录数
- `pageNum`: 当前页码
- `pageSize`: 每页数量
- `pages`: 总页数
- `list`: 当前页数据列表

### 状态码说明
| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 一、用户模块 API

### 1.1 手机号注册

**接口地址**: `POST /user/register/phone`

**接口描述**: 使用手机号注册新用户账号

**请求参数**:
```json
{
  "username": "张三",
  "phone": "13800138000",
  "password": "password123",
  "campusEmail": "zhangsan@campus.edu.cn",
  "identityType": "STUDENT"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| phone | String | 是 | 手机号（11位） |
| password | String | 是 | 密码 |
| campusEmail | String | 否 | 校园邮箱 |
| identityType | String | 否 | 身份类型：STUDENT-学生，TEACHER-教职工 |

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

### 1.2 邮箱注册

**接口地址**: `POST /user/register/email`

**接口描述**: 使用邮箱注册新用户账号

**请求参数**:
```json
{
  "username": "张三",
  "email": "zhangsan@campus.edu.cn",
  "password": "password123",
  "captcha": "123456",
  "identityType": "STUDENT"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名（2-20字符） |
| email | String | 是 | 邮箱地址 |
| password | String | 是 | 密码（6-20位，含字母和数字） |
| captcha | String | 是 | 邮箱验证码（6位数字） |
| identityType | String | 否 | 身份类型：STUDENT-学生，TEACHER-教职工 |

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

### 1.3 用户登录

**接口地址**: `POST /user/login`

**接口描述**: 使用手机号/邮箱和密码登录，返回JWT令牌。支持手机号或邮箱登录。

**请求参数**:
```json
{
  "account": "13800138000",
  "password": "password123"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| account | String | 是 | 手机号或邮箱地址 |
| password | String | 是 | 密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userInfo": {
      "id": 1,
      "username": "张三",
      "phone": "138****8000",
      "identityType": "STUDENT",
      "authStatus": "UNAUTH",
      "creditScore": 100
    }
  }
}
```

---

### 1.4 验证码登录

**接口地址**: `POST /user/login/captcha`

**接口描述**: 使用手机号/邮箱+验证码登录，用户不存在则自动注册

**请求参数**:
```json
{
  "account": "13800138000",
  "captcha": "123456"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| account | String | 是 | 手机号或邮箱地址 |
| captcha | String | 是 | 验证码（6位数字） |

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "isNewUser": false,
    "userInfo": {
      "id": 1,
      "username": "张三",
      "phone": "138****8000",
      "identityType": "STUDENT",
      "authStatus": "UNAUTH",
      "creditScore": 100
    }
  }
}
```

---

### 1.5 发送验证码

**接口地址**: `POST /user/captcha/send`

**接口描述**: 发送手机短信或邮箱验证码

**请求参数**:
```json
{
  "account": "13800138000",
  "type": "LOGIN"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| account | String | 是 | 手机号或邮箱地址 |
| type | String | 是 | 验证码类型：REGISTER-注册，LOGIN-登录，RESET_PASSWORD-重置密码，BIND-绑定 |

**响应示例**:
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": null
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "发送频率过高，请60秒后再试",
  "data": null
}
```

---

### 1.6 用户登出

**接口地址**: `POST /user/logout`

**接口描述**: 退出登录，清除Token

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

### 1.4 刷新Token

**接口地址**: `POST /user/refresh-token`

**接口描述**: 使用刷新令牌获取新的访问令牌

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| refreshToken | String | 是 | 刷新令牌 |

**响应示例**:
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  }
}
```

---

### 1.5 获取当前用户信息

**接口地址**: `GET /user/info`

**接口描述**: 获取当前登录用户的详细信息

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "张三",
    "phone": "138****8000",
    "campusEmail": "zhangsan@campus.edu.cn",
    "identityType": "STUDENT",
    "authStatus": "AUTH_SUCCESS",
    "creditScore": 100,
    "locationPermission": "ALLOW",
    "createTime": "2024-01-01 12:00:00"
  }
}
```

---

### 1.6 更新用户信息

**接口地址**: `PUT /user/info`

**接口描述**: 更新当前用户的基本信息

**请求头**: 需要认证

**请求参数**:
```json
{
  "username": "李四",
  "campusEmail": "lisi@campus.edu.cn"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 1.7 修改密码

**接口地址**: `PUT /user/password`

**接口描述**: 修改当前用户密码

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

### 1.8 校园身份认证

**接口地址**: `POST /user/auth`

**接口描述**: 提交校园卡/学生证进行身份认证

**请求头**: 需要认证

**请求参数**:
```json
{
  "studentTeacherId": "2021001001",
  "authImageUrl": "https://oss.example.com/auth.jpg"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| studentTeacherId | String | 是 | 学号/工号 |
| authImageUrl | String | 是 | 校园卡/学生证图片地址 |

**响应示例**:
```json
{
  "code": 200,
  "message": "认证申请已提交，请等待审核",
  "data": null
}
```

---

### 1.9 获取认证状态

**接口地址**: `GET /user/auth/status`

**接口描述**: 获取当前用户的认证状态

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "authStatus": "AUTH_SUCCESS",
    "authResultRemark": "认证通过"
  }
}
```

---

### 1.10 获取用户详情

**接口地址**: `GET /user/{userId}`

**接口描述**: 根据用户ID获取用户公开信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "张三",
    "identityType": "STUDENT",
    "creditScore": 100,
    "authStatus": "AUTH_SUCCESS"
  }
}
```

---

### 1.11 更新定位权限

**接口地址**: `PUT /user/location-permission`

**接口描述**: 更新用户定位授权状态

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| permission | String | 是 | 定位权限状态：ALLOW-允许，DENY-拒绝 |

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 1.12 校验定位是否在校园内

**接口地址**: `GET /user/location/verify`

**接口描述**: 校验用户当前定位是否在校园围栏内

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| longitude | String | 是 | 经度 |
| latitude | String | 是 | 纬度 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

### 1.13 获取我的发布列表

**接口地址**: `GET /user/my/products`

**接口描述**: 获取当前用户发布的商品列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

### 1.14 获取我的订单列表

**接口地址**: `GET /user/my/orders`

**接口描述**: 获取当前用户的订单列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | String | 否 | - | 订单状态 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 10,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

### 1.15 获取我的收藏列表

**接口地址**: `GET /user/my/favorites`

**接口描述**: 获取当前用户收藏的商品列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 8,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

### 1.16 每日签到

**接口地址**: `POST /user/sign`

**接口描述**: 用户每日签到，获取积分奖励

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "continuousDays": 5,
    "rewardPoints": 1,
    "extraRewardPoints": 0,
    "totalPoints": 156
  }
}
```

---

### 1.17 查询签到状态

**接口地址**: `GET /user/sign/status`

**接口描述**: 查询今日是否已签到及签到统计

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hasSignedToday": true,
    "continuousDays": 5,
    "totalSignDays": 30,
    "todayRewardPoints": 1
  }
}
```

---

### 1.17.1 签到统计

**接口地址**: `GET /user/sign/statistics`

**接口描述**: 获取签到统计信息，包括连续签到天数、累计签到天数、下次奖励等

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hasSignedToday": true,
    "continuousDays": 5,
    "totalSignDays": 30,
    "todayRewardPoints": 1,
    "nextRewardDays": 7,
    "nextRewardPoints": 10,
    "totalPoints": 156
  }
}
```

**说明**:
- `hasSignedToday`: 今日是否已签到
- `continuousDays`: 连续签到天数
- `totalSignDays`: 累计签到天数
- `todayRewardPoints`: 今日签到获得积分
- `nextRewardDays`: 距离下次连续签到奖励还需天数
- `nextRewardPoints`: 下次连续签到奖励积分
- `totalPoints`: 当前总积分

---

### 1.18 获取签到记录

**接口地址**: `GET /user/sign/records`

**接口描述**: 获取签到历史记录

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 30,
    "list": [
      {
        "signDate": "2024-01-15",
        "signTime": "2024-01-15 08:30:00",
        "continuousDays": 5,
        "rewardPoints": 1,
        "isRepair": 0
      }
    ]
  }
}
```

---

### 1.19 签到补签

**接口地址**: `POST /user/sign/repair`

**接口描述**: 补签遗漏的签到，消耗积分

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| signDate | String | 是 | 补签日期（YYYY-MM-DD格式） |

**响应示例**:
```json
{
  "code": 200,
  "message": "补签成功",
  "data": {
    "costPoints": 10,
    "remainingPoints": 146
  }
}
```

---

### 1.20 获取签到日历

**接口地址**: `GET /user/sign/calendar/{month}`

**接口描述**: 获取指定月份的签到日历

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| month | String | 是 | 月份（YYYY-MM格式） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "month": "2024-01",
    "signedDays": [1, 2, 3, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15],
    "continuousDays": 5,
    "totalSignDays": 13
  }
}
```

---

### 1.21 获取我的积分信息

**接口地址**: `GET /user/sign/points`

**接口描述**: 获取当前用户积分信息

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalPoints": 500,
    "availablePoints": 156,
    "usedPoints": 344
  }
}
```

---

### 1.22 获取积分变动记录

**接口地址**: `GET /user/sign/points/logs`

**接口描述**: 获取积分变动历史记录

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 50,
    "list": [
      {
        "changeType": "SIGN_REWARD",
        "changeAmount": 1,
        "beforePoints": 155,
        "afterPoints": 156,
        "remark": "每日签到奖励",
        "createTime": "2024-01-15 08:30:00"
      }
    ]
  }
}
```

---

### 1.23 提交用户评价

**接口地址**: `POST /user/evaluation`

**接口描述**: 交易完成后对交易对方进行评价

**请求头**: 需要认证

**请求参数**:
```json
{
  "orderId": 1001,
  "score": 5,
  "evaluationTag": "发货快,成色相符,态度好",
  "evaluationContent": "商品很好，卖家很热情",
  "evaluationImageUrl": "https://oss.example.com/eval.jpg",
  "isAnonymous": false
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |
| score | Integer | 是 | 评分（1-5星） |
| evaluationTag | String | 否 | 评价标签（多个逗号分隔） |
| evaluationContent | String | 否 | 评价内容 |
| evaluationImageUrl | String | 否 | 评价图片地址 |
| isAnonymous | Boolean | 否 | 是否匿名评价，默认false |

**响应示例**:
```json
{
  "code": 200,
  "message": "评价成功",
  "data": null
}
```

---

### 1.24 追加评价

**接口地址**: `POST /user/evaluation/{id}/append`

**接口描述**: 在原有评价基础上追加内容

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 评价ID |

**请求参数**:
```json
{
  "appendContent": "补充：使用一周后依然很好"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "追加成功",
  "data": null
}
```

---

### 1.25 卖家回复评价

**接口地址**: `POST /user/evaluation/{id}/reply`

**接口描述**: 卖家对收到的评价进行回复

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 评价ID |

**请求参数**:
```json
{
  "replyContent": "感谢您的认可，欢迎再次光临"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "回复成功",
  "data": null
}
```

---

### 1.26 获取用户评价列表

**接口地址**: `GET /user/evaluation/user/{userId}`

**接口描述**: 获取用户收到的评价列表

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 25,
    "list": [
      {
        "id": 1,
        "orderId": 1001,
        "evaluatorName": "李**",
        "score": 5,
        "evaluationTag": "发货快,成色相符",
        "evaluationContent": "商品很好",
        "createTime": "2024-01-15 10:00:00",
        "replyContent": "感谢认可",
        "replyTime": "2024-01-15 12:00:00"
      }
    ]
  }
}
```

---

### 1.26.1 获取我的评价

**接口地址**: `GET /user/evaluation/my`

**接口描述**: 获取当前用户发出的评价列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 15,
    "list": [
      {
        "id": 1,
        "orderId": 1001,
        "evaluatedName": "张**",
        "score": 5,
        "evaluationTag": "发货快,成色相符",
        "evaluationContent": "商品很好，卖家很热情",
        "createTime": "2024-01-15 10:00:00",
        "isAnonymous": false
      }
    ]
  }
}
```

**说明**:
- 该接口返回当前用户发出的所有评价
- `evaluatedName`: 被评价用户的脱敏名称
- `isAnonymous`: 是否匿名评价

---

### 1.27 获取用户评价统计

**接口地址**: `GET /user/evaluation/rating/{userId}`

**接口描述**: 获取用户评价统计数据

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "overallRating": 4.85,
    "ratingLevel": "EXCELLENT",
    "totalEvaluations": 25,
    "positiveCount": 23,
    "neutralCount": 2,
    "negativeCount": 0,
    "positiveRate": 92.00
  }
}
```

---

### 1.28 获取用户评价标签统计

**接口地址**: `GET /user/evaluation/tags/{userId}`

**接口描述**: 获取用户评价标签统计

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {"tagName": "发货快", "tagCount": 18},
    {"tagName": "成色相符", "tagCount": 15},
    {"tagName": "态度好", "tagCount": 12}
  ]
}
```

---

### 1.29 举报评价

**接口地址**: `POST /user/evaluation/{id}/report`

**接口描述**: 举报恶意或虚假评价

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 评价ID |

**请求参数**:
```json
{
  "reportReason": "恶意差评",
  "reportDesc": "该用户未实际购买，恶意刷差评"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "举报成功，我们将尽快处理",
  "data": null
}
```

---

### 1.30 获取活跃度排行榜

**接口地址**: `GET /rank/activity`

**接口描述**: 获取活跃度排行榜

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| type | String | 否 | daily | 排行类型：daily-日榜，weekly-周榜，monthly-月榜 |
| date | String | 否 | - | 日期，格式：yyyy-MM-dd（不传则查询当前日期） |
| limit | Integer | 否 | 100 | 返回数量限制 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "rank": 1,
      "userId": 1,
      "username": "张三",
      "avatar": "https://oss.example.com/avatar.jpg",
      "score": 156,
      "creditScore": 120
    }
  ]
}
```

---

### 1.31 获取交易排行榜

**接口地址**: `GET /rank/trade`

**接口描述**: 获取交易量/交易额排行榜

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| type | String | 否 | count | 排行类型：count-交易量，amount-交易额 |
| period | String | 否 | daily | 时间范围：daily-日榜，weekly-周榜，monthly-月榜 |
| limit | Integer | 否 | 100 | 返回数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "rank": 1,
      "userId": 1,
      "username": "张三",
      "avatar": "https://oss.example.com/avatar.jpg",
      "tradeCount": 25,
      "tradeAmount": 1580.00
    }
  ]
}
```

---

### 1.32 获取信誉排行榜

**接口地址**: `GET /rank/credit`

**接口描述**: 获取信誉分排行榜

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| limit | Integer | 否 | 100 | 返回数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "rank": 1,
      "userId": 1,
      "username": "张三",
      "avatar": "https://oss.example.com/avatar.jpg",
      "creditScore": 150,
      "ratingScore": 4.95
    }
  ]
}
```

---

### 1.33 获取好评排行榜

**接口地址**: `GET /rank/rating`

**接口描述**: 获取好评率排行榜

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| limit | Integer | 否 | 100 | 返回数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "rank": 1,
      "userId": 1,
      "username": "张三",
      "avatar": "https://oss.example.com/avatar.jpg",
      "overallRating": 5.0,
      "positiveRate": 100.00,
      "totalEvaluations": 30
    }
  ]
}
```

---

### 1.34 获取我的排名

**接口地址**: `GET /rank/my`

**接口描述**: 获取当前用户在各排行榜的排名

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "activityRank": 15,
    "activityScore": 85,
    "tradeRank": 8,
    "tradeCount": 12,
    "creditRank": 20,
    "creditScore": 110,
    "ratingRank": 5,
    "overallRating": 4.8
  }
}
```

---

### 1.35 获取我的排行奖励

**接口地址**: `GET /rank/rewards`

**接口描述**: 获取当前用户的排行奖励列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "list": [
      {
        "id": 1,
        "rankType": "ACTIVITY",
        "rankDate": "2024-01-15",
        "rankPosition": 3,
        "rewardPoints": 60,
        "isClaimed": 0,
        "createTime": "2024-01-16 00:30:00"
      }
    ]
  }
}
```

---

### 1.36 领取排行奖励

**接口地址**: `POST /rank/reward/{id}`

**接口描述**: 领取排行奖励积分

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 奖励记录ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "领取成功",
  "data": {
    "rewardPoints": 60,
    "totalPoints": 216
  }
}
```

---

### 1.37 获取排行榜历史

**接口地址**: `GET /rank/history`

**接口描述**: 获取历史排行记录

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| rankType | String | 是 | - | 排行类型：ACTIVITY-活跃度，TRADE-交易，CREDIT-信誉，RATING-好评 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 30,
    "list": [
      {
        "userId": 1,
        "username": "张三",
        "avatar": "https://oss.example.com/avatar.jpg",
        "rankPosition": 3,
        "rankType": "ACTIVITY",
        "rankDate": "2024-01-15",
        "score": 85
      }
    ]
  }
}
```

---

### 1.38 IP定位

**接口地址**: `GET /user/location/ip`

**接口描述**: 根据用户IP地址获取大致位置信息

**请求头**: 无需认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "longitude": "116.407526",
    "latitude": "39.904030",
    "province": "北京市",
    "city": "北京市",
    "district": "海淀区",
    "address": "北京市海淀区",
    "inCampus": true
  }
}
```

**说明**: 
- 该接口通过用户IP地址进行定位，精度较低，建议优先使用前端GPS定位
- `inCampus`字段表示是否在校园围栏内

---

### 1.39 逆地理编码

**接口地址**: `GET /user/location/geocode`

**接口描述**: 将经纬度坐标转换为详细地址

**请求头**: 无需认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| longitude | String | 是 | 经度 |
| latitude | String | 是 | 纬度 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "longitude": "116.407526",
    "latitude": "39.904030",
    "province": "北京市",
    "city": "北京市",
    "district": "海淀区",
    "address": "北京市海淀区中关村大街1号",
    "inCampus": true
  }
}
```

**说明**: 
- 该接口将经纬度坐标转换为详细地址信息
- `inCampus`字段表示该坐标是否在校园围栏内

---

### 1.40 综合定位

**接口地址**: `GET /user/location/current`

**接口描述**: 优先使用前端传递的经纬度，否则使用IP定位

**请求头**: 无需认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| longitude | String | 否 | 经度（可选，不传则使用IP定位） |
| latitude | String | 否 | 纬度（可选，不传则使用IP定位） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "longitude": "116.407526",
    "latitude": "39.904030",
    "province": "北京市",
    "city": "北京市",
    "district": "海淀区",
    "address": "北京市海淀区中关村大街1号",
    "inCampus": true
  }
}
```

**说明**: 
- 如果前端传递了经纬度参数，则使用经纬度进行逆地理编码
- 如果未传递经纬度参数，则使用IP地址进行定位
- 建议前端优先获取GPS定位后传递经纬度参数，定位更准确

---

## 二、用户地址模块 API

### 2.1 获取我的地址列表

**接口地址**: `GET /user/address/list`

**接口描述**: 获取当前用户的所有地址

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "addressType": "PICK_POINT",
      "pickPointName": "1号宿舍楼楼下",
      "isDefault": 1
    }
  ]
}
```

---

### 2.2 获取地址详情

**接口地址**: `GET /user/address/{id}`

**接口描述**: 根据ID获取地址详细信息

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "addressType": "PICK_POINT",
    "pickPointId": 1,
    "pickPointName": "1号宿舍楼楼下",
    "isDefault": 1
  }
}
```

---

### 2.3 新增地址

**接口地址**: `POST /user/address`

**接口描述**: 添加新地址

**请求头**: 需要认证

**请求参数**:
```json
{
  "addressType": "PICK_POINT",
  "pickPointId": 1,
  "customAddress": "",
  "longitude": "116.407526",
  "latitude": "39.904030",
  "isDefault": 0
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| addressType | String | 是 | 地址类型：PICK_POINT-自提点，CUSTOM-自定义 |
| pickPointId | Long | 否 | 自提点ID（addressType为PICK_POINT时必填） |
| customAddress | String | 否 | 自定义详细地址（addressType为CUSTOM时必填） |
| longitude | String | 否 | 经度 |
| latitude | String | 否 | 纬度 |
| isDefault | Integer | 否 | 是否默认地址：0-否，1-是 |

**响应示例**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

### 2.4 更新地址

**接口地址**: `PUT /user/address/{id}`

**接口描述**: 更新地址信息

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

**请求参数**: 同新增地址

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 2.5 删除地址

**接口地址**: `DELETE /user/address/{id}`

**接口描述**: 删除地址

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 2.6 设置默认地址

**接口地址**: `PUT /user/address/{id}/default`

**接口描述**: 设置指定地址为默认地址

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "设置成功",
  "data": null
}
```

---

### 2.7 获取默认地址

**接口地址**: `GET /user/address/default`

**接口描述**: 获取当前用户的默认地址

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "addressType": "PICK_POINT",
    "pickPointName": "1号宿舍楼楼下",
    "isDefault": 1
  }
}
```

---

## 三、校园自提点模块 API

### 3.1 获取自提点列表

**接口地址**: `GET /user/pick-point/list`

**接口描述**: 获取所有启用的校园自提点列表

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "pickPointName": "1号宿舍楼楼下",
      "campusArea": "东校区",
      "detailAddress": "1号宿舍楼一楼大厅",
      "longitude": "116.407526",
      "latitude": "39.904030"
    }
  ]
}
```

---

### 3.2 获取附近自提点

**接口地址**: `GET /user/pick-point/nearby`

**接口描述**: 根据定位获取附近的校园自提点

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| longitude | String | 是 | - | 经度 |
| latitude | String | 是 | - | 纬度 |
| distance | Integer | 否 | 1000 | 距离范围（米） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "pickPointName": "1号宿舍楼楼下",
      "distance": 500
    }
  ]
}
```

---

### 3.3 获取自提点详情

**接口地址**: `GET /user/pick-point/{id}`

**接口描述**: 根据ID获取自提点详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 自提点ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "pickPointName": "1号宿舍楼楼下",
    "campusArea": "东校区",
    "detailAddress": "1号宿舍楼一楼大厅",
    "longitude": "116.407526",
    "latitude": "39.904030",
    "enableStatus": 1
  }
}
```

---

### 3.4 新增自提点（管理员）

**接口地址**: `POST /user/pick-point`

**接口描述**: 添加新的校园自提点

**请求头**: 需要管理员权限

**请求参数**:
```json
{
  "pickPointName": "图书馆驿站",
  "campusArea": "东校区",
  "detailAddress": "图书馆一楼",
  "longitude": "116.407526",
  "latitude": "39.904030"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

### 3.5 更新自提点（管理员）

**接口地址**: `PUT /user/pick-point/{id}`

**接口描述**: 更新自提点信息

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 自提点ID |

**请求参数**: 同新增自提点

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 3.6 删除自提点（管理员）

**接口地址**: `DELETE /user/pick-point/{id}`

**接口描述**: 删除自提点

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 自提点ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 3.7 启用/禁用自提点（管理员）

**接口地址**: `PUT /user/pick-point/{id}/status`

**接口描述**: 切换自提点启用状态

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 自提点ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 是 | 启用状态：0-禁用，1-启用 |

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 3.8 分页查询自提点（管理员）

**接口地址**: `GET /user/pick-point/page`

**接口描述**: 分页查询自提点列表

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| campusArea | String | 否 | - | 校区 |
| status | Integer | 否 | - | 启用状态 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": []
  }
}
```

---

## 四、商品模块 API

### 4.1 发布商品

**接口地址**: `POST /product`

**接口描述**: 发布新商品

**请求头**: 需要认证

**请求参数**:
```json
{
  "productName": "大学物理教材",
  "categoryId": 1,
  "newDegree": "90_NEW",
  "price": 35.00,
  "originalPrice": 68.00,
  "productDesc": "九成新大学物理教材，无笔记无划线",
  "imageUrls": [
    "https://oss.example.com/img1.jpg",
    "https://oss.example.com/img2.jpg"
  ],
  "pickPointId": 1,
  "longitude": "116.407526",
  "latitude": "39.904030",
  "isSeckill": 0,
  "seckillStock": null
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productName | String | 是 | 商品名称 |
| categoryId | Long | 是 | 品类ID |
| newDegree | String | 是 | 新旧程度：NEW-全新，90_NEW-9成新，80_NEW-8成新，70_NEW-7成新，OLD-老旧 |
| price | BigDecimal | 是 | 售价 |
| originalPrice | BigDecimal | 否 | 原价 |
| productDesc | String | 否 | 商品描述 |
| imageUrls | List<String> | 是 | 商品图片URL列表（至少1张，最多9张） |
| pickPointId | Long | 否 | 自提点ID |
| longitude | String | 否 | 经度 |
| latitude | String | 否 | 纬度 |
| isSeckill | Integer | 否 | 是否秒杀商品：0-否，1-是 |
| seckillStock | Integer | 否 | 秒杀库存（isSeckill为1时必填） |

**响应示例**:
```json
{
  "code": 200,
  "message": "发布成功",
  "data": 1001
}
```

---

### 4.2 更新商品

**接口地址**: `PUT /product/{id}`

**接口描述**: 更新商品信息

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**请求参数**: 同发布商品

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 4.3 删除商品

**接口地址**: `DELETE /product/{id}`

**接口描述**: 删除商品（逻辑删除）

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 4.4 获取商品详情

**接口地址**: `GET /product/{id}`

**接口描述**: 根据ID获取商品详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "productName": "大学物理教材",
    "categoryId": 1,
    "categoryName": "教材",
    "newDegree": "90_NEW",
    "newDegreeDesc": "9成新",
    "price": 35.00,
    "originalPrice": 68.00,
    "productDesc": "九成新大学物理教材，无笔记无划线",
    "imageUrls": [
      "https://oss.example.com/img1.jpg",
      "https://oss.example.com/img2.jpg"
    ],
    "mainImageUrl": "https://oss.example.com/img1.jpg",
    "isSeckill": 0,
    "pickPointId": 1,
    "pickPointName": "1号宿舍楼楼下",
    "productStatus": "ON_SHELF",
    "userId": 1,
    "publisherName": "张三",
    "publisherCreditScore": 100,
    "distance": 500.5,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

---

### 4.5 分页查询商品

**接口地址**: `POST /product/page`

**接口描述**: 分页查询商品列表，支持多条件筛选

**请求参数**:
```json
{
  "keyword": "教材",
  "categoryId": 1,
  "minPrice": 10.00,
  "maxPrice": 100.00,
  "newDegree": "90_NEW",
  "isSeckill": 0,
  "longitude": "116.407526",
  "latitude": "39.904030",
  "sortBy": "distance",
  "pageNum": 1,
  "pageSize": 10
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "list": []
  }
}
```

---

### 4.6 搜索商品

**接口地址**: `GET /product/search`

**接口描述**: 关键词搜索商品

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 关键词 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 5,
    "list": []
  }
}
```

---

### 4.7 上架商品

**接口地址**: `PUT /product/{id}/on-shelf`

**接口描述**: 将商品上架

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "上架成功",
  "data": null
}
```

---

### 4.8 下架商品

**接口地址**: `PUT /product/{id}/off-shelf`

**接口描述**: 将商品下架

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "下架成功",
  "data": null
}
```

---

### 4.9 AI识别品类

**接口地址**: `POST /product/ai-category`

**接口描述**: 上传图片识别商品品类

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| imageUrl | String | 是 | 图片URL |

**响应示例**:
```json
{
  "code": 200,
  "message": "识别成功",
  "data": {
    "categoryId": 1,
    "categoryName": "教材",
    "confidence": 0.95
  }
}
```

---

### 4.10 获取推荐商品

**接口地址**: `GET /product/recommend`

**接口描述**: 获取个性化推荐商品列表

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 30,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 3,
    "list": []
  }
}
```

---

### 4.11 收藏商品

**接口地址**: `POST /product/{id}/favorite`

**接口描述**: 收藏指定商品

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": null
}
```

---

### 4.12 取消收藏

**接口地址**: `DELETE /product/{id}/favorite`

**接口描述**: 取消收藏指定商品

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": null
}
```

---

### 4.13 点赞商品

**接口地址**: `POST /product/{id}/like`

**接口描述**: 点赞指定商品

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": null
}
```

---

### 4.14 取消点赞

**接口地址**: `DELETE /product/{id}/like`

**接口描述**: 取消点赞指定商品

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": null
}
```

---

### 4.15 获取附近商品

**接口地址**: `GET /product/nearby`

**接口描述**: 根据定位获取附近商品列表

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| longitude | String | 是 | - | 经度 |
| latitude | String | 是 | - | 纬度 |
| distance | Integer | 否 | 1000 | 距离范围（米） |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": []
  }
}
```

---

### 4.16 保存草稿

**接口地址**: `POST /product/draft`

**接口描述**: 保存商品草稿

**请求头**: 需要认证

**请求参数**: 同发布商品

**响应示例**:
```json
{
  "code": 200,
  "message": "保存成功",
  "data": 1002
}
```

---

### 4.17 获取草稿列表

**接口地址**: `GET /product/draft/list`

**接口描述**: 获取当前用户的草稿列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 3,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

### 4.18 内部接口说明

**说明**: 以下接口为内部接口，仅供其他微服务调用，不对外开放。

#### 4.18.1 根据ID获取商品信息

**接口地址**: `GET /product/inner/{productId}`

**接口描述**: 供其他服务调用，获取商品详细信息

**权限**: 内部服务调用

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "productName": "大学物理教材",
    "categoryId": 1,
    "categoryName": "教材",
    "price": 35.00,
    "productStatus": "ON_SHELF",
    "userId": 1
  }
}
```

---

#### 4.18.2 批量获取商品信息

**接口地址**: `POST /product/inner/batch`

**接口描述**: 供其他服务调用，批量获取商品信息

**权限**: 内部服务调用

**请求参数**:
```json
[1001, 1002, 1003]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "1001": {
      "id": 1001,
      "productName": "大学物理教材",
      "price": 35.00
    },
    "1002": {
      "id": 1002,
      "productName": "高等数学教材",
      "price": 30.00
    }
  }
}
```

---

## 五、商品品类模块 API

### 5.1 获取品类树

**接口地址**: `GET /category/tree`

**接口描述**: 获取完整的品类树形结构

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "categoryName": "教材",
      "parentCategoryId": null,
      "children": [
        {
          "id": 11,
          "categoryName": "理工类教材",
          "parentCategoryId": 1
        }
      ]
    }
  ]
}
```

---

### 5.2 获取一级品类列表

**接口地址**: `GET /category/list`

**接口描述**: 获取所有一级品类

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "categoryName": "教材",
      "aiMappingValue": "textbook"
    },
    {
      "id": 2,
      "categoryName": "电子产品",
      "aiMappingValue": "electronics"
    }
  ]
}
```

---

### 5.3 获取子品类

**接口地址**: `GET /category/children/{parentId}`

**接口描述**: 根据父ID获取子品类列表

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| parentId | Long | 是 | 父品类ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 11,
      "categoryName": "理工类教材",
      "parentCategoryId": 1
    }
  ]
}
```

---

### 5.4 获取品类详情

**接口地址**: `GET /category/{id}`

**接口描述**: 根据ID获取品类详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 品类ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "categoryName": "教材",
    "parentCategoryId": null,
    "aiMappingValue": "textbook",
    "enableStatus": 1
  }
}
```

---

### 5.5 新增品类（管理员）

**接口地址**: `POST /category`

**接口描述**: 添加新品类

**请求头**: 需要管理员权限

**请求参数**:
```json
{
  "categoryName": "体育器材",
  "parentCategoryId": null,
  "aiMappingValue": "sports"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

### 5.6 更新品类（管理员）

**接口地址**: `PUT /category/{id}`

**接口描述**: 更新品类信息

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 品类ID |

**请求参数**: 同新增品类

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 5.7 删除品类（管理员）

**接口地址**: `DELETE /category/{id}`

**接口描述**: 删除品类

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 品类ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 5.8 启用/禁用品类（管理员）

**接口地址**: `PUT /category/{id}/status`

**接口描述**: 切换品类启用状态

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 品类ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 是 | 启用状态：0-禁用，1-启用 |

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 5.9 获取热门品类

**接口地址**: `GET /category/hot`

**接口描述**: 获取热门品类列表

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "categoryName": "教材",
      "productCount": 150
    }
  ]
}
```

---

## 六、IM即时通讯模块 API

### 6.1 获取会话列表

**接口地址**: `GET /session/list`

**接口描述**: 获取当前用户的所有聊天会话

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "targetUserId": 2,
      "targetUsername": "李四",
      "productId": 1001,
      "productName": "大学物理教材",
      "lastMessageContent": "好的，明天下午可以",
      "lastMessageTime": "2024-01-01 15:30:00",
      "unreadCount": 2,
      "sessionStatus": "NORMAL"
    }
  ]
}
```

---

### 6.2 获取会话详情

**接口地址**: `GET /session/{sessionId}`

**接口描述**: 根据会话ID获取详细信息

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "user1Id": 1,
    "user2Id": 2,
    "productId": 1001,
    "lastMessageContent": "好的，明天下午可以",
    "lastMessageTime": "2024-01-01 15:30:00",
    "sessionStatus": "NORMAL"
  }
}
```

---

### 6.3 创建会话

**接口地址**: `POST /session/create`

**接口描述**: 与指定用户创建新会话（基于商品）

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetUserId | Long | 是 | 对方用户ID |
| productId | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "sessionId": 1
  }
}
```

---

### 6.4 获取或创建会话

**接口地址**: `GET /session/get-or-create`

**接口描述**: 获取与指定用户的会话，不存在则创建

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetUserId | Long | 是 | 对方用户ID |
| productId | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "sessionId": 1
  }
}
```

---

### 6.5 删除会话

**接口地址**: `DELETE /session/{sessionId}`

**接口描述**: 删除指定会话

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 6.6 获取未读消息数

**接口地址**: `GET /session/unread-count`

**接口描述**: 获取当前用户的未读消息总数

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 5
}
```

---

### 6.7 标记会话已读

**接口地址**: `PUT /session/{sessionId}/read`

**接口描述**: 将会话标记为已读

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null
}
```

---

### 6.8 获取会话消息列表

**接口地址**: `GET /session/{sessionId}/messages`

**接口描述**: 分页获取会话的消息记录

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3,
    "list": [
      {
        "id": 1,
        "sessionId": 1,
        "senderId": 1,
        "receiverId": 2,
        "messageType": "TEXT",
        "messageContent": "你好，这个还在吗？",
        "isRead": 1,
        "sendTime": "2024-01-01 15:00:00"
      }
    ]
  }
}
```

---

### 6.9 置顶会话

**接口地址**: `PUT /session/{sessionId}/pin`

**接口描述**: 将指定会话置顶

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "置顶成功",
  "data": null
}
```

---

### 6.10 取消置顶

**接口地址**: `DELETE /session/{sessionId}/pin`

**接口描述**: 取消会话置顶

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "取消置顶成功",
  "data": null
}
```

---

### 6.11 发送文字消息

**接口地址**: `POST /message/text`

**接口描述**: 发送文字消息

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |
| content | String | 是 | 消息内容 |

**响应示例**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "messageId": 4001,
    "sendTime": "2024-01-01 15:00:00"
  }
}
```

---

### 6.12 发送图片消息

**接口地址**: `POST /message/image`

**接口描述**: 发送图片消息

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |
| imageUrl | String | 是 | 图片URL |

**响应示例**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "messageId": 4002,
    "sendTime": "2024-01-01 15:01:00"
  }
}
```

---

### 6.13 发送语音消息

**接口地址**: `POST /message/voice`

**接口描述**: 发送语音消息

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |
| voiceUrl | String | 是 | 语音URL |
| duration | Integer | 是 | 语音时长（秒） |

**响应示例**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "messageId": 4003,
    "sendTime": "2024-01-01 15:02:00"
  }
}
```

---

### 6.14 撤回消息

**接口地址**: `DELETE /message/{messageId}`

**接口描述**: 撤回已发送的消息

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| messageId | Long | 是 | 消息ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "撤回成功",
  "data": null
}
```

---

### 6.15 获取快捷回复列表

**接口地址**: `GET /message/quick-reply/list`

**接口描述**: 获取系统预设的快捷回复模板和用户自定义快捷回复

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "systemReplies": [
      {
        "id": 1,
        "replyContent": "几成新？",
        "sort": 1
      },
      {
        "id": 2,
        "replyContent": "能小刀吗？",
        "sort": 2
      }
    ],
    "userReplies": [
      {
        "id": 101,
        "replyContent": "我在图书馆门口等您",
        "sort": 1
      }
    ]
  }
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| systemReplies | Array | 系统预设的快捷回复列表 |
| userReplies | Array | 用户自定义的快捷回复列表 |
| id | Long | 快捷回复ID |
| replyContent | String | 回复内容 |
| sort | Integer | 排序序号 |

---

### 6.16 添加快捷回复

**接口地址**: `POST /message/quick-reply`

**接口描述**: 添加自定义快捷回复

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| content | String | 是 | 回复内容 |

**响应示例**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

### 6.17 删除快捷回复

**接口地址**: `DELETE /message/quick-reply/{id}`

**接口描述**: 删除自定义快捷回复

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 快捷回复ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 6.18 导出聊天记录

**接口地址**: `GET /message/{sessionId}/export`

**接口描述**: 导出指定会话的聊天记录

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "导出成功",
  "data": "https://oss.example.com/chat_history_1.txt"
}
```

---

### 6.19 搜索消息

**接口地址**: `GET /message/search`

**接口描述**: 在会话中搜索消息

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |
| keyword | String | 是 | 关键词 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 10,
      "messageContent": "明天下午可以交易",
      "sendTime": "2024-01-01 15:30:00"
    }
  ]
}
```

---

## 七、AI赋能模块 API

### 7.1 AI拍照分类

#### 7.1.1 识别商品品类

**接口地址**: `POST /ai/category/recognize`

**接口描述**: 上传商品图片识别品类

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| imageUrl | String | 是 | 图片URL |

**响应示例**:
```json
{
  "code": 200,
  "message": "识别成功",
  "data": {
    "categoryId": 1,
    "categoryName": "教材",
    "confidence": 0.95,
    "aiResult": "textbook"
  }
}
```

---

#### 7.1.2 批量识别品类

**接口地址**: `POST /ai/category/batch-recognize`

**接口描述**: 批量上传图片识别品类

**请求参数**:
```json
[
  "https://oss.example.com/img1.jpg",
  "https://oss.example.com/img2.jpg"
]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "识别成功",
  "data": [
    {
      "imageUrl": "https://oss.example.com/img1.jpg",
      "categoryId": 1,
      "categoryName": "教材",
      "confidence": 0.95
    }
  ]
}
```

---

#### 7.1.3 获取识别历史

**接口地址**: `GET /ai/category/history`

**接口描述**: 获取用户的AI识别历史记录

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": []
  }
}
```

---

### 7.2 AI价格参考

#### 7.2.1 获取价格参考

**接口地址**: `GET /ai/price/reference`

**接口描述**: 根据品类和新旧程度获取建议价格区间

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | Long | 是 | 品类ID |
| newDegree | String | 是 | 新旧程度 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "categoryId": 1,
    "categoryName": "教材",
    "newDegree": "90_NEW",
    "minPrice": 20.00,
    "maxPrice": 40.00,
    "sampleCount": 150
  }
}
```

---

#### 7.2.2 获取商品定价建议

**接口地址**: `POST /ai/price/suggest`

**接口描述**: 根据商品信息获取智能定价建议

**请求参数**:
```json
{
  "productName": "大学物理教材",
  "categoryId": 1,
  "newDegree": "90_NEW",
  "productDesc": "九成新，无笔记"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "suggestedPrice": 35.00,
    "minPrice": 20.00,
    "maxPrice": 40.00,
    "confidence": 0.85
  }
}
```

---

#### 7.2.3 更新价格参考数据（管理员）

**接口地址**: `POST /ai/price/update`

**接口描述**: 更新品类价格参考数据

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

#### 7.2.4 获取价格趋势

**接口地址**: `GET /ai/price/trend`

**接口描述**: 获取指定品类的价格趋势

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | Long | 是 | 品类ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "categoryId": 1,
    "trend": [
      {
        "date": "2024-01-01",
        "avgPrice": 35.00
      }
    ]
  }
}
```

---

#### 7.2.5 比价

**接口地址**: `GET /ai/price/compare`

**接口描述**: 与同品类商品进行比价

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| price | BigDecimal | 是 | 价格 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "productId": 1001,
    "inputPrice": 35.00,
    "avgPrice": 30.00,
    "percentile": 75,
    "comparison": "高于平均价格16.7%"
  }
}
```

---

### 7.3 AI个性化推荐

#### 7.3.1 获取首页推荐

**接口地址**: `GET /ai/recommend/home`

**接口描述**: 获取首页个性化推荐商品

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 30,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 3,
    "list": []
  }
}
```

---

#### 7.3.2 获取猜你喜欢

**接口地址**: `GET /ai/recommend/guess-like`

**接口描述**: 基于用户行为的猜你喜欢推荐

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": []
  }
}
```

---

#### 7.3.3 获取相似商品

**接口地址**: `GET /ai/recommend/similar/{productId}`

**接口描述**: 获取与指定商品相似的商品

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 15,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": []
  }
}
```

---

#### 7.3.4 获取同专业推荐

**接口地址**: `GET /ai/recommend/same-major`

**接口描述**: 获取同专业同学的发布商品

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 10,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

#### 7.3.5 获取同宿舍区推荐

**接口地址**: `GET /ai/recommend/same-dorm`

**接口描述**: 获取同宿舍区用户的发布商品

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 8,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

#### 7.3.6 刷新推荐

**接口地址**: `POST /ai/recommend/refresh`

**接口描述**: 刷新推荐结果

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": null
}
```

---

#### 7.3.7 反馈推荐结果

**接口地址**: `POST /ai/recommend/feedback`

**接口描述**: 用户对推荐结果的反馈

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| feedbackType | String | 是 | 反馈类型：like-喜欢，dislike-不喜欢 |

**响应示例**:
```json
{
  "code": 200,
  "message": "反馈成功",
  "data": null
}
```

---

### 7.4 AI智能客服

#### 7.4.1 智能问答

**接口地址**: `POST /ai/service/ask`

**接口描述**: 发送问题获取AI回答

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| question | String | 是 | 问题内容 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "answerId": 1,
    "answer": "您可以点击个人中心-身份认证，上传学生证或校园卡进行认证...",
    "confidence": 0.92
  }
}
```

---

#### 7.4.2 获取常见问题列表

**接口地址**: `GET /ai/service/faq/list`

**接口描述**: 获取系统预设的常见问题

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "questionContent": "如何认证校园身份？",
      "answerContent": "您可以点击个人中心-身份认证...",
      "keyword": "认证,身份"
    }
  ]
}
```

---

#### 7.4.3 搜索问题

**接口地址**: `GET /ai/service/search`

**接口描述**: 搜索相关问题

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 关键词 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "questionContent": "如何认证校园身份？",
      "answerContent": "您可以点击个人中心-身份认证..."
    }
  ]
}
```

---

#### 7.4.4 获取问题分类

**接口地址**: `GET /ai/service/categories`

**接口描述**: 获取问题分类列表

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "categoryName": "认证相关",
      "questionCount": 10
    }
  ]
}
```

---

#### 7.4.5 转人工客服

**接口地址**: `POST /ai/service/transfer`

**接口描述**: 转接人工客服

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "已为您转接人工客服",
  "data": null
}
```

---

#### 7.4.6 反馈问答结果

**接口地址**: `POST /ai/service/feedback`

**接口描述**: 用户对问答结果的反馈

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| answerId | Long | 是 | 问答ID |
| helpful | Boolean | 是 | 是否有帮助 |

**响应示例**:
```json
{
  "code": 200,
  "message": "反馈成功",
  "data": null
}
```

---

#### 7.4.7 获取聊天历史

**接口地址**: `GET /ai/service/history`

**接口描述**: 获取用户的客服聊天历史

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

## 八、营销模块 API

### 8.1 优惠券管理

#### 8.1.1 获取可领取优惠券列表

**接口地址**: `GET /api/marketing/coupon/available`

**接口描述**: 获取当前可领取的优惠券列表

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "couponName": "毕业季满20减5券",
      "couponType": "FULL_REDUCE",
      "fullAmount": 20.00,
      "reduceAmount": 5.00,
      "startTime": "2024-06-01 00:00:00",
      "endTime": "2024-06-30 23:59:59",
      "remainCount": 100
    }
  ]
}
```

---

#### 8.1.2 领取优惠券

**接口地址**: `POST /api/marketing/coupon/{couponId}/receive`

**接口描述**: 领取指定优惠券

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| couponId | Long | 是 | 优惠券ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "领取成功",
  "data": null
}
```

---

#### 8.1.3 获取我的优惠券列表

**接口地址**: `GET /api/marketing/coupon/my`

**接口描述**: 获取当前用户的优惠券列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 否 | 状态：UNUSED-未使用，USED-已使用，EXPIRED-已过期 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "couponId": 1,
      "couponName": "毕业季满20减5券",
      "couponType": "FULL_REDUCE",
      "reduceAmount": 5.00,
      "expireTime": "2024-06-30 23:59:59",
      "couponStatus": "UNUSED"
    }
  ]
}
```

---

#### 8.1.4 获取可用优惠券

**接口地址**: `GET /api/marketing/coupon/usable`

**接口描述**: 获取指定商品可用的优惠券

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| price | BigDecimal | 是 | 商品价格 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "couponName": "毕业季满20减5券",
      "reduceAmount": 5.00
    }
  ]
}
```

---

#### 8.1.5 获取优惠券详情

**接口地址**: `GET /api/marketing/coupon/{couponId}`

**接口描述**: 获取优惠券详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| couponId | Long | 是 | 优惠券ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "couponName": "毕业季满20减5券",
    "couponType": "FULL_REDUCE",
    "fullAmount": 20.00,
    "reduceAmount": 5.00,
    "totalCount": 200,
    "receivedCount": 100,
    "usedCount": 50,
    "startTime": "2024-06-01 00:00:00",
    "endTime": "2024-06-30 23:59:59",
    "enableStatus": 1
  }
}
```

---

#### 8.1.6 新增优惠券（管理员）

**接口地址**: `POST /api/marketing/coupon`

**接口描述**: 创建新的优惠券

**请求头**: 需要管理员权限

**请求参数**:
```json
{
  "couponName": "毕业季满20减5券",
  "couponType": "FULL_REDUCE",
  "fullAmount": 20.00,
  "reduceAmount": 5.00,
  "categoryId": null,
  "totalCount": 200,
  "startTime": "2024-06-01 00:00:00",
  "endTime": "2024-06-30 23:59:59"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": null
}
```

---

#### 8.1.7 更新优惠券（管理员）

**接口地址**: `PUT /api/marketing/coupon/{couponId}`

**接口描述**: 更新优惠券信息

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| couponId | Long | 是 | 优惠券ID |

**请求参数**: 同新增优惠券

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

#### 8.1.8 删除优惠券（管理员）

**接口地址**: `DELETE /api/marketing/coupon/{couponId}`

**接口描述**: 删除优惠券

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| couponId | Long | 是 | 优惠券ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

#### 8.1.9 启用/禁用优惠券（管理员）

**接口地址**: `PUT /api/marketing/coupon/{couponId}/status`

**接口描述**: 切换优惠券启用状态

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| couponId | Long | 是 | 优惠券ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 是 | 启用状态：0-禁用，1-启用 |

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

#### 8.1.10 分页查询优惠券（管理员）

**接口地址**: `GET /api/marketing/coupon/page`

**接口描述**: 分页查询优惠券列表

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| name | String | 否 | - | 优惠券名称 |
| type | String | 否 | - | 优惠券类型 |
| status | Integer | 否 | - | 启用状态 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 5,
    "list": []
  }
}
```

---

#### 8.1.11 发放优惠券（管理员）

**接口地址**: `POST /api/marketing/coupon/{couponId}/grant`

**接口描述**: 向指定用户发放优惠券

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| couponId | Long | 是 | 优惠券ID |

**请求参数**:
```json
[1, 2, 3]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "发放成功",
  "data": null
}
```

---

### 8.2 秒杀活动管理

#### 8.2.1 获取秒杀活动列表

**接口地址**: `GET /api/marketing/seckill/list`

**接口描述**: 获取当前进行中和即将开始的秒杀活动

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "activityName": "每晚8点教材秒杀",
      "productId": 1001,
      "productName": "大学物理教材",
      "seckillPrice": 25.00,
      "originalPrice": 35.00,
      "remainStock": 10,
      "startTime": "2024-01-01 20:00:00",
      "endTime": "2024-01-01 21:00:00",
      "activityStatus": "ON_GOING"
    }
  ]
}
```

---

#### 8.2.2 获取秒杀活动详情

**接口地址**: `GET /api/marketing/seckill/{activityId}`

**接口描述**: 获取秒杀活动详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "activityName": "每晚8点教材秒杀",
    "productId": 1001,
    "seckillPrice": 25.00,
    "totalStock": 50,
    "remainStock": 10,
    "startTime": "2024-01-01 20:00:00",
    "endTime": "2024-01-01 21:00:00",
    "activityStatus": "ON_GOING"
  }
}
```

---

#### 8.2.3 获取秒杀商品详情

**接口地址**: `GET /api/marketing/seckill/product/{productId}`

**接口描述**: 获取秒杀商品的详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "productId": 1001,
    "productName": "大学物理教材",
    "seckillPrice": 25.00,
    "originalPrice": 35.00,
    "remainStock": 10,
    "activityId": 1,
    "activityStatus": "ON_GOING"
  }
}
```

---

#### 8.2.4 参与秒杀

**接口地址**: `POST /api/marketing/seckill/{activityId}/join`

**接口描述**: 参与秒杀抢购

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "秒杀请求已提交",
  "data": 2001
}
```

---

#### 8.2.5 获取秒杀结果

**接口地址**: `GET /api/marketing/seckill/{activityId}/result`

**接口描述**: 查询秒杀抢购结果

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "orderId": 2001,
    "message": "秒杀成功"
  }
}
```

---

#### 8.2.6 创建秒杀活动（管理员）

**接口地址**: `POST /api/marketing/seckill`

**接口描述**: 创建新的秒杀活动

**请求头**: 需要管理员权限

**请求参数**:
```json
{
  "activityName": "每晚8点教材秒杀",
  "productId": 1001,
  "seckillPrice": 25.00,
  "totalStock": 50,
  "startTime": "2024-01-01 20:00:00",
  "endTime": "2024-01-01 21:00:00"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": null
}
```

---

#### 8.2.7 更新秒杀活动（管理员）

**接口地址**: `PUT /api/marketing/seckill/{activityId}`

**接口描述**: 更新秒杀活动信息

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**请求参数**: 同创建秒杀活动

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

#### 8.2.8 删除秒杀活动（管理员）

**接口地址**: `DELETE /api/marketing/seckill/{activityId}`

**接口描述**: 删除秒杀活动

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

#### 8.2.9 分页查询秒杀活动（管理员）

**接口地址**: `GET /api/marketing/seckill/page`

**接口描述**: 分页查询秒杀活动列表

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | String | 否 | - | 活动状态 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": []
  }
}
```

---

#### 8.2.10 获取今日秒杀

**接口地址**: `GET /api/marketing/seckill/today`

**接口描述**: 获取今日的秒杀活动时间表

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "timeSlot": "20:00",
      "activities": [
        {
          "id": 1,
          "activityName": "每晚8点教材秒杀",
          "activityStatus": "NOT_START"
        }
      ]
    }
  ]
}
```

---

#### 8.2.11 获取秒杀时段

**接口地址**: `GET /api/marketing/seckill/time-slots`

**接口描述**: 获取秒杀活动的时间段列表

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "timeSlot": "10:00",
      "activityCount": 2
    },
    {
      "timeSlot": "20:00",
      "activityCount": 3
    }
  ]
}
```

---

#### 8.2.12 设置秒杀提醒

**接口地址**: `POST /api/marketing/seckill/{activityId}/reminder`

**接口描述**: 设置秒杀开始前的提醒

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "设置成功",
  "data": null
}
```

---

#### 8.2.13 取消秒杀提醒

**接口地址**: `DELETE /api/marketing/seckill/{activityId}/reminder`

**接口描述**: 取消秒杀提醒

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "取消成功",
  "data": null
}
```

---

## 九、交易模块 API

### 9.1 订单管理

#### 9.1.1 创建订单

**接口地址**: `POST /order`

**接口描述**: 创建新订单

**请求头**: 需要认证

**请求参数**:
```json
{
  "productId": 1001,
  "couponId": 1,
  "pickPointId": 1,
  "payType": "WECHAT"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| couponId | Long | 否 | 优惠券ID |
| pickPointId | Long | 否 | 自提点ID |
| payType | String | 否 | 支付方式：WECHAT-微信，ALIPAY-支付宝，CAMPUS_CARD-校园卡 |

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 2001
}
```

---

#### 9.1.2 获取订单详情

**接口地址**: `GET /order/{orderId}`

**接口描述**: 根据订单ID获取详细信息

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2001,
    "orderNo": "XS20240101001",
    "userId": 1,
    "productId": 1001,
    "productName": "大学物理教材",
    "productPrice": 35.00,
    "couponDeductAmount": 5.00,
    "actualPayAmount": 30.00,
    "pickPointId": 1,
    "pickPointName": "1号宿舍楼楼下",
    "payType": "WECHAT",
    "payStatus": "PAID",
    "orderStatus": "WAIT_PICK",
    "createTime": "2024-01-01 12:00:00"
  }
}
```

---

#### 9.1.3 根据订单号查询

**接口地址**: `GET /order/no/{orderNo}`

**接口描述**: 根据订单编号查询订单

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | String | 是 | 订单编号 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

---

#### 9.1.4 获取我的订单列表

**接口地址**: `GET /order/my`

**接口描述**: 获取当前用户的订单列表（买家视角）

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | String | 否 | - | 订单状态 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 10,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

#### 9.1.5 取消订单

**接口地址**: `PUT /order/{orderId}/cancel`

**接口描述**: 取消未支付的订单

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 否 | 取消原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "取消成功",
  "data": null
}
```

---

#### 9.1.6 确认收货

**接口地址**: `PUT /order/{orderId}/confirm`

**接口描述**: 买家确认收货

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "确认收货成功",
  "data": null
}
```

---

#### 9.1.7 申请退款

**接口地址**: `POST /order/{orderId}/refund`

**接口描述**: 申请订单退款

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 退款原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "申请成功",
  "data": null
}
```

---

#### 9.1.8 延长收货时间

**接口地址**: `PUT /order/{orderId}/extend`

**接口描述**: 延长订单自动确认时间

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "延长成功",
  "data": null
}
```

---

#### 9.1.9 获取订单统计

**接口地址**: `GET /order/statistics`

**接口描述**: 获取买家订单统计数据

**请求头**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "unpaidCount": 2,
    "waitPickCount": 3,
    "successCount": 10,
    "cancelCount": 1
  }
}
```

---

#### 9.1.10 获取卖家订单列表

**接口地址**: `GET /order/seller`

**接口描述**: 获取卖家视角的订单列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | String | 否 | - | 订单状态 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

#### 9.1.11 发货（标记备好）

**接口地址**: `PUT /order/{orderId}/ready`

**接口描述**: 卖家标记商品已备好（自提场景）

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null
}
```

---

### 9.2 支付管理

#### 9.2.1 发起支付

**接口地址**: `POST /payment/create`

**接口描述**: 创建支付订单

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |
| payType | String | 是 | 支付方式：WECHAT-微信，ALIPAY-支付宝，CAMPUS_CARD-校园卡 |
| clientIp | String | 否 | 客户端IP地址（用于风控） |
| userAgent | String | 否 | 用户代理信息（用于风控） |

**请求示例**:
```json
{
  "orderId": 2001,
  "payType": "WECHAT",
  "clientIp": "192.168.1.100",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "payUrl": "https://pay.example.com/xxx",
    "orderNo": "XS20240101001"
  }
}
```

---

#### 9.2.2 支付回调

**接口地址**: `POST /payment/callback/{payType}`

**接口描述**: 支付成功回调接口（第三方支付调用）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| payType | String | 是 | 支付方式 |

**请求参数**: 第三方支付平台的回调数据

**响应示例**:
```
success
```

---

#### 9.2.3 查询支付状态

**接口地址**: `GET /payment/status/{orderId}`

**接口描述**: 查询订单支付状态

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orderId": 2001,
    "payStatus": "PAID",
    "payTime": "2024-01-01 12:05:00"
  }
}
```

---

#### 9.2.4 申请退款

**接口地址**: `POST /payment/refund`

**接口描述**: 申请订单退款

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |
| reason | String | 是 | 退款原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "申请成功",
  "data": null
}
```

---

#### 9.2.5 退款回调

**接口地址**: `POST /payment/refund/callback/{payType}`

**接口描述**: 退款成功回调接口

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| payType | String | 是 | 支付方式 |

**请求参数**: 第三方支付平台的回调数据

**响应示例**:
```
success
```

---

#### 9.2.6 查询退款状态

**接口地址**: `GET /payment/refund/status/{orderId}`

**接口描述**: 查询退款状态

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orderId": 2001,
    "refundStatus": "SUCCESS",
    "refundTime": "2024-01-01 14:00:00"
  }
}
```

---

#### 9.2.7 获取支付方式列表

**接口地址**: `GET /payment/methods`

**接口描述**: 获取系统支持的支付方式

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "payType": "WECHAT",
      "payName": "微信支付"
    },
    {
      "payType": "ALIPAY",
      "payName": "支付宝"
    },
    {
      "payType": "CAMPUS_CARD",
      "payName": "校园卡"
    }
  ]
}
```

---

#### 9.2.8 获取模拟支付信息（开发测试）

**接口地址**: `GET /payment/mock/info/{payToken}`

**接口描述**: 根据支付Token获取模拟支付详情（仅用于开发测试环境）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| payToken | String | 是 | 支付Token |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orderNo": "XS20240101001",
    "amount": 30.00,
    "productName": "大学物理教材",
    "payType": "WECHAT",
    "expireTime": "2024-01-01 12:30:00"
  }
}
```

---

#### 9.2.9 模拟支付确认（开发测试）

**接口地址**: `POST /payment/mock/confirm`

**接口描述**: 确认模拟支付结果（仅用于开发测试环境）

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| payToken | String | 是 | 支付Token |
| success | Boolean | 否 | 是否支付成功，默认true |

**响应示例**:
```json
{
  "code": 200,
  "message": "支付成功",
  "data": null
}
```

---

### 9.3 订单评价管理

#### 9.3.1 提交评价

**接口地址**: `POST /evaluation`

**接口描述**: 买家对订单提交评价

**请求头**: 需要认证

**请求参数**:
```json
{
  "orderId": 2001,
  "score": 5,
  "evaluationTag": "发货快,成色相符",
  "evaluationContent": "商品很好，卖家很热情",
  "evaluationImageUrl": "https://oss.example.com/eval.jpg"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |
| score | Integer | 是 | 评分（1-5星） |
| evaluationTag | String | 否 | 评价标签（多个用逗号分隔） |
| evaluationContent | String | 否 | 评价内容 |
| evaluationImageUrl | String | 否 | 评价图片地址 |

**响应示例**:
```json
{
  "code": 200,
  "message": "评价成功",
  "data": null
}
```

---

#### 9.3.2 获取订单评价

**接口地址**: `GET /evaluation/order/{orderId}`

**接口描述**: 获取指定订单的评价信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderId": 2001,
    "score": 5,
    "evaluationTag": "发货快,成色相符",
    "evaluationContent": "商品很好，卖家很热情",
    "createTime": "2024-01-02 10:00:00"
  }
}
```

---

#### 9.3.3 获取商品评价列表

**接口地址**: `GET /evaluation/product/{productId}`

**接口描述**: 获取指定商品的所有评价

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

#### 9.3.4 卖家回复评价

**接口地址**: `POST /evaluation/{evaluationId}/reply`

**接口描述**: 卖家对评价进行回复

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| evaluationId | Long | 是 | 评价ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| content | String | 是 | 回复内容 |

**响应示例**:
```json
{
  "code": 200,
  "message": "回复成功",
  "data": null
}
```

---

#### 9.3.5 获取我的评价列表

**接口地址**: `GET /evaluation/my`

**接口描述**: 获取当前用户发出的评价列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 3,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

#### 9.3.6 获取收到的评价

**接口地址**: `GET /evaluation/received`

**接口描述**: 卖家获取收到的评价列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 8,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

### 9.4 交易纠纷管理

#### 9.4.1 申请纠纷

**接口地址**: `POST /dispute`

**接口描述**: 发起交易纠纷

**请求头**: 需要认证

**请求参数**:
```json
{
  "orderId": 2001,
  "disputeType": "PRODUCT_NOT_MATCH",
  "evidenceUrl": "https://oss.example.com/evidence1.jpg,https://oss.example.com/evidence2.jpg"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |
| disputeType | String | 是 | 纠纷类型：PRODUCT_NOT_MATCH-商品不符，NO_DELIVERY-未发货，OTHER-其他 |
| evidenceUrl | String | 否 | 举证材料地址（多个用逗号分隔） |

**响应示例**:
```json
{
  "code": 200,
  "message": "申请成功",
  "data": null
}
```

---

#### 9.4.2 获取纠纷详情

**接口地址**: `GET /dispute/{disputeId}`

**接口描述**: 获取纠纷详细信息

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| disputeId | Long | 是 | 纠纷ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderId": 2001,
    "initiatorId": 1,
    "accusedId": 2,
    "disputeType": "PRODUCT_NOT_MATCH",
    "evidenceUrl": "https://oss.example.com/evidence1.jpg",
    "disputeStatus": "PENDING",
    "createTime": "2024-01-03 10:00:00"
  }
}
```

---

#### 9.4.3 获取我的纠纷列表

**接口地址**: `GET /dispute/my`

**接口描述**: 获取当前用户相关的纠纷列表

**请求头**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | String | 否 | - | 纠纷状态 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 2,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

#### 9.4.4 撤销纠纷

**接口地址**: `DELETE /dispute/{disputeId}`

**接口描述**: 撤销已提交的纠纷申请

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| disputeId | Long | 是 | 纠纷ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "撤销成功",
  "data": null
}
```

---

#### 9.4.5 补充证据

**接口地址**: `POST /dispute/{disputeId}/evidence`

**接口描述**: 补充纠纷证据材料

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| disputeId | Long | 是 | 纠纷ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| evidenceUrl | String | 是 | 证据材料URL |

**响应示例**:
```json
{
  "code": 200,
  "message": "补充成功",
  "data": null
}
```

---

#### 9.4.6 获取纠纷处理记录

**接口地址**: `GET /dispute/{disputeId}/records`

**接口描述**: 获取纠纷的处理过程记录

**请求头**: 需要认证

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| disputeId | Long | 是 | 纠纷ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "disputeId": 1,
      "operateType": "CREATE",
      "operateContent": "用户发起纠纷",
      "operateTime": "2024-01-03 10:00:00"
    }
  ]
}
```

---

## 十、运营管理模块 API（管理员）

### 10.1 管理员认证

#### 10.1.1 管理员登录

**接口地址**: `POST /admin/auth/login`

**接口描述**: 管理员登录，返回JWT令牌

**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 管理员用户名 |
| password | String | 是 | 密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "adminInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "role": "SUPER_ADMIN"
    }
  }
}
```

---

#### 10.1.2 管理员登出

**接口地址**: `POST /admin/auth/logout`

**接口描述**: 管理员退出登录，清除Token

**请求头**: 需要管理员认证

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

#### 10.1.3 获取当前管理员信息

**接口地址**: `GET /admin/auth/info`

**接口描述**: 获取当前登录管理员的详细信息

**请求头**: 需要管理员认证

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "138****0000",
    "email": "admin@campus.edu.cn",
    "role": "SUPER_ADMIN",
    "createTime": "2024-01-01 00:00:00"
  }
}
```

---

#### 10.1.4 刷新管理员Token

**接口地址**: `POST /admin/auth/refresh-token`

**接口描述**: 使用刷新令牌获取新的访问令牌

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| refreshToken | String | 是 | 刷新令牌 |

**响应示例**:
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  }
}
```

---

### 10.2 商品审核管理

#### 10.2.1 获取待审核商品列表

**接口地址**: `GET /admin/audit/pending`

**接口描述**: 分页获取待审核商品列表

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 15,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": []
  }
}
```

---

#### 10.2.2 审核通过

**接口地址**: `PUT /admin/audit/{productId}/pass`

**接口描述**: 审核通过商品

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "审核通过",
  "data": null
}
```

---

#### 10.2.3 审核驳回

**接口地址**: `PUT /admin/audit/{productId}/reject`

**接口描述**: 驳回商品并填写原因

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 驳回原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "审核驳回",
  "data": null
}
```

---

#### 10.2.4 批量审核通过

**接口地址**: `PUT /admin/audit/batch/pass`

**接口描述**: 批量审核通过商品

**请求头**: 需要管理员权限

**请求参数**:
```json
{
  "productIds": [1001, 1002, 1003]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量审核成功",
  "data": {
    "successCount": 3,
    "failCount": 0
  }
}
```

---

#### 10.2.5 获取审核记录

**接口地址**: `GET /admin/audit/{productId}/records`

**接口描述**: 获取商品审核记录

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "productId": 1001,
      "adminId": 1,
      "auditResult": "PASS",
      "auditTime": "2024-01-01 15:00:00"
    }
  ]
}
```

---

#### 10.2.6 强制下架

**接口地址**: `PUT /admin/audit/{productId}/force-off`

**接口描述**: 强制下架违规商品

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 下架原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "下架成功",
  "data": null
}
```

---

#### 10.2.7 获取审核统计

**接口地址**: `GET /admin/audit/statistics`

**接口描述**: 获取审核统计数据

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pendingCount": 15,
    "passCount": 100,
    "rejectCount": 5
  }
}
```

---

### 10.3 用户管理

#### 10.3.1 获取用户列表

**接口地址**: `GET /admin/user/page`

**接口描述**: 分页获取用户列表

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| username | String | 否 | - | 用户名 |
| authStatus | String | 否 | - | 认证状态 |
| identityType | String | 否 | - | 身份类型 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "list": []
  }
}
```

---

#### 10.3.2 获取用户详情

**接口地址**: `GET /admin/user/{userId}`

**接口描述**: 获取用户详细信息

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "张三",
    "phone": "13800138000",
    "identityType": "STUDENT",
    "authStatus": "AUTH_SUCCESS",
    "creditScore": 100
  }
}
```

---

#### 10.3.3 获取待审核认证列表

**接口地址**: `GET /admin/user/auth/pending`

**接口描述**: 获取待审核的校园认证列表

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 10,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1,
    "list": []
  }
}
```

---

#### 10.3.4 审核通过认证

**接口地址**: `PUT /admin/user/auth/{userId}/pass`

**接口描述**: 审核通过用户认证

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "审核通过",
  "data": null
}
```

---

#### 10.3.5 审核驳回认证

**接口地址**: `PUT /admin/user/auth/{userId}/reject`

**接口描述**: 驳回用户认证并填写原因

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 驳回原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "审核驳回",
  "data": null
}
```

---

#### 10.3.6 封禁用户

**接口地址**: `PUT /admin/user/{userId}/ban`

**接口描述**: 封禁指定用户

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 封禁原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "封禁成功",
  "data": null
}
```

---

#### 10.3.7 解封用户

**接口地址**: `PUT /admin/user/{userId}/unban`

**接口描述**: 解封指定用户

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "解封成功",
  "data": null
}
```

---

#### 10.3.8 调整信誉分

**接口地址**: `PUT /admin/user/{userId}/credit`

**接口描述**: 调整用户信誉分

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| score | Integer | 是 | 调整分数（正数加分，负数减分） |
| reason | String | 是 | 调整原因 |

**响应示例**:
```json
{
  "code": 200,
  "message": "调整成功",
  "data": {
    "newScore": 95
  }
}
```

---

#### 10.3.9 获取用户认证记录

**接口地址**: `GET /admin/user/auth/{userId}/records`

**接口描述**: 获取用户认证审核记录

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "authStatus": "AUTH_SUCCESS",
      "authTime": "2024-01-01 10:00:00"
    }
  ]
}
```

---

### 10.4 数据统计

#### 10.4.1 获取今日数据概览

**接口地址**: `GET /admin/statistics/today`

**接口描述**: 获取今日运营数据概览

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "newUserCount": 50,
    "productPublishCount": 100,
    "orderSuccessCount": 80,
    "transactionAmount": 5000.00,
    "seckillParticipateCount": 200,
    "couponUseCount": 30
  }
}
```

---

#### 10.4.2 获取日期范围统计

**接口地址**: `GET /admin/statistics/range`

**接口描述**: 获取指定日期范围的统计数据

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | LocalDate | 是 | 开始日期 |
| endDate | LocalDate | 是 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalUserCount": 500,
    "totalProductCount": 1000,
    "totalOrderCount": 800,
    "totalTransactionAmount": 50000.00
  }
}
```

---

#### 10.4.3 获取商品发布趋势

**接口地址**: `GET /admin/statistics/product/trend`

**接口描述**: 获取商品发布数量趋势

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| days | Integer | 否 | 30 | 天数 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "date": "2024-01-01",
      "count": 100
    }
  ]
}
```

---

#### 10.4.4 获取成交趋势

**接口地址**: `GET /admin/statistics/order/trend`

**接口描述**: 获取成交数量和金额趋势

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| days | Integer | 否 | 30 | 天数 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "date": "2024-01-01",
      "orderCount": 80,
      "transactionAmount": 5000.00
    }
  ]
}
```

---

#### 10.4.5 获取热门品类统计

**接口地址**: `GET /admin/statistics/category/hot`

**接口描述**: 获取热门品类统计数据

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "categoryId": 1,
      "categoryName": "教材",
      "productCount": 300,
      "orderCount": 200
    }
  ]
}
```

---

#### 10.3.6 获取用户增长趋势

**接口地址**: `GET /admin/statistics/user/growth`

**接口描述**: 获取用户注册增长趋势

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| days | Integer | 否 | 30 | 天数 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "date": "2024-01-01",
      "newUserCount": 50
    }
  ]
}
```

---

#### 10.3.7 获取秒杀活动统计

**接口地址**: `GET /admin/statistics/seckill`

**接口描述**: 获取秒杀活动统计数据

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 否 | 活动ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalParticipateCount": 500,
    "successCount": 50,
    "successRate": 0.10
  }
}
```

---

#### 10.4.8 获取优惠券统计

**接口地址**: `GET /admin/statistics/coupon`

**接口描述**: 获取优惠券使用统计数据

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalIssuedCount": 1000,
    "totalUsedCount": 300,
    "useRate": 0.30
  }
}
```

---

#### 10.4.9 获取AI分类准确率统计

**接口地址**: `GET /admin/statistics/ai/accuracy`

**接口描述**: 获取AI分类准确率数据

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalRecognizeCount": 500,
    "correctCount": 475,
    "accuracyRate": 0.95
  }
}
```

---

#### 10.4.10 导出统计报表

**接口地址**: `GET /admin/statistics/export`

**接口描述**: 导出指定日期范围的统计报表

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | LocalDate | 是 | 开始日期 |
| endDate | LocalDate | 是 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "message": "导出成功",
  "data": "https://oss.example.com/statistics_report.xlsx"
}
```

---

#### 10.4.11 获取用户活跃度统计

**接口地址**: `GET /admin/statistics/user/activity`

**接口描述**: 获取用户活跃度数据

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "dailyActiveUsers": 200,
    "weeklyActiveUsers": 500,
    "monthlyActiveUsers": 800
  }
}
```

---

### 10.5 系统配置管理

#### 10.5.1 获取配置列表

**接口地址**: `GET /admin/config/list`

**接口描述**: 获取所有系统配置列表

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "configKey": "campus_fence",
      "configValue": "116.40,39.90",
      "configDesc": "校园围栏经纬度"
    }
  ]
}
```

---

#### 10.5.2 获取配置详情

**接口地址**: `GET /admin/config/{configKey}`

**接口描述**: 根据配置键获取配置值

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configKey | String | 是 | 配置键 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "configKey": "campus_fence",
    "configValue": "116.40,39.90",
    "configDesc": "校园围栏经纬度"
  }
}
```

---

#### 10.5.3 更新配置

**接口地址**: `PUT /admin/config/{configKey}`

**接口描述**: 更新系统配置

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configKey | String | 是 | 配置键 |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configValue | String | 是 | 配置值 |

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

#### 10.5.4 批量更新配置

**接口地址**: `PUT /admin/config/batch`

**接口描述**: 批量更新系统配置

**请求头**: 需要管理员权限

**请求参数**:
```json
[
  {
    "configKey": "campus_fence",
    "configValue": "116.40,39.90"
  }
]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

#### 10.5.5 获取校园围栏配置

**接口地址**: `GET /admin/config/campus-fence`

**接口描述**: 获取校园围栏经纬度配置

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "longitude": "116.40",
    "latitude": "39.90",
    "radius": 1000
  }
}
```

---

#### 10.5.6 更新校园围栏配置

**接口地址**: `PUT /admin/config/campus-fence`

**接口描述**: 更新校园围栏经纬度配置

**请求头**: 需要管理员权限

**请求参数**:
```json
{
  "longitude": "116.40",
  "latitude": "39.90",
  "radius": 1000
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

#### 10.5.7 获取秒杀配置

**接口地址**: `GET /admin/config/seckill`

**接口描述**: 获取秒杀相关配置

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "qpsLimit": 500,
    "maxStockPerUser": 1
  }
}
```

---

#### 10.5.8 更新秒杀配置

**接口地址**: `PUT /admin/config/seckill`

**接口描述**: 更新秒杀相关配置

**请求头**: 需要管理员权限

**请求参数**:
```json
{
  "qpsLimit": 500,
  "maxStockPerUser": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

#### 10.5.9 刷新缓存

**接口地址**: `POST /admin/config/refresh-cache`

**接口描述**: 刷新系统配置缓存

**请求头**: 需要管理员权限

**响应示例**:
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": null
}
```

---

#### 10.5.10 获取配置修改记录

**接口地址**: `GET /admin/config/history`

**接口描述**: 获取配置修改历史记录

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| configKey | String | 否 | - | 配置键 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": [
      {
        "id": 1,
        "configKey": "campus_fence",
        "oldValue": "116.40,39.90",
        "newValue": "116.41,39.91",
        "adminId": 1,
        "updateTime": "2024-01-01 10:00:00"
      }
    ]
  }
}
```

---

### 10.6 纠纷处理管理

#### 10.6.1 获取纠纷列表

**接口地址**: `GET /admin/dispute/page`

**接口描述**: 分页获取纠纷列表

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | String | 否 | - | 纠纷状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决 |
| disputeType | String | 否 | - | 纠纷类型 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "list": [
      {
        "id": 1,
        "orderId": 2001,
        "initiatorId": 1,
        "initiatorName": "张三",
        "accusedId": 2,
        "accusedName": "李四",
        "disputeType": "PRODUCT_NOT_MATCH",
        "disputeStatus": "PENDING",
        "createTime": "2024-01-03 10:00:00"
      }
    ]
  }
}
```

---

#### 10.6.2 获取纠纷详情

**接口地址**: `GET /admin/dispute/{disputeId}`

**接口描述**: 获取纠纷详细信息

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| disputeId | Long | 是 | 纠纷ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderId": 2001,
    "orderNo": "XS20240101001",
    "initiatorId": 1,
    "initiatorName": "张三",
    "accusedId": 2,
    "accusedName": "李四",
    "disputeType": "PRODUCT_NOT_MATCH",
    "evidenceUrl": "https://oss.example.com/evidence1.jpg",
    "disputeStatus": "PENDING",
    "createTime": "2024-01-03 10:00:00",
    "orderInfo": {
      "productId": 1001,
      "productName": "大学物理教材",
      "productPrice": 35.00
    }
  }
}
```

---

#### 10.6.3 处理纠纷

**接口地址**: `PUT /admin/dispute/{disputeId}/process`

**接口描述**: 管理员处理纠纷

**请求头**: 需要管理员权限

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| disputeId | Long | 是 | 纠纷ID |

**请求参数**:
```json
{
  "processResult": "RESOLVED",
  "processRemark": "经核实，商品确实与描述不符，同意退款",
  "refundAmount": 30.00,
  "deductCreditScore": 5
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| processResult | String | 是 | 处理结果：RESOLVED-已解决，REJECTED-驳回 |
| processRemark | String | 是 | 处理备注 |
| refundAmount | BigDecimal | 否 | 退款金额（processResult为RESOLVED时可选） |
| deductCreditScore | Integer | 否 | 扣除信誉分（正数扣分，负数加分） |

**响应示例**:
```json
{
  "code": 200,
  "message": "处理成功",
  "data": null
}
```

---

### 10.7 操作日志管理

#### 10.7.1 获取操作日志列表

**接口地址**: `GET /admin/log/page`

**接口描述**: 分页获取管理员操作日志

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| adminId | Long | 否 | - | 管理员ID |
| operateType | String | 否 | - | 操作类型 |
| startDate | LocalDate | 否 | - | 开始日期 |
| endDate | LocalDate | 否 | - | 结束日期 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "list": [
      {
        "id": 1,
        "adminId": 1,
        "adminName": "系统管理员",
        "operateType": "AUDIT_PASS",
        "operateDesc": "审核通过商品：大学物理教材",
        "operateIp": "192.168.1.100",
        "operateTime": "2024-01-01 15:00:00"
      }
    ]
  }
}
```

---

#### 10.7.2 导出操作日志

**接口地址**: `GET /admin/log/export`

**接口描述**: 导出指定日期范围的操作日志

**请求头**: 需要管理员权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| adminId | Long | 否 | 管理员ID |
| operateType | String | 否 | 操作类型 |
| startDate | LocalDate | 是 | 开始日期 |
| endDate | LocalDate | 是 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "message": "导出成功",
  "data": "https://oss.example.com/operation_log.xlsx"
}
```

---

## 附录

### A. 枚举值说明

#### A.1 身份类型（identityType）
| 值 | 说明 |
|----|------|
| STUDENT | 学生 |
| TEACHER | 教职工 |

#### A.2 认证状态（authStatus）
| 值 | 说明 |
|----|------|
| UNAUTH | 未认证 |
| UNDER_REVIEW | 审核中 |
| AUTH_SUCCESS | 认证成功 |
| AUTH_FAILED | 认证失败 |

#### A.3 新旧程度（newDegree）
| 值 | 说明 |
|----|------|
| NEW | 全新 |
| 90_NEW | 9成新 |
| 80_NEW | 8成新 |
| 70_NEW | 7成新 |
| OLD | 老旧 |

#### A.4 商品状态（productStatus）
| 值 | 说明 |
|----|------|
| DRAFT | 草稿 |
| ON_SHELF | 上架 |
| SOLD_OUT | 已成交 |
| OFF_SHELF | 下架 |
| ILLEGAL | 违规 |

#### A.5 订单状态（orderStatus）
| 值 | 说明 |
|----|------|
| UNPAID | 待付款 |
| WAIT_PICK | 待自提 |
| SUCCESS | 已成交 |
| CANCEL | 已取消 |

#### A.6 支付状态（payStatus）
| 值 | 说明 |
|----|------|
| UNPAID | 未支付 |
| PAID | 已支付 |
| REFUNDED | 已退款 |

#### A.7 支付方式（payType）
| 值 | 说明 |
|----|------|
| WECHAT | 微信支付 |
| ALIPAY | 支付宝 |
| CAMPUS_CARD | 校园卡 |

#### A.8 优惠券类型（couponType）
| 值 | 说明 |
|----|------|
| FULL_REDUCE | 满减券 |
| DIRECT_REDUCE | 直降券 |
| CATEGORY | 品类券 |

#### A.9 优惠券状态（couponStatus）
| 值 | 说明 |
|----|------|
| UNUSED | 未使用 |
| USED | 已使用 |
| EXPIRED | 已过期 |

#### A.10 秒杀活动状态（activityStatus）
| 值 | 说明 |
|----|------|
| NOT_START | 未开始 |
| ON_GOING | 进行中 |
| END | 已结束 |

#### A.11 纠纷类型（disputeType）
| 值 | 说明 |
|----|------|
| PRODUCT_NOT_MATCH | 商品不符 |
| NO_DELIVERY | 未发货 |
| OTHER | 其他 |

#### A.12 纠纷状态（disputeStatus）
| 值 | 说明 |
|----|------|
| PENDING | 待处理 |
| PROCESSING | 处理中 |
| RESOLVED | 已解决 |

#### A.13 消息类型（messageType）
| 值 | 说明 |
|----|------|
| TEXT | 文字 |
| IMAGE | 图片 |
| VOICE | 语音 |

---

### B. 错误码说明

#### B.1 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

#### B.2 用户模块错误码（1000-1999）

| 错误码 | 说明 | 触发场景 |
|--------|------|---------|
| 1001 | 用户名已存在 | 注册时用户名重复 |
| 1002 | 手机号已注册 | 注册时手机号重复 |
| 1003 | 密码错误 | 登录时密码不正确 |
| 1004 | 用户不存在 | 查询用户信息时 |
| 1005 | 认证信息不完整 | 提交认证时缺少必填项 |
| 1006 | 验证码错误 | 验证码校验失败 |
| 1007 | 验证码已过期 | 验证码超过有效期 |
| 1008 | 用户已被封禁 | 用户状态为封禁状态 |
| 1009 | 信誉分过低 | 信誉分低于60分 |

#### B.3 商品模块错误码（2000-2999）

| 错误码 | 说明 | 触发场景 |
|--------|------|---------|
| 2001 | 商品不存在 | 查询商品详情时商品ID无效 |
| 2002 | 商品已下架 | 操作已下架商品 |
| 2003 | 商品已售出 | 购买已成交商品 |
| 2004 | 无权操作该商品 | 非商品所有者操作商品 |
| 2005 | 品类不存在 | 发布商品时品类ID无效 |
| 2006 | 图片数量超限 | 上传图片超过9张 |
| 2007 | 未通过校园认证 | 发布商品时用户未认证 |
| 2008 | 信誉分过低 | 发布商品时信誉分<60 |
| 2009 | 不在校园范围内 | 发布商品时定位校验失败 |
| 2010 | 草稿不存在 | 操作草稿时草稿ID无效 |
| 2011 | 商品状态异常 | 商品状态不允许该操作 |
| 2012 | 商品信息不完整 | 发布商品时缺少必填项 |
| 2013 | 价格格式错误 | 价格不是有效的数字格式 |
| 2014 | 商品名称过长 | 商品名称超过最大长度限制 |
| 2015 | 商品描述过长 | 商品描述超过最大长度限制 |

#### B.4 订单模块错误码（3000-3999）

| 错误码 | 说明 | 触发场景 |
|--------|------|---------|
| 3001 | 订单不存在 | 查询订单时订单ID无效 |
| 3002 | 订单状态异常 | 订单状态不允许该操作 |
| 3003 | 支付失败 | 支付过程中发生错误 |
| 3004 | 退款失败 | 退款过程中发生错误 |
| 3005 | 订单已取消 | 操作已取消的订单 |
| 3006 | 订单已支付 | 重复支付订单 |

#### B.5 营销模块错误码（4000-4999）

| 错误码 | 说明 | 触发场景 |
|--------|------|---------|
| 4001 | 优惠券不存在 | 优惠券ID无效 |
| 4002 | 优惠券已领完 | 优惠券库存为0 |
| 4003 | 优惠券已过期 | 优惠券已过有效期 |
| 4004 | 优惠券不满足使用条件 | 订单金额不满足使用门槛 |
| 4005 | 优惠券已被领取 | 用户已领取过该优惠券 |
| 4006 | 优惠券已使用 | 优惠券已被使用 |
| 5001 | 秒杀活动不存在 | 秒杀活动ID无效 |
| 5002 | 秒杀活动未开始 | 秒杀活动尚未开始 |
| 5003 | 秒杀活动已结束 | 秒杀活动已结束 |
| 5004 | 秒杀库存不足 | 秒杀商品库存不足 |

#### B.6 IM模块错误码（6000-6999）

| 错误码 | 说明 | 触发场景 |
|--------|------|---------|
| 6001 | 会话不存在 | 会话ID无效 |
| 6002 | 消息发送失败 | 消息发送过程中发生错误 |
| 6003 | 消息已撤回 | 操作已撤回的消息 |
| 6004 | 会话已关闭 | 会话状态为已关闭 |

---

### C. 接口调用示例

#### C.1 用户注册并登录示例

**步骤1: 注册**
```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "张三",
    "phone": "13800138000",
    "password": "password123",
    "identityType": "STUDENT"
  }'
```

**步骤2: 登录**
```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "password": "password123"
  }'
```

**步骤3: 获取用户信息（使用Token）**
```bash
curl -X GET http://localhost:8080/user/info \
  -H "Authorization: Bearer {accessToken}"
```

---

#### C.2 发布商品示例

```bash
curl -X POST http://localhost:8080/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "productName": "大学物理教材",
    "categoryId": 1,
    "newDegree": "90_NEW",
    "price": 35.00,
    "originalPrice": 68.00,
    "productDesc": "九成新大学物理教材，无笔记无划线",
    "imageUrls": [
      "https://oss.example.com/img1.jpg",
      "https://oss.example.com/img2.jpg"
    ],
    "pickPointId": 1,
    "longitude": "116.407526",
    "latitude": "39.904030"
  }'
```

---

#### C.3 创建订单并支付示例

**步骤1: 创建订单**
```bash
curl -X POST http://localhost:8080/order \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "productId": 1001,
    "couponId": 1,
    "pickPointId": 1,
    "payType": "WECHAT"
  }'
```

**步骤2: 发起支付**
```bash
curl -X POST http://localhost:8080/payment/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "orderId": 2001,
    "payType": "WECHAT"
  }'
```

---

## 十一、内部接口 API（供其他服务调用）

### 11.1 根据ID获取用户信息

**接口地址**: `GET /user/inner/{userId}`

**接口描述**: 供其他微服务调用，获取用户公开信息

**请求头**: 内部服务调用（需验证服务身份）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "张三",
    "avatar": "https://oss.example.com/avatar.jpg",
    "phone": "138****8000",
    "identityType": "STUDENT",
    "authStatus": "VERIFIED",
    "creditScore": 95
  }
}
```

**说明**:
- 该接口仅供内部微服务调用，外部无法访问
- 返回用户的基本公开信息，不包含敏感信息
- 手机号已脱敏处理

---

### 11.2 批量获取用户信息

**接口地址**: `POST /user/inner/batch`

**接口描述**: 供其他微服务调用，批量获取用户信息

**请求头**: 内部服务调用（需验证服务身份）

**请求参数**:
```json
{
  "userIds": [1, 2, 3, 4, 5]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "username": "张三",
      "avatar": "https://oss.example.com/avatar1.jpg",
      "phone": "138****8000",
      "identityType": "STUDENT",
      "authStatus": "VERIFIED",
      "creditScore": 95
    },
    {
      "id": 2,
      "username": "李四",
      "avatar": "https://oss.example.com/avatar2.jpg",
      "phone": "139****9000",
      "identityType": "TEACHER",
      "authStatus": "VERIFIED",
      "creditScore": 98
    }
  ]
}
```

**说明**:
- 该接口仅供内部微服务调用
- 一次最多查询100个用户信息
- 返回用户的基本公开信息，不包含敏感信息

---

### 11.3 获取用户认证状态

**接口地址**: `GET /user/inner/{userId}/auth-status`

**接口描述**: 供其他微服务调用，获取用户认证状态

**请求头**: 内部服务调用（需验证服务身份）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "authStatus": "VERIFIED",
    "identityType": "STUDENT",
    "realName": "张三",
    "schoolName": "XX大学",
    "studentId": "2021001",
    "authTime": "2024-01-01 10:00:00"
  }
}
```

**说明**:
- 该接口仅供内部微服务调用
- 返回用户的认证状态和认证信息
- 如果用户未认证，`authStatus`为`UNVERIFIED`，其他字段为null

---

### 11.4 批量获取用户认证状态

**接口地址**: `POST /user/inner/auth-status/batch`

**接口描述**: 供其他微服务调用，批量获取用户认证状态

**请求头**: 内部服务调用（需验证服务身份）

**请求参数**:
```json
{
  "userIds": [1, 2, 3, 4, 5]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "userId": 1,
      "authStatus": "VERIFIED",
      "identityType": "STUDENT",
      "realName": "张三",
      "schoolName": "XX大学",
      "studentId": "2021001",
      "authTime": "2024-01-01 10:00:00"
    },
    {
      "userId": 2,
      "authStatus": "VERIFIED",
      "identityType": "TEACHER",
      "realName": "李四",
      "schoolName": "XX大学",
      "teacherId": "T2021001",
      "authTime": "2024-01-02 10:00:00"
    }
  ]
}
```

**说明**:
- 该接口仅供内部微服务调用
- 一次最多查询100个用户认证状态
- 如果用户未认证，`authStatus`为`UNVERIFIED`，其他字段为null

---

## 更新日志

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0.0 | 2024-01-01 | 初始版本，完成所有模块API文档 |

---

**文档编写**: FNUSALE开发团队  
**最后更新**: 2024年1月1日
