# Use Case Implementation Examples

This document provides real-world examples from the Wofuf codebase to illustrate proper use case implementation patterns.

## Complete Use Case Example: GetPlayer

### GetPlayerController.kt
```kotlin
package dev.saraki.wofuf.modules.players.useCases.getPlayer

import dev.saraki.wofuf.modules.players.config.PlayerApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PlayerApiConstantV1.Base.BY_NAME_OR_UUID)
class GetPlayerController(
    private val getPlayerUseCase: GetPlayerUseCase
) : BaseController() {

    @GetMapping
    fun getPlayerData(@PathVariable playerNameOrUuid: String): ApiResponse<GetPlayerDto.Response> {
        val result = getPlayerUseCase.execute(
            GetPlayerDto.Request(playerNameOrUuid)
        ).getOrThrow()
        return ApiResponse.success(GetPlayerDtoMap.from(result))
    }
}
```

### GetPlayerUseCase.kt
```kotlin
package dev.saraki.wofuf.modules.players.useCases.getPlayer

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class GetPlayerUseCase(private val playerRepository: PlayerRepo) : UseCase<GetPlayerDto.Request, Player> {
    override fun execute(request: GetPlayerDto.Request): Result<Player> {
        if (request.playerNameOrUuid.isBlank()) {
            return GetPlayerErrors.UserNameOrUuidEmptyError()
        }

        if (request.playerNameOrUuid.length >= 36) {
            val playerUuid = request.playerNameOrUuid
            val playerIdOrError = PlayerId.create(UniqueEntityId(playerUuid))
            if (playerIdOrError.isFailure) {
                return GetPlayerErrors.GetPlayerError()
            }

            val playerId = playerIdOrError.getOrThrow()
            val player = playerRepository.findByPlayerId(playerId)
                ?: return GetPlayerErrors.GetPlayerError()
            return Result.success(player)
        } else {
            val playerName = request.playerNameOrUuid
            val player = playerRepository.findByName(playerName)
                ?: return GetPlayerErrors.GetPlayerError()
            return Result.success(player)
        }
    }
}
```

### GetPlayerDto.kt
```kotlin
package dev.saraki.wofuf.modules.players.useCases.getPlayer

class GetPlayerDto {
    data class Request(
        val playerNameOrUuid: String,
    )

    data class Response(
        val id: String,
        val name: String,
        val firstLogin: Long,
        val lastLogin: Long,
        val totalPlaytimeSeconds: Long,
        val updateTime: Long
    )
}
```

### GetPlayerErrors.kt
```kotlin
package dev.saraki.wofuf.modules.players.useCases.getPlayer

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class GetPlayerErrors {

    // UserNameOrUuid empty
    class UserNameOrUuidEmptyError() : Result.Failure<Player>(
        exception = UseCaseError(
            code = " UserName_Or_Uuid_Empty_Error",
            message = "Failed to get player, username or uuid is empty"
        )
    )

    // Failed to get player
    class GetPlayerError() : Result.Failure<Player>(
        exception = UseCaseError(
            code = "GET_PLAYER_ERROR",
            message = "Failed to get player, player not found"
        )
    )
}
```

### GetPlayerDtoMap.kt
```kotlin
package dev.saraki.wofuf.modules.players.useCases.getPlayer

import dev.saraki.wofuf.modules.players.domain.Player

abstract class GetPlayerDtoMap {
    companion object {
        fun from(player: Player): GetPlayerDto.Response =
            GetPlayerDto.Response(
                id = player.playerId.stringValue,
                name = player.playerName.stringValue,
                firstLogin = player.firstLogin,
                lastLogin = player.lastLogin,
                totalPlaytimeSeconds = player.totalPlaytimeSeconds,
                updateTime = player.updateTime,
            )
    }
}
```

## Complete Use Case Example: ReplyToPost

### ReplyToPostController.kt
```kotlin
package dev.saraki.wofuf.modules.forum.useCases.posts.replyToPost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Reply to a post directly (not to a comment)
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.REPLIES)
class ReplyToPostController(
    private val replyToPostUseCase: ReplyToPostUseCase
) : BaseController() {

    @PostMapping
    fun replyToPost(
        @PathVariable postId: String,
        @RequestBody request: ReplyToPostRequest
    ): ApiResponse<ReplyToPostDto.Response> {
        val result = replyToPostUseCase.execute(
            ReplyToPostDto.Request(
                postId = postId,
                userId = request.userId,
                comment = request.comment,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class ReplyToPostRequest(
    val userId: String,
    val comment: String,
)
```

