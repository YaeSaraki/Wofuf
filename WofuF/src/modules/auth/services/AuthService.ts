/**
 * 认证服务 - 处理用户注册、登录、令牌管理
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
}

/**
 * 认证服务实现
 */
export class AuthService implements IAuthService {
  private static readonly CACHE_MODULE = 'auth_service'
  private static readonly TOKEN_KEY = 'auth_tokens'
  private static readonly USER_KEY = 'current_user'

  // 存储的令牌
  private tokens: AuthSession | null = null

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
        '/api/v1/users/me/sessions',
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
        '/api/v1/users/me/tokens',
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
        '/api/v1/users/me/sessions',
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
        '/api/v1/users/me',
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
        '/api/v1/users',
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
    return this.tokens !== null
  }

  /**
   * 保存令牌到内存和 localStorage
   */
  private saveTokens(tokens: AuthSession): void {
    this.tokens = tokens
    try {
      localStorage.setItem(AuthService.TOKEN_KEY, JSON.stringify(tokens))
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
