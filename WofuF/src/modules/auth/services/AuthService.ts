/**
 * 认证服务 - 处理用户注册、登录、令牌管理
 * 支持自动刷新 Token 机制
 */

import type { RequestOptions } from '@SU/async/RequestOptions.ts'
import type {
  User,
  AuthSession,
  RegisterRequest,
  RegisterResponse,
  LoginRequest,
  LoginResponse,
  RefreshTokenResponse,
} from '@M/auth/dtos/User.ts'
import type { ApiResponse } from '@S/infra/api/v1/models/ApiResponse.ts'
import { Result } from '@S/core/Result.ts'
import { http } from '@S/infra/api/http.ts'
import { cacheService } from '@S/infra/cache'
import { UserApiConstantV1 } from '@S/infra/api/v1/ApiConstants.ts'
import { isTokenExpired, getTokenExpiration } from '@M/auth/utils/jwt.ts'

/**
 * 认证服务接口
 */
export interface IAuthService {
  // 用户注册
  register(data: RegisterRequest, options?: RequestOptions): Promise<Result<RegisterResponse>>

  // 用户登录
  login(data: LoginRequest, options?: RequestOptions): Promise<Result<LoginResponse>>

  // 刷新令牌
  refreshToken(refreshToken: string, options?: RequestOptions): Promise<Result<RefreshTokenResponse>>

  // 注销登录
  logout(options?: RequestOptions): Promise<Result<void>>

  // 获取当前用户
  getCurrentUser(options?: RequestOptions): Promise<Result<User>>

  // 删除用户
  deleteUser(options?: RequestOptions): Promise<Result<void>>

  // 确保有效 Token（自动刷新）
  ensureValidToken(): Promise<boolean>

  // 检查 Token 是否即将过期
  isTokenExpiringSoon(): boolean
}

/**
 * 认证服务实现
 * 支持 JWT 自动刷新机制
 */
export class AuthService implements IAuthService {
  private static readonly CACHE_MODULE = 'auth_service'
  private static readonly TOKEN_KEY = 'auth_tokens'
  private static readonly USER_KEY = 'current_user'

  // Token 刷新相关配置
  private static readonly REFRESH_BUFFER_SECONDS = 120 // 提前 2 分钟刷新
  private static readonly MIN_REFRESH_INTERVAL = 10_000 // 最小刷新间隔 10 秒

  // 存储的令牌
  private tokens: AuthSession | null = null

  // 刷新锁 - 防止并发刷新
  private refreshPromise: Promise<boolean> | null = null
  private lastRefreshTime = 0

  constructor() {
    // 从 localStorage 恢复令牌
    this.loadTokensFromStorage()
  }

