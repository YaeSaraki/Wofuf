/**
 * API 常量定义 - 集中管理所有模块的 API 路径
 * 与后端 ApiConstantV1.kt 保持同步
 */

// 基础 API 路径
const API_BASE_PATH = '/api/v1'

// 路径参数常量
export const ApiParam = {
  USER_ID: 'userId',
  USERNAME: 'username',
  POST_ID: 'postId',
  POST_SLUG: 'postSlug',
  COMMENT_ID: 'commentId',
  MEMBER_ID: 'memberId',
  PLAYER_ID: 'playerId',
  PLAYER_NAME_OR_UUID: 'playerNameOrUuid',
}

// 用户模块 API 常量
export const UserApiConstantV1 = {
  BASE: `${API_BASE_PATH}/users`,

  // 路径参数
  Param: {
    USER_ID: ApiParam.USER_ID,
    USERNAME: ApiParam.USERNAME,
  },

  // 基础路径
  Base: {
    ROOT: `${API_BASE_PATH}/users`,
    BY_ID: `${API_BASE_PATH}/users/{${ApiParam.USER_ID}}`,
    BY_USERNAME: `${API_BASE_PATH}/users/username/{${ApiParam.USERNAME}}`,
    ME: `${API_BASE_PATH}/users/me`,
  },

  // 当前用户相关
  Me: {
    PROFILE: `${API_BASE_PATH}/users/me`,
    SESSIONS: `${API_BASE_PATH}/users/me/sessions`,
    TOKENS: `${API_BASE_PATH}/users/me/tokens`,
  },
}

// 论坛模块 API 常量
export const ForumApiConstantV1 = {
  BASE: `${API_BASE_PATH}/forum`,

  // 路径参数
  Param: {
    POST_ID: ApiParam.POST_ID,
    POST_SLUG: ApiParam.POST_SLUG,
    COMMENT_ID: ApiParam.COMMENT_ID,
    MEMBER_ID: ApiParam.MEMBER_ID,
  },

  // 基础路径
  Base: {
    ROOT: `${API_BASE_PATH}/forum`,
  },

  // 成员相关路径
  Members: {
    ROOT: `${API_BASE_PATH}/forum/members`,
    BY_ID: `${API_BASE_PATH}/forum/members/{${ApiParam.MEMBER_ID}}`,
    CURRENT: `${API_BASE_PATH}/forum/members/current`,
    BY_USERNAME: `${API_BASE_PATH}/forum/members/username/{${ApiParam.USERNAME}}`,
  },

  // 帖子相关路径
  Posts: {
    ROOT: `${API_BASE_PATH}/forum/posts`,
    BY_ID: `${API_BASE_PATH}/forum/posts/{${ApiParam.POST_ID}}`,
    BY_SLUG: `${API_BASE_PATH}/forum/posts/slug/{${ApiParam.POST_SLUG}}`,
    RECENT: `${API_BASE_PATH}/forum/posts/recent`,
    POPULAR: `${API_BASE_PATH}/forum/posts/popular`,
    LIKES: `${API_BASE_PATH}/forum/posts/{${ApiParam.POST_ID}}/likes`,
    COMMENTS: `${API_BASE_PATH}/forum/posts/{${ApiParam.POST_ID}}/comments`,
  },

  // 评论相关路径
  Comments: {
    ROOT: `${API_BASE_PATH}/forum/comments`,
    BY_ID: `${API_BASE_PATH}/forum/comments/{${ApiParam.COMMENT_ID}}`,
    REPLIES: `${API_BASE_PATH}/forum/comments/{${ApiParam.COMMENT_ID}}/replies`,
    STATS: `${API_BASE_PATH}/forum/comments/{${ApiParam.COMMENT_ID}}/stats`,
    BY_POST_SLUG: `${API_BASE_PATH}/forum/posts/slug/{${ApiParam.POST_SLUG}}/comments`,
  },
}

// 玩家模块 API 常量
export const PlayerApiConstantV1 = {
  BASE: `${API_BASE_PATH}/players`,

  // 路径参数
  Param: {
    PLAYER_ID: ApiParam.PLAYER_ID,
    PLAYER_NAME_OR_UUID: ApiParam.PLAYER_NAME_OR_UUID,
    PLAYER_UUID: 'playerUuid',
  },

  // 基础路径
  Base: {
    ROOT: `${API_BASE_PATH}/players`,
    BY_ID: `${API_BASE_PATH}/players/{${ApiParam.PLAYER_ID}}`,
    BY_NAME_OR_UUID: `${API_BASE_PATH}/players/playerNameOrUuid/{${ApiParam.PLAYER_NAME_OR_UUID}}`,
    RANDOM: `${API_BASE_PATH}/players/random`,
  },

  // 玩家数据路径
  Data: {
    ADVANCEMENTS: `${API_BASE_PATH}/players/advancements/{playerUuid}`,
    STATISTICS: `${API_BASE_PATH}/players/statistics/{playerUuid}`,
    SKINS: `${API_BASE_PATH}/players/skins/{playerUuid}`,
  },

  // 特殊功能路径
  Features: {
    RANDOM_PROFILE: `${API_BASE_PATH}/players/random-profile`,
    YESTERDAY_ONLINE: `${API_BASE_PATH}/players/yesterday`,
    SEARCH: `${API_BASE_PATH}/players/search`,
    SERVER_STATUS: `${API_BASE_PATH}/players/server-status`,
  },

  // 玩家相关路径
  Player: {
    SKIN: `${API_BASE_PATH}/players/{${ApiParam.PLAYER_ID}}/skin`,
    STATISTICS: `${API_BASE_PATH}/players/{${ApiParam.PLAYER_ID}}/statistics`,
    ADVANCEMENTS: `${API_BASE_PATH}/players/{${ApiParam.PLAYER_ID}}/advancements`,
    YESTERDAY_ONLINE: `${API_BASE_PATH}/players/{${ApiParam.PLAYER_ID}}/yesterday-online`,
  },
}

// API 工具方法
export class ApiUtils {
  /**
   * 替换路径中的参数
   */
  static replaceParams(path: string, params: Record<string, string>): string {
    let result = path
    Object.keys(params).forEach(key => {
      const value = params[key]
      if (value !== undefined) {
        result = result.replace(`{${key}}`, value)
      }
    })
    return result
  }

  /**
   * 构建带查询参数的URL
   */
  static buildUrl(basePath: string, params?: Record<string, any>): string {
    if (!params) return basePath

    const queryParams = new URLSearchParams()
    Object.keys(params).forEach(key => {
      const value = params[key]
      if (value !== undefined && value !== null) {
        queryParams.append(key, value.toString())
      }
    })

    const queryString = queryParams.toString()
    return queryString ? `${basePath}?${queryString}` : basePath
  }
}

export default {
  User: UserApiConstantV1,
  Forum: ForumApiConstantV1,
  Player: PlayerApiConstantV1,
  Utils: ApiUtils,
}