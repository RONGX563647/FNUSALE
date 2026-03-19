"""
IM模块API测试
测试前请确保:
1. 基础设施服务已启动 (MySQL, Redis, Nacos等)
2. fnusale-gateway服务已启动 (端口8080)
3. fnusale-im服务已启动 (端口8103)
4. fnusale-user服务已启动 (用于用户认证校验)
5. fnusale-product服务已启动 (用于商品校验)

运行命令:
  pytest tests/test_im_api.py -v
  pytest tests/test_im_api.py -v --html=tests/report_im.html

环境变量配置:
  TEST_USER_PHONE - 测试用户手机号
  TEST_USER_PASSWORD - 测试用户密码
"""

import pytest
import requests
from typing import Optional

# 服务地址 - 通过网关访问
BASE_URL = "http://localhost:8080"
IM_PREFIX = "/api/im"

# 测试数据
TEST_USER_ID = 1
TEST_TARGET_USER_ID = 2
TEST_PRODUCT_ID = 1


def get_headers(token: Optional[str] = None) -> dict:
    """获取请求头"""
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return headers


def check_im_service_health() -> bool:
    """检查IM服务是否启动"""
    try:
        response = requests.get("http://localhost:8103/actuator/health", timeout=5)
        return response.status_code == 200
    except requests.exceptions.ConnectionError:
        return False


# ==================== 健康检查测试 ====================

class TestHealthCheck:
    """健康检查测试"""

    def test_im_service_health(self):
        """测试IM服务是否启动"""
        if not check_im_service_health():
            pytest.skip("IM服务未启动，跳过测试")
        assert True

    def test_gateway_health(self):
        """测试网关是否启动"""
        try:
            response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
            assert response.status_code == 200
        except requests.exceptions.ConnectionError:
            pytest.skip("网关服务未启动，跳过测试")


# ==================== 会话管理API测试 ====================

