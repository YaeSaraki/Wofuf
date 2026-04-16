/**
 * 管理后台 DTO 定义
 */

// ==================== 权限点 ====================
export type PermissionPoint =
  | 'POST_PIN'
  | 'POST_FEATURE'
  | 'POST_HIDE'
  | 'POST_REVIEW'
  | 'POST_DELETE_ANY'
  | 'COMMENT_DELETE_ANY'
  | 'COMMENT_VIEW_HIDDEN'
  | 'CATEGORY_MANAGE'
  | 'USER_BAN'
  | 'USER_VIEW_BANNED'
  | 'VIEW_MEMBER_PROFILES'
  | 'ADMIN_ACCESS'
  | 'PERMISSION_GRANT'

// ==================== 帖子状态 ====================
export type PostStatus = 'NORMAL' | 'HIDDEN' | 'UNDER_REVIEW'

// ==================== 帖子管理 ====================
export interface PostActionResponse {
  postId: string
  isPinned?: boolean
  isFeatured?: boolean
  status?: string
  isHidden?: boolean
  message: string
}

export interface PostForReview {
  postId: string
  title: string
  status: string
  dateTimePosted: number
  authorId: string
}

export interface GetPostsForReviewResponse {
  posts: PostForReview[]
  total: number
  page: number
  size: number
}

// ==================== 评论管理 ====================
export interface CommentSummary {
  commentId: string
  postId: string
  postSlug: string
  content: string
  isHidden: boolean
  hiddenAt: number | null
  hiddenBy: string | null
  authorId: string
  authorNickname: string
  createdAt: number
}

export interface HiddenComment {
  commentId: string
  postId: string
  content: string
  isHidden: boolean
  hiddenAt: number | null
  hiddenBy: string | null
  authorId: string
}

export interface GetHiddenCommentsResponse {
  comments: HiddenComment[]
  total: number
  page: number
  size: number
}

export interface GetCommentsResponse {
  comments: CommentSummary[]
  total: number
  page: number
  size: number
}

export interface CommentActionResponse {
  commentId: string
  isHidden: boolean
  message: string
}

// 批量操作单个结果
export interface BatchCommentResult {
  commentId: string
  isHidden: boolean
  success: boolean
  message: string
}

// 批量隐藏/显示评论响应
export interface BatchCommentActionResponse {
  successCount: number
  failCount: number
  results: BatchCommentResult[]
  message: string
}

// ==================== 成员管理 ====================
export interface BannedMember {
  memberId: string
  nickname: string
  isBanned: boolean
  bannedAt: number | null
  bannedUntil: number | null
  bannedReason: string | null
  bannedBy: string | null
}

export interface GetBannedMembersResponse {
  members: BannedMember[]
  total: number
  page: number
  size: number
}

export interface MemberActionResponse {
  memberId: string
  isBanned?: boolean
  bannedUntil?: string | null
  permission?: string
  granted?: boolean
  revoked?: boolean
  message: string
}

export interface MemberPostSummary {
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

export interface MemberSummary {
  memberId: string
  userId: string
  playerId: string
  nickname: string
  reputation: number
  isBanned: boolean
  permissions: string[]
}

export interface GetMembersListResponse {
  members: MemberSummary[]
  total: number
  page: number
  size: number
}

export interface MemberProfile {
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
  postHistory: MemberPostSummary[]
  totalPosts: number
}

// ==================== 统计数据 ====================
export interface AdminStats {
  totalPosts: number
  totalComments: number
  totalMembers: number
  pendingReview: number
  hiddenPosts: number
  hiddenComments: number
  bannedMembers: number
}

// ==================== 图片管理 ====================
export interface ImageSummary {
  imageId: string
  objectName: string
  md5: string
  folder: string
  uploaderMemberId: string | null
  uploaderNickname: string | null
  uploadedAt: number
  fileSize: number
  contentType: string
  fileName: string
}

export interface GetImagesResponse {
  images: ImageSummary[]
  total: number
  page: number
  size: number
}

export interface DeleteImageResponse {
  success: boolean
  message: string
}

export interface GetImageUrlResponse {
  url: string
  expiresInSeconds: number
}

// ==================== 操作日志 ====================
export type OperationType =
  | 'POST_PIN'
  | 'POST_UNPIN'
  | 'POST_FEATURE'
  | 'POST_UNFEATURE'
  | 'POST_HIDE'
  | 'POST_SHOW'
  | 'POST_SET_REVIEW'
  | 'POST_APPROVE'
  | 'POST_DELETE'
  | 'POST_EDIT'
  | 'COMMENT_HIDE'
  | 'COMMENT_SHOW'
  | 'COMMENT_DELETE'
  | 'COMMENT_EDIT'
  | 'MEMBER_BAN'
  | 'MEMBER_UNBAN'
  | 'MEMBER_GRANT_PERMISSION'
  | 'MEMBER_REVOKE_PERMISSION'
  | 'IMAGE_DELETE'
  | 'UNKNOWN'

export type TargetType = 'POST' | 'COMMENT' | 'MEMBER' | 'IMAGE'

export interface OperationLogEntry {
  logId: string
  operationType: OperationType
  operationName: string
  targetType: TargetType
  targetId: string
  operatorId: string
  operatorNickname: string | null
  details: string | null
  createdAt: number
}

export interface GetOperationLogsResponse {
  logs: OperationLogEntry[]
  total: number
  page: number
  size: number
  totalPages: number
}