### ReplyToPostUseCase.kt
```kotlin
package dev.saraki.wofuf.modules.forum.useCases.posts.replyToPost

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import org.springframework.stereotype.Service

@Service
class ReplyToPostUseCase(
    private val memberRepo: MemberRepo,
    private val postRepo: PostRepo,
) : UseCase<ReplyToPostDto.Request, ReplyToPostDto.Response> {
    override fun execute(request: ReplyToPostDto.Request): Result<ReplyToPostDto.Response> {
        if (request.comment.isBlank()) {
            return ReplyToPostErrors.CommentTextEmptyError()
        }

        // Validate post ID
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return ReplyToPostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // Get post
        val post = postRepo.findPostByPostId(postId) ?: return ReplyToPostErrors.PostNotFoundError(request.postId)

        // Get member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return ReplyToPostErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId) ?: return ReplyToPostErrors.MemberNotFoundError(request.userId)

        // Validate comment text
        val commentTextOrError = CommentText.create(request.comment)
        if (commentTextOrError.isFailure) {
            return ReplyToPostErrors.CommentTextEmptyError()
        }
        val commentText = commentTextOrError.getOrThrow()

        // Add comment directly to post (no parent comment)
        val updatedPost = post.addComment(member.memberId, post.postId, commentText, null)
        if (updatedPost.isFailure) {
            return Result.failure(updatedPost.exceptionOrThrow())
        }

        // Save the updated post
        postRepo.save(updatedPost.getOrThrow())

        return Result.success(ReplyToPostDto.Response())
    }
}
```

### ReplyToPostDto.kt
```kotlin
package dev.saraki.wofuf.modules.forum.useCases.posts.replyToPost

class ReplyToPostDto {
    data class Request(
        val postId: String,
        val userId: String,
        val comment: String,
    )

    data class Response(
        val success: Boolean = true
    )
}
```

### ReplyToPostErrors.kt
```kotlin
package dev.saraki.wofuf.modules.forum.useCases.posts.replyToPost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class ReplyToPostErrors {

    // Comment text is empty
    class CommentTextEmptyError() : Result.Failure<ReplyToPostDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_TEXT_EMPTY",
            message = "Comment text cannot be empty"
        )
    )

    // Post not found
    class PostNotFoundError(val postId: String) : Result.Failure<ReplyToPostDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND",
            message = "Couldn't find a post by postId {$postId}"
        )
    )

    // Member not found
    class MemberNotFoundError(val userId: String) : Result.Failure<ReplyToPostDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by userId {$userId}"
        )
    )
}
```

## Complex Use Case Example: ReplyToComment

### ReplyToCommentController.kt
```kotlin
package dev.saraki.wofuf.modules.forum.useCases.comments.replyToComment

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ForumApiConstantV1.Comments.REPLIES)
class ReplyToCommentController(
    private val replyToCommentUseCase: ReplyToCommentUseCase
) : BaseController() {

    @PostMapping()
    fun replyToComment(
        @PathVariable parentCommentId: String,
        @RequestBody request: ReplyToCommentRequest
    ): ApiResponse<ReplyToCommentDto.Response> {
        val result = replyToCommentUseCase.execute(
            ReplyToCommentDto.Request(
                postSlug = request.postSlug,
                userId = request.userId,
                comment = request.comment,
                parentCommentId = parentCommentId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class ReplyToCommentRequest(
    val postSlug: String,
    val userId: String,
    val comment: String,
)
```

