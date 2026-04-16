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
  BatchCommentActionResponse,
  BannedMember,
  GetBannedMembersResponse,
  MemberActionResponse,
  MemberProfile,
  GetMembersListResponse,
  AdminStats,
  GetImagesResponse,
  DeleteImageResponse,
  GetImageUrlResponse,
  GetOperationLogsResponse,
  OperationType,
  TargetType,
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
  getPostsForReview(
    page: number,
    size: number,
    options?: RequestOptions,
  ): Promise<Result<GetPostsForReviewResponse>>

  /* ---------------- 评论管理 ---------------- */
  hideComment(commentId: string, options?: RequestOptions): Promise<Result<CommentActionResponse>>
  showComment(commentId: string, options?: RequestOptions): Promise<Result<CommentActionResponse>>
  batchHideComments(
    commentIds: string[],
    options?: RequestOptions,
  ): Promise<Result<BatchCommentActionResponse>>
  batchShowComments(
    commentIds: string[],
    options?: RequestOptions,
  ): Promise<Result<BatchCommentActionResponse>>
  getHiddenComments(
    page: number,
    size: number,
    options?: RequestOptions,
  ): Promise<Result<GetHiddenCommentsResponse>>

  /* ---------------- 成员管理 ---------------- */
  banMember(
    memberId: string,
    reason?: string,
    bannedUntilMinutes?: number,
    options?: RequestOptions,
  ): Promise<Result<MemberActionResponse>>
  unbanMember(memberId: string, options?: RequestOptions): Promise<Result<MemberActionResponse>>
  grantPermission(
    memberId: string,
    permission: PermissionPoint,
    options?: RequestOptions,
  ): Promise<Result<MemberActionResponse>>
  revokePermission(
    memberId: string,
    permission: PermissionPoint,
    options?: RequestOptions,
  ): Promise<Result<MemberActionResponse>>
  getBannedMembers(
    page: number,
    size: number,
    options?: RequestOptions,
  ): Promise<Result<GetBannedMembersResponse>>
  getMemberProfile(
    memberId: string,
    page?: number,
    size?: number,
    options?: RequestOptions,
  ): Promise<Result<MemberProfile>>
  getMembersList(
    nickname?: string,
    page?: number,
    size?: number,
    options?: RequestOptions,
  ): Promise<Result<GetMembersListResponse>>

  /* ---------------- 权限检查 ---------------- */
  hasPermission(permission: PermissionPoint, forceRefresh?: boolean): Promise<boolean>
  hasAnyPermission(permissions: PermissionPoint[], forceRefresh?: boolean): Promise<boolean>

  /* ---------------- 统计数据 ---------------- */
  getAdminStats(options?: RequestOptions): Promise<Result<AdminStats>>

  /* ---------------- 图片管理 ---------------- */
  getImages(
    page: number,
    size: number,
    folder?: string,
    uploaderMemberId?: string,
    options?: RequestOptions,
  ): Promise<Result<GetImagesResponse>>
  getImageUrl(imageId: string, options?: RequestOptions): Promise<Result<GetImageUrlResponse>>
  deleteImage(imageId: string, options?: RequestOptions): Promise<Result<DeleteImageResponse>>

  /* ---------------- 操作日志 ---------------- */
  getOperationLogs(
    page?: number,
    size?: number,
    operatorId?: string,
    operationType?: OperationType,
    targetType?: TargetType,
    options?: RequestOptions,
  ): Promise<Result<GetOperationLogsResponse>>
}

/**
 * 管理后台服务实现
 */
export class AdminService implements IAdminService {
  private static readonly ADMIN_BASE = '/api/v1/forum/admin'

  /* ==================== 帖子管理 ==================== */

