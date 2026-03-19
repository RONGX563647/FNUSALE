import { http } from '@/utils/request'
import type { Result, PageResult, PageParams } from '@/types/common'
import type {
  UserRegisterDTO,
  UserUpdateDTO,
  UserAuthDTO,
  UserVO,
  LoginVO,
  UserAddressDTO,
  UserAddressVO,
  CampusPickPointDTO,
  CampusPickPointVO,
  SignDTO,
  SignStatusVO,
  SignResultVO,
  SignRecordVO,
  UserPointsVO,
  UserEvaluationDTO,
  EvaluationAppendDTO,
  EvaluationReplyDTO,
  EvaluationReportDTO,
  UserEvaluationVO,
  UserRatingVO,
  RankingUserVO,
  MyRankingVO,
  RankingRewardVO
} from '@/types/user'

// 用户 API
export const userApi = {
  // 上传头像
  uploadAvatar(file: any): Promise<Result<{ url: string }>> {
    return http.upload('/upload/avatar', { file })
  },

  // 上传认证图片
  uploadAuthImage(file: any): Promise<Result<{ url: string }>> {
    return http.upload('/upload/auth', { file })
  },

  // 用户注册（手机号）
  registerByPhone(data: Omit<UserRegisterDTO, 'registerType'>): Promise<Result<void>> {
    return http.post('/user/register/phone', { ...data, registerType: 'PHONE' })
  },

  // 用户注册（邮箱）
  registerByEmail(data: Omit<UserRegisterDTO, 'registerType'>): Promise<Result<void>> {
    return http.post('/user/register/email', { ...data, registerType: 'EMAIL' })
  },

  // 用户登录（手机号）
  loginByPhone(phone: string, password: string): Promise<Result<LoginVO>> {
    return http.post('/user/login', { phone, password, loginType: 'PHONE' })
  },

  // 用户登录（邮箱）
  loginByEmail(email: string, password: string): Promise<Result<LoginVO>> {
    return http.post('/user/login', { email, password, loginType: 'EMAIL' })
  },

  // 验证码登录
  loginByCaptcha(account: string, captcha: string): Promise<Result<LoginVO>> {
    return http.post('/user/login/captcha', { account, captcha })
  },

  // 发送验证码
  sendCaptcha(account: string, type: string = 'LOGIN'): Promise<Result<void>> {
    return http.post('/user/captcha/send', { account, type })
  },

  // 用户登出
  logout(): Promise<Result<void>> {
    return http.post('/user/logout')
  },

  // 刷新Token
  refreshToken(refreshToken: string): Promise<Result<LoginVO>> {
    return http.post('/user/refresh-token', { refreshToken })
  },

  // 获取当前用户信息
  getCurrentUserInfo(): Promise<Result<UserVO>> {
    return http.get('/user/info')
  },

  // 更新用户信息
  updateUserInfo(data: UserUpdateDTO): Promise<Result<void>> {
    return http.put('/user/info', data)
  },

  // 修改密码
  updatePassword(oldPassword: string, newPassword: string): Promise<Result<void>> {
    return http.put('/user/password', { oldPassword, newPassword })
  },

  // 校园身份认证
  submitAuth(data: UserAuthDTO): Promise<Result<void>> {
    return http.post('/user/auth', data)
  },

  // 获取认证状态
  getAuthStatus(): Promise<Result<UserVO>> {
    return http.get('/user/auth/status')
  },

  // 获取用户详情
  getUserById(userId: number): Promise<Result<UserVO>> {
    return http.get(`/user/${userId}`)
  },

  // 更新定位权限
  updateLocationPermission(permission: 'ALLOW' | 'DENY'): Promise<Result<void>> {
    return http.put('/user/location-permission', { permission })
  },

  // 校验定位是否在校园内
  verifyLocation(longitude: string, latitude: string): Promise<Result<boolean>> {
    return http.get('/user/location/verify', { longitude, latitude })
  },

  // IP定位
  getLocationByIp(): Promise<Result<{
    longitude: string
    latitude: string
    province: string
    city: string
    district: string
    address: string
    inCampus: boolean
  }>> {
    return http.get('/user/location/ip')
  },

  // 逆地理编码
  geocodeLocation(longitude: string, latitude: string): Promise<Result<{
    longitude: string
    latitude: string
    province: string
    city: string
    district: string
    address: string
    inCampus: boolean
  }>> {
    return http.get('/user/location/geocode', { longitude, latitude })
  },

  // 综合定位
  getCurrentLocation(longitude?: string, latitude?: string): Promise<Result<{
    longitude: string
    latitude: string
    province: string
    city: string
    district: string
    address: string
    inCampus: boolean
  }>> {
    return http.get('/user/location/current', { longitude, latitude })
  },

  // 获取我的发布列表
  getMyProducts(params: PageParams): Promise<Result<PageResult<unknown>>> {
    return http.get('/user/my/products', params)
  },

  // 获取我的订单列表
  getMyOrders(params: PageParams & { status?: string }): Promise<Result<PageResult<unknown>>> {
    return http.get('/user/my/orders', params)
  },

  // 获取我的收藏列表
  getMyFavorites(params: PageParams): Promise<Result<PageResult<unknown>>> {
    return http.get('/user/my/favorites', params)
  }
}