### ReplyToCommentUseCase.kt
```kotlin
package dev.saraki.wofuf.modules.forum.useCases.comments.replyToComment

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import org.springframework.stereotype.Service

@Service
class ReplyToCommentUseCase(
    private val memberRepo: MemberRepo,
    private val postRepo: PostRepo,
    private val commentRepo: CommentRepo,
) : UseCase<ReplyToCommentDto.Request, ReplyToCommentDto.Response> {
    override fun execute(request: ReplyToCommentDto.Request): Result<ReplyToCommentDto.Response> {
        if (request.postSlug.isBlank()) {
            return ReplyToCommentErrors.PostSlugEmptyError()
        }

        if (request.comment.isBlank()) {
            return ReplyToCommentErrors.CommentTextEmptyError()
        }

        // Validate post slug
        val postSlugOrError = PostSlug.createFromExisting(request.postSlug)
        if (postSlugOrError.isFailure) {
            return ReplyToCommentErrors.PostNotFoundError(request.postSlug)
        }
        val postSlug = postSlugOrError.getOrThrow()

        // Get post
        val post = postRepo.findPostBySlug(postSlug) ?: return ReplyToCommentErrors.PostNotFoundError(request.postSlug)

        // Get member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return ReplyToCommentErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId) ?: return ReplyToCommentErrors.MemberNotFoundError(request.userId)

        // Get parent comment
        val parentCommentIdOrError = CommentId.create(UniqueEntityId(request.parentCommentId))
        if (parentCommentIdOrError.isFailure) {
            return ReplyToCommentErrors.CommentNotFoundError(request.parentCommentId)
        }
        val parentCommentId = parentCommentIdOrError.getOrThrow()
        val parentComment = commentRepo.findCommentByCommentId(parentCommentId) ?: return ReplyToCommentErrors.CommentNotFoundError(request.parentCommentId)

        // Validate comment text
        val commentTextOrError = CommentText.create(request.comment)
        if (commentTextOrError.isFailure) {
            return ReplyToCommentErrors.CommentTextEmptyError()
        }
        val commentText = commentTextOrError.getOrThrow()

        // Add comment to post
        val updatedPost = post.addComment(member.memberId, post.postId, commentText, parentCommentId)
        if (updatedPost.isFailure) {
            return Result.failure(updatedPost.exceptionOrThrow())
        }

        // Save the updated post
        postRepo.save(updatedPost.getOrThrow())

        return Result.success(ReplyToCommentDto.Response())
    }
}
```

### ReplyToCommentDto.kt
```kotlin
package dev.saraki.wofuf.modules.forum.useCases.comments.replyToComment

class ReplyToCommentDto {
    data class Request(
        val postSlug: String,
        val userId: String,
        val comment: String,
        val parentCommentId: String,
    )

    data class Response(
        val success: Boolean = true
    )
}
```

### ReplyToCommentErrors.kt
```kotlin
package dev.saraki.wofuf.modules.forum.useCases.comments.replyToComment

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class ReplyToCommentErrors {

    // Post slug is empty
    class PostSlugEmptyError() : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "POST_SLUG_EMPTY",
            message = "Post slug cannot be empty"
        )
    )

    // Post not found
    class PostNotFoundError(val slug: String) : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND",
            message = "Couldn't find a post by slug {$slug}"
        )
    )

    // Comment not found
    class CommentNotFoundError(val commentId: String) : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_NOT_FOUND",
            message = "Couldn't find a comment by commentId {$commentId}"
        )
    )

    // Member not found
    class MemberNotFoundError(val userId: String) : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by userId {$userId}"
        )
    )

    // Comment text is empty
    class CommentTextEmptyError() : Result.Failure<ReplyToCommentDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_TEXT_EMPTY",
            message = "Comment text cannot be empty"
        )
    )
}
```

## Domain Layer Examples

### Player Aggregate (Domain Entity)
```kotlin
package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerAdvancement
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerName
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerSkin
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerStatistic
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId

data class PlayerProps(
    val playerName: PlayerName,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val updateTime: Long,
    val statistics: Map<String, PlayerStatistic>,
    val advancements: Map<String, PlayerAdvancement>,
    val playerSkin: PlayerSkin
)

class Player private constructor(
    props: PlayerProps,
    id: UniqueEntityId?,
) : AggregateRoot<PlayerProps>(props, id) {
    
    val playerId: PlayerId
        get() = PlayerId.create(_id).getOrThrow()

    val playerName: PlayerName
        get() = props.playerName

    val firstLogin: Long
        get() = props.firstLogin

    val lastLogin: Long
        get() = props.lastLogin

    val totalPlaytimeSeconds: Long
        get() = props.totalPlaytimeSeconds

    val updateTime: Long
        get() = props.updateTime

    val advancements: Map<String, PlayerAdvancement>
        get() = props.advancements

    val statistics: Map<String, PlayerStatistic>
        get() = props.statistics

    val playerSkin: PlayerSkin
        get() = props.playerSkin

    fun updateProps(props: PlayerProps): Result<Player> {
        return create(props, id)
    }

    companion object {
        fun create(
            props: PlayerProps,
            id: UniqueEntityId?
        ):
                Result<Player> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.playerName, "Player name cannot be null or blank")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            val defaultProps = props.copy(
                firstLogin = props.firstLogin,
                lastLogin = props.lastLogin,
                totalPlaytimeSeconds = props.totalPlaytimeSeconds,
                updateTime = props.updateTime,
                statistics = props.statistics,
                advancements = props.advancements,
                playerSkin = props.playerSkin,
            )
            val player = Player(defaultProps, id)
            return Result.success(player)
        }
    }
}
```