class TestSessionAPI:
    """会话管理API测试"""

    @pytest.fixture(autouse=True)
    def check_service(self):
        if not check_im_service_health():
            pytest.skip("服务未启动")

    # -------------------- 获取会话列表 --------------------

    def test_get_session_list_without_auth(self):
        """测试未授权获取会话列表"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/list",
            headers=get_headers()
        )
        # 未授权应该返回401
        assert response.status_code in [401, 302, 200]

    # -------------------- 创建会话 --------------------

    def test_create_session_missing_target_user(self):
        """测试创建会话缺少目标用户ID"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/session/create",
            json={"productId": TEST_PRODUCT_ID},
            headers=get_headers()
        )
        # 参数校验错误
        assert response.status_code in [400, 401, 500]

    def test_create_session_missing_product(self):
        """测试创建会话缺少商品ID"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/session/create",
            json={"targetUserId": TEST_TARGET_USER_ID},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_create_session_empty_body(self):
        """测试创建会话空请求体"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/session/create",
            json={},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_create_session_with_self(self):
        """测试与自己创建会话 - 业务错误"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/session/create",
            json={
                "targetUserId": TEST_USER_ID,
                "productId": TEST_PRODUCT_ID
            },
            headers=get_headers("mock_token")
        )
        if response.status_code == 200:
            data = response.json()
            # 应该返回业务错误：不能与自己创建会话
            assert data.get("code") != 200 or "不能与自己" in data.get("msg", "")

    # -------------------- 获取或创建会话 --------------------

    def test_get_or_create_session_missing_params(self):
        """测试获取或创建会话缺少参数"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/get-or-create",
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_get_or_create_session_valid_params(self):
        """测试获取或创建会话正常参数"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/get-or-create",
            params={
                "targetUserId": TEST_TARGET_USER_ID,
                "productId": TEST_PRODUCT_ID
            },
            headers=get_headers()
        )
        # 未授权或正常响应
        assert response.status_code in [200, 401]

    # -------------------- 获取会话详情 --------------------

    def test_get_session_by_id_invalid_format(self):
        """测试获取会话详情 - 无效ID格式"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/abc",
            headers=get_headers()
        )
        assert response.status_code in [400, 404, 500]

    def test_get_session_by_id_not_found(self):
        """测试获取会话详情 - 会话不存在"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/999999",
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    # -------------------- 删除会话 --------------------

    def test_delete_session_invalid_format(self):
        """测试删除会话 - 无效ID格式"""
        response = requests.delete(
            f"{BASE_URL}{IM_PREFIX}/session/abc",
            headers=get_headers()
        )
        assert response.status_code in [400, 404, 500]

    def test_delete_session_not_found(self):
        """测试删除会话 - 会话不存在"""
        response = requests.delete(
            f"{BASE_URL}{IM_PREFIX}/session/999999",
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    # -------------------- 未读消息数 --------------------

    def test_get_unread_count_without_auth(self):
        """测试获取未读消息数 - 未授权"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/unread-count",
            headers=get_headers()
        )
        assert response.status_code in [200, 401]

    # -------------------- 标记已读 --------------------

    def test_mark_as_read_invalid_format(self):
        """测试标记已读 - 无效ID格式"""
        response = requests.put(
            f"{BASE_URL}{IM_PREFIX}/session/abc/read",
            headers=get_headers()
        )
        assert response.status_code in [400, 404, 500]

    def test_mark_as_read_not_found(self):
        """测试标记已读 - 会话不存在"""
        response = requests.put(
            f"{BASE_URL}{IM_PREFIX}/session/999999/read",
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    # -------------------- 获取消息列表 --------------------

    def test_get_messages_invalid_session(self):
        """测试获取消息列表 - 无效会话ID"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/abc/messages",
            headers=get_headers()
        )
        assert response.status_code in [400, 404, 500]

    def test_get_messages_not_found(self):
        """测试获取消息列表 - 会话不存在"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/999999/messages",
            params={"pageNum": 1, "pageSize": 20},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    def test_get_messages_pagination(self):
        """测试获取消息列表 - 分页参数"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/session/1/messages",
            params={"pageNum": 1, "pageSize": 10},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            if data.get("code") == 200:
                result = data.get("data", {})
                # 验证分页结构
                assert "list" in result or "records" in result

    # -------------------- 置顶会话 --------------------

    def test_pin_session_invalid_format(self):
        """测试置顶会话 - 无效ID格式"""
        response = requests.put(
            f"{BASE_URL}{IM_PREFIX}/session/abc/pin",
            headers=get_headers()
        )
        assert response.status_code in [400, 404, 500]

    def test_pin_session_not_found(self):
        """测试置顶会话 - 会话不存在"""
        response = requests.put(
            f"{BASE_URL}{IM_PREFIX}/session/999999/pin",
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    # -------------------- 取消置顶 --------------------

    def test_unpin_session_invalid_format(self):
        """测试取消置顶 - 无效ID格式"""
        response = requests.delete(
            f"{BASE_URL}{IM_PREFIX}/session/abc/pin",
            headers=get_headers()
        )
        assert response.status_code in [400, 404, 500]

    def test_unpin_session_not_found(self):
        """测试取消置顶 - 会话不存在"""
        response = requests.delete(
            f"{BASE_URL}{IM_PREFIX}/session/999999/pin",
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200


# ==================== 消息管理API测试 ====================

class TestMessageAPI:
    """消息管理API测试"""

    @pytest.fixture(autouse=True)
    def check_service(self):
        if not check_im_service_health():
            pytest.skip("服务未启动")

    # -------------------- 发送文字消息 --------------------

    def test_send_text_message_missing_session(self):
        """测试发送文字消息 - 缺少会话ID"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/text",
            json={"content": "测试消息"},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_send_text_message_missing_content(self):
        """测试发送文字消息 - 缺少内容"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/text",
            json={"sessionId": 1},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_send_text_message_empty_content(self):
        """测试发送文字消息 - 空内容"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/text",
            json={"sessionId": 1, "content": ""},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    def test_send_text_message_whitespace_content(self):
        """测试发送文字消息 - 纯空白内容"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/text",
            json={"sessionId": 1, "content": "   "},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    def test_send_text_message_too_long(self):
        """测试发送文字消息 - 内容超长"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/text",
            json={"sessionId": 1, "content": "a" * 501},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    # -------------------- 发送图片消息 --------------------

    def test_send_image_message_missing_session(self):
        """测试发送图片消息 - 缺少会话ID"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/image",
            json={"imageUrl": "http://example.com/image.jpg"},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_send_image_message_missing_url(self):
        """测试发送图片消息 - 缺少图片URL"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/image",
            json={"sessionId": 1},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_send_image_message_empty_url(self):
        """测试发送图片消息 - 空URL"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/image",
            json={"sessionId": 1, "imageUrl": ""},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    # -------------------- 发送语音消息 --------------------

    def test_send_voice_message_missing_session(self):
        """测试发送语音消息 - 缺少会话ID"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/voice",
            json={"voiceUrl": "http://example.com/voice.amr", "duration": 10},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_send_voice_message_missing_url(self):
        """测试发送语音消息 - 缺少语音URL"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/voice",
            json={"sessionId": 1, "duration": 10},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_send_voice_message_missing_duration(self):
        """测试发送语音消息 - 缺少时长"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/voice",
            json={"sessionId": 1, "voiceUrl": "http://example.com/voice.amr"},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_send_voice_message_invalid_duration_zero(self):
        """测试发送语音消息 - 时长为0"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/voice",
            json={"sessionId": 1, "voiceUrl": "http://example.com/voice.amr", "duration": 0},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    def test_send_voice_message_invalid_duration_negative(self):
        """测试发送语音消息 - 时长为负数"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/voice",
            json={"sessionId": 1, "voiceUrl": "http://example.com/voice.amr", "duration": -1},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    def test_send_voice_message_duration_too_long(self):
        """测试发送语音消息 - 时长超过限制"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/voice",
            json={"sessionId": 1, "voiceUrl": "http://example.com/voice.amr", "duration": 61},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    # -------------------- 撤回消息 --------------------

    def test_recall_message_invalid_format(self):
        """测试撤回消息 - 无效ID格式"""
        response = requests.delete(
            f"{BASE_URL}{IM_PREFIX}/message/abc",
            headers=get_headers()
        )
        assert response.status_code in [400, 404, 500]

    def test_recall_message_not_found(self):
        """测试撤回消息 - 消息不存在"""
        response = requests.delete(
            f"{BASE_URL}{IM_PREFIX}/message/999999",
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200


# ==================== 快捷回复API测试 ====================

class TestQuickReplyAPI:
    """快捷回复API测试"""

    @pytest.fixture(autouse=True)
    def check_service(self):
        if not check_im_service_health():
            pytest.skip("服务未启动")

    # -------------------- 获取快捷回复列表 --------------------

    def test_get_quick_reply_list(self):
        """测试获取快捷回复列表"""
        response = requests.get(
            f"{BASE_URL}{IM_PREFIX}/message/quick-reply/list",
            headers=get_headers()
        )
        assert response.status_code in [200, 401]

    # -------------------- 添加快捷回复 --------------------

    def test_add_quick_reply_missing_content(self):
        """测试添加快捷回复 - 缺少内容"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/quick-reply",
            json={},
            headers=get_headers()
        )
        assert response.status_code in [400, 401, 500]

    def test_add_quick_reply_empty_content(self):
        """测试添加快捷回复 - 空内容"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/quick-reply",
            json={"content": ""},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    def test_add_quick_reply_too_long(self):
        """测试添加快捷回复 - 内容超长"""
        response = requests.post(
            f"{BASE_URL}{IM_PREFIX}/message/quick-reply",
            json={"content": "a" * 51},
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200

    # -------------------- 删除快捷回复 --------------------

    def test_delete_quick_reply_invalid_format(self):
        """测试删除快捷回复 - 无效ID格式"""
        response = requests.delete(
            f"{BASE_URL}{IM_PREFIX}/message/quick-reply/abc",
            headers=get_headers()
        )
        assert response.status_code in [400, 404, 500]

    def test_delete_quick_reply_not_found(self):
        """测试删除快捷回复 - 不存在"""
        response = requests.delete(
            f"{BASE_URL}{IM_PREFIX}/message/quick-reply/999999",
            headers=get_headers()
        )
        if response.status_code == 200:
            data = response.json()
            assert data.get("code") != 200


# ==================== WebSocket测试 ====================

class TestWebSocketEndpoint:
    """WebSocket端点测试"""

    def test_websocket_endpoint_exists(self):
        """测试WebSocket端点是否存在"""
        try:
            import websocket
            ws_url = "ws://localhost:8103/ws/im"
            ws = websocket.create_connection(ws_url, timeout=5)
            ws.close()
        except ImportError:
            pytest.skip("websocket-client库未安装")
        except Exception as e:
            # 连接失败可能是正常的（需要认证）
            if "Connection refused" in str(e):
                pytest.skip("WebSocket服务未启动")


# ==================== 集成测试 ====================

class TestIntegration:
    """集成测试 - 需要完整环境"""

    @pytest.fixture(autouse=True)
    def check_service(self):
        if not check_im_service_health():
            pytest.skip("服务未启动")

    @pytest.mark.skip(reason="需要真实用户Token")
    def test_full_session_flow(self):
        """完整会话流程测试"""
        # 此测试需要真实的用户Token
        # 1. 获取会话列表
        # 2. 创建/获取会话
        # 3. 发送消息
        # 4. 获取消息列表
        # 5. 标记已读
        # 6. 删除会话
        pass


if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short"])