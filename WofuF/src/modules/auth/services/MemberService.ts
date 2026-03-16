/**
 * 成员服务 - 处理论坛成员相关操作
 */

import type { RequestOptions } from '@SU/async/RequestOptions.ts'
import type {
  Member,
  CreateMemberRequest,
  GetCurrentMemberResponse,
} from '@M/auth/dtos/Member.ts'
import type { ApiResponse } from '@S/infra/api/v1/models/ApiResponse.ts'
import { Result } from '@S/core/Result.ts'
import { http } from '@S/infra/api/http.ts'
import { cacheService } from '@S/infra/cache'
import { authService } from '@M/auth/services/AuthService.ts'
import { ForumApiConstantV1 } from '@S/infra/api/v1/ApiConstants.ts'

/**
 * 成员服务接口
 */
export interface IMemberService {
  // 创建成员 (绑定玩家)
  createMember(data: CreateMemberRequest, options?: RequestOptions): Promise<Result<void>>

  // 获取当前成员信息
  getCurrentMember(options?: RequestOptions): Promise<Result<GetCurrentMemberResponse>>

  // 根据用户名获取成员
  getMemberByUsername(username: string, options?: RequestOptions): Promise<Result<Member>>

  // 检查当前用户是否已是成员
  isMember(): Promise<boolean>
}

/**
 * 成员服务实现
 */
export class MemberService implements IMemberService {
  private static readonly CACHE_MODULE = 'member_service'

  /* ==================== 创建成员 ==================== */
  public async createMember(
    data: CreateMemberRequest,
    options?: RequestOptions,
  ): Promise<Result<void>> {
    try {
      const response = await http.post<ApiResponse<void>>(
        ForumApiConstantV1.Members.ROOT,
        data,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        // 清除成员缓存
        cacheService.delete(MemberService.CACHE_MODULE, 'current_member')
        return Result.success(undefined)
      }
      return Result.failure(response.data.message || '创建成员失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 获取当前成员 ==================== */
  public async getCurrentMember(
    options?: RequestOptions,
  ): Promise<Result<GetCurrentMemberResponse>> {
    const cacheKey = 'current_member'

    // 检查缓存
    const cached = cacheService.get<GetCurrentMemberResponse>(
      MemberService.CACHE_MODULE,
      cacheKey,
    )
    if (cached) {
      return Result.success(cached)
    }

    // 获取当前用户ID
    const tokens = authService.getTokens()
    if (!tokens) {
      return Result.failure('未登录')
    }

    try {
      const response = await http.get<ApiResponse<GetCurrentMemberResponse>>(
        '/api/v1/forum/members/current',
        {
          signal: options?.signal,
          headers: {
            ...authService.getAuthHeaders(),
            userId: tokens.userId,
          },
        }
      )

      if (response.data.success) {
        const member = response.data.data
        cacheService.set(MemberService.CACHE_MODULE, cacheKey, member)
        return Result.success(member)
      }
      return Result.failure(response.data.message || '获取成员信息失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 根据用户名获取成员 ==================== */
  public async getMemberByUsername(
    username: string,
    options?: RequestOptions,
  ): Promise<Result<Member>> {
    const cacheKey = `member_username_${username}`

    // 检查缓存
    const cached = cacheService.get<Member>(MemberService.CACHE_MODULE, cacheKey)
    if (cached) {
      return Result.success(cached)
    }

    try {
      const response = await http.get<ApiResponse<Member>>(
        `/api/v1/forum/members/username/${username}`,
        {
          signal: options?.signal,
        }
      )

      if (response.data.success) {
        const member = response.data.data
        cacheService.set(MemberService.CACHE_MODULE, cacheKey, member)
        return Result.success(member)
      }
      return Result.failure(response.data.message || '获取成员信息失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 检查是否已是成员 ==================== */
  public async isMember(): Promise<boolean> {
    if (!authService.isAuthenticated()) {
      return false
    }

    const result = await this.getCurrentMember()
    return result.isSuccess
  }
}

// 导出单例
export const memberService = new MemberService()