// 用户地址 API
export const addressApi = {
  // 获取我的地址列表
  getList(): Promise<Result<UserAddressVO[]>> {
    return http.get('/user/address/list')
  },

  // 获取地址详情
  getById(id: number): Promise<Result<UserAddressVO>> {
    return http.get(`/user/address/${id}`)
  },

  // 新增地址
  add(data: UserAddressDTO): Promise<Result<void>> {
    return http.post('/user/address', data)
  },

  // 更新地址
  update(id: number, data: UserAddressDTO): Promise<Result<void>> {
    return http.put(`/user/address/${id}`, data)
  },

  // 删除地址
  delete(id: number): Promise<Result<void>> {
    return http.delete(`/user/address/${id}`)
  },

  // 设置默认地址
  setDefault(id: number): Promise<Result<void>> {
    return http.put(`/user/address/${id}/default`)
  },

  // 获取默认地址
  getDefault(): Promise<Result<UserAddressVO>> {
    return http.get('/user/address/default')
  }
}

// 校园自提点 API
export const pickPointApi = {
  // 获取自提点列表（启用的）
  getList(): Promise<Result<CampusPickPointVO[]>> {
    return http.get('/user/pick-point/list')
  },

  // 获取附近自提点
  getNearby(longitude: string, latitude: string, distance = 1000): Promise<Result<CampusPickPointVO[]>> {
    return http.get('/user/pick-point/nearby', { longitude, latitude, distance })
  },

  // 获取自提点详情
  getById(id: number): Promise<Result<CampusPickPointVO>> {
    return http.get(`/user/pick-point/${id}`)
  },

  // 新增自提点（管理员）
  add(data: CampusPickPointDTO): Promise<Result<void>> {
    return http.post('/user/pick-point', data)
  },

  // 更新自提点（管理员）
  update(id: number, data: CampusPickPointDTO): Promise<Result<void>> {
    return http.put(`/user/pick-point/${id}`, data)
  },

  // 删除自提点（管理员）
  delete(id: number): Promise<Result<void>> {
    return http.delete(`/user/pick-point/${id}`)
  },

  // 启用/禁用自提点（管理员）
  updateStatus(id: number, status: number): Promise<Result<void>> {
    return http.put(`/user/pick-point/${id}/status`, { status })
  },

  // 分页查询自提点（管理员）
  getPage(params: PageParams & { campusArea?: string; status?: number }): Promise<Result<PageResult<CampusPickPointVO>>> {
    return http.get('/user/pick-point/page', params)
  }
}

// 签到 API
export const signApi = {
  // 每日签到
  sign(): Promise<Result<SignResultVO>> {
    return http.post('/user/sign')
  },

  // 查询签到状态
  getStatus(): Promise<Result<SignStatusVO>> {
    return http.get('/user/sign/status')
  },

  // 签到统计
  getStatistics(): Promise<Result<SignStatusVO>> {
    return http.get('/user/sign/statistics')
  },

  // 签到记录
  getRecords(params: PageParams): Promise<Result<PageResult<SignRecordVO>>> {
    return http.get('/user/sign/records', params)
  },

  // 签到日历
  getCalendar(month: string): Promise<Result<string[]>> {
    return http.get(`/user/sign/calendar/${month}`)
  },

  // 补签
  repair(data: SignDTO): Promise<Result<SignResultVO>> {
    return http.post('/user/sign/repair', data)
  }
}

