# 投票领域服务重构记录

## 重构完成日期
2026-03-18

## 概述

本文档记录投票领域服务重构的实际执行过程，遵循 DDD 原则，将投票逻辑从聚合内部迁移到独立的领域服务。

## 重构原因

### 原有设计问题

1. **聚合边界不清晰**: `Post` 和 `Comment` 持有 `votes` 集合，违反了 DDD 聚合边界原则
2. **投票实体身份不明**: `PostVote` 和 `CommentVote` 作为 `Entity` 而非 `AggregateRoot`，但拥有独立生命周期
3. **潜在并发问题**: 投票操作直接修改聚合内部集合，存在竞态条件风险

### 目标架构

```
┌─────────────────────┐     ┌─────────────────────┐     ┌─────────────────────┐
│    Post 聚合根       │     │   PostVote 聚合      │     │    Member 聚合       │
├─────────────────────┤     ├─────────────────────┤     ├─────────────────────┤
│ - postId            │     │ - voteId            │     │ - memberId          │
│ - memberId (引用)   │     │ - postId (引用)     │     │ - name              │
│ - title             │     │ - memberId (引用)   │     │ - ...               │
│ - slug              │     │ - voteType          │     └─────────────────────┘
│ - points (冗余)     │     └─────────────────────┘
└─────────────────────┘              │
         ▲                           │
         │         领域服务           │
         │    PostVoteDomainService   │
         │    (跨聚合协调)            │
         └───────────────────────────┘
```

## 实际重构步骤

### 步骤 1: 创建/更新值对象

#### VoteStatus (已存在)
文件: `domain/valueObjects/VoteStatus.kt`

```kotlin
data class VoteStatus(
    val wasUpvotedByMe: Boolean,
    val wasDownvotedByMe: Boolean
) {
    companion object {
        fun empty() = VoteStatus(false, false)
    }
}
```

#### VoteResult (新建)
文件: `domain/valueObjects/VoteResult.kt`

```kotlin
data class VoteResult(
    val newPoints: Int,
    val entityId: String
)
```

### 步骤 2: 更新领域服务接口

#### PostVoteDomainService
文件: `domain/services/PostVoteDomainService.kt`

```kotlin
interface PostVoteDomainService {
    fun getVoteStatus(postId: PostId, memberId: MemberId): VoteStatus
    fun getVoteStatuses(postIds: List<PostId>, memberId: MemberId): Map<PostId, VoteStatus>
    fun upvote(postId: PostId, memberId: MemberId): Result<VoteResult>
    fun downvote(postId: PostId, memberId: MemberId): Result<VoteResult>
}
```

#### CommentVoteDomainService
文件: `domain/services/CommentVoteDomainService.kt`

```kotlin
interface CommentVoteDomainService {
    fun getVoteStatus(commentId: CommentId, memberId: MemberId): VoteStatus
    fun getVoteStatuses(commentIds: List<CommentId>, memberId: MemberId): Map<CommentId, VoteStatus>
    fun upvote(commentId: CommentId, memberId: MemberId): Result<VoteResult>
    fun downvote(commentId: CommentId, memberId: MemberId): Result<VoteResult>
}
```

### 步骤 3: 实现领域服务

#### PostVoteDomainServiceImpl
文件: `domain/services/PostVoteDomainServiceImpl.kt`

关键实现点:
- 使用 `@Transactional` 确保投票操作的原子性
- Toggle 逻辑：已点赞再点击则取消
- 批量查询优化：避免 N+1 查询
- 积分实时计算：从数据库统计而非内存计算

#### CommentVoteDomainServiceImpl
文件: `domain/services/CommentVoteDomainServiceImpl.kt`

实现逻辑与 PostVoteDomainServiceImpl 类似。

### 步骤 4: 重构聚合实体

#### Post 聚合重构
文件: `domain/Post.kt`

**移除内容:**
- `votes: PostVotes` 属性
- `comments: Comments` 属性
- `addVote()`, `removeVote()` 方法
- `hasUpvotedBy()`, `hasDownvotedBy()` 方法
- `getVoteByMember()` 方法
- `computeVotePoints()` 私有方法

**保留内容:**
- `points: Int` 属性（冗余存储，由领域服务更新）
- `updateScore()` 方法（供领域服务调用）

#### Comment 聚合重构
文件: `domain/Comment.kt`

**移除内容:**
- `votes: CommentVotes` 属性
- `addVote()`, `removeVote()` 方法
- `getVotes()` 方法
- `computeVotePoints()` 私有方法

**保留内容:**
- `points: Int?` 属性
- `updateScore()` 方法

### 步骤 5: 更新 Entity Mapper

#### PostEntityMapper
文件: `infra/repos/jpa/mappers/PostEntityMapper.kt`

移除 `toDomain()` 中对 `comments` 和 `votes` 的初始化。

#### CommentEntityMapper
文件: `infra/repos/jpa/mappers/CommentEntityMapper.kt`

移除 `toDomain()` 中对 `votes` 的初始化。

### 步骤 6: 更新 Repository 实现

#### PostRepoImpl
文件: `infra/repos/impl/PostRepoImpl.kt`

- 移除 `PostVotesRepo` 依赖
- 移除 `save()` 方法中对 `votes.saveBulk()` 的调用

