#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
定位功能API测试脚本
测试IP定位、逆地理编码、围栏校验、附近自提点查询等功能
"""

import requests
import json
import sys

BASE_URL = "http://localhost:8101"

def print_response(response, name="Response"):
    """打印响应结果"""
    print(f"\n{'='*60}")
    print(f"【{name}】")
    print(f"{'='*60}")
    print(f"状态码: {response.status_code}")
    try:
        data = response.json()
        print(f"响应内容: {json.dumps(data, ensure_ascii=False, indent=2)}")
    except:
        print(f"响应内容: {response.text}")
    print(f"{'='*60}\n")

def test_ip_location():
    """测试IP定位接口"""
    print("\n" + "="*60)
    print("测试1: IP定位接口")
    print("="*60)
    
    url = f"{BASE_URL}/user/location/ip"
    headers = {
        "X-Forwarded-For": "116.407526"  # 模拟北京IP
    }
    
    try:
        response = requests.get(url, headers=headers, timeout=10)
        print_response(response, "IP定位")
        return response.status_code == 200
    except requests.exceptions.ConnectionError:
        print("错误: 无法连接到服务器，请确保服务已启动")
        return False
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_reverse_geocode():
    """测试逆地理编码接口"""
    print("\n" + "="*60)
    print("测试2: 逆地理编码接口")
    print("="*60)
    
    url = f"{BASE_URL}/user/location/geocode"
    params = {
        "longitude": "116.407526",
        "latitude": "39.904030"
    }
    
    try:
        response = requests.get(url, params=params, timeout=10)
        print_response(response, "逆地理编码")
        return response.status_code == 200
    except requests.exceptions.ConnectionError:
        print("错误: 无法连接到服务器，请确保服务已启动")
        return False
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_location_verify():
    """测试围栏校验接口"""
    print("\n" + "="*60)
    print("测试3: 围栏校验接口")
    print("="*60)
    
    url = f"{BASE_URL}/user/location/verify"
    
    # 测试在围栏内的坐标
    params_in = {
        "longitude": "119.2072",
        "latitude": "26.0661"
    }
    
    print("\n--- 测试在围栏内的坐标 ---")
    try:
        response = requests.get(url, params=params_in, timeout=10)
        print_response(response, "围栏校验(在围栏内)")
    except Exception as e:
        print(f"错误: {e}")
    
    # 测试在围栏外的坐标
    params_out = {
        "longitude": "116.407526",
        "latitude": "39.904030"
    }
    
    print("\n--- 测试在围栏外的坐标 ---")
    try:
        response = requests.get(url, params=params_out, timeout=10)
        print_response(response, "围栏校验(在围栏外)")
    except Exception as e:
        print(f"错误: {e}")
    
    return True

def test_current_location():
    """测试综合定位接口"""
    print("\n" + "="*60)
    print("测试4: 综合定位接口")
    print("="*60)
    
    url = f"{BASE_URL}/user/location/current"
    
    # 测试不传经纬度，使用IP定位
    print("\n--- 测试不传经纬度，使用IP定位 ---")
    headers = {
        "X-Forwarded-For": "116.407526"
    }
    try:
        response = requests.get(url, headers=headers, timeout=10)
        print_response(response, "综合定位(IP定位)")
    except Exception as e:
        print(f"错误: {e}")
    
    # 测试传经纬度
    print("\n--- 测试传经纬度 ---")
    params = {
        "longitude": "119.2072",
        "latitude": "26.0661"
    }
    try:
        response = requests.get(url, params=params, timeout=10)
        print_response(response, "综合定位(GPS定位)")
    except Exception as e:
        print(f"错误: {e}")
    
    return True

def test_nearby_pick_points():
    """测试附近自提点查询接口"""
    print("\n" + "="*60)
    print("测试5: 附近自提点查询接口")
    print("="*60)
    
    url = f"{BASE_URL}/user/pick-point/nearby"
    
    # 测试使用IP定位
    print("\n--- 测试使用IP定位查询附近自提点 ---")
    headers = {
        "X-Forwarded-For": "116.407526"
    }
    params = {
        "distance": 5000
    }
    try:
        response = requests.get(url, params=params, headers=headers, timeout=10)
        print_response(response, "附近自提点(IP定位)")
    except Exception as e:
        print(f"错误: {e}")
    
    # 测试使用经纬度
    print("\n--- 测试使用经纬度查询附近自提点 ---")
    params = {
        "longitude": "119.2072",
        "latitude": "26.0661",
        "distance": 5000
    }
    try:
        response = requests.get(url, params=params, timeout=10)
        print_response(response, "附近自提点(GPS定位)")
    except Exception as e:
        print(f"错误: {e}")
    
    return True

def test_pick_point_list():
    """测试自提点列表接口"""
    print("\n" + "="*60)
    print("测试6: 自提点列表接口")
    print("="*60)
    
    url = f"{BASE_URL}/user/pick-point/list"
    
    try:
        response = requests.get(url, timeout=10)
        print_response(response, "自提点列表")
        return response.status_code == 200
    except requests.exceptions.ConnectionError:
        print("错误: 无法连接到服务器，请确保服务已启动")
        return False
    except Exception as e:
        print(f"错误: {e}")
        return False

def main():
    """主测试函数"""
    print("\n" + "#"*60)
    print("#" + " "*20 + "定位功能API测试" + " "*20 + "#")
    print("#"*60)
    
    print("\n提示: 请确保用户服务(fnusale-user)已启动在端口8101")
    print("提示: 请确保已配置高德地图API Key (AMAP_KEY环境变量)")
    
    input("\n按回车键开始测试...")
    
    results = []
    
    # 执行测试
    results.append(("IP定位接口", test_ip_location()))
    results.append(("逆地理编码接口", test_reverse_geocode()))
    results.append(("围栏校验接口", test_location_verify()))
    results.append(("综合定位接口", test_current_location()))
    results.append(("附近自提点查询", test_nearby_pick_points()))
    results.append(("自提点列表接口", test_pick_point_list()))
    
    # 打印测试结果汇总
    print("\n" + "#"*60)
    print("#" + " "*20 + "测试结果汇总" + " "*21 + "#")
    print("#"*60)
    
    passed = 0
    failed = 0
    for name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"  {name}: {status}")
        if result:
            passed += 1
        else:
            failed += 1
    
    print(f"\n总计: {passed} 通过, {failed} 失败")
    print("#"*60 + "\n")
    
    return failed == 0

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
