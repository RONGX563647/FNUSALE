import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'
import type { ProductVO } from '@/types/product'
import type { AiPriceSuggestDTO } from '@/types/ai'

// AI 分类 API
export const aiCategoryApi = {
  // 识别商品品类
  recognize(imageUrl: string): Promise<Result<unknown>> {
    return http.post('/ai/category/recognize', null, { params: { imageUrl } })
  },

  // 批量识别品类
  batchRecognize(imageUrls: string[]): Promise<Result<unknown[]>> {
    return http.post('/ai/category/batch-recognize', imageUrls)
  },

  // 获取识别历史
  getHistory(params: PageParams): Promise<Result<unknown>> {
    return http.get('/ai/category/history', { params })
  }
}

// AI 推荐 API
export const aiRecommendApi = {
  // 获取首页推荐
  getHomeRecommend(params: PageParams): Promise<Result<PageResult<ProductVO>>> {
    return http.get('/ai/recommend/home', { params })
  },

  // 获取猜你喜欢
  getGuessLike(params: PageParams): Promise<Result<PageResult<ProductVO>>> {
    return http.get('/ai/recommend/guess-like', { params })
  },

  // 获取相似商品
  getSimilarProducts(productId: number, params: PageParams): Promise<Result<PageResult<ProductVO>>> {
    return http.get(`/ai/recommend/similar/${productId}`, { params })
  },

  // 获取同专业推荐
  getSameMajorRecommend(params: PageParams): Promise<Result<PageResult<ProductVO>>> {
    return http.get('/ai/recommend/same-major', { params })
  },

  // 获取同宿舍区推荐
  getSameDormRecommend(params: PageParams): Promise<Result<PageResult<ProductVO>>> {
    return http.get('/ai/recommend/same-dorm', { params })
  },

  // 刷新推荐
  refresh(): Promise<Result<void>> {
    return http.post('/ai/recommend/refresh')
  },

  // 反馈推荐结果
  feedback(productId: number, feedbackType: string): Promise<Result<void>> {
    return http.post('/ai/recommend/feedback', null, { params: { productId, feedbackType } })
  }
}

// AI 价格参考 API
export const aiPriceApi = {
  // 获取价格参考
  getReference(categoryId: number, newDegree: string): Promise<Result<unknown>> {
    return http.get('/ai/price/reference', { params: { categoryId, newDegree } })
  },

  // 获取商品定价建议
  suggest(data: AiPriceSuggestDTO): Promise<Result<unknown>> {
    return http.post('/ai/price/suggest', data)
  },

  // 更新价格参考数据
  update(data: unknown): Promise<Result<void>> {
    return http.post('/ai/price/update', data)
  },

  // 获取价格趋势
  getTrend(categoryId: number): Promise<Result<unknown>> {
    return http.get('/ai/price/trend', { params: { categoryId } })
  },

  // 比价
  compare(productId: number, price: number): Promise<Result<unknown>> {
    return http.get('/ai/price/compare', { params: { productId, price } })
  }
}

// AI 智能客服 API
export const aiServiceApi = {
  // 智能问答
  ask(question: string): Promise<Result<unknown>> {
    return http.post('/ai/service/ask', null, { params: { question } })
  },

  // 获取常见问题列表
  getFaqList(): Promise<Result<unknown[]>> {
    return http.get('/ai/service/faq/list')
  },

  // 搜索问题
  search(keyword: string): Promise<Result<unknown[]>> {
    return http.get('/ai/service/search', { params: { keyword } })
  },

  // 获取问题分类
  getCategories(): Promise<Result<unknown[]>> {
    return http.get('/ai/service/categories')
  },

  // 转人工客服
  transferToHuman(): Promise<Result<void>> {
    return http.post('/ai/service/transfer')
  },

  // 反馈问答结果
  feedback(answerId: number, helpful: boolean): Promise<Result<void>> {
    return http.post('/ai/service/feedback', null, { params: { answerId, helpful } })
  },

  // 获取聊天历史
  getHistory(params: PageParams): Promise<Result<unknown>> {
    return http.get('/ai/service/history', { params })
  }
}