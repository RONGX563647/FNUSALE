# OSS 上传功能测试指南

## 测试准备

### 1. 安装依赖

```bash
# 安装 Python 依赖
pip install requests Pillow
```

### 2. 配置测试账号

编辑 `test_oss_full.py` 文件，修改测试账号配置：

```python
TEST_ACCOUNT = {
    "phone": "您的手机号",
    "password": "您的密码",
    "loginType": "PHONE"
}
```

### 3. 启动服务

确保用户服务已启动（端口 8101）：

```bash
cd backend/fnusale-user
mvn spring-boot:run
```

## 测试方法

### 方法一：Python 完整测试（推荐）

```bash
# 进入测试脚本目录
cd backend/fnusale-user

# 运行完整测试脚本
python test_oss_full.py
```

测试流程：
1. 自动登录获取 Token
2. 创建测试图片
3. 测试头像上传
4. 测试认证图片上传
5. 显示测试结果
6. 清理测试文件

### 方法二：使用 curl 命令

#### 1. 登录获取 Token

```bash
curl -X POST http://localhost:8101/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "password": "test123456",
    "loginType": "PHONE"
  }'
```

响应示例：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "userInfo": {...}
  }
}
```

#### 2. 测试头像上传

```bash
# 准备测试图片
convert -size 200x200 xc:blue test_avatar.jpg

# 上传头像
curl -X POST http://localhost:8101/upload/avatar \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test_avatar.jpg"
```

#### 3. 测试认证图片上传

```bash
# 准备测试图片
convert -size 800x600 xc:green test_auth.jpg

# 上传认证图片
curl -X POST http://localhost:8101/upload/auth \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test_auth.jpg"
```

### 方法三：使用 Postman

#### 1. 登录请求
- **Method**: POST
- **URL**: `http://localhost:8101/user/login`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "phone": "13800138000",
  "password": "test123456",
  "loginType": "PHONE"
}
```

#### 2. 上传头像
- **Method**: POST
- **URL**: `http://localhost:8101/upload/avatar`
- **Headers**: 
  - `Authorization: Bearer YOUR_TOKEN`
- **Body** (form-data):
  - Key: `file`, Type: File, Value: 选择图片文件

#### 3. 上传认证图片
- **Method**: POST
- **URL**: `http://localhost:8101/upload/auth`
- **Headers**: 
  - `Authorization: Bearer YOUR_TOKEN`
- **Body** (form-data):
  - Key: `file`, Type: File, Value: 选择图片文件

### 方法四：使用批处理脚本（Windows）

```bash
# 运行批处理脚本
test_upload.bat
```

## 预期结果

### 成功响应示例

```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "url": "https://rongxpicture.oss-cn-beijing.aliyuncs.com/user/avatar/1/20260319/abc123.jpg"
  }
}
```

### 失败响应示例

```json
{
  "code": 500,
  "message": "文件上传失败：文件类型不支持",
  "data": null
}
```

## 常见问题排查

### 1. 登录失败

**问题**: 无法登录或提示用户不存在

**解决**:
- 确保数据库中已有测试账号
- 使用注册接口先注册账号
- 检查密码是否正确

### 2. 连接失败

**问题**: `Connection refused` 或 `无法连接到服务器`

**解决**:
- 检查用户服务是否启动
- 确认端口 8101 是否被占用
- 检查防火墙设置

### 3. OSS 上传失败

**问题**: 上传时返回 500 错误

**解决**:
- 检查 OSS 配置是否正确（endpoint, bucket, accessKey）
- 确认 OSS Bucket 是否存在
- 检查 AccessKey 权限
- 查看服务端日志

### 4. 文件类型错误

**问题**: 提示"不支持的文件类型"

**解决**:
- 确保上传的是图片文件（jpg, png, gif, webp）
- 检查文件扩展名是否正确
- 使用 `-F "file=@xxx.jpg"` 格式上传

## 查看上传结果

### 1. 阿里云 OSS 控制台

1. 登录阿里云 OSS 控制台
2. 进入 `rongxpicture` Bucket
3. 查看文件目录：
   - 头像：`user/avatar/{userId}/{date}/`
   - 认证图片：`user/auth/{userId}/{date}/`

### 2. 直接访问 URL

使用返回的 URL 直接在浏览器访问：
```
https://rongxpicture.oss-cn-beijing.aliyuncs.com/user/avatar/1/20260319/abc123.jpg
```

## 性能测试

### 批量上传测试

```python
# 批量测试上传（可选）
for i in range(10):
    filename = f"test_{i}.jpg"
    create_test_image(filename)
    success, url = upload_file(UPLOAD_URL, filename, token, f"测试{i}")
    print(f"上传{i}: {'成功' if success else '失败'}")
```

## 清理测试数据

测试完成后，建议：
1. 删除测试脚本创建的临时文件
2. 在 OSS 控制台清理测试文件
3. 删除测试账号（如不需要）

## 下一步

测试通过后，可以：
1. 在前端应用中集成头像上传功能
2. 在认证页面集成证件照片上传
3. 配置 OSS CDN 加速图片访问
4. 实现图片压缩和优化
