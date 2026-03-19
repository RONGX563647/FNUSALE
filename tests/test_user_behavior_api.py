"""
用户行为API测试
"""
import pytest
from conftest import assert_success, assert_fail, get_response_data


class TestFavorite:
    """收藏功能测试"""
    
    def test_add_favorite_success(self, authed_product_api, created_product):
        """测试收藏商品成功"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        response = authed_product_api.add_favorite(product_id)
        assert_success(response)
        
        # 清理：取消收藏
        authed_product_api.remove_favorite(product_id)
    
    def test_add_favorite_without_auth(self, product_api):
        """测试未登录收藏"""
        response = product_api.add_favorite(1)
        assert_fail(response)
    
    def test_add_favorite_product_not_found(self, authed_product_api):
        """测试收藏不存在的商品"""
        response = authed_product_api.add_favorite(999999)
        assert_fail(response)
    
    def test_add_favorite_duplicate(self, authed_product_api, created_product):
        """测试重复收藏"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        # 第一次收藏
        authed_product_api.add_favorite(product_id)
        
        # 再次收藏应该失败
        response = authed_product_api.add_favorite(product_id)
        assert_fail(response)
    
    def test_remove_favorite_success(self, authed_product_api, created_product):
        """测试取消收藏成功"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        # 先收藏
        authed_product_api.add_favorite(product_id)
        
        # 再取消
        response = authed_product_api.remove_favorite(product_id)
        assert_success(response)
    
    def test_remove_favorite_not_exists(self, authed_product_api):
        """测试取消不存在的收藏"""
        response = authed_product_api.remove_favorite(999999)
        assert_fail(response)


class TestLike:
    """点赞功能测试"""
    
    def test_add_like_success(self, authed_product_api, created_product):
        """测试点赞成功"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        response = authed_product_api.add_like(product_id)
        assert_success(response)
        
        # 清理：取消点赞
        authed_product_api.remove_like(product_id)
    
    def test_add_like_without_auth(self, product_api):
        """测试未登录点赞"""
        response = product_api.add_like(1)
        assert_fail(response)
    
    def test_add_like_product_not_found(self, authed_product_api):
        """测试点赞不存在的商品"""
        response = authed_product_api.add_like(999999)
        assert_fail(response)
    
    def test_add_like_duplicate(self, authed_product_api, created_product):
        """测试重复点赞"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        # 第一次点赞
        authed_product_api.add_like(product_id)
        
        # 再次点赞应该失败
        response = authed_product_api.add_like(product_id)
        assert_fail(response)
    
    def test_remove_like_success(self, authed_product_api, created_product):
        """测试取消点赞成功"""
        product_id, _ = created_product
        if not product_id:
            pytest.skip("商品创建失败")
        
        # 先点赞
        authed_product_api.add_like(product_id)
        
        # 再取消
        response = authed_product_api.remove_like(product_id)
        assert_success(response)
    
    def test_remove_like_not_exists(self, authed_product_api):
        """测试取消不存在的点赞"""
        response = authed_product_api.remove_like(999999)
        assert_fail(response)
