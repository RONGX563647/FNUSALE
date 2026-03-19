# 阿里云 OSS 图片上传功能使用说明

## 功能概述
已完成用户模块的图片上传功能，使用阿里云 OSS 作为文件存储服务，支持：
- 用户头像上传和更换
- 用户认证图片上传（学生证/校园卡照片）

## 后端配置

### 1. 环境变量配置
在启动用户服务前，需要配置以下环境变量或在配置文件中设置：

```bash
# 阿里云 OSS 配置
OSS_ACCESS_KEY_ID=your_access_key_id
OSS_ACCESS_KEY_SECRET=your_access_key_secret
OSS_BUCKET_NAME=fnusale
OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
OSS_DOMAIN=https://your-domain.com  # 可选，自定义域名
```

### 2. 配置文件
`fnusale-user/src/main/resources/application.yml` 已添加 OSS 配置：

```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: ${OSS_ACCESS_KEY_ID:}
    access-key-secret: ${OSS_ACCESS_KEY_SECRET:}
    bucket-name: ${OSS_BUCKET_NAME:fnusale}
    domain: ${OSS_DOMAIN:}
```

### 3. API 接口

#### 3.1 上传头像
- **接口**: `POST /api/upload/avatar`
- **Content-Type**: `multipart/form-data`
- **参数**: 
  - `file`: 图片文件
- **响应**:
```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "url": "https://fnusale.oss-cn-hangzhou.aliyuncs.com/user/avatar/123/20260319/uuid.jpg"
  }
}
```

#### 3.2 上传认证图片
- **接口**: `POST /api/upload/auth`
- **Content-Type**: `multipart/form-data`
- **参数**: 
  - `file`: 图片文件
- **响应**:
```json
{
  "code": 200,
  "message": "认证图片上传成功",
  "data": {
    "url": "https://fnusale.oss-cn-hangzhou.aliyuncs.com/user/auth/123/20260319/uuid.jpg"
  }
}
```

### 4. 文件限制

#### 头像
- 文件类型：jpg, jpeg, png, gif, webp
- 文件大小：最大 2MB
- 存储路径：`user/avatar/{userId}/{yyyyMMdd}/{uuid}.jpg`

#### 认证图片
- 文件类型：jpg, jpeg, png, gif, webp
- 文件大小：最大 5MB
- 存储路径：`user/auth/{userId}/{yyyyMMdd}/{uuid}.jpg`

### 5. 安全特性
- JWT Token 认证：只有登录用户可上传
- 文件类型校验：只允许图片格式
- 文件大小限制：防止大文件攻击
- 旧文件自动删除：更换头像时自动删除 OSS 上的旧头像
- 用户隔离：每个用户的文件存储在独立目录

## 前端使用

### 1. API 调用

```typescript
import { userApi } from '@/api/user'

// 上传头像
const uploadAvatar = async (file: any) => {
  const res = await userApi.uploadAvatar({ tempFilePath: file.path })
  return res.data.url
}

// 上传认证图片
const uploadAuthImage = async (file: any) => {
  const res = await userApi.uploadAuthImage({ tempFilePath: file.path })
  return res.data.url
}
```

### 2. 页面集成

#### 头像上传 (pages/user/profile.vue)
- 点击头像区域选择图片
- 自动上传到 OSS
- 上传成功后更新用户信息

#### 认证图片上传 (pages/user/auth.vue)
- 点击上传区域选择证件照片
- 自动上传到 OSS
- 上传成功后提交认证申请

### 3. 错误处理
- 上传失败会显示错误提示
- 文件类型错误会提示只支持图片格式
- 文件超大会提示大小限制

## OSS Bucket 配置

### 1. 创建 Bucket
1. 登录阿里云 OSS 控制台
2. 创建 Bucket，名称：`fnusale`
3. 地域选择：根据业务选择（如杭州）
4. 读写权限：私有（推荐）
5. 其他配置保持默认

### 2. CORS 配置（如需前端直传）
如果后续需要前端直传 OSS，需要配置 CORS：
- 允许 Origin：`*` 或指定域名
- 允许方法：`POST`, `PUT`, `DELETE`
- 允许 Header：`*`
- 暴露 Header：`ETag`

### 3. 生命周期规则（可选）
可配置生命周期规则自动清理临时文件：
- 前缀：`user/temp/`
- 过期天数：7 天

## 测试指南

### 1. 后端测试
```bash
# 使用 curl 测试头像上传
curl -X POST http://localhost:8101/upload/avatar \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/avatar.jpg"
```

### 2. 前端测试
1. 启动用户服务
2. 启动前端应用
3. 登录用户账号
4. 进入个人中心测试头像上传
5. 进入认证页面测试认证图片上传

## 注意事项

1. **费用控制**: 配置合理的存储容量和流量限制
2. **图片审核**: 认证图片需要人工审核确保合规
3. **备份策略**: 重要图片（认证图片）建议定期备份
4. **隐私保护**: OSS Bucket 设置为私有，通过后端接口访问
5. **CDN 加速**: 建议配置 CDN 域名加速图片访问

## 后续优化建议

1. **前端直传**: 实现前端直传 OSS，减少服务器带宽压力
2. **图片压缩**: 前端上传前自动压缩图片
3. **图片处理**: 使用 OSS 图片处理生成缩略图
4. **上传进度**: 显示上传进度条
5. **批量上传**: 支持批量上传多张图片

## 相关文件

### 后端
- `OssConfig.java` - OSS 配置类
- `OssService.java` - OSS 服务类
- `FileUploadController.java` - 文件上传控制器
- `UserServiceImpl.java` - 用户服务实现（集成 OSS）

### 前端
- `src/api/user.ts` - 用户 API（包含上传方法）
- `src/utils/request.ts` - 请求工具（包含 upload 方法）
- `src/pages/user/profile.vue` - 个人中心（头像上传）
- `src/pages/user/auth.vue` - 认证页面（认证图片上传）
