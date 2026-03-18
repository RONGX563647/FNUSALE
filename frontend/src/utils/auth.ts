import type { TokenInfo } from '@/types/auth'

const TOKEN_KEY = 'admin_token'

// 获取 Token
export function getToken(): string | null {
  const tokenStr = localStorage.getItem(TOKEN_KEY)
  if (!tokenStr) return null

  try {
    const tokenInfo: TokenInfo = JSON.parse(tokenStr)
    // 检查是否过期
    if (Date.now() > tokenInfo.expireTime) {
      removeToken()
      return null
    }
    return tokenInfo.accessToken
  } catch {
    removeToken()
    return null
  }
}

// 获取完整 Token 信息
export function getTokenInfo(): TokenInfo | null {
  const tokenStr = localStorage.getItem(TOKEN_KEY)
  if (!tokenStr) return null

  try {
    const tokenInfo: TokenInfo = JSON.parse(tokenStr)
    if (Date.now() > tokenInfo.expireTime) {
      removeToken()
      return null
    }
    return tokenInfo
  } catch {
    removeToken()
    return null
  }
}

// 设置 Token
export function setToken(accessToken: string, refreshToken: string, expiresIn: number): void {
  const tokenInfo: TokenInfo = {
    accessToken,
    refreshToken,
    expiresIn,
    expireTime: Date.now() + expiresIn * 1000
  }
  localStorage.setItem(TOKEN_KEY, JSON.stringify(tokenInfo))
}

// 获取 Refresh Token
export function getRefreshToken(): string | null {
  const tokenStr = localStorage.getItem(TOKEN_KEY)
  if (!tokenStr) return null

  try {
    const tokenInfo: TokenInfo = JSON.parse(tokenStr)
    return tokenInfo.refreshToken
  } catch {
    return null
  }
}

// 移除 Token
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// 检查是否已登录
export function isAuthenticated(): boolean {
  return !!getToken()
}