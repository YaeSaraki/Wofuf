/**
 * 帖子相关数据传输对象
 * 对应后端 API 结构
 */

// 帖子类型枚举
export enum PostType {
  TEXT = 'TEXT',
  LINK = 'LINK',
}

// 帖子分类枚举
export enum PostCategory {
  DISCUSSION = 'DISCUSSION',
  QUESTION = 'QUESTION',
  SHOWCASE = 'SHOWCASE',
  NEWS = 'NEWS',
  GUIDE = 'GUIDE',
}

// 成员信息 (在帖子中显示)
export interface MemberDto {
  memberId: string
  nickname: string
  reputation: number
  playerId: string | null  // 玩家UUID，用于获取皮肤
}

// 帖子 DTO (对应后端 PostDto)
export interface PostDto {
  postId?: string
  slug: string
  title: string
  createdAt: string
  memberPostBy: MemberDto
  numComments: number
  points: number
  text: string
  link: string
  type: PostType
  category: PostCategory
  wasUpvotedByMe: boolean | null
  wasDownvotedByMe: boolean | null
}

// 评论 DTO (对应后端 CommentDto)
export interface CommentDto {
  postSlug: string
  postTitle: string
  commentId: string
  parentCommentId: string | null
  text: string
  memberId: string
  memberNickname: string  // 用户昵称
  playerId: string | null  // 玩家UUID，用于获取皮肤
  memberPlayerSkin: string | null  // 用户头像皮肤 (base64) - 可能不再由后端返回
  createdAt: string
  childComments: CommentDto[]
  points: number
}

// ========== 请求/响应类型 ==========

// 创建帖子请求
export interface CreatePostRequest {
  userId: string
  title: string
  type: string
  text?: string
  link?: string
}

// 创建帖子响应
export interface CreatePostResponse {
  postId: string
  slug: string
  success: boolean
}

// 获取帖子列表响应 (Recent/Popular)
export interface GetPostsResponse {
  posts: PostDto[]
}

// 获取单个帖子响应
export interface GetPostResponse {
  post: PostDto
}

// 获取评论列表响应
export interface GetCommentsResponse {
  comments: CommentDto[]
}

// 投票请求
export interface VoteRequest {
  userId: string
}

// 投票响应
export interface VoteResponse {
  success: boolean
  newPoints: number
  points: number
}

// 回复帖子请求
export interface ReplyToPostRequest {
  userId: string
  comment: string
}

// 回复评论请求
export interface ReplyToCommentRequest {
  postSlug: string
  userId: string
  comment: string
}

// ========== 兼容旧类型 (逐步迁移) ==========

// 旧的 Post 类型 (用于过渡期)
export interface Post {
  id: string
  memberId: string
  slug: string
  title: string
  type: PostType
  text?: string
  link?: string
  comments: CommentDto[]
  votes: object
  totalNumComments: number
  points: number
  dateTimePosted: string
  member?: MemberDto
}

// 旧的 Comment 类型
export interface Comment {
  id: string
  memberId: string
  text: string
  postId: string
  parentCommentId?: string
  points: number
  votes: object
  createdAt: string
  member?: MemberDto
}