  public async pinPost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/pin`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '置顶失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async unpinPost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/unpin`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '取消置顶失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async featurePost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/feature`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '加精失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async unfeaturePost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/unfeature`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '取消加精失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async hidePost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/hide`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '隐藏失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async showPost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/show`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '显示失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async setPostUnderReview(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/review`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '设置审核失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async approvePost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<PostActionResponse>> {
    try {
      const response = await http.post<ApiResponse<PostActionResponse>>(
        `${AdminService.ADMIN_BASE}/posts/${postId}/approve`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '审核通过失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getPostsForReview(
    page: number = 0,
    size: number = 20,
    options?: RequestOptions,
  ): Promise<Result<GetPostsForReviewResponse>> {
    try {
      const response = await http.get<ApiResponse<GetPostsForReviewResponse>>(
        `${AdminService.ADMIN_BASE}/posts/for-review`,
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { page, size } },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取待审核帖子失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 评论管理 ==================== */

  public async hideComment(
    commentId: string,
    options?: RequestOptions,
  ): Promise<Result<CommentActionResponse>> {
    try {
      const response = await http.post<ApiResponse<CommentActionResponse>>(
        `${AdminService.ADMIN_BASE}/comments/${commentId}/hide`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '隐藏评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async showComment(
    commentId: string,
    options?: RequestOptions,
  ): Promise<Result<CommentActionResponse>> {
    try {
      const response = await http.post<ApiResponse<CommentActionResponse>>(
        `${AdminService.ADMIN_BASE}/comments/${commentId}/show`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '显示评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getHiddenComments(
    page: number = 0,
    size: number = 20,
    options?: RequestOptions,
  ): Promise<Result<GetHiddenCommentsResponse>> {
    try {
      const response = await http.get<ApiResponse<GetHiddenCommentsResponse>>(
        `${AdminService.ADMIN_BASE}/comments/hidden`,
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { page, size } },
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
    contentSearch?: string,
    includeHidden: boolean = false,
    options?: RequestOptions,
  ): Promise<Result<GetCommentsResponse>> {
    try {
      const response = await http.get<ApiResponse<GetCommentsResponse>>(
        `${AdminService.ADMIN_BASE}/comments`,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
          params: { page, size, search, contentSearch, includeHidden },
        },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async batchHideComments(
    commentIds: string[],
    options?: RequestOptions,
  ): Promise<Result<BatchCommentActionResponse>> {
    try {
      const response = await http.post<ApiResponse<BatchCommentActionResponse>>(
        `${AdminService.ADMIN_BASE}/comments/batch-hide`,
        { commentIds },
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '批量隐藏评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async batchShowComments(
    commentIds: string[],
    options?: RequestOptions,
  ): Promise<Result<BatchCommentActionResponse>> {
    try {
      const response = await http.post<ApiResponse<BatchCommentActionResponse>>(
        `${AdminService.ADMIN_BASE}/comments/batch-show`,
        { commentIds },
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '批量显示评论失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 成员管理 ==================== */

  public async banMember(
    memberId: string,
    reason?: string,
    bannedUntilMinutes?: number,
    options?: RequestOptions,
  ): Promise<Result<MemberActionResponse>> {
    try {
      const params: Record<string, string | number> = {}
      if (reason) params.reason = reason
      if (bannedUntilMinutes) params.bannedUntilMinutes = bannedUntilMinutes

      const response = await http.post<ApiResponse<MemberActionResponse>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/ban`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders(), params },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '封禁成员失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async unbanMember(
    memberId: string,
    options?: RequestOptions,
  ): Promise<Result<MemberActionResponse>> {
    try {
      const response = await http.post<ApiResponse<MemberActionResponse>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/unban`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '解封成员失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async grantPermission(
    memberId: string,
    permission: PermissionPoint,
    options?: RequestOptions,
  ): Promise<Result<MemberActionResponse>> {
    try {
      const response = await http.post<ApiResponse<MemberActionResponse>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/permissions`,
        {},
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { permission } },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '授予权限失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async revokePermission(
    memberId: string,
    permission: PermissionPoint,
    options?: RequestOptions,
  ): Promise<Result<MemberActionResponse>> {
    try {
      const response = await http.delete<ApiResponse<MemberActionResponse>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/permissions/${permission}`,
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '撤销权限失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getBannedMembers(
    page: number = 0,
    size: number = 20,
    options?: RequestOptions,
  ): Promise<Result<GetBannedMembersResponse>> {
    try {
      const response = await http.get<ApiResponse<GetBannedMembersResponse>>(
        `${AdminService.ADMIN_BASE}/members/banned`,
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { page, size } },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取封禁成员失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getMemberProfile(
    memberId: string,
    page: number = 0,
    size: number = 10,
    options?: RequestOptions,
  ): Promise<Result<MemberProfile>> {
    try {
      const response = await http.get<ApiResponse<MemberProfile>>(
        `${AdminService.ADMIN_BASE}/members/${memberId}/profile`,
        { signal: options?.signal, headers: authService.getAuthHeaders(), params: { page, size } },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取用户资料失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getMembersList(
    nickname?: string,
    page: number = 0,
    size: number = 20,
    options?: RequestOptions,
  ): Promise<Result<GetMembersListResponse>> {
    try {
      const response = await http.get<ApiResponse<GetMembersListResponse>>(
        `${AdminService.ADMIN_BASE}/members`,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
          params: { nickname: nickname || undefined, page, size },
        },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取成员列表失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 权限检查 ==================== */

  public async hasPermission(
    permission: PermissionPoint,
    forceRefresh: boolean = false,
  ): Promise<boolean> {
    if (!authService.isAuthenticated()) return false

    const memberResult = await memberService.getCurrentMember(undefined, forceRefresh)
    if (!memberResult.isSuccess) return false

    const member = memberResult.getValue()
    // 兼容 isAdminUser 和 adminUser 两种字段名
    const isAdmin = member.isAdminUser === true || member.adminUser === true
    // 检查是否是管理员或拥有特定权限
    return isAdmin || (member.permissions?.includes(permission) ?? false)
  }

  public async hasAnyPermission(
    permissions: PermissionPoint[],
    forceRefresh: boolean = false,
  ): Promise<boolean> {
    if (!authService.isAuthenticated()) return false

    const memberResult = await memberService.getCurrentMember(undefined, forceRefresh)
    if (!memberResult.isSuccess) return false

    const member = memberResult.getValue()
    // 兼容 isAdminUser 和 adminUser 两种字段名
    const isAdmin = member.isAdminUser === true || member.adminUser === true
    if (isAdmin) return true

    return permissions.some((p) => member.permissions?.includes(p) ?? false)
  }

  /* ==================== 统计数据 ==================== */

  public async getAdminStats(options?: RequestOptions): Promise<Result<AdminStats>> {
    try {
      const response = await http.get<ApiResponse<AdminStats>>(`${AdminService.ADMIN_BASE}/stats`, {
        signal: options?.signal,
        headers: authService.getAuthHeaders(),
      })
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取统计数据失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 图片管理 ==================== */

  public async getImages(
    page: number = 0,
    size: number = 20,
    folder?: string,
    uploaderMemberId?: string,
    options?: RequestOptions,
  ): Promise<Result<GetImagesResponse>> {
    try {
      const response = await http.get<ApiResponse<GetImagesResponse>>(
        `${AdminService.ADMIN_BASE}/images`,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
          params: { page, size, folder: folder || undefined, uploaderMemberId: uploaderMemberId || undefined },
        },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取图片列表失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async deleteImage(
    imageId: string,
    options?: RequestOptions,
  ): Promise<Result<DeleteImageResponse>> {
    try {
      const response = await http.delete<ApiResponse<DeleteImageResponse>>(
        `${AdminService.ADMIN_BASE}/images/${imageId}`,
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '删除图片失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  public async getImageUrl(
    imageId: string,
    options?: RequestOptions,
  ): Promise<Result<GetImageUrlResponse>> {
    try {
      const response = await http.get<ApiResponse<GetImageUrlResponse>>(
        `${AdminService.ADMIN_BASE}/images/${imageId}/url`,
        { signal: options?.signal, headers: authService.getAuthHeaders() },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取图片URL失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 操作日志 ==================== */

  public async getOperationLogs(
    page: number = 0,
    size: number = 20,
    operatorId?: string,
    operationType?: OperationType,
    targetType?: TargetType,
    options?: RequestOptions,
  ): Promise<Result<GetOperationLogsResponse>> {
    try {
      const params: Record<string, string | number> = { page, size }
      if (operatorId) params.operatorId = operatorId
      if (operationType) params.operationType = operationType
      if (targetType) params.targetType = targetType

      const response = await http.get<ApiResponse<GetOperationLogsResponse>>(
        `${AdminService.ADMIN_BASE}/logs`,
        { signal: options?.signal, headers: authService.getAuthHeaders(), params },
      )
      if (response.data.success) return Result.success(response.data.data)
      return Result.failure(response.data.message || '获取操作日志失败')
    } catch (error) {
      return this.handleError(error)
    }
  }

  /* ==================== 错误处理 ==================== */

  private handleError(error: unknown): Result<never> {
    const err = error as {
      response?: { data?: { message?: string }; status?: number }
      message?: string
    }

    // 处理权限不足
    if (err.response?.status === 403) {
      return Result.failure('权限不足')
    }

    return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
  }
}

// 导出单例
export const adminService = new AdminService()
