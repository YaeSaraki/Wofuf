/**
 * 论坛服务 - 处理帖子、评论相关操作
 */

import type { RequestOptions } from '@SU/async/RequestOptions.ts'
import type {
  CreatePostRequest,
  CreatePostResponse,
  GetPostsResponse,
  GetPostResponse,
  GetCommentsResponse,
  VoteResponse,
  ReplyToPostRequest,
  ReplyToCommentRequest,
  PostCategory,
} from '@M/forum/dtos/Post.ts'
import type { ApiResponse } from '@S/infra/api/v1/models/ApiResponse.ts'
import { Result } from '@S/core/Result.ts'
import { http } from '@S/infra/api/http.ts'
import { cacheService } from '@S/infra/cache'
import { authService } from '@M/auth/services/AuthService.ts'

export interface IForumService {
  /* ---------------- 帖子列表 ---------------- */
  getRecentPosts(page: number, size: number, category?: PostCategory, options?: RequestOptions): Promise<Result<GetPostsResponse>>
  getPopularPosts(page: number, size: number, category?: PostCategory, options?: RequestOptions): Promise<Result<GetPostsResponse>>

  /* ---------------- 帖子详情 ---------------- */
  getPostBySlug(slug: string, options?: RequestOptions): Promise<Result<GetPostResponse>>

  /* ---------------- 帖子操作 ---------------- */
  createPost(postData: CreatePostRequest, options?: RequestOptions): Promise<Result<CreatePostResponse>>
  editPost(postId: string, data: { title?: string; text?: string; link?: string }, options?: RequestOptions): Promise<Result<void>>
  deletePost(postId: string, options?: RequestOptions): Promise<Result<void>>

  /* ---------------- 投票 ---------------- */
  upvotePost(postId: string, options?: RequestOptions): Promise<Result<VoteResponse>>
  downvotePost(postId: string, options?: RequestOptions): Promise<Result<VoteResponse>>
  unvotePost(postId: string, options?: RequestOptions): Promise<Result<VoteResponse>>
  upvoteComment(commentId: string, options?: RequestOptions): Promise<Result<VoteResponse>>
  downvoteComment(commentId: string, options?: RequestOptions): Promise<Result<VoteResponse>>

  /* ---------------- 评论 ---------------- */
  getCommentsByPostSlug(postSlug: string, options?: RequestOptions): Promise<Result<GetCommentsResponse>>
  replyToPost(postId: string, data: ReplyToPostRequest, options?: RequestOptions): Promise<Result<void>>
  replyToPostBySlug(postSlug: string, data: ReplyToPostRequest, options?: RequestOptions): Promise<Result<void>>
  replyToComment(commentId: string, data: ReplyToCommentRequest, options?: RequestOptions): Promise<Result<void>>
}

export class ForumService implements IForumService {
  private static readonly CACHE_MODULE = 'forum_service'

  /* ==================== 帖子列表 ==================== */

  /**
   * 获取最新帖子
   */
  public async getRecentPosts(
    page: number = 0,       // 页码 1 开始
    size: number = 10,      // 每页条数
    category?: PostCategory,
    options?: RequestOptions,
  ): Promise<Result<GetPostsResponse>> {
    const cacheKey = `recent_posts_${page}_${size}_${category || 'all'}`

    return cacheService.withCacheAndDeduplication<Result<GetPostsResponse>>(
      ForumService.CACHE_MODULE,
      cacheKey,
      async () => {
        try {
          const tokens = authService.getTokens()
          const params: Record<string, string | number> = { page, size }

          if (tokens?.userId) {
            params.userId = tokens.userId
          }
          if (category) {
            params.category = category
          }

          const response = await http.get<ApiResponse<GetPostsResponse>>(
            '/api/v1/forum/posts/recent',
            {
              signal: options?.signal,
              params,
            }
          )

          if (response.data.success) {
            return Result.success(response.data.data)
          }
          return Result.failure(response.data.message || '获取帖子失败')
        } catch (error) {
          const err = error as { response?: { data?: { message?: string } }; message?: string }
          return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
        }
      }
    )
  }

