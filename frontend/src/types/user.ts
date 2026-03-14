// 用户相关类型定义

// 用户登录请求
export interface UserLoginDTO {
  phone: string
  password: string
}

// 用户注册请求
export interface UserRegisterDTO {
  phone: string
  password: string
  nickname?: string
  code: string
}

// 用户更新请求
export interface UserUpdateDTO {
  nickname?: string
  avatar?: string
  gender?: string
  major?: string
  dormArea?: string
}

// 用户认证请求
export interface UserAuthDTO {
  realName: string
  identityType: string // STUDENT/TEACHER
  idCardNo: string
  idCardImage?: string
  studentIdImage?: string
}

// 用户信息
export interface UserVO {
  id: number
  phone: string
  nickname: string
  avatar: string
  gender: string
  authStatus: string // NONE/PENDING/APPROVED/REJECTED
  identityType: string
  creditScore: number
  major: string
  dormArea: string
  locationPermission: string // ALLOW/DENY
  createTime: string
}

// 登录响应
export interface LoginVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserVO
}

// 用户地址
export interface UserAddressDTO {
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number
}

// 校园自提点
export interface CampusPickPointDTO {
  name: string
  address: string
  campusArea: string
  longitude: string
  latitude: string
  contactPhone?: string
  openTime?: string
}