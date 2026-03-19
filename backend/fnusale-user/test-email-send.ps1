# 邮箱验证码测试脚本
Write-Host "=== 开始测试邮箱验证码发送 ===" -ForegroundColor Green

# 测试配置
$testEmail = "lrx563647@qq.com"
$apiUrl = "http://localhost:8101/api/user/captcha/send"

Write-Host "`n测试邮箱：$testEmail"
Write-Host "API 地址：$apiUrl`n"

# 构建请求体
$body = @{
    account = $testEmail
    type = "REGISTER"
} | ConvertTo-Json -Compress

Write-Host "请求数据：$body`n"

try {
    # 发送请求
    Write-Host "正在发送验证码..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri $apiUrl -Method Post -ContentType "application/json; charset=utf-8" -Body $body
    
    Write-Host "`n✓ 请求成功！" -ForegroundColor Green
    Write-Host "响应数据：$response"
    
    if ($response.code -eq 0) {
        Write-Host "`n✓✓✓ 邮件验证码发送成功！" -ForegroundColor Green
        Write-Host "请检查邮箱：$testEmail" -ForegroundColor Cyan
    } else {
        Write-Host "`n✗ 请求失败：$($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "`n✗ 请求失败！" -ForegroundColor Red
    Write-Host "错误信息：$_" -ForegroundColor Red
    Write-Host "`n可能的原因：" -ForegroundColor Yellow
    Write-Host "1. 服务未启动（localhost:8101）"
    Write-Host "2. 网络连接问题"
    Write-Host "3. 邮箱配置错误"
    Write-Host "4. SMTP 服务器不可达"
}

Write-Host "`n=== 测试结束 ===" -ForegroundColor Green
