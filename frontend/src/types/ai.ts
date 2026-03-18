// AI 相关类型定义

// AI 价格建议请求
export interface AiPriceSuggestDTO {
  categoryId: number
  title: string
  description: string
  newDegree: string
}

// AI 识别结果
export interface AiCategoryResult {
  categoryId: number
  categoryName: string
  confidence: number
}