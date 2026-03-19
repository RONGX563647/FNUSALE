"""
Pytest配置和fixtures
"""
import pytest
from faker import Faker
from api_client import ProductAPI, CategoryAPI, UserAPI
from config import BASE_URL, TEST_USER, ADMIN_USER, TEST_PRODUCT, TEST_CATEGORY

fake = Faker('zh_CN')


# ==================== API客户端Fixtures ====================

@pytest.fixture(scope="session")
def product_api():
    """商品API客户端"""
    return ProductAPI(BASE_URL)


@pytest.fixture(scope="session")
def category_api():
    """品类API客户端"""
    return CategoryAPI(BASE_URL)


@pytest.fixture(scope="session")
def user_api():
    """用户API客户端"""
    return UserAPI(BASE_URL)


# ==================== 认证Fixtures ====================

@pytest.fixture(scope="session")
def user_token(user_api):
    """普通用户Token"""
    response = user_api.login_by_phone(TEST_USER["phone"], TEST_USER["password"])
    if response["status_code"] == 200 and response["data"].get("code") == 200:
        return response["data"]["data"].get("accessToken")
    return None


@pytest.fixture(scope="session")
def admin_token(user_api):
    """管理员Token"""
    response = user_api.login_by_phone(ADMIN_USER["phone"], ADMIN_USER["password"])
    if response["status_code"] == 200 and response["data"].get("code") == 200:
        return response["data"]["data"].get("accessToken")
    return None


@pytest.fixture
def authed_product_api(product_api, user_token):
    """已认证的商品API客户端"""
    if user_token:
        product_api.set_token(user_token)
    yield product_api
    product_api.clear_token()


@pytest.fixture
def authed_category_api(category_api, admin_token):
    """已认证的品类API客户端（需要管理员权限）"""
    if admin_token:
        category_api.set_token(admin_token)
    yield category_api
    category_api.clear_token()


# ==================== 测试数据Fixtures ====================

@pytest.fixture
def test_product_data():
    """测试商品数据"""
    return {
        "productName": f"测试商品_{fake.word()}_{fake.random_number(digits=6)}",
        "categoryId": 1,
        "price": round(fake.pyfloat(min_value=1, max_value=1000, right_digits=2), 2),
        "originalPrice": round(fake.pyfloat(min_value=100, max_value=2000, right_digits=2), 2),
        "newDegree": fake.random_element(["NEW", "90_NEW", "80_NEW", "70_NEW"]),
        "productDesc": fake.text(max_nb_chars=200),
        "imageUrls": [fake.image_url() for _ in range(3)]
    }


@pytest.fixture
def test_category_data():
    """测试品类数据"""
    return {
        "categoryName": f"测试分类_{fake.word()}_{fake.random_number(digits=6)}",
        "parentCategoryId": 0,
        "enableStatus": 1
    }


@pytest.fixture
def created_product(authed_product_api, test_product_data):
    """创建测试商品（自动清理）"""
    response = authed_product_api.publish_product(test_product_data)
    product_id = None
    if response["status_code"] == 200 and response["data"].get("code") == 200:
        product_id = response["data"]["data"]
    
    yield product_id, test_product_data
    
    # 清理：删除创建的商品
    if product_id:
        authed_product_api.delete_product(product_id)


@pytest.fixture
def created_category(authed_category_api, test_category_data):
    """创建测试品类（自动清理）"""
    response = authed_category_api.add_category(test_category_data)
    category_id = None
    if response["status_code"] == 200 and response["data"].get("code") == 200:
        category_id = response["data"]["data"]
    
    yield category_id, test_category_data
    
    # 清理：删除创建的品类
    if category_id:
        authed_category_api.delete_category(category_id)


# ==================== 工具函数 ====================

def assert_success(response):
    """断言响应成功"""
    assert response["status_code"] == 200, f"HTTP状态码错误: {response['status_code']}"
    assert response["data"].get("code") == 200, f"业务码错误: {response['data']}"


def assert_fail(response, expected_code=None):
    """断言响应失败"""
    if expected_code:
        assert response["data"].get("code") == expected_code
    else:
        assert response["data"].get("code") != 200


def get_response_data(response):
    """获取响应数据"""
    return response["data"].get("data")
