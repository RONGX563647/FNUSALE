#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
阿里云 OSS 上传功能测试脚本
测试用户头像和认证图片上传接口
"""

import requests
import base64
import os
from PIL import Image
import io

# 配置信息
BASE_URL = "http://localhost:8101"
UPLOAD_AVATAR_URL = f"{BASE_URL}/upload/avatar"
UPLOAD_AUTH_URL = f"{BASE_URL}/upload/auth"
LOGIN_URL = f"{BASE_URL}/user/login"

# 测试用户账号（请根据实际情况修改）
TEST_PHONE = "13800138000"
TEST_PASSWORD = "test123456"


def create_test_image(filename="test_image.jpg", size=(200, 200), color="blue"):
    """创建测试图片"""
    img = Image.new('RGB', size, color=color)
    img.save(filename, 'JPEG')
    return filename


def login(phone, password):
    """登录获取 Token"""
    print(f"\n=== 正在登录 ===")
    print(f"手机号：{phone}")
    
    try:
        response = requests.post(LOGIN_URL, json={
            "phone": phone,
            "password": password,
            "loginType": "PHONE"
        })
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") == 200:
                token = result["data"]["accessToken"]
                print(f"✓ 登录成功")
                print(f"Token: {token[:50]}...")
                return token
            else:
                print(f"✗ 登录失败：{result.get('message')}")
        else:
            print(f"✗ HTTP 错误：{response.status_code}")
    except Exception as e:
        print(f"✗ 请求失败：{e}")
    
    return None


def upload_file(url, file_path, token, description="文件"):
    """上传文件"""
    print(f"\n=== 正在上传{description} ===")
    print(f"文件路径：{file_path}")
    print(f"上传地址：{url}")
    
    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    try:
        with open(file_path, 'rb') as f:
            files = {'file': (os.path.basename(file_path), f, 'image/jpeg')}
            response = requests.post(url, headers=headers, files=files)
        
        print(f"HTTP 状态码：{response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print(f"响应：{result}")
            
            if result.get("code") == 200:
                file_url = result["data"]["url"]
                print(f"✓ {description}上传成功")
                print(f"文件 URL: {file_url}")
                return True, file_url
            else:
                print(f"✗ {description}上传失败：{result.get('message')}")
        else:
            print(f"✗ HTTP 错误：{response.status_code}")
            print(f"响应内容：{response.text}")
    except Exception as e:
        print(f"✗ 请求失败：{e}")
    
    return False, None


def test_upload():
    """执行上传测试"""
    print("=" * 60)
    print("阿里云 OSS 上传功能测试")
    print("=" * 60)
    
    # 1. 创建测试图片
    print("\n[步骤 1] 创建测试图片...")
    avatar_file = create_test_image("test_avatar.jpg", size=(200, 200), color="blue")
    auth_file = create_test_image("test_auth.jpg", size=(800, 600), color="green")
    print(f"✓ 头像测试图片：{avatar_file}")
    print(f"✓ 认证测试图片：{auth_file}")
    
    # 2. 登录获取 Token
    print("\n[步骤 2] 登录获取 Token...")
    token = login(TEST_PHONE, TEST_PASSWORD)
    
    if not token:
        print("\n✗ 登录失败，无法继续测试")
        print("请确保：")
        print("1. 用户服务已启动 (端口 8101)")
        print("2. 测试账号存在且密码正确")
        print("\n您可以先手动注册一个账号，然后修改脚本中的 TEST_PHONE 和 TEST_PASSWORD")
        return
    
    # 3. 测试头像上传
    print("\n[步骤 3] 测试头像上传...")
    success, avatar_url = upload_file(UPLOAD_AVATAR_URL, avatar_file, token, "头像")
    
    # 4. 测试认证图片上传
    print("\n[步骤 4] 测试认证图片上传...")
    success, auth_url = upload_file(UPLOAD_AUTH_URL, auth_file, token, "认证图片")
    
    # 5. 清理测试文件
    print("\n[步骤 5] 清理测试文件...")
    try:
        os.remove(avatar_file)
        os.remove(auth_file)
        print("✓ 测试文件已清理")
    except Exception as e:
        print(f"✗ 清理文件失败：{e}")
    
    # 6. 测试结果总结
    print("\n" + "=" * 60)
    print("测试结果总结")
    print("=" * 60)
    print(f"头像上传：{'✓ 成功' if avatar_url else '✗ 失败'}")
    print(f"认证图片上传：{'✓ 成功' if auth_url else '✗ 失败'}")
    
    if avatar_url and auth_url:
        print("\n✓ 所有测试通过！OSS 上传功能正常工作")
    else:
        print("\n✗ 部分测试失败，请检查配置和日志")


if __name__ == "__main__":
    try:
        test_upload()
    except KeyboardInterrupt:
        print("\n\n测试被用户中断")
    except Exception as e:
        print(f"\n✗ 测试异常：{e}")
