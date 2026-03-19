#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简单的 OSS 上传测试脚本
使用 curl 或 Python 测试上传接口
"""

import requests
import os

# 配置
BASE_URL = "http://localhost:8101"
TOKEN = "YOUR_TOKEN_HERE"  # 替换为您的 JWT Token


def test_upload_avatar(token, image_path):
    """测试头像上传"""
    url = f"{BASE_URL}/upload/avatar"
    headers = {"Authorization": f"Bearer {token}"}
    
    with open(image_path, 'rb') as f:
        files = {'file': (os.path.basename(image_path), f, 'image/jpeg')}
        response = requests.post(url, headers=headers, files=files)
    
    print(f"头像上传响应：{response.json()}")
    return response.json()


def test_upload_auth(token, image_path):
    """测试认证图片上传"""
    url = f"{BASE_URL}/upload/auth"
    headers = {"Authorization": f"Bearer {token}"}
    
    with open(image_path, 'rb') as f:
        files = {'file': (os.path.basename(image_path), f, 'image/jpeg')}
        response = requests.post(url, headers=headers, files=files)
    
    print(f"认证图片上传响应：{response.json()}")
    return response.json()


if __name__ == "__main__":
    # 使用方法：
    # 1. 先登录获取 Token
    # 2. 替换 TOKEN 变量
    # 3. 准备测试图片
    # 4. 运行脚本
    
    test_image = "test.jpg"
    
    # 创建测试图片（如果没有）
    if not os.path.exists(test_image):
        from PIL import Image
        img = Image.new('RGB', (200, 200), color='blue')
        img.save(test_image, 'JPEG')
        print(f"创建测试图片：{test_image}")
    
    # 测试上传
    if TOKEN == "YOUR_TOKEN_HERE":
        print("请先替换 TOKEN 变量！")
        print("\n获取 Token 的方法：")
        print("1. 使用 Postman 调用登录接口获取")
        print("2. 或运行以下 curl 命令:")
        print(f"""
curl -X POST {BASE_URL}/user/login \\
  -H "Content-Type: application/json" \\
  -d '{{
    "phone": "您的手机号",
    "password": "您的密码",
    "loginType": "PHONE"
  }}'
        """)
    else:
        print("开始测试头像上传...")
        test_upload_avatar(TOKEN, test_image)
        
        print("\n开始测试认证图片上传...")
        test_upload_auth(TOKEN, test_image)
        
        # 清理
        if os.path.exists(test_image):
            os.remove(test_image)
