# 用户模块 OSS 图片上传实现计划

## 目标
使用阿里云 OSS 完成用户模块的图片上传功能，包括：
1. 用户头像上传
2. 用户认证图片上传（学生证/校园卡照片）

## 当前状态分析

### 后端现状
- 用户实体 `User` 包含 `avatarUrl` 和 `authImageUrl` 字段用于存储图片 URL
- `UserController` 中有更新用户信息和提交认证的接口
- `UserUpdateDTO` 和 `UserAuthDTO` 已经定义了接收图片 URL 的字段
- 目前前端只是临时使用本地临时路径，没有实际的文件上传功能

### 前端现状
- `auth.vue` 中有图片上传 UI，但 `uploadImage` 方法只选择了图片，没有实际上传
- `profile.vue` 中有头像更换 UI，但 `changeAvatar` 方法同样没有实际上传
- 两个页面都标记了 `TODO: 上传图片` 的注释

## 实现方案

### 1. 后端实现（fnusale-user 服务）

#### 1.1 添加阿里云 OSS 依赖
在 `fnusale-user/pom.xml` 中添加：
```xml
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.17.4</version>
</dependency>
```

#### 1.2 创建 OSS 配置类
文件：`fnusale-user/src/main/java/com/fnusale/user/config/OssConfig.java`

配置内容：
- OSS Endpoint（从环境变量或配置文件读取）
- AccessKeyId
- AccessKeySecret
- Bucket 名称
- 自定义域名（可选，用于 CDN 加速）

#### 1.3 创建 OSS 服务类
文件：`fnusale-user/src/main/java/com/fnusale/user/service/OssService.java`

实现方法：
- `uploadFile(MultipartFile file, String dir)`: 上传文件到 OSS
- `deleteFile(String url)`: 删除 OSS 上的文件
- `generateUploadUrl(String dir)`: 生成签名上传 URL（可选，用于前端直传）
- 文件名校名策略：使用 UUID 避免重复
- 目录结构：`user/avatar/{userId}/{uuid}.jpg`、`user/auth/{userId}/{uuid}.jpg`

#### 1.4 创建文件上传 Controller
文件：`fnusale-user/src/main/java/com/fnusale/user/controller/FileUploadController.java`

接口设计：
- `POST /upload/avatar`: 上传头像
  - 参数：MultipartFile file
  - 返回：{ url: "https://..." }
  
- `POST /upload/auth`: 上传认证图片
  - 参数：MultipartFile file
  - 返回：{ url: "https://..." }

#### 1.5 更新 UserService
在 `UserService` 中：
- `updateUserInfo`: 如果上传了新头像，先调用 OSS 上传，再更新数据库
- `submitAuth`: 如果上传了认证图片，先调用 OSS 上传，再更新数据库

#### 1.6 添加配置文件
在 `application.yml` 中添加：
```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: ${OSS_ACCESS_KEY_ID:}
    access-key-secret: ${OSS_ACCESS_KEY_SECRET:}
    bucket-name: ${OSS_BUCKET_NAME:fnusale}
    domain: ${OSS_DOMAIN:}  # 自定义域名，可选
```

### 2. 前端实现（frontend-uniapp）

#### 2.1 添加上传 API
文件：`frontend-uniapp/src/api/user.ts`

添加方法：
- `uploadAvatar(file: File)`: 上传头像
- `uploadAuthImage(file: File)`: 上传认证图片

#### 2.2 更新 auth.vue
修改 `uploadImage` 方法：
1. 使用 `uni.chooseImage` 选择图片
2. 使用 `uni.uploadFile` 上传到后端 `/user/upload/auth` 接口
3. 获取返回的 OSS URL
4. 更新 `formData.authImageUrl`

#### 2.3 更新 profile.vue
修改 `changeAvatar` 方法：
1. 使用 `uni.chooseImage` 选择图片
2. 使用 `uni.uploadFile` 上传到后端 `/user/upload/avatar` 接口
3. 获取返回的 OSS URL
4. 调用 `updateUserInfo` 更新用户信息

### 3. 安全与优化考虑

#### 3.1 文件校验
- 文件类型限制：只允许 jpg、png、jpeg 格式
- 文件大小限制：头像不超过 2MB，认证图片不超过 5MB
- 图片尺寸校验：使用后端或前端进行尺寸限制

#### 3.2 安全策略
- 使用 JWT 认证确保只有登录用户可上传
- 上传时校验用户 ID 与路径匹配
- OSS 权限设置为私有读写，通过后端签名 URL 访问

#### 3.3 性能优化
- 前端压缩图片后再上传
- 使用 OSS CDN 加速图片访问
- 可选：实现前端直传 OSS（减少服务器带宽压力）

### 4. 测试计划

#### 4.1 单元测试
- 测试 OSS 服务类的上传、删除方法
- 测试文件类型和大小校验

#### 4.2 集成测试
- 测试头像上传接口
- 测试认证图片上传接口
- 测试完整的用户信息更新流程
- 测试完整的认证提交流程

#### 4.3 前端测试
- 测试头像选择和上传
- 测试认证图片选择和上传
- 测试图片预览功能
- 测试错误处理（文件过大、格式错误等）

## 实施步骤

### 阶段一：后端基础建设（1-2 天）
1. 添加阿里云 OSS 依赖
2. 创建 OSS 配置类和服务类
3. 创建文件上传 Controller
4. 配置 application.yml

### 阶段二：后端业务集成（1 天）
1. 更新 UserService，集成 OSS 上传
2. 添加文件校验逻辑
3. 编写单元测试

### 阶段三：前端实现（1 天）
1. 添加上传 API
2. 更新 auth.vue 实现认证图片上传
3. 更新 profile.vue 实现头像上传
4. 添加错误处理和用户反馈

### 阶段四：测试与优化（1 天）
1. 进行集成测试
2. 性能优化（图片压缩、CDN 配置）
3. 安全加固
4. 文档更新

## 技术栈
- 后端：Spring Boot + Aliyun OSS SDK
- 前端：uni-app + uni.uploadFile
- 存储：阿里云 OSS
- 安全：JWT 认证 + OSS 私有 bucket

## 风险与注意事项
1. OSS 费用控制：设置合理的存储和流量限制
2. 图片审核：认证图片需要人工审核，确保合规
3. 备份策略：重要图片（认证图片）需要备份
4. 隐私保护：用户头像等公开图片注意隐私设置
5. 跨域配置：如使用前端直传，需配置 OSS CORS

## 交付物
1. 完整的后端 OSS 上传服务
2. 前端头像和认证图片上传功能
3. 单元测试和集成测试用例
4. 配置文档和使用说明
