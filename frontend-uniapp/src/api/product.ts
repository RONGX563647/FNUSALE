import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'
import type { ProductPublishDTO, ProductQueryDTO, ProductVO, ProductCategoryVO, ProductCategoryDTO } from '@/types/product'

const BASE_URL = '/product'

export const productApi = {
  // 发布商品
  publish(data: ProductPublishDTO): Promise<Result<number>> {
    return http.post(BASE_URL, data)
  },

  // 更新商品
  update(id: number, data: ProductPublishDTO): Promise<Result<void>> {
    return http.put(`${BASE_URL}/${id}`, data)
  },

  // 删除商品
  delete(id: number): Promise<Result<void>> {
    return http.delete(`${BASE_URL}/${id}`)
  },

  // 获取商品详情
  getById(id: number): Promise<Result<ProductVO>> {
    return http.get(`${BASE_URL}/${id}`)
  },

  // 分页查询商品
  getPage(data: ProductQueryDTO): Promise<Result<PageResult<ProductVO>>> {
    return http.post(`${BASE_URL}/page`, data)
  },

  // 搜索商品
  search(keyword: string, params: PageParams): Promise<Result<PageResult<ProductVO>>> {
    return http.get(`${BASE_URL}/search`, { keyword, ...params })
  },

  // 上架商品
  onShelf(id: number): Promise<Result<void>> {
    return http.put(`${BASE_URL}/${id}/on-shelf`)
  },

  // 下架商品
  offShelf(id: number): Promise<Result<void>> {
    return http.put(`${BASE_URL}/${id}/off-shelf`)
  },

  // AI识别品类
  recognizeCategory(imageUrl: string): Promise<Result<unknown>> {
    return http.post(`${BASE_URL}/ai-category`, { imageUrl })
  },

  // 获取推荐商品
  getRecommend(params: PageParams): Promise<Result<PageResult<ProductVO>>> {
    return http.get(`${BASE_URL}/recommend`, params)
  },

  // 收藏商品
  addFavorite(id: number): Promise<Result<void>> {
    return http.post(`${BASE_URL}/${id}/favorite`)
  },

  // 取消收藏
  removeFavorite(id: number): Promise<Result<void>> {
    return http.delete(`${BASE_URL}/${id}/favorite`)
  },

  // 点赞商品
  addLike(id: number): Promise<Result<void>> {
    return http.post(`${BASE_URL}/${id}/like`)
  },

  // 取消点赞
  removeLike(id: number): Promise<Result<void>> {
    return http.delete(`${BASE_URL}/${id}/like`)
  },

  // 获取附近商品
  getNearby(
    longitude: string,
    latitude: string,
    distance = 1000,
    params: PageParams
  ): Promise<Result<PageResult<ProductVO>>> {
    return http.get(`${BASE_URL}/nearby`, { longitude, latitude, distance, ...params })
  },

  // 保存草稿
  saveDraft(data: ProductPublishDTO): Promise<Result<number>> {
    return http.post(`${BASE_URL}/draft`, data)
  },

  // 获取草稿列表
  getDraftList(params: PageParams): Promise<Result<PageResult<ProductVO>>> {
    return http.get(`${BASE_URL}/draft/list`, params)
  },

  // 根据ID获取商品信息（内部接口）
  getInnerById(productId: number): Promise<Result<ProductVO>> {
    return http.get(`${BASE_URL}/inner/${productId}`)
  },

  // 批量获取商品信息（内部接口）
  getInnerBatch(productIds: number[]): Promise<Result<Record<string, ProductVO>>> {
    return http.post(`${BASE_URL}/inner/batch`, productIds)
  }
}

// 商品品类 API
export const categoryApi = {
  // 获取品类树
  getTree(): Promise<Result<ProductCategoryVO[]>> {
    return http.get('/category/tree')
  },

  // 获取一级品类列表
  getList(): Promise<Result<ProductCategoryVO[]>> {
    return http.get('/category/list')
  },

  // 获取子品类
  getChildren(parentId: number): Promise<Result<ProductCategoryVO[]>> {
    return http.get(`/category/children/${parentId}`)
  },

  // 获取品类详情
  getById(id: number): Promise<Result<ProductCategoryVO>> {
    return http.get(`/category/${id}`)
  },

  // 新增品类
  add(data: ProductCategoryDTO): Promise<Result<void>> {
    return http.post('/category', data)
  },

  // 更新品类
  update(id: number, data: ProductCategoryDTO): Promise<Result<void>> {
    return http.put(`/category/${id}`, data)
  },

  // 删除品类
  delete(id: number): Promise<Result<void>> {
    return http.delete(`/category/${id}`)
  },

  // 启用/禁用品类
  updateStatus(id: number, status: number): Promise<Result<void>> {
    return http.put(`/category/${id}/status`, { status })
  },

  // 获取热门品类
  getHotCategories(): Promise<Result<ProductCategoryVO[]>> {
    return http.get('/category/hot')
  }
}
