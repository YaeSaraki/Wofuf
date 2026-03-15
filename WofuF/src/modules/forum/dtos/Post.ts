// 帖子数据传输对象
export interface Post {
  id: string
  memberId: string
  slug: string
  title: string
  type: PostType
  text?: string
  link?: string
  comments: Comment[]
  votes: PostVotes
  totalNumComments: number
  points: number
  dateTimePosted: string
  member?: Member // 添加member信息
}

export enum PostType {
  TEXT = 'TEXT',
  LINK = 'LINK',
}

export type PostVotes = object

export interface Comment {
  id: string
  memberId: string
  text: string
  postId: string
  parentCommentId?: string
  points: number
  votes: CommentVotes
  createdAt: string
  member?: Member // 添加member信息
}

export type CommentVotes = object

export interface Member {
  id: string
  userId: string
  playerId: string
  nickname: string
  reputation: number
}

export interface CreatePostRequest {
  userId: string
  title: string
  type: string
  text?: string
  link?: string
}

export interface CreatePostResponse {
  postId: string
  slug: string
  success: boolean
}

export interface GetPostsResponse {
  posts: Post[]
  total: number
}

export interface GetPostResponse {
  post: Post
}
