import type {RequestOptions} from '@SU/async/RequestOptions.ts'
import type {
  Post,
  CreatePostRequest,
  CreatePostResponse,
  GetPostsResponse,
  GetPostResponse,
  Comment,
} from '@M/forum/dtos/Post.ts'
import type {ApiResponse} from '@S/infra/api/v1/models/ApiResponse.ts'
import {Result} from '@S/core/Result.ts'
import {http} from '@S/infra/api/http.ts'
import {cacheService} from '@S/infra/cache'

export interface IForumService {
  /* ---------------- 获取帖子列表 ---------------- */
  getPosts(params?: { page?: number; limit?: number }, options?: RequestOptions): Promise<Result<GetPostsResponse>>

  /* ---------------- 获取单个帖子 ---------------- */
  getPostBySlug(slug: string, options?: RequestOptions): Promise<Result<GetPostResponse>>

  /* ---------------- 创建帖子 ---------------- */
  createPost(postData: CreatePostRequest, options?: RequestOptions): Promise<Result<CreatePostResponse>>

  /* ---------------- 获取帖子评论 ---------------- */
  getCommentsByPostSlug(postSlug: string, options?: RequestOptions): Promise<Result<Comment[]>>
}

export class ForumService implements IForumService {
  private static readonly CACHE_MODULE = 'forum_service'

  /* ---------------- 获取帖子列表 ---------------- */
  public async getPosts(
    params?: { page?: number; limit?: number },
    options?: RequestOptions,
  ): Promise<Result<GetPostsResponse>> {
    const page = params?.page || 1
    const limit = params?.limit || 10
    const cacheKey = `posts_page_${page}_limit_${limit}`

    // 尝试从缓存获取
    const cached = cacheService.get<GetPostsResponse>(ForumService.CACHE_MODULE, cacheKey)
    if (cached) {
      return Result.success(cached)
    }

    try {
      const response = await http.get<ApiResponse<GetPostsResponse>>('/v1/forum/posts', {
        signal: options?.signal,
        params: { page, limit },
      })

      if (response.data.success) {
        const result = response.data.data
        // 缓存结果
        cacheService.set(ForumService.CACHE_MODULE, cacheKey, result)
        return Result.success(result)
      } else {
        return Result.failure(response.data.message || '获取帖子列表失败')
      }
    } catch (error: any) {
      return Result.failure(error.message || '网络请求失败')
    }
  }

  /* ---------------- 获取单个帖子 ---------------- */
  public async getPostBySlug(
    slug: string,
    options?: RequestOptions,
  ): Promise<Result<GetPostResponse>> {
    const cacheKey = `post_slug_${slug}`

    // 尝试从缓存获取
    const cached = cacheService.get<GetPostResponse>(ForumService.CACHE_MODULE, cacheKey)
    if (cached) {
      return Result.success(cached)
    }

    try {
      const response = await http.get<ApiResponse<GetPostResponse>>(`/v1/forum/posts/${slug}`, {
        signal: options?.signal,
      })

      if (response.data.success) {
        const result = response.data.data
        // 缓存结果
        cacheService.set(ForumService.CACHE_MODULE, cacheKey, result)
        return Result.success(result)
      } else {
        return Result.failure(response.data.message || '获取帖子失败')
      }
    } catch (error: any) {
      return Result.failure(error.message || '网络请求失败')
    }
  }

  /* ---------------- 创建帖子 ---------------- */
  public async createPost(
    postData: CreatePostRequest,
    options?: RequestOptions,
  ): Promise<Result<CreatePostResponse>> {
    try {
      const response = await http.post<ApiResponse<CreatePostResponse>>('/v1/forum/posts', postData, {
        signal: options?.signal,
      })

      if (response.data.success) {
        // 清除相关缓存
        cacheService.clearModule(ForumService.CACHE_MODULE)
        return Result.success(response.data.data)
      } else {
        return Result.failure(response.data.message || '创建帖子失败')
      }
    } catch (error: any) {
      return Result.failure(error.message || '网络请求失败')
    }
  }

  /* ---------------- 获取帖子评论 ---------------- */
  public async getCommentsByPostSlug(
    postSlug: string,
    options?: RequestOptions,
  ): Promise<Result<Comment[]>> {
    const cacheKey = `comments_post_${postSlug}`

    // 尝试从缓存获取
    const cached = cacheService.get<Comment[]>(ForumService.CACHE_MODULE, cacheKey)
    if (cached) {
      return Result.success(cached)
    }

    try {
      const response = await http.get<ApiResponse<Comment[]>>(`/v1/forum/comments/post/${postSlug}`, {
        signal: options?.signal,
      })

      if (response.data.success) {
        const result = response.data.data
        // 缓存结果
        cacheService.set(ForumService.CACHE_MODULE, cacheKey, result)
        return Result.success(result)
      } else {
        return Result.failure(response.data.message || '获取评论失败')
      }
    } catch (error: any) {
      return Result.failure(error.message || '网络请求失败')
    }
  }
}
