"""
交易模块API测试
测试前请确保:
1. 基础设施服务已启动 (MySQL, Redis, Nacos等)
2. fnusale-gateway服务已启动 (端口8080)
3. fnusale-trade服务已启动 (端口8106)
4. fnusale-user服务已启动 (用户认证)
5. fnusale-product服务已启动 (商品数据)

运行命令: pytest tests/test_trade_api.py -v
生成报告: pytest tests/test_trade_api.py -v --html=trade_report.html
"""

import pytest
import requests
import time
import random
from typing import Optional

# 通过网关访问
BASE_URL = "http://localhost:8080/api"
TRADE_PREFIX = "/trade"

# 测试用户Token
TEST_TOKEN: Optional[str] = None

# 服务是否可用
SERVICE_AVAILABLE = False


def get_headers(token: Optional[str] = None) -> dict:
    """获取请求头"""
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return headers


def skip_if_service_unavailable():
    """如果服务不可用则跳过测试"""
    if not SERVICE_AVAILABLE:
        pytest.skip("交易服务不可用，跳过测试")


class TestServiceHealth:
    """服务健康检查"""

    def test_gateway_health(self):
        """测试网关是否启动"""
        global SERVICE_AVAILABLE
        try:
            response = requests.get(
                f"{BASE_URL}{TRADE_PREFIX}/payment/methods",
                timeout=5
            )
            if response.status_code == 503:
                pytest.skip("交易服务未启动（返回503），跳过测试")
            SERVICE_AVAILABLE = True
            assert response.status_code in [200, 401, 403]
        except requests.exceptions.ConnectionError:
            pytest.skip("网关服务未启动，跳过测试")

    def test_trade_service_health(self):
        """测试交易服务是否启动"""
        try:
            response = requests.get("http://localhost:8106/actuator/health", timeout=5)
            assert response.status_code == 200
        except requests.exceptions.ConnectionError:
            pytest.skip("交易服务未启动，跳过测试")


class TestUserLogin:
    """用户登录获取Token"""

    def test_login(self):
        """测试用户登录"""
        global TEST_TOKEN

        skip_if_service_unavailable()

        # 尝试登录获取Token
        login_data = {
            "username": "testuser",
            "password": "123456"
        }

        try:
            response = requests.post(
                f"{BASE_URL}/user/login",
                json=login_data,
                headers=get_headers(),
                timeout=10
            )

            if response.status_code == 200:
                data = response.json()
                if data.get("code") == 200 and data.get("data"):
                    TEST_TOKEN = data["data"].get("accessToken")
                    print(f"\n登录成功，获取Token")
        except Exception as e:
            print(f"\n登录失败: {e}")

        # 如果登录失败，使用模拟Token
        if not TEST_TOKEN:
            TEST_TOKEN = "test_mock_token_for_api_test"
            print(f"\n使用模拟Token进行测试")


