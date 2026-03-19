@echo off
echo ========================================
echo 阿里云 OSS 上传功能测试
echo ========================================
echo.

REM 设置 Token（需要先登录获取）
set TOKEN=YOUR_TOKEN_HERE

echo 使用方法:
echo 1. 先调用登录接口获取 Token
echo 2. 将 Token 设置到上面的 TOKEN 变量
echo 3. 准备一张测试图片
echo 4. 运行此脚本
echo.

echo 登录接口:
curl -X POST http://localhost:8101/user/login -H "Content-Type: application/json" -d "{\"phone\":\"13800138000\",\"password\":\"test123456\",\"loginType\":\"PHONE\"}"
echo.
echo.

echo 测试头像上传:
curl -X POST http://localhost:8101/upload/avatar -H "Authorization: Bearer %TOKEN%" -F "file=@test_avatar.jpg"
echo.
echo.

echo 测试认证图片上传:
curl -X POST http://localhost:8101/upload/auth -H "Authorization: Bearer %TOKEN%" -F "file=@test_auth.jpg"
echo.

pause
