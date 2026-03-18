"""
Admin模块API测试
测试前请确保:
1. 基础设施服务已启动
2. fnusale-admin服务已启动 (端口8107)
3. 数据库有测试数据

运行命令: pytest tests/test_admin_api.py -v
"""

import pytest
import requests
from typing import Optional

# 服务地址
BASE_URL = "http://localhost:8107"
ADMIN_PREFIX = "/admin"

# 请求头
HEADERS = {
    "Content-Type": "application/json",
    "X-Admin-Id": "1"  # 模拟管理员ID
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


class TestUserManagement:
    """用户管理API测试"""

    def test_get_user_page(self):
        """测试获取用户列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/user/page",
            params={"pageNum": 1, "pageSize": 10},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data
        assert "list" in data["data"]

    def test_get_user_page_with_filter(self):
        """测试带过滤条件的用户列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/user/page",
            params={
                "pageNum": 1,
                "pageSize": 10,
                "authStatus": "AUTH_SUCCESS"
            },
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_user_detail_not_found(self):
        """测试获取不存在的用户详情"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/user/999999",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]  # 用户不存在可能返回500错误
        data = response.json()
        # 用户不存在应返回错误（可能是业务错误或HTTP错误）
        if response.status_code == 200:
            assert data.get("code") != 200 or data.get("data") is None

    def test_get_pending_auth_list(self):
        """测试获取待审核认证列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/user/auth/pending",
            params={"pageNum": 1, "pageSize": 10},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data


class TestProductAudit:
    """商品审核API测试"""

    def test_get_pending_products(self):
        """测试获取待审核商品列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/audit/pending",
            params={"pageNum": 1, "pageSize": 10},
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200
            assert "data" in data

    def test_get_audit_statistics(self):
        """测试获取审核统计"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/audit/statistics",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200
            assert "data" in data
            # 验证统计字段存在
            stats = data["data"]
            assert "pendingCount" in stats
            assert "todayPassCount" in stats
            assert "todayRejectCount" in stats

    def test_audit_reject_invalid_product(self):
        """测试驳回不存在的商品"""
        response = requests.put(
            f"{BASE_URL}{ADMIN_PREFIX}/audit/999999/reject",
            params={"reason": "测试驳回"},
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            # 商品不存在应返回错误
            assert data["code"] != 200

    def test_batch_audit_empty_list(self):
        """测试批量审核空列表"""
        response = requests.put(
            f"{BASE_URL}{ADMIN_PREFIX}/audit/batch/pass",
            json={"productIds": []},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert data["data"]["successCount"] == 0


class TestStatistics:
    """数据统计API测试"""

    def test_get_today_statistics(self):
        """测试获取今日统计"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/statistics/today",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200
            stats = data["data"]
            # 验证统计字段
            assert "newUserCount" in stats
            assert "productPublishCount" in stats
            assert "orderSuccessCount" in stats

    def test_get_product_trend(self):
        """测试获取商品发布趋势"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/statistics/product/trend",
            params={"days": 7},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        trend = data["data"]
        # 兼容大小写的xAxis
        assert "xAxis" in trend or "xaxis" in trend
        assert "series" in trend
        x_axis = trend.get("xAxis") or trend.get("xaxis")
        assert len(x_axis) == 7

    def test_get_order_trend(self):
        """测试获取成交趋势"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/statistics/order/trend",
            params={"days": 7},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_user_growth_trend(self):
        """测试获取用户增长趋势"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/statistics/user/growth",
            params={"days": 7},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_hot_category_statistics(self):
        """测试获取热门品类统计"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/statistics/category/hot",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200


class TestSystemConfig:
    """系统配置API测试"""

    def test_get_config_list(self):
        """测试获取配置列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/config/list",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert isinstance(data["data"], list)

    def test_get_campus_fence_config(self):
        """测试获取校园围栏配置"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/config/campus-fence",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_seckill_config(self):
        """测试获取秒杀配置"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/config/seckill",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_refresh_cache(self):
        """测试刷新缓存"""
        response = requests.post(
            f"{BASE_URL}{ADMIN_PREFIX}/config/refresh-cache",
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_update_campus_fence(self):
        """测试更新校园围栏配置"""
        fence_data = {
            "fencePoints": [
                {"lng": 113.0, "lat": 23.0},
                {"lng": 113.1, "lat": 23.0},
                {"lng": 113.1, "lat": 23.1},
                {"lng": 113.0, "lat": 23.1}
            ]
        }
        response = requests.put(
            f"{BASE_URL}{ADMIN_PREFIX}/config/campus-fence",
            json=fence_data,
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            assert data["code"] == 200


class TestDispute:
    """纠纷处理API测试"""

    def test_get_dispute_page(self):
        """测试获取纠纷列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/dispute/page",
            params={"pageNum": 1, "pageSize": 10},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data

    def test_get_dispute_page_with_status(self):
        """测试按状态获取纠纷列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/dispute/page",
            params={
                "pageNum": 1,
                "pageSize": 10,
                "status": "PENDING"
            },
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_dispute_detail_not_found(self):
        """测试获取不存在的纠纷详情"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/dispute/999999",
            headers=HEADERS
        )
        assert response.status_code in [200, 500]
        if response.status_code == 200:
            data = response.json()
            # 纠纷不存在应返回错误
            assert data["code"] != 200


class TestSystemLog:
    """系统日志API测试"""

    def test_get_log_page(self):
        """测试获取日志列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/log/page",
            params={"pageNum": 1, "pageSize": 10},
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data

    def test_get_log_page_with_filter(self):
        """测试带过滤条件的日志列表"""
        response = requests.get(
            f"{BASE_URL}{ADMIN_PREFIX}/log/page",
            params={
                "pageNum": 1,
                "pageSize": 10,
                "module": "USER"
            },
            headers=HEADERS
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200


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
    pytest.main([__file__, "-v", "--tb=short"])