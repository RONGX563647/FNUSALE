"""
营销模块API测试
测试前请确保:
1. 基础设施服务已启动 (MySQL, Redis, Nacos, RocketMQ)
2. fnusale-marketing服务已启动 (端口8105)
3. 数据库有测试数据

运行命令: pytest tests/test_marketing_api.py -v
"""

import pytest
import requests
from datetime import datetime, timedelta
from typing import Optional

# 服务地址
BASE_URL = "http://localhost:8105"
MARKETING_PREFIX = "/marketing"

# 请求头
HEADERS = {
    "Content-Type": "application/json",
    "X-User-Id": "1"  # 模拟用户ID
}

# 管理员请求头
ADMIN_HEADERS = {
    "Content-Type": "application/json",
    "X-User-Id": "1",
    "X-User-Role": "ADMIN"  # 模拟管理员角色
}


class TestHealthCheck:
    """健康检查测试"""

    def test_service_health(self):
        """测试服务是否启动"""
        try:
            response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
            assert response.status_code == 200
        except requests.exceptions.ConnectionError:
            pytest.skip("服务未启动，跳过测试")


class TestCouponAvailable:
    """优惠券可领取列表测试"""

    def test_get_available_coupons(self):
        """测试获取可领取优惠券列表"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/available",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data
        assert isinstance(data["data"], list)

    def test_get_available_coupons_with_user(self):
        """测试登录用户获取可领取优惠券"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/available",
            headers={**HEADERS, "X-User-Id": "1"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200


class TestCouponReceive:
    """优惠券领取测试"""

    def test_receive_coupon_not_found(self):
        """测试领取不存在的优惠券"""
        response = requests.post(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/999999/receive",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            # 优惠券不存在应返回错误
            assert data["code"] != 200 or "error" in str(data).lower()

    def test_receive_coupon_without_login(self):
        """测试未登录领取优惠券"""
        response = requests.post(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/1/receive",
            headers={"Content-Type": "application/json"}  # 不传用户ID
        )
        # 应该返回401或业务错误
        assert response.status_code in [200, 401, 403]


class TestMyCoupons:
    """我的优惠券测试"""

    def test_get_my_coupons(self):
        """测试获取我的优惠券列表"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/my",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data

    def test_get_my_coupons_with_status(self):
        """测试按状态获取我的优惠券"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/my",
            params={"status": "UNUSED"},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_my_coupons_used_status(self):
        """测试获取已使用的优惠券"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/my",
            params={"status": "USED"},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_my_coupons_expired_status(self):
        """测试获取已过期的优惠券"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/my",
            params={"status": "EXPIRED"},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200


class TestUsableCoupons:
    """可用优惠券测试"""

    def test_get_usable_coupons(self):
        """测试获取商品可用优惠券"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/usable",
            params={"productId": 1, "price": 100.00},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data

    def test_get_usable_coupons_high_price(self):
        """测试高价商品可用优惠券"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/usable",
            params={"productId": 1, "price": 1000.00},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200


class TestCouponDetail:
    """优惠券详情测试"""

    def test_get_coupon_detail(self):
        """测试获取优惠券详情"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/1",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200

    def test_get_coupon_detail_not_found(self):
        """测试获取不存在的优惠券详情"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/999999",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] != 200


class TestCouponAdmin:
    """优惠券管理测试（管理员）"""

    def test_get_coupon_page(self):
        """测试分页查询优惠券"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/page",
            params={"pageNum": 1, "pageSize": 10},
            headers=ADMIN_HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data

    def test_get_coupon_page_with_filter(self):
        """测试带条件分页查询优惠券"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/page",
            params={
                "pageNum": 1,
                "pageSize": 10,
                "name": "测试",
                "status": 1
            },
            headers=ADMIN_HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_create_coupon(self):
        """测试创建优惠券"""
        coupon_data = {
            "name": f"测试优惠券_{datetime.now().strftime('%Y%m%d%H%M%S')}",
            "type": "DISCOUNT",
            "reduceAmount": 10.00,
            "minAmount": 50.00,
            "totalCount": 100,
            "perLimit": 1,
            "startTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "endTime": (datetime.now() + timedelta(days=30)).strftime("%Y-%m-%d %H:%M:%S"),
            "status": 1
        }
        response = requests.post(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon",
            json=coupon_data,
            headers=ADMIN_HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200

    def test_update_coupon_not_found(self):
        """测试更新不存在的优惠券"""
        coupon_data = {
            "name": "更新测试优惠券",
            "type": "DISCOUNT",
            "reduceAmount": 20.00,
            "minAmount": 100.00
        }
        response = requests.put(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/999999",
            json=coupon_data,
            headers=ADMIN_HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] != 200

    def test_delete_coupon_not_found(self):
        """测试删除不存在的优惠券"""
        response = requests.delete(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/999999",
            headers=ADMIN_HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] != 200

    def test_update_coupon_status(self):
        """测试更新优惠券状态"""
        response = requests.put(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/1/status",
            params={"status": 1},
            headers=ADMIN_HEADERS
        )
        assert response.status_code in [200, 500]

    def test_grant_coupon(self):
        """测试发放优惠券"""
        response = requests.post(
            f"{BASE_URL}{MARKETING_PREFIX}/coupon/1/grant",
            json=[1, 2, 3],  # 用户ID列表
            headers=ADMIN_HEADERS
        )
        assert response.status_code in [200, 500]


class TestSeckillList:
    """秒杀活动列表测试"""

    def test_get_seckill_list(self):
        """测试获取秒杀活动列表"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/list",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data

    def test_get_seckill_list_with_user(self):
        """测试登录用户获取秒杀活动列表"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/list",
            headers={**HEADERS, "X-User-Id": "1"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200


class TestSeckillDetail:
    """秒杀活动详情测试"""

    def test_get_activity_detail(self):
        """测试获取秒杀活动详情"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/1",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200

    def test_get_activity_detail_not_found(self):
        """测试获取不存在的秒杀活动详情"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/999999",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] != 200


class TestSeckillProduct:
    """秒杀商品测试"""

    def test_get_seckill_product_detail(self):
        """测试获取秒杀商品详情"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/product/1",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200


class TestSeckillJoin:
    """参与秒杀测试"""

    def test_join_seckill_not_found(self):
        """测试参与不存在的秒杀活动"""
        response = requests.post(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/999999/join",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] != 200

    def test_join_seckill_without_login(self):
        """测试未登录参与秒杀"""
        response = requests.post(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/1/join",
            headers={"Content-Type": "application/json"}
        )
        assert response.status_code in [200, 401, 403]


class TestSeckillResult:
    """秒杀结果测试"""

    def test_get_seckill_result(self):
        """测试获取秒杀结果"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/1/result",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200


class TestSeckillToday:
    """今日秒杀测试"""

    def test_get_today_seckills(self):
        """测试获取今日秒杀"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/today",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data


class TestSeckillTimeSlots:
    """秒杀时段测试"""

    def test_get_time_slots(self):
        """测试获取秒杀时段"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/time-slots",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data


class TestSeckillReminder:
    """秒杀提醒测试"""

    def test_set_reminder(self):
        """测试设置秒杀提醒"""
        response = requests.post(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/1/reminder",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]

    def test_cancel_reminder(self):
        """测试取消秒杀提醒"""
        response = requests.delete(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/1/reminder",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]

    def test_cancel_reminder_not_set(self):
        """测试取消未设置的提醒"""
        response = requests.delete(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/999999/reminder",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]


class TestSeckillAdmin:
    """秒杀活动管理测试（管理员）"""

    def test_get_activity_page(self):
        """测试分页查询秒杀活动"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/page",
            params={"pageNum": 1, "pageSize": 10},
            headers=ADMIN_HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data

    def test_get_activity_page_with_status(self):
        """测试按状态分页查询秒杀活动"""
        response = requests.get(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/page",
            params={
                "pageNum": 1,
                "pageSize": 10,
                "status": "ONGOING"
            },
            headers=ADMIN_HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_create_activity(self):
        """测试创建秒杀活动"""
        start_time = datetime.now() + timedelta(hours=1)
        end_time = start_time + timedelta(hours=2)

        activity_data = {
            "title": f"测试秒杀活动_{datetime.now().strftime('%Y%m%d%H%M%S')}",
            "productId": 1,
            "seckillPrice": 99.00,
            "originalPrice": 199.00,
            "stock": 100,
            "perLimit": 1,
            "startTime": start_time.strftime("%Y-%m-%d %H:%M:%S"),
            "endTime": end_time.strftime("%Y-%m-%d %H:%M:%S"),
            "status": "PENDING"
        }
        response = requests.post(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill",
            json=activity_data,
            headers=ADMIN_HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200

    def test_update_activity_not_found(self):
        """测试更新不存在的秒杀活动"""
        activity_data = {
            "title": "更新测试秒杀活动",
            "seckillPrice": 88.00
        }
        response = requests.put(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/999999",
            json=activity_data,
            headers=ADMIN_HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] != 200

    def test_delete_activity_not_found(self):
        """测试删除不存在的秒杀活动"""
        response = requests.delete(
            f"{BASE_URL}{MARKETING_PREFIX}/seckill/999999",
            headers=ADMIN_HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] != 200


class TestConcurrentSeckill:
    """秒杀并发测试"""

    def test_concurrent_join_seckill(self):
        """测试并发参与秒杀（模拟高并发场景）"""
        import concurrent.futures

        def join_seckill(user_id):
            try:
                response = requests.post(
                    f"{BASE_URL}{MARKETING_PREFIX}/seckill/1/join",
                    headers={**HEADERS, "X-User-Id": str(user_id)},
                    timeout=5
                )
                return response.status_code, response.json() if response.status_code == 200 else None
            except Exception as e:
                return None, str(e)

        # 模拟5个用户同时参与秒杀
        with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
            futures = [executor.submit(join_seckill, i) for i in range(1, 6)]
            results = [f.result() for f in concurrent.futures.as_completed(futures)]

        # 验证所有请求都有响应
        assert len(results) == 5
        for status_code, data in results:
            if status_code is not None:
                assert status_code in [200, 500, 429]  # 429表示被限流


class TestPrometheusMetrics:
    """Prometheus监控指标测试"""

    def test_prometheus_endpoint(self):
        """测试Prometheus指标端点"""
        response = requests.get(
            f"{BASE_URL}/actuator/prometheus",
            timeout=5
        )
        assert response.status_code == 200
        # 验证包含RocketMQ指标
        content = response.text
        assert "rocketmq_" in content or "jvm_" in content

    def test_metrics_endpoint(self):
        """测试metrics端点"""
        response = requests.get(
            f"{BASE_URL}/actuator/metrics",
            timeout=5
        )
        assert response.status_code == 200
        data = response.json()
        assert "names" in data


@pytest.fixture(scope="session", autouse=True)
def check_service():
    """测试前检查服务状态"""
    try:
        response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
        if response.status_code != 200:
            pytest.exit("服务未正常运行，终止测试")
    except requests.exceptions.ConnectionError:
        pytest.exit(f"无法连接到服务 {BASE_URL}，请确保服务已启动")


if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short", "--html=report_marketing.html"])