### PlayerId Value Object
```kotlin
package dev.saraki.wofuf.modules.players.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject

data class PlayerIdProps(
    val value: UniqueEntityId
)

class PlayerId private constructor(
    props: PlayerIdProps
) : ValueObject<PlayerIdProps>(props) {
    val stringValue: String
        get() = props.value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<PlayerId> {
            val guardResult = Guard.againstNullOrUndefined(value, "PlayerId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(PlayerId(PlayerIdProps(value)))
        }
    }
}
```

## Infrastructure Layer Examples

### Player Repository Interface
```kotlin
package dev.saraki.wofuf.modules.players.infra.repos

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId

interface PlayerRepo {
    fun findByPlayerId(playerId: PlayerId): Player?
    fun findByName(name: String): Player?
    fun findRandom(limit: Int = 1): List<Player>
    fun findYesterdayOnline(from: Long, to: Long): List<Player>
    fun countAll(): Long
    fun save(player: Player): Player
}
```

### Player Repository Implementation
```kotlin
package dev.saraki.wofuf.modules.players.infra.repos.impl

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.modules.players.infra.repos.jpa.PlayerJpaRepo
import dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers.PlayerEntityMapper
import org.springframework.stereotype.Repository

@Repository
class PlayerRepoImpl(
    private val playerJpaRepo: PlayerJpaRepo
) : PlayerRepo {

    override fun findByPlayerId(playerId: PlayerId): Player? {
        val id = playerId.stringValue
        return playerJpaRepo.findById(id)
            .map(PlayerEntityMapper::toDomain)
            .orElse(null)
    }

    override fun findByName(name: String): Player? =
        playerJpaRepo.findByPlayerName(name)
            ?.let(PlayerEntityMapper::toDomain)

    override fun findRandom(limit: Int): List<Player> =
        playerJpaRepo.findRandom(limit)
            .map(PlayerEntityMapper::toDomain)

    override fun findYesterdayOnline(from: Long, to: Long): List<Player> =
        playerJpaRepo.findYesterdayOnline(from, to)
            .map(PlayerEntityMapper::toDomain)

    override fun countAll(): Long =
        playerJpaRepo.count()

    override fun save(player: Player): Player {
        val entity = PlayerEntityMapper.toEntity(player)
        return PlayerEntityMapper.toDomain(playerJpaRepo.save(entity))
    }
}
```

### JPA Repository
```kotlin
package dev.saraki.wofuf.modules.players.infra.repos.jpa

import dev.saraki.wofuf.modules.players.infra.repos.jpa.entities.PlayerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PlayerJpaRepo : JpaRepository<PlayerEntity, String> {

    fun findByPlayerName(name: String): PlayerEntity?

    @Query(
        """
        SELECT p FROM PlayerEntity p
        ORDER BY RAND() 
        LIMIT :limit
    """
    )
    fun findRandom(limit: Int): List<PlayerEntity>

    @Query(
        """
        SELECT p FROM PlayerEntity p
        WHERE p.lastLogin BETWEEN :from AND :to
    """
    )
    fun findYesterdayOnline(from: Long, to: Long): List<PlayerEntity>
}
```

## Configuration Examples

### API Path Configuration
```kotlin
package dev.saraki.wofuf.modules.players.config

import dev.saraki.wofuf.shared.config.ApiConstantV1

object PlayerApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/players"
    
    object Param {
        const val PLAYER_UUID = "playerUuid"
        const val PLAYER_NAME_OR_UUID = "playerNameOrUuid"
    }
    
    object Base {
        const val ROOT = BASE
        const val BY_UUID = "$BASE/{${Param.PLAYER_UUID}}"
        const val BY_NAME_OR_UUID = "$BASE/playerNameOrUuid/{${Param.PLAYER_NAME_OR_UUID}}"
    }
}
```

