/**
 * 管理后台服务 - 处理论坛管理相关操作
 */

import type { RequestOptions } from '@SU/async/RequestOptions.ts'
import type {
  PermissionPoint,
  PostActionResponse,
  GetPostsForReviewResponse,
  HiddenComment,
  GetHiddenCommentsResponse,
  GetCommentsResponse,
  CommentActionResponse,
  BannedMember,
  GetBannedMembersResponse,
  MemberActionResponse,
} from '@M/forum/admin/dtos/Admin.ts'
import type { ApiResponse } from '@S/infra/api/v1/models/ApiResponse.ts'
import { Result } from '@S/core/Result.ts'
import { http } from '@S/infra/api/http.ts'
import { authService } from '@M/auth/services/AuthService.ts'
import { memberService } from '@M/auth/services/MemberService.ts'

/**
 * 管理后台服务接口
 */
export interface IAdminService {
  /* ---------------- 帖子管理 ---------------- */
  pinPost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>>
  unpinPost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>>
  featurePost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>>
  unfeaturePost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>>
  hidePost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>>
  showPost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>>
  setPostUnderReview(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>>
  approvePost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>>
  getPostsForReview(page: number, size: number, options?: RequestOptions): Promise<Result<GetPostsForReviewResponse>>

  /* ---------------- 评论管理 ---------------- */
  hideComment(commentId: string, options?: RequestOptions): Promise<Result<CommentActionResponse>>
  showComment(commentId: string, options?: RequestOptions): Promise<Result<CommentActionResponse>>
  getHiddenComments(page: number, size: number, options?: RequestOptions): Promise<Result<GetHiddenCommentsResponse>>

  /* ---------------- 成员管理 ---------------- */
  banMember(memberId: string, reason?: string, bannedUntilMinutes?: number, options?: RequestOptions): Promise<Result<MemberActionResponse>>
  unbanMember(memberId: string, options?: RequestOptions): Promise<Result<MemberActionResponse>>
  grantPermission(memberId: string, permission: PermissionPoint, options?: RequestOptions): Promise<Result<MemberActionResponse>>
  revokePermission(memberId: string, permission: PermissionPoint, options?: RequestOptions): Promise<Result<MemberActionResponse>>
  getBannedMembers(page: number, size: number, options?: RequestOptions): Promise<Result<GetBannedMembersResponse>>

  /* ---------------- 权限检查 ---------------- */
  hasPermission(permission: PermissionPoint): Promise<boolean>
  hasAnyPermission(permissions: PermissionPoint[]): Promise<boolean>
}

/**
 * 管理后台服务实现
 */
export class AdminService implements IAdminService {
  private static readonly ADMIN_BASE = '/api/v1/forum/admin'

  /* ==================== 帖子管理 ==================== */