// 用户积分 API
export const pointsApi = {
  // 获取我的积分
  getMyPoints(): Promise<Result<UserPointsVO>> {
    return http.get('/user/sign/points')
  },

  // 获取积分变动记录
  getPointsLogs(params: PageParams & { changeType?: string }): Promise<Result<PageResult<{
    id: number
    changeType: string
    changeAmount: number
    beforePoints: number
    afterPoints: number
    remark: string
    createTime: string
  }>>> {
    return http.get('/user/sign/points/logs', params)
  }
}

// 用户评价 API
export const evaluationApi = {
  // 提交评价
  submit(data: UserEvaluationDTO): Promise<Result<void>> {
    return http.post('/user/evaluation', data)
  },

  // 追加评价
  append(id: number, data: EvaluationAppendDTO): Promise<Result<void>> {
    return http.post(`/user/evaluation/${id}/append`, data)
  },

  // 卖家回复
  reply(id: number, data: EvaluationReplyDTO): Promise<Result<void>> {
    return http.post(`/user/evaluation/${id}/reply`, data)
  },

  // 获取用户评价列表
  getUserEvaluations(userId: number, params: PageParams): Promise<Result<PageResult<UserEvaluationVO>>> {
    return http.get(`/user/evaluation/user/${userId}`, params)
  },

  // 获取我的评价
  getMyEvaluations(params: PageParams): Promise<Result<PageResult<UserEvaluationVO>>> {
    return http.get('/user/evaluation/my', params)
  },

  // 获取评价统计
  getUserRating(userId: number): Promise<Result<UserRatingVO>> {
    return http.get(`/user/evaluation/rating/${userId}`)
  },

  // 获取评价标签统计
  getUserTags(userId: number): Promise<Result<{ tagName: string; tagCount: number }[]>> {
    return http.get(`/user/evaluation/tags/${userId}`)
  },

  // 举报评价
  report(id: number, data: EvaluationReportDTO): Promise<Result<void>> {
    return http.post(`/user/evaluation/${id}/report`, data)
  }
}

// 排行榜 API
export const rankingApi = {
  // 活跃度排行榜
  getActivity(type: string = 'daily', date?: string, limit: number = 100): Promise<Result<RankingUserVO[]>> {
    return http.get('/rank/activity', { type, date, limit })
  },

  // 交易排行榜
  getTrade(type: string = 'daily', date?: string): Promise<Result<RankingUserVO[]>> {
    return http.get('/rank/trade', { type, date })
  },

  // 信誉排行榜
  getCredit(): Promise<Result<RankingUserVO[]>> {
    return http.get('/rank/credit')
  },

  // 好评排行榜
  getRating(): Promise<Result<RankingUserVO[]>> {
    return http.get('/rank/rating')
  },

  // 我的排名
  getMyRanking(): Promise<Result<MyRankingVO>> {
    return http.get('/rank/my')
  },

  // 获取排行榜（统一入口）
  getLeaderboard(type: string): Promise<Result<PageResult<RankingUserVO>>> {
    if (type === 'CREDIT') {
      return http.get('/rank/credit')
    } else if (type === 'CHECKIN') {
      return http.get('/rank/activity', { type: 'daily' })
    }
    return http.get('/rank/activity', { type })
  },

  // 排行榜历史
  getHistory(rankType: string, params: PageParams): Promise<Result<PageResult<RankingUserVO>>> {
    return http.get('/rank/history', { rankType, ...params })
  },

  // 领取奖励
  claimReward(id: number): Promise<Result<void>> {
    return http.post(`/rank/reward/${id}`)
  },

  // 我的奖励列表
  getMyRewards(isClaimed?: boolean): Promise<Result<RankingRewardVO[]>> {
    return http.get('/rank/rewards', { isClaimed })
  }
}