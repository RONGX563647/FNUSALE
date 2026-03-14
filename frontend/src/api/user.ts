import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'
import type {
  UserLoginDTO,
  UserRegisterDTO,
  UserUpdateDTO,
  UserAuthDTO,
  UserVO,
  LoginVO,
  UserAddressDTO
} from '@/types/user'

const BASE_URL = '/user'

export const userApi = {
  // 用户注册
  register(data: UserRegisterDTO): Promise<Result<void>> {
    return http.post(`${BASE_URL}/register`, data)
  },

  // 用户登录
  login(data: UserLoginDTO): Promise<Result<LoginVO>> {
    return http.post(`${BASE_URL}/login`, data)
  },

  // 用户登出
  logout(): Promise<Result<void>> {
    return http.post(`${BASE_URL}/logout`)
  },

  // 刷新Token
  refreshToken(refreshToken: string): Promise<Result<LoginVO>> {
    return http.post(`${BASE_URL}/refresh-token`, null, { params: { refreshToken } })
  },

  // 获取当前用户信息
  getCurrentUserInfo(): Promise<Result<UserVO>> {
    return http.get(`${BASE_URL}/info`)
  },

  // 更新用户信息
  updateUserInfo(data: UserUpdateDTO): Promise<Result<void>> {
    return http.put(`${BASE_URL}/info`, data)
  },

  // 修改密码
  updatePassword(oldPassword: string, newPassword: string): Promise<Result<void>> {
    return http.put(`${BASE_URL}/password`, null, { params: { oldPassword, newPassword } })
  },

  // 校园身份认证
  submitAuth(data: UserAuthDTO): Promise<Result<void>> {
    return http.post(`${BASE_URL}/auth`, data)
  },

  // 获取认证状态
  getAuthStatus(): Promise<Result<UserVO>> {
    return http.get(`${BASE_URL}/auth/status`)
  },

  // 获取用户详情
  getUserById(userId: number): Promise<Result<UserVO>> {
    return http.get(`${BASE_URL}/${userId}`)
  },

  // 更新定位权限
  updateLocationPermission(permission: string): Promise<Result<void>> {
    return http.put(`${BASE_URL}/location-permission`, null, { params: { permission } })
  },

  // 校验定位是否在校园内
  verifyLocation(longitude: string, latitude: string): Promise<Result<boolean>> {
    return http.get(`${BASE_URL}/location/verify`, { params: { longitude, latitude } })
  },

  // 获取我的发布列表
  getMyProducts(params: PageParams): Promise<Result<PageResult<unknown>>> {
    return http.get(`${BASE_URL}/my/products`, { params })
  },

  // 获取我的订单列表
  getMyOrders(params: PageParams & { status?: string }): Promise<Result<PageResult<unknown>>> {
    return http.get(`${BASE_URL}/my/orders`, { params })
  },

  // 获取我的收藏列表
  getMyFavorites(params: PageParams): Promise<Result<PageResult<unknown>>> {
    return http.get(`${BASE_URL}/my/favorites`, { params })
  }
}

// 用户地址 API
export const addressApi = {
  // 获取我的地址列表
  getList(): Promise<Result<unknown[]>> {
    return http.get('/address/list')
  },

  // 获取地址详情
  getById(id: number): Promise<Result<unknown>> {
    return http.get(`/address/${id}`)
  },

  // 新增地址
  add(data: UserAddressDTO): Promise<Result<void>> {
    return http.post('/address', data)
  },

  // 更新地址
  update(id: number, data: UserAddressDTO): Promise<Result<void>> {
    return http.put(`/address/${id}`, data)
  },

  // 删除地址
  delete(id: number): Promise<Result<void>> {
    return http.delete(`/address/${id}`)
  },

  // 设置默认地址
  setDefault(id: number): Promise<Result<void>> {
    return http.put(`/address/${id}/default`)
  },

  // 获取默认地址
  getDefault(): Promise<Result<unknown>> {
    return http.get('/address/default')
  }
}

// 校园自提点 API
export const pickPointApi = {
  // 获取自提点列表
  getList(): Promise<Result<unknown[]>> {
    return http.get('/pick-point/list')
  },

  // 获取附近自提点
  getNearby(longitude: string, latitude: string, distance = 1000): Promise<Result<unknown[]>> {
    return http.get('/pick-point/nearby', { params: { longitude, latitude, distance } })
  },

  // 获取自提点详情
  getById(id: number): Promise<Result<unknown>> {
    return http.get(`/pick-point/${id}`)
  },

  // 新增自提点
  add(data: unknown): Promise<Result<void>> {
    return http.post('/pick-point', data)
  },

  // 更新自提点
  update(id: number, data: unknown): Promise<Result<void>> {
    return http.put(`/pick-point/${id}`, data)
  },

  // 删除自提点
  delete(id: number): Promise<Result<void>> {
    return http.delete(`/pick-point/${id}`)
  },

  // 启用/禁用自提点
  updateStatus(id: number, status: number): Promise<Result<void>> {
    return http.put(`/pick-point/${id}/status`, null, { params: { status } })
  },

  // 分页查询自提点
  getPage(params: PageParams & { campusArea?: string; status?: number }): Promise<Result<PageResult<unknown>>> {
    return http.get('/pick-point/page', { params })
  }
}