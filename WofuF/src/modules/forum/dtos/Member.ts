/**
 * 成员资料 DTO
 */
export interface MemberProfileDto {
  memberId: string
  userId: string
  playerId: string
  nickname: string
  reputation: number
  permissions: string[]
  isBanned: boolean
  bannedAt: string | null
  bannedUntil: string | null
  bannedReason: string | null
  bannedBy: string | null
  postHistory: PostSummary[]
  totalPosts: number
  commentCount: number
  joinedAt: string | null
}

export interface PostSummary {
  postId: string
  slug: string
  title: string
  category: string
  points: number
  numComments: number
  status: string
  isPinned: boolean
  isFeatured: boolean
  createdAt: string
}

export interface CommentSummary {
  commentId: string
  postId: string
  postSlug: string
  postTitle: string
  content: string
  createdAt: string
  points: number
}

export interface GetMemberCommentsResponse {
  comments: CommentSummary[]
  total: number
  page: number
  size: number
}

export interface UpdateNicknameRequest {
  memberId: string
  newNickname: string
}

export interface ApiResponse<T> {
  success: boolean
  code: string | null
  data: T | null
  message: string | null
}
