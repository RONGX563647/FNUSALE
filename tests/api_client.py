"""
API客户端封装
"""
import requests
from typing import Optional, Dict, Any, List
from config import BASE_URL, TIMEOUT


class APIClient:
    """API客户端基类"""
    
    def __init__(self, base_url: str = BASE_URL):
        self.base_url = base_url
        self.token: Optional[str] = None
        self.session = requests.Session()
    
    def set_token(self, token: str):
        """设置认证Token"""
        self.token = token
        self.session.headers.update({"Authorization": f"Bearer {token}"})
    
    def clear_token(self):
        """清除认证Token"""
        self.token = None
        self.session.headers.pop("Authorization", None)
    
    def request(self, method: str, path: str, **kwargs) -> Dict[str, Any]:
        """发送请求"""
        url = f"{self.base_url}{path}"
        kwargs.setdefault("timeout", TIMEOUT)
        
        response = self.session.request(method, url, **kwargs)
        return self._handle_response(response)
    
    def _handle_response(self, response: requests.Response) -> Dict[str, Any]:
        """处理响应"""
        try:
            data = response.json()
        except ValueError:
            data = {"raw_text": response.text}
        
        return {
            "status_code": response.status_code,
            "data": data,
            "headers": dict(response.headers)
        }
    
    def get(self, path: str, params: Dict = None) -> Dict[str, Any]:
        """GET请求"""
        return self.request("GET", path, params=params)
    
    def post(self, path: str, json: Dict = None, data: Dict = None) -> Dict[str, Any]:
        """POST请求"""
        return self.request("POST", path, json=json, data=data)
    
    def put(self, path: str, json: Dict = None, params: Dict = None) -> Dict[str, Any]:
        """PUT请求"""
        return self.request("PUT", path, json=json, params=params)
    
    def delete(self, path: str) -> Dict[str, Any]:
        """DELETE请求"""
        return self.request("DELETE", path)


class ProductAPI(APIClient):
    """商品API客户端"""
    
    # ==================== 商品管理 ====================
    
    def publish_product(self, product_data: Dict) -> Dict[str, Any]:
        """发布商品"""
        return self.post("/product", json=product_data)
    
    def update_product(self, product_id: int, product_data: Dict) -> Dict[str, Any]:
        """更新商品"""
        return self.put(f"/product/{product_id}", json=product_data)
    
    def delete_product(self, product_id: int) -> Dict[str, Any]:
        """删除商品"""
        return self.delete(f"/product/{product_id}")
    
    def get_product(self, product_id: int) -> Dict[str, Any]:
        """获取商品详情"""
        return self.get(f"/product/{product_id}")
    
    def get_product_page(self, query: Dict = None) -> Dict[str, Any]:
        """分页查询商品"""
        return self.post("/product/page", json=query or {})
    
    def search_products(self, keyword: str, page_num: int = 1, page_size: int = 10) -> Dict[str, Any]:
        """搜索商品"""
        return self.get("/product/search", params={
            "keyword": keyword,
            "pageNum": page_num,
            "pageSize": page_size
        })
    
    def on_shelf(self, product_id: int) -> Dict[str, Any]:
        """上架商品"""
        return self.put(f"/product/{product_id}/on-shelf")
    
    def off_shelf(self, product_id: int) -> Dict[str, Any]:
        """下架商品"""
        return self.put(f"/product/{product_id}/off-shelf")
    
    def save_draft(self, draft_data: Dict) -> Dict[str, Any]:
        """保存草稿"""
        return self.post("/product/draft", json=draft_data)
    
    def get_draft_list(self, page_num: int = 1, page_size: int = 10) -> Dict[str, Any]:
        """获取草稿列表"""
        return self.get("/product/draft/list", params={
            "pageNum": page_num,
            "pageSize": page_size
        })
    
    def get_recommend(self, page_num: int = 1, page_size: int = 10) -> Dict[str, Any]:
        """获取推荐商品"""
        return self.get("/product/recommend", params={
            "pageNum": page_num,
            "pageSize": page_size
        })
    
    def get_nearby(self, longitude: str, latitude: str, distance: int = 1000,
                   page_num: int = 1, page_size: int = 10) -> Dict[str, Any]:
        """获取附近商品"""
        return self.get("/product/nearby", params={
            "longitude": longitude,
            "latitude": latitude,
            "distance": distance,
            "pageNum": page_num,
            "pageSize": page_size
        })
    
    def ai_recognize_category(self, image_url: str) -> Dict[str, Any]:
        """AI识别品类"""
        return self.post("/product/ai-category", params={"imageUrl": image_url})
    
    # ==================== 用户行为 ====================
    
    def add_favorite(self, product_id: int) -> Dict[str, Any]:
        """收藏商品"""
        return self.post(f"/product/{product_id}/favorite")
    
    def remove_favorite(self, product_id: int) -> Dict[str, Any]:
        """取消收藏"""
        return self.delete(f"/product/{product_id}/favorite")
    
    def add_like(self, product_id: int) -> Dict[str, Any]:
        """点赞商品"""
        return self.post(f"/product/{product_id}/like")
    
    def remove_like(self, product_id: int) -> Dict[str, Any]:
        """取消点赞"""
        return self.delete(f"/product/{product_id}/like")
    
    # ==================== 内部接口 ====================
    
    def get_product_inner(self, product_id: int) -> Dict[str, Any]:
        """内部接口：获取商品"""
        return self.get(f"/product/inner/{product_id}")
    
    def get_products_batch(self, product_ids: List[int]) -> Dict[str, Any]:
        """内部接口：批量获取商品"""
        return self.post("/product/inner/batch", json=product_ids)


class CategoryAPI(APIClient):
    """品类API客户端"""
    
    def get_tree(self) -> Dict[str, Any]:
        """获取品类树"""
        return self.get("/category/tree")
    
    def get_list(self) -> Dict[str, Any]:
        """获取一级品类列表"""
        return self.get("/category/list")
    
    def get_children(self, parent_id: int) -> Dict[str, Any]:
        """获取子品类"""
        return self.get(f"/category/children/{parent_id}")
    
    def get_category(self, category_id: int) -> Dict[str, Any]:
        """获取品类详情"""
        return self.get(f"/category/{category_id}")
    
    def add_category(self, category_data: Dict) -> Dict[str, Any]:
        """新增品类"""
        return self.post("/category", json=category_data)
    
    def update_category(self, category_id: int, category_data: Dict) -> Dict[str, Any]:
        """更新品类"""
        return self.put(f"/category/{category_id}", json=category_data)
    
    def delete_category(self, category_id: int) -> Dict[str, Any]:
        """删除品类"""
        return self.delete(f"/category/{category_id}")
    
    def update_status(self, category_id: int, status: int) -> Dict[str, Any]:
        """启用/禁用品类"""
        return self.put(f"/category/{category_id}/status", params={"status": status})
    
    def get_hot_categories(self) -> Dict[str, Any]:
        """获取热门品类"""
        return self.get("/category/hot")


class UserAPI(APIClient):
    """用户API客户端（用于登录获取Token）"""
    
    def login_by_phone(self, phone: str, password: str) -> Dict[str, Any]:
        """手机号登录"""
        return self.post("/user/login", json={
            "loginType": "PHONE",
            "phone": phone,
            "password": password
        })
    
    def login_by_email(self, email: str, password: str) -> Dict[str, Any]:
        """邮箱登录"""
        return self.post("/user/login", json={
            "loginType": "EMAIL",
            "email": email,
            "password": password
        })
    
    def get_current_user(self) -> Dict[str, Any]:
        """获取当前用户信息"""
        return self.get("/user/current")
