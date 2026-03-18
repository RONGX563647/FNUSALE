// 商品相关类型定义

// 商品发布请求
export interface ProductPublishDTO {
  title: string
  description: string
  categoryId: number
  price: number
  originalPrice?: number
  newDegree: string
  images: string[]
  pickPointId?: number
  [key: string]: unknown
}

// 商品查询参数
export interface ProductQueryDTO {
  categoryId?: number
  keyword?: string
  minPrice?: number
  maxPrice?: number
  newDegree?: string
  status?: string
  orderBy?: string
  pageNum?: number
  pageSize?: number
  [key: string]: unknown
}

// 商品信息
export interface ProductVO {
  id: number
  title: string
  description: string
  categoryId: number
  categoryName: string
  price: number
  originalPrice: number
  newDegree: string
  images: string[]
  mainImage: string
  sellerId: number
  sellerName: string
  sellerAvatar: string
  pickPointId: number
  pickPointName: string
  status: string // DRAFT/PENDING/APPROVED/REJECTED/ON_SHELF/OFF_SHELF/SOLD
  viewCount: number
  likeCount: number
  favoriteCount: number
  isLiked: boolean
  isFavorite: boolean
  createTime: string
  updateTime: string
}

// 商品品类
export interface ProductCategoryVO {
  id: number
  name: string
  parentId: number
  level: number
  icon: string
  sort: number
  status: number
  children?: ProductCategoryVO[]
}

// 商品品类请求
export interface ProductCategoryDTO {
  name: string
  parentId?: number
  icon?: string
  sort?: number
  [key: string]: unknown
}