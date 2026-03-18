#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
管理员认证API测试脚本
测试 /admin/auth/* 系列接口
"""

import requests
import json
import sys

BASE_URL = "http://localhost:8080"

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    END = '\033[0m'

def print_test(name, passed, message=""):
    status = f"{Colors.GREEN}✓ PASS{Colors.END}" if passed else f"{Colors.RED}✗ FAIL{Colors.END}"
    print(f"{status} - {name}")
    if message:
        print(f"    {message}")

def print_info(msg):
    print(f"{Colors.BLUE}[INFO]{Colors.END} {msg}")

def print_error(msg):
    print(f"{Colors.RED}[ERROR]{Colors.END} {msg}")

def test_admin_auth_apis():
    print("\n" + "="*60)
    print("管理员认证API测试")
    print("="*60 + "\n")
    
    access_token = None
    refresh_token = None
    
    # 测试1: 管理员登录
    print_info("测试1: 管理员登录 POST /admin/auth/login")
    try:
        login_data = {
            "username": "admin",
            "password": "admin123"
        }
        response = requests.post(
            f"{BASE_URL}/admin/auth/login",
            json=login_data,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") == 200:
                data = result.get("data", {})
                access_token = data.get("accessToken")
                refresh_token = data.get("refreshToken")
                admin_info = data.get("adminInfo", {})
                
                print_test("管理员登录", True, 
                    f"用户: {admin_info.get('username')}, 角色: {admin_info.get('role')}")
                print(f"    AccessToken: {access_token[:50]}..." if access_token else "    AccessToken: None")
            else:
                print_test("管理员登录", False, f"业务错误: {result.get('message')}")
        else:
            print_test("管理员登录", False, f"HTTP状态码: {response.status_code}")
    except requests.exceptions.ConnectionError:
        print_error("无法连接到服务器，请确保后端服务已启动")
        return False
    except Exception as e:
        print_test("管理员登录", False, str(e))
    
    if not access_token:
        print_error("登录失败，无法继续后续测试")
        return False
    
    print()
    
    # 测试2: 获取当前管理员信息
    print_info("测试2: 获取管理员信息 GET /admin/auth/info")
    try:
        response = requests.get(
            f"{BASE_URL}/admin/auth/info",
            headers={
                "Authorization": f"Bearer {access_token}",
                "Content-Type": "application/json"
            }
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") == 200:
                admin_info = result.get("data", {})
                print_test("获取管理员信息", True,
                    f"ID: {admin_info.get('id')}, 用户名: {admin_info.get('username')}")
                print(f"    权限: {admin_info.get('permissions', [])}")
            else:
                print_test("获取管理员信息", False, f"业务错误: {result.get('message')}")
        else:
            print_test("获取管理员信息", False, f"HTTP状态码: {response.status_code}")
    except Exception as e:
        print_test("获取管理员信息", False, str(e))
    
    print()
    
    # 测试3: 刷新Token
    print_info("测试3: 刷新Token POST /admin/auth/refresh-token")
    try:
        response = requests.post(
            f"{BASE_URL}/admin/auth/refresh-token",
            params={"refreshToken": refresh_token},
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") == 200:
                data = result.get("data", {})
                new_access_token = data.get("accessToken")
                print_test("刷新Token", True, 
                    f"新Token: {new_access_token[:50]}..." if new_access_token else "新Token: None")
                # 更新token
                if new_access_token:
                    access_token = new_access_token
            else:
                print_test("刷新Token", False, f"业务错误: {result.get('message')}")
        else:
            print_test("刷新Token", False, f"HTTP状态码: {response.status_code}")
    except Exception as e:
        print_test("刷新Token", False, str(e))
    
    print()
    
    # 测试4: 管理员登出
    print_info("测试4: 管理员登出 POST /admin/auth/logout")
    try:
        response = requests.post(
            f"{BASE_URL}/admin/auth/logout",
            headers={
                "Authorization": f"Bearer {access_token}",
                "Content-Type": "application/json"
            }
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") == 200:
                print_test("管理员登出", True)
            else:
                print_test("管理员登出", False, f"业务错误: {result.get('message')}")
        else:
            print_test("管理员登出", False, f"HTTP状态码: {response.status_code}")
    except Exception as e:
        print_test("管理员登出", False, str(e))
    
    print()
    
    # 测试5: 登录失败测试 - 错误密码
    print_info("测试5: 登录失败测试 - 错误密码")
    try:
        login_data = {
            "username": "admin",
            "password": "wrongpassword"
        }
        response = requests.post(
            f"{BASE_URL}/admin/auth/login",
            json=login_data,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") != 200:
                print_test("错误密码登录被拒绝", True, f"错误信息: {result.get('message')}")
            else:
                print_test("错误密码登录被拒绝", False, "应该返回错误但返回成功")
        else:
            print_test("错误密码登录被拒绝", True, f"HTTP状态码: {response.status_code}")
    except Exception as e:
        print_test("错误密码登录被拒绝", False, str(e))
    
    print()
    print("="*60)
    print("测试完成")
    print("="*60)
    
    return True

if __name__ == "__main__":
    if len(sys.argv) > 1:
        BASE_URL = sys.argv[1]
    
    print(f"{Colors.YELLOW}使用API地址: {BASE_URL}{Colors.END}")
    test_admin_auth_apis()
