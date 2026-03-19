"""
商品API测试
"""
import pytest
from conftest import assert_success, assert_fail, get_response_data


class TestProductPublish:
    """商品发布测试"""
    
    def test_publish_product_success(self, authed_product_api, test_product_data):
        """测试发布商品成功"""
        response = authed_product_api.publish_product(test_product_data)
        
        assert_success(response)
        product_id = get_response_data(response)
        assert product_id is not None
        
        # 清理
        if product_id:
            authed_product_api.delete_product(product_id)
    
    def test_publish_product_without_auth(self, product_api, test_product_data):
        """测试未登录发布商品"""
        response = product_api.publish_product(test_product_data)
        assert_fail(response, expected_code=401)
    
    def test_publish_product_empty_name(self, authed_product_api):
        """测试商品名称为空"""
        data = {"productName": "", "categoryId": 1, "price": 99.9}
        response = authed_product_api.publish_product(data)
        assert_fail(response)
    
    def test_publish_product_invalid_price(self, authed_product_api):
        """测试价格无效"""
        data = {
            "productName": "测试商品",
            "categoryId": 1,
            "price": -1,
            "newDegree": "NEW",
            "imageUrls": ["http://example.com/1.jpg"]
        }
        response = authed_product_api.publish_product(data)
        assert_fail(response)
    
    def test_publish_product_no_images(self, authed_product_api):
        """测试没有图片"""
        data = {
            "productName": "测试商品",
            "categoryId": 1,
            "price": 99.9,
            "newDegree": "NEW",
            "imageUrls": []
        }
        response = authed_product_api.publish_product(data)
        assert_fail(response)


class TestProductQuery:
    """商品查询测试"""
    
    def test_get_product_success(self, product_api, created_product):
        """测试获取商品详情"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        response = product_api.get_product(product_id)
        assert_success(response)
        
        data = get_response_data(response)
        assert data["id"] == product_id
    
    def test_get_product_not_found(self, product_api):
        """测试商品不存在"""
        response = product_api.get_product(999999)
        assert_fail(response)
    
    def test_get_product_page_default(self, product_api):
        """测试分页查询商品（默认参数）"""
        response = product_api.get_product_page({
            "pageNum": 1,
            "pageSize": 10
        })
        assert_success(response)
        
        data = get_response_data(response)
        assert "list" in data
        assert "total" in data
    
    def test_get_product_page_with_category(self, product_api):
        """测试按品类查询商品"""
        response = product_api.get_product_page({
            "pageNum": 1,
            "pageSize": 10,
            "categoryId": 1
        })
        assert_success(response)
    
    def test_get_product_page_with_price_range(self, product_api):
        """测试按价格范围查询"""
        response = product_api.get_product_page({
            "pageNum": 1,
            "pageSize": 10,
            "minPrice": 10,
            "maxPrice": 100
        })
        assert_success(response)
    
    def test_get_product_page_with_sort(self, product_api):
        """测试排序查询"""
        response = product_api.get_product_page({
            "pageNum": 1,
            "pageSize": 10,
            "sortBy": "price",
            "sortOrder": "asc"
        })
        assert_success(response)
    
    def test_search_products(self, product_api):
        """测试搜索商品"""
        response = product_api.search_products("测试", page_num=1, page_size=10)
        assert_success(response)
    
    def test_search_products_empty_keyword(self, product_api):
        """测试空关键词搜索"""
        response = product_api.search_products("", page_num=1, page_size=10)
        # 空关键词应该返回结果或提示错误
        assert response["status_code"] == 200


class TestProductStatus:
    """商品状态管理测试"""
    
    def test_off_shelf_success(self, authed_product_api, created_product):
        """测试下架商品"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        response = authed_product_api.off_shelf(product_id)
        assert_success(response)
    
    def test_on_shelf_success(self, authed_product_api, created_product):
        """测试上架商品"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        # 先下架
        authed_product_api.off_shelf(product_id)
        
        # 再上架
        response = authed_product_api.on_shelf(product_id)
        assert_success(response)
    
    def test_on_shelf_already_on_shelf(self, authed_product_api, created_product):
        """测试上架已上架商品"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        # 已上架状态再上架应该失败
        response = authed_product_api.on_shelf(product_id)
        assert_fail(response)


class TestProductUpdate:
    """商品更新测试"""
    
    def test_update_product_success(self, authed_product_api, created_product):
        """测试更新商品成功"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        # 先下架才能编辑
        authed_product_api.off_shelf(product_id)
        
        update_data = {
            "productName": "更新后的商品名称",
            "categoryId": 1,
            "price": 88.88,
            "originalPrice": 188.88,
            "newDegree": "90_NEW",
            "productDesc": "更新后的描述",
            "imageUrls": ["http://example.com/new.jpg"]
        }
        
        response = authed_product_api.update_product(product_id, update_data)
        assert_success(response)
    
    def test_update_product_not_owner(self, authed_product_api, product_api):
        """测试更新他人商品"""
        # 使用不同用户更新
        response = authed_product_api.update_product(1, {"productName": "test"})
        assert_fail(response)


class TestProductDelete:
    """商品删除测试"""
    
    def test_delete_product_success(self, authed_product_api, test_product_data):
        """测试删除商品成功"""
        # 先创建商品
        create_response = authed_product_api.publish_product(test_product_data)
        if create_response["data"].get("code") != 200:
            pytest.skip("商品创建失败")
        
        product_id = get_response_data(create_response)
        
        # 删除商品
        response = authed_product_api.delete_product(product_id)
        assert_success(response)
    
    def test_delete_product_not_found(self, authed_product_api):
        """测试删除不存在的商品"""
        response = authed_product_api.delete_product(999999)
        assert_fail(response)


class TestProductDraft:
    """商品草稿测试"""
    
    def test_save_draft_success(self, authed_product_api):
        """测试保存草稿成功"""
        draft_data = {
            "productName": "草稿商品",
            "categoryId": 1,
            "price": 50.0
        }
        
        response = authed_product_api.save_draft(draft_data)
        assert_success(response)
        
        product_id = get_response_data(response)
        if product_id:
            authed_product_api.delete_product(product_id)
    
    def test_get_draft_list(self, authed_product_api):
        """测试获取草稿列表"""
        response = authed_product_api.get_draft_list(page_num=1, page_size=10)
        assert_success(response)


class TestProductRecommend:
    """商品推荐测试"""
    
    def test_get_recommend_products(self, product_api):
        """测试获取推荐商品"""
        response = product_api.get_recommend(page_num=1, page_size=10)
        assert_success(response)


class TestProductNearby:
    """附近商品测试"""
    
    def test_get_nearby_products(self, product_api):
        """测试获取附近商品"""
        response = product_api.get_nearby(
            longitude="116.397128",
            latitude="39.916527",
            distance=1000,
            page_num=1,
            page_size=10
        )
        assert_success(response)


class TestProductInner:
    """内部接口测试"""
    
    def test_get_product_inner(self, product_api):
        """测试内部接口获取商品"""
        response = product_api.get_product_inner(1)
        assert response["status_code"] == 200
    
    def test_get_products_batch(self, product_api):
        """测试批量获取商品"""
        response = product_api.get_products_batch([1, 2, 3])
        assert response["status_code"] == 200