#### CommentRepoImpl
文件: `infra/repos/impl/CommentRepoImpl.kt`

- 移除 `CommentVotesRepo` 依赖
- 移除 `save()` 和 `saveBulk()` 方法中对 votes 的保存

### 步骤 7: 重构 UseCase

#### UpvotePostUseCase
文件: `useCases/posts/upvotePost/UpvotePostUseCase.kt`

```kotlin
@Service
class UpvotePostUseCase(
    private val memberRepo: MemberRepo,
    private val postVoteDomainService: PostVoteDomainService,
) : UseCase<UpvotePostDto.Request, UpvotePostDto.Response> {
    override fun execute(request: UpvotePostDto.Request): Result<UpvotePostDto.Response> {
        // 验证输入
        // 获取 Member
        // 使用领域服务处理投票
        val result = postVoteDomainService.upvote(postId, member.memberId)
        // 返回结果
    }
}
```

#### 其他重构的 UseCase:
- `DownvotePostUseCase` - 使用领域服务
- `UpvoteCommentUseCase` - 使用领域服务
- `DownvoteCommentUseCase` - 使用领域服务
- `GetRecentPostsUseCase` - 批量获取投票状态
- `GetPopularPostsUseCase` - 批量获取投票状态
- `GetPostBySlugUseCase` - 获取单个帖子投票状态
- `CreatePostUseCase` - 移除对 votes 的初始化
- `ReplyToPostUseCase` - 移除对 votes 的初始化

## 文件变更清单

### 新建文件
| 文件路径 | 说明 |
|---------|------|
| `domain/valueObjects/VoteResult.kt` | 投票结果值对象 |

### 修改文件
| 文件路径 | 变更内容 |
|---------|---------|
| `domain/services/PostVoteDomainService.kt` | 添加 upvote/downvote 方法 |
| `domain/services/PostVoteDomainServiceImpl.kt` | 实现 upvote/downvote 方法 |
| `domain/services/CommentVoteDomainService.kt` | 添加 upvote/downvote 方法 |
| `domain/services/CommentVoteDomainServiceImpl.kt` | 实现 upvote/downvote 方法 |
| `domain/Post.kt` | 移除 votes/comments 集合及相关方法 |
| `domain/Comment.kt` | 移除 votes 集合及相关方法 |
| `infra/repos/jpa/mappers/PostEntityMapper.kt` | 移除 votes/comments 初始化 |
| `infra/repos/jpa/mappers/CommentEntityMapper.kt` | 移除 votes 初始化 |
| `infra/repos/impl/PostRepoImpl.kt` | 移除 votes 保存逻辑 |
| `infra/repos/impl/CommentRepoImpl.kt` | 移除 votes 保存逻辑 |
| `useCases/posts/upvotePost/UpvotePostUseCase.kt` | 使用领域服务 |
| `useCases/posts/downvotePost/DownvotePostUseCase.kt` | 使用领域服务 |
| `useCases/comments/upvoteComment/UpvoteCommentUseCase.kt` | 使用领域服务 |
| `useCases/comments/downvoteComment/DownvoteCommentUseCase.kt` | 使用领域服务 |
| `useCases/posts/getRecentPosts/GetRecentPostsUseCase.kt` | 批量获取投票状态 |
| `useCases/posts/getPopularPosts/GetPopularPostsUseCase.kt` | 批量获取投票状态 |
| `useCases/posts/getPostBySlug/GetPostBySlugUseCase.kt` | 获取投票状态 |
| `useCases/posts/createPost/CreatePostUseCase.kt` | 移除 votes 初始化 |
| `useCases/comments/replyToPost/ReplyToPostUseCase.kt` | 移除 votes 初始化 |

### 保留但不再使用的文件
| 文件路径 | 说明 |
|---------|------|
| `domain/PostVotes.kt` | WatchedList 实现，保留作为历史参考 |
| `domain/CommentVotes.kt` | WatchedList 实现，保留作为历史参考 |

## 性能优化效果

### N+1 查询问题解决

**优化前:**
```kotlin
// 对每个帖子单独查询 - N+1 问题
posts.map { post ->
    val wasUpvoted = postVotesRepo.exists(post.postId, memberId, VoteType.UPVOTE)
    val wasDownvoted = postVotesRepo.exists(post.postId, memberId, VoteType.DOWNVOTE)
}
```

**优化后:**
```kotlin
// 批量查询 - 一次数据库访问
val voteStatusMap = postVoteDomainService.getVoteStatuses(
    posts.map { it.postId },
    memberId
)
posts.map { post ->
    val voteStatus = voteStatusMap[post.postId] ?: VoteStatus.empty()
}
```

## 后续优化建议

1. **并发控制**: 添加乐观锁 (`@Version`) 或使用数据库唯一约束处理并发投票
2. **缓存策略**: 投票状态可以使用 Redis 缓存
3. **事件发布**: 投票完成后发布领域事件通知其他模块
4. **异步处理**: 积分更新可以考虑异步处理

## 测试建议

1. 领域服务单元测试
2. Toggle 逻辑测试（点赞->取消->点踩->取消）
3. 并发投票测试
4. 批量查询性能测试
