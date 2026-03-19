"""
商品品类API测试
"""
import pytest
from conftest import assert_success, assert_fail, get_response_data


class TestCategoryQuery:
    """品类查询测试"""
    
    def test_get_category_tree(self, category_api):
        """测试获取品类树"""
        response = category_api.get_tree()
        assert_success(response)
        
        data = get_response_data(response)
        assert isinstance(data, list)
    
    def test_get_category_list(self, category_api):
        """测试获取一级品类列表"""
        response = category_api.get_list()
        assert_success(response)
        
        data = get_response_data(response)
        assert isinstance(data, list)
    
    def test_get_children_success(self, category_api):
        """测试获取子品类"""
        # 假设品类ID 1 存在
        response = category_api.get_children(1)
        assert response["status_code"] == 200
    
    def test_get_children_not_found(self, category_api):
        """测试获取不存在品类的子品类"""
        response = category_api.get_children(999999)
        assert_fail(response)
    
    def test_get_category_success(self, category_api):
        """测试获取品类详情"""
        response = category_api.get_category(1)
        assert response["status_code"] == 200
    
    def test_get_category_not_found(self, category_api):
        """测试获取不存在的品类"""
        response = category_api.get_category(999999)
        assert_fail(response)
    
    def test_get_hot_categories(self, category_api):
        """测试获取热门品类"""
        response = category_api.get_hot_categories()
        assert_success(response)


class TestCategoryCreate:
    """品类创建测试"""
    
    def test_create_category_success(self, authed_category_api, test_category_data):
        """测试创建品类成功"""
        response = authed_category_api.add_category(test_category_data)
        assert_success(response)
        
        category_id = get_response_data(response)
        if category_id:
            authed_category_api.delete_category(category_id)
    
    def test_create_category_without_auth(self, category_api, test_category_data):
        """测试未登录创建品类"""
        response = category_api.add_category(test_category_data)
        assert_fail(response)
    
    def test_create_category_empty_name(self, authed_category_api):
        """测试品类名称为空"""
        response = authed_category_api.add_category({"categoryName": ""})
        assert_fail(response)
    
    def test_create_category_duplicate_name(self, authed_category_api, created_category):
        """测试重复品类名称"""
        _, category_data = created_category
        if not category_data:
            pytest.skip("品类创建失败")
        
        # 尝试创建同名品类
        response = authed_category_api.add_category(category_data)
        assert_fail(response)


class TestCategoryUpdate:
    """品类更新测试"""
    
    def test_update_category_success(self, authed_category_api, created_category):
        """测试更新品类成功"""
        category_id, _ = created_category
        if not category_id:
            pytest.skip("品类创建失败")
        
        update_data = {
            "categoryName": f"更新后的品类_{pytest.__version__}"
        }
        
        response = authed_category_api.update_category(category_id, update_data)
        assert_success(response)
    
    def test_update_category_not_found(self, authed_category_api):
        """测试更新不存在的品类"""
        response = authed_category_api.update_category(999999, {"categoryName": "test"})
        assert_fail(response)


class TestCategoryDelete:
    """品类删除测试"""
    
    def test_delete_category_success(self, authed_category_api, test_category_data):
        """测试删除品类成功"""
        # 先创建品类
        create_response = authed_category_api.add_category(test_category_data)
        if create_response["data"].get("code") != 200:
            pytest.skip("品类创建失败")
        
        category_id = get_response_data(create_response)
        
        # 删除品类
        response = authed_category_api.delete_category(category_id)
        assert_success(response)
    
    def test_delete_category_not_found(self, authed_category_api):
        """测试删除不存在的品类"""
        response = authed_category_api.delete_category(999999)
        assert_fail(response)


class TestCategoryStatus:
    """品类状态管理测试"""
    
    def test_disable_category_success(self, authed_category_api, created_category):
        """测试禁用品类"""
        category_id, _ = created_category
        if not category_id:
            pytest.skip("品类创建失败")
        
        response = authed_category_api.update_status(category_id, 0)
        assert_success(response)
    
    def test_enable_category_success(self, authed_category_api, created_category):
        """测试启用品类"""
        category_id, _ = created_category
        if not category_id:
            pytest.skip("品类创建失败")
        
        # 先禁用
        authed_category_api.update_status(category_id, 0)
        
        # 再启用
        response = authed_category_api.update_status(category_id, 1)
        assert_success(response)
    
    def test_update_status_invalid(self, authed_category_api, created_category):
        """测试无效状态值"""
        category_id, _ = created_category
        if not category_id:
            pytest.skip("品类创建失败")
        
        response = authed_category_api.update_status(category_id, 2)
        assert_fail(response)
