// 用户相关类型定义

// 用户登录请求
export interface UserLoginDTO {
  phone?: string
  email?: string
  password: string
  loginType: 'PHONE' | 'EMAIL' // 登录类型
}

// 用户注册请求
export interface UserRegisterDTO {
  username: string
  phone?: string
  email?: string
  password: string
  identityType?: 'STUDENT' | 'TEACHER' // 身份类型
  registerType: 'PHONE' | 'EMAIL' // 注册类型
}

// 用户更新请求
export interface UserUpdateDTO {
  username?: string
  avatarUrl?: string
  birthday?: string
  [key: string]: unknown
}

// 用户认证请求
export interface UserAuthDTO {
  studentTeacherId: string // 学号/工号
  identityType: 'STUDENT' | 'TEACHER'
  authImageUrl: string // 校园卡/学生证审核图片地址
  [key: string]: unknown
}

// 用户信息
export interface UserVO {
  id: number
  studentTeacherId: string // 学号/工号（脱敏）
  username: string
  phone: string // 脱敏
  campusEmail?: string
  identityType: 'STUDENT' | 'TEACHER'
  authStatus: 'UNAUTH' | 'UNDER_REVIEW' | 'AUTH_SUCCESS' | 'AUTH_FAILED'
  creditScore: number
  locationPermission: 'ALLOW' | 'DENY'
  avatarUrl?: string
  birthday?: string
  createTime: string
  authResultRemark?: string // 认证审核备注
}

// 登录响应
export interface LoginVO {
  accessToken: string
  refreshToken: string
  tokenType?: string
  expiresIn: number
  userInfo: UserVO
}

// 用户地址
export interface UserAddressDTO {
  addressType: 'PICK_POINT' | 'CUSTOM' // 地址类型
  pickPointId?: number // 自提点ID（PICK_POINT时必填）
  customAddress?: string // 自定义地址（CUSTOM时必填）
  longitude?: string // 经度
  latitude?: string // 纬度
  isDefault: number // 是否默认：0-否，1-是
  [key: string]: unknown
}

// 用户地址VO
export interface UserAddressVO {
  id: number
  userId: number
  addressType: 'PICK_POINT' | 'CUSTOM'
  pickPointId?: number
  pickPointName?: string // 自提点名称
  customAddress?: string
  longitude?: string
  latitude?: string
  isDefault: boolean
  createTime: string
  updateTime: string
}

// 校园自提点
export interface CampusPickPointDTO {
  pickPointName: string
  campusArea: string
  detailAddress: string
  longitude: string
  latitude: string
  enableStatus?: number // 启用状态：0-禁用，1-启用
  [key: string]: unknown
}

// 校园自提点VO
export interface CampusPickPointVO {
  id: number
  pickPointName: string
  campusArea: string
  detailAddress: string
  longitude: string
  latitude: string
  enableStatus: boolean
  distance?: number // 距离（米），附近查询时返回
  createTime: string
}

// ==================== 签到相关类型 ====================

// 签到请求
export interface SignDTO {
  signDate?: string // 补签时使用，格式：yyyy-MM-dd
  [key: string]: unknown
}

// 签到状态
export interface SignStatusVO {
  hasSigned: boolean
  continuousDays: number
  totalDays: number
  todayReward: number
  nextRewardDays: number
  nextRewardPoints: number
}

// 签到结果
export interface SignResultVO {
  success: boolean
  rewardPoints: number
  continuousDays: number
  hasContinuousReward: boolean
  continuousRewardPoints: number
  message: string
}

// 签到记录
export interface SignRecordVO {
  id: number
  signDate: string
  signTime: string
  continuousDays: number
  rewardPoints: number
  isRepair: boolean
}

// ==================== 积分相关类型 ====================

// 用户积分
export interface UserPointsVO {
  totalPoints: number
  availablePoints: number
  usedPoints: number
  continuousSignDays: number
  totalSignDays: number
}

// ==================== 评价相关类型 ====================

// 用户评价请求
export interface UserEvaluationDTO {
  orderId: number
  score: number
  evaluationTag?: string
  evaluationContent?: string
  evaluationImageUrl?: string
  isAnonymous?: number
  [key: string]: unknown
}

// 追加评价请求
export interface EvaluationAppendDTO {
  appendContent: string
  appendImageUrl?: string
  [key: string]: unknown
}

// 卖家回复请求
export interface EvaluationReplyDTO {
  replyContent: string
  [key: string]: unknown
}

// 举报评价请求
export interface EvaluationReportDTO {
  reportReason: string
  reportDesc?: string
  [key: string]: unknown
}

// 用户评价
export interface UserEvaluationVO {
  id: number
  orderId: number
  evaluatorId: number
  evaluatorName: string
  evaluatorAvatar: string
  score: number
  evaluationTag: string
  evaluationContent: string
  evaluationImageUrl: string
  isAnonymous: boolean
  replyContent: string
  replyTime: string
  createTime: string
}

// 用户评价分
export interface UserRatingVO {
  userId: number
  username: string
  avatarUrl: string
  overallRating: number
  ratingLevel: string
  totalEvaluations: number
  positiveCount: number
  neutralCount: number
  negativeCount: number
  positiveRate: number
  last30dEvaluations: number
  last30dRating: number
}

// ==================== 排行榜相关类型 ====================

// 排行榜用户
export interface RankingUserVO {
  rank: number
  userId: number
  username: string
  avatarUrl: string
  score: number
  creditScore: number
  rating: number
  isCurrentUser: boolean
}

// 排名信息
export interface RankingInfo {
  rank: number
  score: string
  inList: boolean
}

// 我的排名
export interface MyRankingVO {
  activity: RankingInfo
  trade: RankingInfo
  credit: RankingInfo
  rating: RankingInfo
}

// 排行榜奖励
export interface RankingRewardVO {
  id: number
  rankType: string
  rankTypeName: string
  rankDate: string
  rankPosition: number
  rewardPoints: number
  rewardCouponId: number
  rewardCouponName: string
  isClaimed: boolean
}