class TestPaymentAPI:
    """支付相关API测试"""

    def test_get_payment_methods(self):
        """测试获取支付方式列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/payment/methods",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert isinstance(data["data"], list)

        # 验证支付方式字段
        for method in data["data"]:
            assert "payType" in method
            assert "payName" in method
            assert "icon" in method

    def test_create_payment_unauthorized(self):
        """测试未登录创建支付"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/payment/create",
            json={"orderId": 1, "payType": "WECHAT"},
            headers=get_headers(),  # 不带Token
            timeout=10
        )

        # 应该返回401未授权
        assert response.status_code in [401, 403]

    def test_create_payment_invalid_order(self):
        """测试创建支付-无效订单"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/payment/create",
            json={"orderId": 999999, "payType": "WECHAT"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        # 订单不存在应返回错误
        assert data["code"] != 200

    def test_create_payment_invalid_pay_type(self):
        """测试创建支付-无效支付方式"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/payment/create",
            json={"orderId": 1, "payType": "INVALID"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        # 支付方式无效应返回错误
        assert data["code"] != 200

    def test_query_payment_status_unauthorized(self):
        """测试未登录查询支付状态"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/payment/status/1",
            headers=get_headers(),
            timeout=10
        )

        assert response.status_code in [401, 403]


class TestOrderAPI:
    """订单相关API测试"""

    def test_get_my_orders_unauthorized(self):
        """测试未登录获取我的订单"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/my",
            headers=get_headers(),
            timeout=10
        )

        assert response.status_code in [401, 403]

    def test_get_my_orders(self):
        """测试获取我的订单列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/my",
            params={"pageNum": 1, "pageSize": 10},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "data" in data

    def test_get_my_orders_with_status(self):
        """测试按状态获取订单列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/my",
            params={"pageNum": 1, "pageSize": 10, "status": "UNPAID"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_seller_orders(self):
        """测试获取卖家订单列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/seller",
            params={"pageNum": 1, "pageSize": 10},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_order_statistics(self):
        """测试获取订单统计"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/statistics",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

        stats = data["data"]
        assert "unpaidCount" in stats
        assert "waitPickCount" in stats
        assert "successCount" in stats
        assert "cancelCount" in stats
        assert "totalCount" in stats

    def test_get_order_by_id_not_found(self):
        """测试获取不存在的订单"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/999999",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        # 订单不存在应返回错误
        assert data["code"] != 200

    def test_get_order_by_no_not_found(self):
        """测试根据订单号查询不存在的订单"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/no/INVALID_ORDER_NO",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_cancel_order_invalid(self):
        """测试取消无效订单"""
        skip_if_service_unavailable()
        response = requests.put(
            f"{BASE_URL}{TRADE_PREFIX}/order/999999/cancel",
            params={"reason": "测试取消"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_confirm_receipt_invalid(self):
        """测试确认收货无效订单"""
        skip_if_service_unavailable()
        response = requests.put(
            f"{BASE_URL}{TRADE_PREFIX}/order/999999/confirm",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_extend_receive_time_invalid(self):
        """测试延长收货时间无效订单"""
        skip_if_service_unavailable()
        response = requests.put(
            f"{BASE_URL}{TRADE_PREFIX}/order/999999/extend",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_mark_ready_invalid(self):
        """测试标记备货无效订单"""
        skip_if_service_unavailable()
        response = requests.put(
            f"{BASE_URL}{TRADE_PREFIX}/order/999999/ready",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_apply_refund_invalid(self):
        """测试申请退款无效订单"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/order/999999/refund",
            params={"reason": "测试退款"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200


class TestOrderEvaluationAPI:
    """订单评价API测试"""

    def test_get_my_evaluations(self):
        """测试获取我的评价列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation/my",
            params={"pageNum": 1, "pageSize": 10},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_received_evaluations(self):
        """测试获取收到的评价列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation/received",
            params={"pageNum": 1, "pageSize": 10},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_evaluation_by_order_not_found(self):
        """测试获取不存在订单的评价"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation/order/999999",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert data["data"] is None

    def test_get_evaluation_by_product(self):
        """测试获取商品评价列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation/product/1",
            params={"pageNum": 1, "pageSize": 10},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_submit_evaluation_unauthorized(self):
        """测试未登录提交评价"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation",
            json={
                "orderId": 1,
                "score": 5,
                "evaluationContent": "好评"
            },
            headers=get_headers(),
            timeout=10
        )

        assert response.status_code in [401, 403]

    def test_submit_evaluation_invalid_order(self):
        """测试提交评价-无效订单"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation",
            json={
                "orderId": 999999,
                "score": 5,
                "evaluationContent": "好评"
            },
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_reply_evaluation_invalid(self):
        """测试回复无效评价"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation/999999/reply",
            params={"content": "感谢评价"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200


class TestTradeDisputeAPI:
    """交易纠纷API测试"""

    def test_get_my_disputes(self):
        """测试获取我的纠纷列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/dispute/my",
            params={"pageNum": 1, "pageSize": 10},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_my_disputes_with_status(self):
        """测试按状态获取纠纷列表"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/dispute/my",
            params={"pageNum": 1, "pageSize": 10, "status": "PENDING"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200

    def test_get_dispute_not_found(self):
        """测试获取不存在的纠纷"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/dispute/999999",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_create_dispute_unauthorized(self):
        """测试未登录创建纠纷"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/dispute",
            json={
                "orderId": 1,
                "disputeType": "PRODUCT_NOT_MATCH",
                "evidenceUrl": "http://example.com/evidence.jpg"
            },
            headers=get_headers(),
            timeout=10
        )

        assert response.status_code in [401, 403]

    def test_create_dispute_invalid_order(self):
        """测试创建纠纷-无效订单"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/dispute",
            json={
                "orderId": 999999,
                "disputeType": "PRODUCT_NOT_MATCH"
            },
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_cancel_dispute_invalid(self):
        """测试撤销无效纠纷"""
        skip_if_service_unavailable()
        response = requests.delete(
            f"{BASE_URL}{TRADE_PREFIX}/dispute/999999",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_add_evidence_invalid(self):
        """测试补充证据无效纠纷"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/dispute/999999/evidence",
            params={"evidenceUrl": "http://example.com/new_evidence.jpg"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_get_dispute_records_invalid(self):
        """测试获取无效纠纷的处理记录"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/dispute/999999/records",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200


class TestMockPaymentFlow:
    """模拟支付完整流程测试"""

    def test_mock_pay_info_invalid_token(self):
        """测试获取模拟支付信息-无效Token"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/payment/mock/info/invalid_token_12345",
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200

    def test_mock_pay_confirm_invalid_token(self):
        """测试模拟支付确认-无效Token"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/payment/mock/confirm",
            params={"payToken": "invalid_token_12345", "success": True},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] != 200


class TestPaymentRefund:
    """支付退款API测试"""

    def test_apply_refund_unauthorized(self):
        """测试未登录申请退款"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/payment/refund",
            params={"orderId": 1, "reason": "不想要了"},
            headers=get_headers(),
            timeout=10
        )

        assert response.status_code in [401, 403]

    def test_query_refund_status_unauthorized(self):
        """测试未登录查询退款状态"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/payment/refund/status/1",
            headers=get_headers(),
            timeout=10
        )

        assert response.status_code in [401, 403]


class TestDataValidation:
    """数据校验测试"""

    def test_create_order_missing_product_id(self):
        """测试创建订单-缺少商品ID"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/order",
            json={"payType": "WECHAT"},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code in [400, 500]

    def test_submit_evaluation_missing_score(self):
        """测试提交评价-缺少评分"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation",
            json={"orderId": 1},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code in [400, 500]

    def test_create_dispute_missing_type(self):
        """测试创建纠纷-缺少类型"""
        skip_if_service_unavailable()
        response = requests.post(
            f"{BASE_URL}{TRADE_PREFIX}/dispute",
            json={"orderId": 1},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code in [400, 500]


class TestPagination:
    """分页测试"""

    def test_order_pagination(self):
        """测试订单分页"""
        skip_if_service_unavailable()
        response1 = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/my",
            params={"pageNum": 1, "pageSize": 5},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response1.status_code == 200
        data1 = response1.json()

        response2 = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/order/my",
            params={"pageNum": 2, "pageSize": 5},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response2.status_code == 200
        data2 = response2.json()

        if data1["data"]["records"] and data2["data"]["records"]:
            assert data1["data"]["pageNum"] == 1
            assert data2["data"]["pageNum"] == 2

    def test_evaluation_pagination(self):
        """测试评价分页"""
        skip_if_service_unavailable()
        response = requests.get(
            f"{BASE_URL}{TRADE_PREFIX}/evaluation/my",
            params={"pageNum": 1, "pageSize": 20},
            headers=get_headers(TEST_TOKEN),
            timeout=10
        )

        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 200
        assert "records" in data["data"]
        assert "total" in data["data"]


@pytest.fixture(scope="session", autouse=True)
def setup_session():
    """测试会话初始化"""
    global TEST_TOKEN

    print("\n" + "=" * 50)
    print("交易模块API测试开始")
    print("=" * 50)

    # 检查服务状态
    try:
        response = requests.get("http://localhost:8080/api/trade/payment/methods", timeout=5)
        if response.status_code in [200, 401, 403]:
            print("✓ 网关服务正常")
    except:
        print("✗ 网关服务未启动，部分测试将被跳过")

    try:
        response = requests.get("http://localhost:8106/actuator/health", timeout=5)
        if response.status_code == 200:
            print("✓ 交易服务正常")
    except:
        print("✗ 交易服务未启动，部分测试将被跳过")

    # 尝试登录获取Token
    try:
        login_response = requests.post(
            f"{BASE_URL}/user/login",
            json={"username": "testuser", "password": "123456"},
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        if login_response.status_code == 200:
            login_data = login_response.json()
            if login_data.get("code") == 200 and login_data.get("data"):
                TEST_TOKEN = login_data["data"].get("accessToken")
                print(f"✓ 登录成功")
    except Exception as e:
        print(f"✗ 登录失败: {e}")
        TEST_TOKEN = "test_mock_token"

    yield

    print("\n" + "=" * 50)
    print("交易模块API测试完成")
    print("=" * 50)


if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short", "-s"])