  /**
   * 获取热门帖子
   */
  public async getPopularPosts(
    page: number = 0,
    size: number = 10,
    category?: PostCategory,
    options?: RequestOptions,
  ): Promise<Result<GetPostsResponse>> {
    const cacheKey = `popular_posts_${page}_${size}_${category || 'all'}`

    return cacheService.withCacheAndDeduplication<Result<GetPostsResponse>>(
      ForumService.CACHE_MODULE,
      cacheKey,
      async () => {
        try {
          const tokens = authService.getTokens()
          const params: Record<string, string | number> = { page, size }

          if (tokens?.userId) {
            params.userId = tokens.userId
          }
          if (category) {
            params.category = category
          }

          const response = await http.get<ApiResponse<GetPostsResponse>>(
            '/api/v1/forum/posts/popular',
            {
              signal: options?.signal,
              params,
            }
          )

          if (response.data.success) {
            return Result.success(response.data.data)
          }
          return Result.failure(response.data.message || '获取帖子失败')
        } catch (error) {
          const err = error as { response?: { data?: { message?: string } }; message?: string }
          return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
        }
      }
    )
  }

  /* ==================== 帖子详情 ==================== */


  /**
   * 根据 Slug 获取帖子
   */
  public async getPostBySlug(
    slug: string,
    options?: RequestOptions,
  ): Promise<Result<GetPostResponse>> {
    const cacheKey = `post_slug_${slug}`

    return cacheService.withCacheAndDeduplication<Result<GetPostResponse>>(
      ForumService.CACHE_MODULE,
      cacheKey,
      async () => {
        try {
          const tokens = authService.getTokens()
          const params: Record<string, string> | undefined = tokens?.userId
            ? { userId: tokens.userId }
            : undefined

          console.debug('[ForumService] 请求帖子详情:', slug, 'params:', params)

          const response = await http.get<ApiResponse<GetPostResponse>>(
            `/api/v1/forum/posts/slug/${slug}`,
            {
              signal: options?.signal,
              params,
            }
          )

          console.debug('[ForumService] 帖子响应:', response.data)

          if (response.data.success) {
            return Result.success(response.data.data)
          }
          console.error('[ForumService] API 返回失败:', response.data.message)
          return Result.failure(response.data.message || '获取帖子失败')
        } catch (error) {
          const err = error as { response?: { data?: { message?: string }; status?: number }; message?: string }
          console.error('[ForumService] 请求异常:', err.response?.status, err.message, err.response?.data)
          return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
        }
      }
    )
  }

  /* ==================== 帖子操作 ==================== */

