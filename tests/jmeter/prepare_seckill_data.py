"""
秒杀压测数据准备脚本
用于准备压测所需的测试数据：
1. 创建秒杀活动
2. 预热Redis库存
3. 准备测试用户

使用方法:
    python tests/jmeter/prepare_seckill_data.py --activity-id 1 --stock 50 --users 1000
"""

import requests
import argparse
import time
from datetime import datetime, timedelta

BASE_URL = "http://localhost:8105"
ADMIN_HEADERS = {
    "Content-Type": "application/json",
    "X-User-Id": "1",
    "X-User-Role": "ADMIN"
}


def create_seckill_activity(product_id: int, stock: int, seckill_price: float):
    """创建秒杀活动"""
    start_time = datetime.now() + timedelta(minutes=5)
    end_time = start_time + timedelta(hours=1)
    
    activity_data = {
        "activityName": f"压测秒杀活动_{datetime.now().strftime('%Y%m%d%H%M%S')}",
        "productId": product_id,
        "seckillPrice": seckill_price,
        "totalStock": stock,
        "startTime": start_time.strftime("%Y-%m-%d %H:%M:%S"),
        "endTime": end_time.strftime("%Y-%m-%d %H:%M:%S")
    }
    
    response = requests.post(
        f"{BASE_URL}/marketing/seckill",
        json=activity_data,
        headers=ADMIN_HEADERS
    )
    
    if response.status_code == 200:
        data = response.json()
        if data.get("code") == 200:
            print(f"✅ 创建秒杀活动成功")
            return True
        else:
            print(f"❌ 创建秒杀活动失败: {data.get('message')}")
    else:
        print(f"❌ 创建秒杀活动失败: HTTP {response.status_code}")
    return False


def get_seckill_activities():
    """获取秒杀活动列表"""
    response = requests.get(
        f"{BASE_URL}/marketing/seckill/list",
        headers={"X-User-Id": "1"}
    )
    
    if response.status_code == 200:
        data = response.json()
        if data.get("code") == 200:
            return data.get("data", [])
    return []


def warmup_redis(activity_id: int):
    """预热Redis库存（调用内部接口）"""
    print(f"⏳ 等待活动预热...")
    time.sleep(10)  # 等待定时任务预热
    
    # 检查Redis是否预热成功
    response = requests.get(
        f"{BASE_URL}/marketing/seckill/{activity_id}",
        headers={"X-User-Id": "1"}
    )
    
    if response.status_code == 200:
        data = response.json()
        if data.get("code") == 200:
            activity = data.get("data", {})
            print(f"✅ 活动信息: ID={activity.get('id')}, 库存={activity.get('remainStock')}")
            return True
    return False


def check_service_health():
    """检查服务健康状态"""
    try:
        response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
        if response.status_code == 200:
            print("✅ 服务运行正常")
            return True
        else:
            print(f"❌ 服务异常: HTTP {response.status_code}")
    except Exception as e:
        print(f"❌ 无法连接服务: {e}")
    return False


def main():
    parser = argparse.ArgumentParser(description="秒杀压测数据准备")
    parser.add_argument("--product-id", type=int, default=1, help="商品ID")
    parser.add_argument("--stock", type=int, default=50, help="秒杀库存")
    parser.add_argument("--price", type=float, default=9.9, help="秒杀价格")
    parser.add_argument("--users", type=int, default=500, help="压测用户数")
    parser.add_argument("--activity-id", type=int, help="使用现有活动ID")
    
    args = parser.parse_args()
    
    print("=" * 50)
    print("秒杀压测数据准备")
    print("=" * 50)
    
    # 1. 检查服务状态
    if not check_service_health():
        print("\n请先启动服务!")
        return
    
    # 2. 获取或创建活动
    if args.activity_id:
        activity_id = args.activity_id
        print(f"\n使用现有活动: ID={activity_id}")
    else:
        print(f"\n创建新活动: 商品ID={args.product_id}, 库存={args.stock}, 价格={args.price}")
        if not create_seckill_activity(args.product_id, args.stock, args.price):
            print("\n创建活动失败，尝试使用现有活动...")
        
        # 获取最新活动
        activities = get_seckill_activities()
        if activities:
            activity_id = activities[0].get("id")
            print(f"使用活动: ID={activity_id}")
        else:
            print("❌ 没有可用的秒杀活动")
            return
    
    # 3. 预热Redis
    warmup_redis(activity_id)
    
    # 4. 输出压测参数
    print("\n" + "=" * 50)
    print("压测参数")
    print("=" * 50)
    print(f"活动ID: {activity_id}")
    print(f"压测用户数: {args.users}")
    print(f"服务地址: {BASE_URL}")
    print("\nJMeter命令:")
    print(f"jmeter -n -t tests/jmeter/seckill_stress_test.jmx \\")
    print(f"  -JTHREAD_COUNT={args.users} \\")
    print(f"  -JACTIVITY_ID={activity_id} \\")
    print(f"  -l results/seckill_{activity_id}.jtl \\")
    print(f"  -e -o results/report_{activity_id}")
    print("\n或者使用GUI模式:")
    print(f"jmeter -t tests/jmeter/seckill_stress_test.jmx")


if __name__ == "__main__":
    main()
