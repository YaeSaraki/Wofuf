/**
 * Axios 请求拦截器 - 自动刷新 JWT Token
 */

import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { http } from '@S/infra/api/http.ts'
import { authService } from '@M/auth/services/AuthService.ts'

// 不需要认证的 API 路径
const PUBLIC_PATHS = [
  '/api/v1/users/login',
  '/api/v1/users/register',
  '/api/v1/users/me/sessions', // POST 登录
  '/api/v1/players', // 公开的玩家信息
  '/api/v1/forum/posts', // 公开的帖子
]

/**
 * 检查是否是公开 API
 */
function isPublicPath(url: string | undefined): boolean {
  if (!url) return false
  const isPublic = PUBLIC_PATHS.some(path => url.includes(path))
  console.debug('[AuthInterceptor] 检查路径:', url, '是否公开:', isPublic)
  return isPublic
}

/**
 * 标记需要刷新后重试的请求
 */
interface PendingRequest {
  config: InternalAxiosRequestConfig
  resolve: (value: unknown) => void
  reject: (reason: unknown) => void
}

// 等待刷新完成的请求队列
let isRefreshing = false
let pendingRequests: PendingRequest[] = []

/**
 * 处理刷新后重试的请求
 */
function processQueue(error: Error | null): void {
  pendingRequests.forEach(pending => {
    if (error) {
      pending.reject(error)
    } else {
      pending.resolve(http(pending.config))
    }
  })
  pendingRequests = []
}

/**
 * 设置请求拦截器 - 自动添加认证头并在需要时刷新 Token
 */
export function setupAuthInterceptor(): void {
  // 请求拦截器
  http.interceptors.request.use(
    async (config) => {
      // 公开 API 不需要认证
      if (isPublicPath(config.url)) {
        console.debug('[AuthInterceptor] 公开路径，跳过认证:', config.url)
        return config
      }

      // 确保 Token 有效（自动刷新）
      const hasValidToken = await authService.ensureValidToken()
      console.debug('[AuthInterceptor] Token 状态:', hasValidToken ? '有效' : '无效')

      if (hasValidToken) {
        // 添加认证头
        const authHeaders = authService.getAuthHeaders()
        Object.entries(authHeaders).forEach(([key, value]) => {
          config.headers.set(key, value)
        })
        console.debug('[AuthInterceptor] 已添加认证头')
      }

      return config
    },
    (error) => Promise.reject(error)
  )

  // 响应拦截器 - 处理 401 错误
  http.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

      // 如果是 401 错误且不是刷新 Token 的请求
      if (
        error.response?.status === 401 &&
        !originalRequest._retry &&
        !originalRequest.url?.includes('/tokens')
      ) {
        // 防止重复重试
        originalRequest._retry = true

        // 如果正在刷新，将请求加入队列
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            pendingRequests.push({
              config: originalRequest,
              resolve,
              reject,
            })
          })
        }

        isRefreshing = true

        try {
          // 尝试刷新 Token
          const success = await authService.ensureValidToken()

          if (success) {
            // 刷新成功，重试原始请求
            const authHeaders = authService.getAuthHeaders()
            Object.entries(authHeaders).forEach(([key, value]) => {
              originalRequest.headers.set(key, value)
            })

            // 处理队列中的请求
            processQueue(null)

            return http(originalRequest)
          } else {
            // 刷新失败，清除认证状态
            processQueue(new Error('Token refresh failed'))
            // 可以在这里触发登出或跳转登录页
            return Promise.reject(error)
          }
        } catch (refreshError) {
          processQueue(refreshError as Error)
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }

      return Promise.reject(error)
    }
  )
}
