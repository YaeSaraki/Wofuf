/**
 * 用户数据传输对象
 */

// 用户信息
export interface User {
  userId: string
  username: string
  email: string
  isEmailVerified?: boolean
  isAdminUser?: boolean
}

// JWT 令牌类型
export type JwtToken = string
export type RefreshToken = string

// 认证会话
export interface AuthSession {
  userId: string
  accessToken: JwtToken
  refreshToken: RefreshToken
}

// 注册请求
export interface RegisterRequest {
  email: string
  username: string
  password: string
}

// 注册响应
export interface RegisterResponse {
  userId: string
  username: string
  email: string
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  userId: string
  accessToken: JwtToken
  refreshToken: RefreshToken
}

// 刷新令牌请求
export interface RefreshTokenRequest {
  refreshToken: RefreshToken
}

// 刷新令牌响应
export interface RefreshTokenResponse {
  userId: string
  accessToken: JwtToken
  refreshToken: RefreshToken
}
