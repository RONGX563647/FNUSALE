"""
测试配置
"""
import os

# API基础URL - 通过网关访问
BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/api")

# 服务直接访问URL（调试用）
PRODUCT_SERVICE_URL = os.getenv("PRODUCT_SERVICE_URL", "http://localhost:8102")

# 测试用户配置
TEST_USER = {
    "phone": "13800138000",
    "password": "test123456"
}

# 管理员用户配置
ADMIN_USER = {
    "phone": "13900139000",
    "password": "admin123456"
}

# 请求超时时间（秒）
TIMEOUT = 30

# 测试数据
TEST_PRODUCT = {
    "productName": "测试商品_自动化测试",
    "categoryId": 1,
    "price": 99.99,
    "originalPrice": 199.99,
    "newDegree": "NEW",
    "productDesc": "这是一个自动化测试商品",
    "imageUrls": [
        "https://example.com/image1.jpg",
        "https://example.com/image2.jpg"
    ]
}

TEST_CATEGORY = {
    "categoryName": "测试分类_自动化测试",
    "parentCategoryId": 0,
    "enableStatus": 1
}