  public async pinPost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/pin`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '置顶失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async unpinPost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/unpin`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '取消置顶失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async featurePost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/feature`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '加精失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async unfeaturePost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/unfeature`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '取消加精失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async hidePost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/hide`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '隐藏失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async showPost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/show`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '显示失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async setPostUnderReview(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/review`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '设置审核失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async approvePost(postId: string, options?: RequestOptions): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/approve`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '审核通过失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getPostsForReview(page: number = 0, size: number = 20, options?: RequestOptions): Promise<Result<GetPostsForReviewResponse>> {
    try {
      const response = await http.get<ApiResponse<GetPostsForReviewResponse>>(
        `${AdminService.ADMIN_BASE}/posts/for-review`,
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { page, size } }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取待审核帖子失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 评论管理 ==================== */

  public async hideComment(commentId: string, options?: RequestOptions): Promise<Result<CommentActionResponse>> {
    try {
      const response = await http.post<ApiResponse<CommentActionResponse>>(
        `${AdminService.ADMIN_BASE}/comments/${commentId}/hide`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '隐藏评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async showComment(commentId: string, options?: RequestOptions): Promise<Result<CommentActionResponse>> {
    try {
      const response = await http.post<ApiResponse<CommentActionResponse>>(
        `${AdminService.ADMIN_BASE}/comments/${commentId}/show`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '显示评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getHiddenComments(page: number = 0, size: number = 20, options?: RequestOptions): Promise<Result<GetHiddenCommentsResponse>> {
    try {
      const response = await http.get<ApiResponse<GetHiddenCommentsResponse>>(
        `${AdminService.ADMIN_BASE}/comments/hidden`,
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { page, size } }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取隐藏评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getComments(
    page: number = 0,
    size: number = 20,
    search?: string,
    includeHidden: boolean = false,
    options?: RequestOptions
  ): Promise<Result<GetCommentsResponse>> {
    try {
      const response = await http.get<ApiResponse<GetCommentsResponse>>(
        `${AdminService.ADMIN_BASE}/comments`,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
          params: { page, size, search, includeHidden }
        }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 成员管理 ==================== */

  public async banMember(
    memberId: string, 
    reason?: string, 
    bannedUntilMinutes?: number, 
    options?: RequestOptions
  ): Promise<Result<MemberActionResponse>> {
    try {
      const params: Record<string, string | number> = {}
      if (reason) params.reason = reason
      if (bannedUntilMinutes) params.bannedUntilMinutes = bannedUntilMinutes

      const response = await http.post<ApiResponse<MemberActionResponse>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/ban`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders(), params }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '封禁成员失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async unbanMember(memberId: string, options?: RequestOptions): Promise<Result<MemberActionResponse>> {
    try {
      const response = await http.post<ApiResponse<MemberActionResponse>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/unban`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '解封成员失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async grantPermission(memberId: string, permission: PermissionPoint, options?: RequestOptions): Promise<Result<MemberActionResponse>> {
    try {
      const response = await http.post<ApiResponse<MemberActionResponse>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/permissions`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { permission } }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '授予权限失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async revokePermission(memberId: string, permission: PermissionPoint, options?: RequestOptions): Promise<Result<MemberActionResponse>> {
    try {
      const response = await http.delete<ApiResponse<MemberActionResponse>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/permissions/${permission}`,
        { signal: options?.signal, headers: authService.getAuthHeaders() }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '撤销权限失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getBannedMembers(page: number = 0, size: number = 20, options?: RequestOptions): Promise<Result<GetBannedMembersResponse>> {
    try {
      const response = await http.get<ApiResponse<GetBannedMembersResponse>>(
        `${AdminService.ADMIN_BASE}/members/banned`,
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { page, size } }
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取封禁成员失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 权限检查 ==================== */

  public async hasPermission(permission: PermissionPoint, forceRefresh: boolean = false): Promise<boolean> {
    if (!authService.isAuthenticated()) return false
    
    const memberResult = await memberService.getCurrentMember(undefined, forceRefresh)
    if (!memberResult.isSuccess) return false
    
    const member = memberResult.getValue()
    // 检查是否是管理员或拥有特定权限
    return member.isAdminUser === true || (member.permissions?.includes(permission) ?? false)
  }

  public async hasAnyPermission(permissions: PermissionPoint[], forceRefresh: boolean = false): Promise<boolean> {
    if (!authService.isAuthenticated()) return false
    
    const memberResult = await memberService.getCurrentMember(undefined, forceRefresh)
    if (!memberResult.isSuccess) return false
    
    const member = memberResult.getValue()
    if (member.isAdminUser === true) return true
    
    return permissions.some(p => member.permissions?.includes(p) ?? false)
  }

  /* ==================== 错误处理 ==================== */

  private handleError(error: unknown): Result<never> {
    const err = error as { response?: { data?: { message?: string }; status?: number }; message?: string }
    
    // 处理权限不足
    if (err.response?.status === 403) {
      return Result.failure('权限不足')
    }
    
    return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
  }
}

// 导出单例
export const adminService = new AdminService()