  /**
   * 创建帖子
   */
  public async createPost(
    postData: CreatePostRequest,
    options?: RequestOptions,
  ): Promise<Result<CreatePostResponse>> {
    try {
      const response = await http.post<ApiResponse<CreatePostResponse>>(
        '/api/v1/forum/posts',
        postData,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        cacheService.clearModule(ForumService.CACHE_MODULE)
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message || '创建帖子失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 编辑帖子
   */
  public async editPost(
    postId: string,
    data: { title?: string; text?: string; link?: string },
    options?: RequestOptions,
  ): Promise<Result<void>> {
    try {
      const response = await http.put<ApiResponse<void>>(
        `/api/v1/forum/posts/${postId}`,
        { postId, ...data },
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        cacheService.clearModule(ForumService.CACHE_MODULE)
        return Result.success(undefined)
      }
      return Result.failure(response.data.message || '编辑帖子失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 删除帖子
   */
  public async deletePost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<void>> {
    try {
      const response = await http.delete<ApiResponse<void>>(
        `/api/v1/forum/posts/${postId}`,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        cacheService.clearModule(ForumService.CACHE_MODULE)
        return Result.success(undefined)
      }
      return Result.failure(response.data.message || '删除帖子失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 投票 ==================== */

  /**
   * 给帖子点赞
   */
  public async upvotePost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<VoteResponse>> {
    try {
      if (!authService.isAuthenticated()) {
        return Result.failure('请先登录')
      }

      const response = await http.put<ApiResponse<VoteResponse>>(
        `/api/v1/forum/posts/${postId}/upvote`,
        {},
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message || '投票失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 给帖子点踩
   */
  public async downvotePost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<VoteResponse>> {
    try {
      if (!authService.isAuthenticated()) {
        return Result.failure('请先登录')
      }

      const response = await http.put<ApiResponse<VoteResponse>>(
        `/api/v1/forum/posts/${postId}/downvote`,
        {},
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message || '投票失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 取消帖子投票
   */
  public async unvotePost(
    postId: string,
    options?: RequestOptions,
  ): Promise<Result<VoteResponse>> {
    try {
      if (!authService.isAuthenticated()) {
        return Result.failure('请先登录')
      }

      const response = await http.put<ApiResponse<VoteResponse>>(
        `/api/v1/forum/posts/${postId}/unvote`,
        {},
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message || '取消投票失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 给评论点赞
   */
  public async upvoteComment(
    commentId: string,
    options?: RequestOptions,
  ): Promise<Result<VoteResponse>> {
    try {
      if (!authService.isAuthenticated()) {
        return Result.failure('请先登录')
      }

      const response = await http.put<ApiResponse<VoteResponse>>(
        `/api/v1/forum/comments/${commentId}/upvote`,
        {},
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message || '投票失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 给评论点踩
   */
  public async downvoteComment(
    commentId: string,
    options?: RequestOptions,
  ): Promise<Result<VoteResponse>> {
    try {
      if (!authService.isAuthenticated()) {
        return Result.failure('请先登录')
      }

      const response = await http.put<ApiResponse<VoteResponse>>(
        `/api/v1/forum/comments/${commentId}/downvote`,
        {},
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message || '投票失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /* ==================== 评论 ==================== */

  /**
   * 获取帖子的评论
   */
  public async getCommentsByPostSlug(
    postSlug: string,
    options?: RequestOptions,
  ): Promise<Result<GetCommentsResponse>> {
    const cacheKey = `comments_${postSlug}`

    return cacheService.withCacheAndDeduplication<Result<GetCommentsResponse>>(
      ForumService.CACHE_MODULE,
      cacheKey,
      async () => {
        try {
          const tokens = authService.getTokens()
          const params: Record<string, string> | undefined = tokens?.userId
            ? { userId: tokens.userId }
            : undefined

          const response = await http.get<ApiResponse<GetCommentsResponse>>(
            `/api/v1/forum/posts/slug/${postSlug}/comments`,
            {
              signal: options?.signal,
              params,
            }
          )

          if (response.data.success) {
            return Result.success(response.data.data)
          }
          return Result.failure(response.data.message || '获取评论失败')
        } catch (error) {
          const err = error as { response?: { data?: { message?: string } }; message?: string }
          return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
        }
      }
    )
  }

  /**
   * 回复帖子（通过 UUID）
   */
  public async replyToPost(
    postId: string,
    data: ReplyToPostRequest,
    options?: RequestOptions,
  ): Promise<Result<void>> {
    try {
      const response = await http.post<ApiResponse<void>>(
        `/api/v1/forum/posts/${postId}/replies`,
        data,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        cacheService.clearModule(ForumService.CACHE_MODULE)
        return Result.success(undefined)
      }
      return Result.failure(response.data.message || '回复失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 回复帖子（通过 Slug）
   */
  public async replyToPostBySlug(
    postSlug: string,
    data: ReplyToPostRequest,
    options?: RequestOptions,
  ): Promise<Result<void>> {
    try {
      const response = await http.post<ApiResponse<void>>(
        `/api/v1/forum/posts/slug/${postSlug}/replies`,
        data,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        cacheService.clearModule(ForumService.CACHE_MODULE)
        return Result.success(undefined)
      }
      return Result.failure(response.data.message || '回复失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 回复评论
   */
  public async replyToComment(
    commentId: string,
    data: ReplyToCommentRequest,
    options?: RequestOptions,
  ): Promise<Result<void>> {
    try {
      const response = await http.post<ApiResponse<void>>(
        `/api/v1/forum/comments/${commentId}/replies`,
        data,
        {
          signal: options?.signal,
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success) {
        cacheService.clearModule(ForumService.CACHE_MODULE)
        return Result.success(undefined)
      }
      return Result.failure(response.data.message || '回复失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }
}

// 导出单例
export const forumService = new ForumService()
