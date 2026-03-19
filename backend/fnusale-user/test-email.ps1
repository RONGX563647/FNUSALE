# 测试邮件验证码发送
$body = @{
    account = "lrx563647@qq.com"
    type = "REGISTER"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8101/user/captcha/send" -Method Post -ContentType "application/json" -Body $body
    Write-Host "✓ 请求成功！" -ForegroundColor Green
    Write-Host "响应：$response"
} catch {
    Write-Host "✗ 请求失败！" -ForegroundColor Red
    Write-Host "错误：$_"
}
