#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
完整的 OSS 上传功能测试脚本
包含登录、头像上传、认证图片上传
"""

import requests
import os
import sys
from PIL import Image

# ==================== 配置区域 ====================
BASE_URL = "http://localhost:8101"

# 测试账号配置（请根据实际情况修改）
TEST_ACCOUNT = {
    "phone": "13800138000",      # 测试手机号
    "password": "test123456",    # 测试密码
    "loginType": "PHONE"
}

# 或者使用邮箱登录
# TEST_ACCOUNT = {
#     "email": "test@example.com",
#     "password": "test123456",
#     "loginType": "EMAIL"
# }
# ================================================


def create_test_image(filename, size=(200, 200), color="blue"):
    """创建测试图片"""
    try:
        img = Image.new('RGB', size, color=color)
        img.save(filename, 'JPEG')
        print(f"✓ 创建测试图片：{filename} ({size[0]}x{size[1]})")
        return True
    except Exception as e:
        print(f"✗ 创建图片失败：{e}")
        return False


def login():
    """登录获取 Token"""
    print("\n" + "="*60)
    print("步骤 1: 登录获取 Token")
    print("="*60)
    
    url = f"{BASE_URL}/user/login"
    
    try:
        response = requests.post(url, json=TEST_ACCOUNT)
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") == 200:
                token = result["data"]["accessToken"]
                username = result["data"]["userInfo"].get("username", "unknown")
                print(f"✓ 登录成功")
                print(f"  用户：{username}")
                print(f"  Token: {token[:50]}...")
                return token
            else:
                print(f"✗ 登录失败：{result.get('message')}")
                print(f"  响应：{result}")
        else:
            print(f"✗ HTTP 错误：{response.status_code}")
            print(f"  响应：{response.text}")
    except requests.exceptions.ConnectionError:
        print(f"✗ 无法连接到服务器：{BASE_URL}")
        print(f"  请确保用户服务已启动 (端口 8101)")
    except Exception as e:
        print(f"✗ 请求失败：{e}")
    
    return None


def upload_file(url, file_path, token, description):
    """上传文件"""
    print(f"\n上传{description}...")
    print(f"  文件：{file_path}")
    print(f"  地址：{url}")
    
    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    try:
        with open(file_path, 'rb') as f:
            files = {'file': (os.path.basename(file_path), f, 'image/jpeg')}
            response = requests.post(url, headers=headers, files=files, timeout=30)
        
        print(f"  HTTP 状态：{response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") == 200:
                file_url = result["data"]["url"]
                print(f"✓ {description}上传成功")
                print(f"  URL: {file_url}")
                return True, file_url
            else:
                print(f"✗ {description}上传失败：{result.get('message')}")
        else:
            print(f"✗ HTTP 错误：{response.status_code}")
            try:
                error = response.json()
                print(f"  错误：{error.get('message', 'Unknown')}")
            except:
                print(f"  响应：{response.text[:200]}")
    except requests.exceptions.Timeout:
        print(f"✗ 请求超时")
    except Exception as e:
        print(f"✗ 上传失败：{e}")
    
    return False, None


def test_upload(token):
    """执行上传测试"""
    print("\n" + "="*60)
    print("步骤 2: 测试文件上传")
    print("="*60)
    
    results = {
        "avatar": False,
        "auth": False
    }
    
    # 测试头像上传
    avatar_file = "test_avatar.jpg"
    if create_test_image(avatar_file, size=(200, 200), color="blue"):
        success, url = upload_file(
            f"{BASE_URL}/upload/avatar",
            avatar_file,
            token,
            "头像"
        )
        results["avatar"] = success
        
        # 清理文件
        if os.path.exists(avatar_file):
            os.remove(avatar_file)
            print(f"  已清理测试文件：{avatar_file}")
    
    # 测试认证图片上传
    auth_file = "test_auth.jpg"
    if create_test_image(auth_file, size=(800, 600), color="green"):
        success, url = upload_file(
            f"{BASE_URL}/upload/auth",
            auth_file,
            token,
            "认证图片"
        )
        results["auth"] = success
        
        # 清理文件
        if os.path.exists(auth_file):
            os.remove(auth_file)
            print(f"  已清理测试文件：{auth_file}")
    
    return results


def print_summary(results):
    """打印测试总结"""
    print("\n" + "="*60)
    print("测试总结")
    print("="*60)
    
    avatar_status = "✓ 成功" if results["avatar"] else "✗ 失败"
    auth_status = "✓ 成功" if results["auth"] else "✗ 失败"
    
    print(f"头像上传：{avatar_status}")
    print(f"认证图片上传：{auth_status}")
    
    if results["avatar"] and results["auth"]:
        print("\n✓ 所有测试通过！OSS 上传功能正常工作")
        print("\n您可以：")
        print("1. 登录阿里云 OSS 控制台查看上传的文件")
        print("2. 访问返回的 URL 查看图片")
        print("3. 在前端应用中测试头像和认证图片上传")
        return 0
    else:
        print("\n✗ 部分测试失败，请检查:")
        print("1. OSS 配置是否正确（endpoint, bucket, accessKey）")
        print("2. 用户服务日志")
        print("3. 网络连接是否正常")
        return 1


def main():
    """主函数"""
    print("\n" + "="*60)
    print("阿里云 OSS 上传功能测试")
    print("="*60)
    print(f"服务器地址：{BASE_URL}")
    print(f"测试账号：{TEST_ACCOUNT.get('phone', TEST_ACCOUNT.get('email', 'N/A'))}")
    
    # 检查依赖
    try:
        import PIL
    except ImportError:
        print("\n✗ 缺少依赖：Pillow")
        print("  请运行：pip install Pillow")
        sys.exit(1)
    
    # 1. 登录
    token = login()
    if not token:
        print("\n✗ 登录失败，无法继续测试")
        print("\n请确保:")
        print("1. 用户服务已启动")
        print("2. 测试账号存在且密码正确")
        print("3. 数据库连接正常")
        return 1
    
    # 2. 测试上传
    results = test_upload(token)
    
    # 3. 打印总结
    return print_summary(results)


if __name__ == "__main__":
    try:
        exit_code = main()
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print("\n\n测试被用户中断")
        sys.exit(1)
    except Exception as e:
        print(f"\n✗ 测试异常：{e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
