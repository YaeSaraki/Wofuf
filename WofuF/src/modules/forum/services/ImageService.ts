/**
 * 图片上传响应
 */
export interface UploadImageResponse {
  url: string
  markdown: string
  md5: string
  isDuplicate: boolean
  success: boolean
}

/**
 * 图片服务 - 处理图片上传相关操作
 */

import type { ApiResponse } from '@S/infra/api/v1/models/ApiResponse.ts'
import { Result } from '@S/core/Result.ts'
import { http } from '@S/infra/api/http.ts'
import { authService } from '@M/auth/services/AuthService.ts'

export class ImageService {
  /**
   * 上传图片
   * @param file 图片文件
   * @param folder 存储文件夹，默认为 'posts'
   */
  public async uploadImage(
    file: File,
    folder: string = 'posts'
  ): Promise<Result<UploadImageResponse>> {
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('folder', folder)

      const response = await http.post<ApiResponse<UploadImageResponse>>(
        '/api/v1/forum/images/upload',
        formData,
        {
          headers: {
            ...authService.getAuthHeaders(),
            'Content-Type': 'multipart/form-data',
          },
        }
      )

      if (response.data.success) {
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message || '图片上传失败')
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } }; message?: string }
      return Result.failure(err.response?.data?.message || err.message || '网络请求失败')
    }
  }

  /**
   * 从 Markdown 文本中提取图片 URL
   */
  public extractImageUrls(markdown: string): string[] {
    const regex = /!\[.*?\]\((.*?)\)/g
    const urls: string[] = []
    let match
    while ((match = regex.exec(markdown)) !== null) {
      if (match[1]) {
        urls.push(match[1])
      }
    }
    return urls
  }

  /**
   * 统计 Markdown 文本中的图片数量
   */
  public countImages(markdown: string): number {
    const regex = /!\[.*?\]\((.*?)\)/g
    const matches = markdown.match(regex)
    return matches ? matches.length : 0
  }

  /**
   * 获取已有图片列表
   * @param folder 文件夹名称，默认为 'posts'
   */
  public async getExistingImages(folder: string = 'posts'): Promise<string[]> {
    try {
      const response = await http.get<ApiResponse<{ images: string[] }>>(
        `/api/v1/forum/images/list`,
        {
          params: { folder },
          headers: authService.getAuthHeaders(),
        }
      )

      if (response.data.success && response.data.data?.images) {
        return response.data.data.images
      }
      return []
    } catch (error) {
      console.warn('[ImageService] Failed to load existing images:', error)
      // 返回空数组而不是抛出错误，让用户可以继续操作
      return []
    }
  }
}

// 导出单例
export const imageService = new ImageService()