  /* ==================== 用户注册 ==================== */
  public async register(
    data: RegisterRequest,
    options?: RequestOptions,
  ): Promise<Result<RegisterResponse>> {
    try {
      const response = await http.post<ApiResponse<RegisterResponse>>(
        UserApiConstantV1.Base.ROOT,
        data,
        { signal: options?.signal }
      )

      if (response.data.success) {
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message || '注册失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 用户登录 ==================== */
  public async login(
    data: LoginRequest,
    options?: RequestOptions,
  ): Promise<Result<LoginResponse>> {
    try {
      const response = await http.post<ApiResponse<LoginResponse>>(
        UserApiConstantV1.Me.SESSIONS,
        data,
        { signal: options?.signal }
      )

      if (response.data.success) {
        const loginData = response.data.data
        // 保存令牌
        this.saveTokens({
          userId: loginData.userId,
          accessToken: loginData.accessToken,
          refreshToken: loginData.refreshToken,
        })
        return Result.success(loginData)
      }
      return Result.failure(response.data.message || '登录失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 刷新令牌 ==================== */
  public async refreshToken(
    refreshToken: string,
    options?: RequestOptions,
  ): Promise<Result<RefreshTokenResponse>> {
    try {
      const response = await http.post<ApiResponse<RefreshTokenResponse>>(
        UserApiConstantV1.Me.TOKENS,
        { refreshToken },
        {
          signal: options?.signal,
          headers: this.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        const tokenData = response.data.data
        // 更新令牌
        this.saveTokens({
          userId: tokenData.userId,
          accessToken: tokenData.accessToken,
          refreshToken: tokenData.refreshToken,
        })
        return Result.success(tokenData)
      }
      // 刷新失败，清除令牌
      this.clearTokens()
      return Result.failure(response.data.message || '刷新令牌失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      this.clearTokens()
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 注销登录 ==================== */
  public async logout(options?: RequestOptions): Promise<Result<void>> {
    try {
      const response = await http.delete<ApiResponse<void>>(
        UserApiConstantV1.Me.SESSIONS,
        {
          signal: options?.signal,
          headers: this.getAuthHeaders(),
        }
      )

      // 无论成功失败都清除本地令牌
      this.clearTokens()

      if (response.data.success) {
        return Result.success(undefined)
      }
      return Result.failure(response.data.message || '注销失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      this.clearTokens()
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 获取当前用户 ==================== */
  public async getCurrentUser(options?: RequestOptions): Promise<Result<User>> {
    const cacheKey = 'current_user'

    // 检查缓存
    const cached = cacheService.get<User>(AuthService.CACHE_MODULE, cacheKey)
    if (cached) {
      return Result.success(cached)
    }

    try {
      const response = await http.get<ApiResponse<User>>(
        UserApiConstantV1.Base.ME,
        {
          signal: options?.signal,
          headers: this.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        const user = response.data.data
        cacheService.set(AuthService.CACHE_MODULE, cacheKey, user)
        return Result.success(user)
      }
      return Result.failure(response.data.message || '获取用户信息失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 删除用户 ==================== */
  public async deleteUser(options?: RequestOptions): Promise<Result<void>> {
    try {
      const response = await http.delete<ApiResponse<void>>(
        UserApiConstantV1.Base.ROOT,
        {
          signal: options?.signal,
          headers: this.getAuthHeaders(),
        }
      )

      // 删除用户后清除所有数据
      this.clearTokens()
      cacheService.clearModule(AuthService.CACHE_MODULE)

      if (response.data.success) {
        return Result.success(undefined)
      }
      return Result.failure(response.data.message || '删除用户失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 令牌管理 ==================== */

  /**
   * 获取认证请求头
   */
  public getAuthHeaders(): Record<string, string> {
    const tokens = this.getTokens()
    if (!tokens) return {}
    return { MeoKey: tokens.accessToken }
  }

  /**
   * 获取当前令牌
   */
  public getTokens(): AuthSession | null {
    return this.tokens
  }

  /**
   * 检查是否已登录
   */
  public isAuthenticated(): boolean {
    return this.tokens !== null && !this.isAccessTokenExpired()
  }

  /**
   * 检查 Access Token 是否已过期
   */
  public isAccessTokenExpired(): boolean {
    if (!this.tokens?.accessToken) return true
    return isTokenExpired(this.tokens.accessToken, 0)
  }

  /**
   * 检查 Token 是否即将过期（需要刷新）
   */
  public isTokenExpiringSoon(): boolean {
    if (!this.tokens?.accessToken) return false
    return isTokenExpired(this.tokens.accessToken, AuthService.REFRESH_BUFFER_SECONDS)
  }

  /**
   * 确保有效的 Token（自动刷新机制）
   * @returns 是否有有效的 Token
   */
  public async ensureValidToken(): Promise<boolean> {
    // 没有 Token
    if (!this.tokens) {
      return false
    }

    // Access Token 仍然有效且不需要刷新
    if (!this.isTokenExpiringSoon()) {
      return true
    }

    // 检查是否有 Refresh Token
    if (!this.tokens.refreshToken) {
      this.clearTokens()
      return false
    }

    // 如果已经在刷新中，等待刷新完成
    if (this.refreshPromise) {
      return this.refreshPromise
    }

    // 检查最小刷新间隔，防止频繁刷新
    const now = Date.now()
    if (now - this.lastRefreshTime < AuthService.MIN_REFRESH_INTERVAL) {
      // 刚刚刷新过，认为 Token 仍然有效
      return !this.isAccessTokenExpired()
    }

    // 开始刷新
    this.refreshPromise = this.doRefresh()

    try {
      const success = await this.refreshPromise
      return success
    } finally {
      this.refreshPromise = null
    }
  }

  /**
   * 执行 Token 刷新
   */
  private async doRefresh(): Promise<boolean> {
    if (!this.tokens?.refreshToken) {
      return false
    }

    try {
      const result = await this.refreshToken(this.tokens.refreshToken)
      this.lastRefreshTime = Date.now()

      if (result.isSuccess) {
        return true
      }

      // 刷新失败，清除 Token
      this.clearTokens()
      return false
    } catch {
      this.clearTokens()
      return false
    }
  }

  /**
   * 保存令牌到内存和 localStorage
   */
  private saveTokens(tokens: AuthSession): void {
    // 计算 Token 过期时间
    const expiresAt = getTokenExpiration(tokens.accessToken)
    this.tokens = {
      ...tokens,
      expiresAt: expiresAt ?? undefined,
    }

    try {
      localStorage.setItem(AuthService.TOKEN_KEY, JSON.stringify(this.tokens))
    } catch {
      // localStorage 不可用时忽略
    }
  }

  /**
   * 从 localStorage 加载令牌
   */
  private loadTokensFromStorage(): void {
    try {
      const stored = localStorage.getItem(AuthService.TOKEN_KEY)
      if (stored) {
        this.tokens = JSON.parse(stored)

        // 加载后检查 Token 是否已过期
        if (this.isAccessTokenExpired()) {
          // Token 已过期，尝试刷新
          this.refreshPromise = this.doRefresh()
        }
      }
    } catch {
      this.tokens = null
    }
  }

  /**
   * 清除令牌
   */
  private clearTokens(): void {
    this.tokens = null
    this.refreshPromise = null
    this.lastRefreshTime = 0
    cacheService.clearModule(AuthService.CACHE_MODULE)
    try {
      localStorage.removeItem(AuthService.TOKEN_KEY)
      localStorage.removeItem(AuthService.USER_KEY)
    } catch {
      // localStorage 不可用时忽略
    }
  }
}

// 导出单例
export const authService = new AuthService()