### Forum API Configuration
```kotlin
package dev.saraki.wofuf.modules.forum.config

import dev.saraki.wofuf.shared.config.ApiConstantV1

object ForumApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/forum"

    // ===================== 路径参数 =====================
    object Param {
        const val POST_ID = "postId"
        const val COMMENT_ID = "commentId"
        const val MEMBER_ID = "memberId"
    }

    // ===================== 基础路径 =====================
    object Base {
        /** /api/v1/forum */
        const val ROOT = BASE
    }

    // ===================== 成员相关路径 =====================
    object Members {
        /** /api/v1/forum/members */
        const val ROOT = "$BASE/members"

        /** /api/v1/forum/members/{memberId} */
        const val BY_ID = "$ROOT/{${Param.MEMBER_ID}}"
    }

    // ===================== 帖子相关路径 =====================
    object Posts {
        /** /api/v1/forum/posts */
        const val ROOT = "$BASE/posts"

        /** /api/v1/forum/posts/{postId} */
        const val BY_ID = "$ROOT/{${Param.POST_ID}}"

        /** /api/v1/forum/posts/{postId}/likes */
        const val LIKES = "$BY_ID/likes"

        /** /api/v1/forum/posts/{postId}/comments */
        const val COMMENTS = "$BY_ID/comments"
    }

    // ===================== 评论相关路径 =====================
    object Comments {
        /** /api/v1/forum/comments */
        const val ROOT = "$BASE/comments"

        /** /api/v1/forum/comments/{commentId} */
        const val BY_ID = "$ROOT/{${Param.COMMENT_ID}}"

        /** /api/v1/forum/comments/post/{postSlug} */
        const val BY_POST_SLUG = "$ROOT/post/{postSlug}"

        /** /api/v1/forum/comments/{commentId}/replies */
        const val REPLIES = "$BY_ID/replies"
    }

    // ===================== 工具方法 =====================
    /**
     * 构建单个帖子路径
     * @param postId 帖子ID
     * @return 完整路径，如 "/api/v1/forum/posts/1001"
     */
    fun buildPostPath(postId: String): String {
        return Posts.BY_ID.replace("{${Param.POST_ID}}", postId)
    }

    /**
     * 构建帖子点赞路径
     * @param postId 帖子ID
     * @return 完整路径，如 "/api/v1/forum/posts/1001/likes"
     */
    fun buildPostLikesPath(postId: String): String {
        return Posts.LIKES.replace("{${Param.POST_ID}}", postId)
    }

    /**
     * 构建单个评论路径
     * @param commentId 评论ID
     * @return 完整路径，如 "/api/v1/forum/comments/2001"
     */
    fun buildCommentPath(commentId: String): String {
        return Comments.BY_ID.replace("{${Param.COMMENT_ID}}", commentId)
    }

    /**
     * 构建帖子下的评论路径
     * @param postId 帖子ID
     * @return 完整路径，如 "/api/v1/forum/posts/1001/comments"
     */
    fun buildPostCommentsPath(postId: String): String {
        return Posts.COMMENTS.replace("{${Param.POST_ID}}", postId)
    }

    /**
     * 构建单个成员路径
     * @param memberId 成员ID
     * @return 完整路径，如 "/api/v1/forum/members/3001"
     */
    fun buildMemberPath(memberId: String): String {
        return Members.BY_ID.replace("{${Param.MEMBER_ID}}", memberId)
    }

    /**
     * 构建通过帖子slug获取评论的路径
     * @param postSlug 帖子slug
     * @return 完整路径，如 "/api/v1/forum/comments/post/my-post-slug-1234567"
     */
    fun buildCommentsByPostSlugPath(postSlug: String): String {
        return Comments.BY_POST_SLUG.replace("{postSlug}", postSlug)
    }

    /**
     * 构建回复评论的路径
     * @param commentId 评论ID
     * @return 完整路径，如 "/api/v1/forum/comments/2001/replies"
     */
    fun buildCommentRepliesPath(commentId: String): String {
        return Comments.REPLIES.replace("{${Param.COMMENT_ID}}", commentId)
    }
}
```

## Testing Examples

### Unit Test Example
```kotlin
package dev.saraki.wofuf.modules.players.useCases.getPlayer

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.shared.core.Result
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetPlayerUseCaseTest {

    private val playerRepo = mockk<PlayerRepo>()
    private val useCase = GetPlayerUseCase(playerRepo)

    @Test
    fun `should return player when found by name`() {
        // Given
        val playerName = "testPlayer"
        val expectedPlayer = mockk<Player>()
        every { playerRepo.findByName(playerName) } returns expectedPlayer

        // When
        val result = useCase.execute(GetPlayerDto.Request(playerName))

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedPlayer, result.getOrThrow())
    }

    @Test
    fun `should return error when player name is blank`() {
        // Given
        val blankName = ""

        // When
        val result = useCase.execute(GetPlayerDto.Request(blankName))

        // Then
        assertTrue(result.isFailure)
        assertEquals(" UserName_Or_Uuid_Empty_Error", result.exceptionOrThrow().code)
    }
}
```

These examples demonstrate the complete implementation patterns used throughout the Wofuf codebase. Each example shows the proper structure, naming conventions, error handling, and integration patterns that AI agents should follow when generating new code.
