package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostStatus
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostType
import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 14:03
 *   @description: Post aggregate root entity
 */

data class PostProps(
    val memberId: MemberId,
    val slug: PostSlug,
    val title: PostTitle,
    val type: PostType,
    val category: PostCategory = PostCategory.DISCUSSION,
    val text: PostText?,
    val link: PostLink?,
    val totalNumComments: Int?,
    val points: Int,
    val dateTimePosted: LocalDateTime,
    // 管理功能相关字段
    val status: PostStatus = PostStatus.NORMAL,
    val isPinned: Boolean = false,
    val isFeatured: Boolean = false,
    val pinnedAt: LocalDateTime? = null,
    val featuredAt: LocalDateTime? = null,
    val hiddenAt: LocalDateTime? = null,
    val hiddenBy: MemberId? = null
)

class Post private constructor(
    props: PostProps,
    id: UniqueEntityId?
) : AggregateRoot<PostProps>(props, id) {

    val postId: PostId
        get() = PostId.create(_id).getOrThrow()

    val memberId: MemberId
        get() = props.memberId

    val slug: PostSlug
        get() = props.slug

    val title: PostTitle
        get() = props.title

    val type: PostType
        get() = props.type

    val category: PostCategory
        get() = props.category

    val text: PostText?
        get() = props.text

    val link: PostLink?
        get() = props.link

    val totalNumComments: Int?
        get() = props.totalNumComments

    val points: Int
        get() = props.points

    val dateTimePosted: LocalDateTime
        get() = props.dateTimePosted

    // 管理功能相关属性
    val status: PostStatus
        get() = props.status

    val isPinned: Boolean
        get() = props.isPinned

    val isFeatured: Boolean
        get() = props.isFeatured

    val pinnedAt: LocalDateTime?
        get() = props.pinnedAt

    val featuredAt: LocalDateTime?
        get() = props.featuredAt

    val hiddenAt: LocalDateTime?
        get() = props.hiddenAt

    val hiddenBy: MemberId?
        get() = props.hiddenBy

    /**
     * 更新基础积分（总点赞-总点踩）
     * 用于从持久化层加载最新基础积分
     * 返回新的 Post 实例（不可变模式）
     */
    fun updateScore(totalNumUpvotes: Int, totalNumDownvotes: Int): Post {
        val newProps = props.copy(points = totalNumUpvotes - totalNumDownvotes)
        return Post.create(newProps, _id).getOrThrow()
    }

    fun edit(
        title: PostTitle? = null,
        text: PostText? = null,
        link: PostLink? = null,
        category: PostCategory? = null
    ): Result<Post> {
        val newProps = PostProps(
            memberId = props.memberId,
            slug = props.slug,
            title = title ?: props.title,
            type = props.type,
            category = category ?: props.category,
            text = text ?: props.text,
            link = link ?: props.link,
            totalNumComments = props.totalNumComments,
            points = props.points,
            dateTimePosted = props.dateTimePosted,
            status = props.status,
            isPinned = props.isPinned,
            isFeatured = props.isFeatured,
            pinnedAt = props.pinnedAt,
            featuredAt = props.featuredAt,
            hiddenAt = props.hiddenAt,
            hiddenBy = props.hiddenBy
        )
        return Post.create(newProps, _id)
    }

    // ==================== 管理功能方法 ====================

    /**
     * 置顶帖子
     */
    fun pin(): Result<Post> {
        if (props.isPinned) {
            return Result.failure(AppError("帖子已被置顶"))
        }
        val newProps = props.copy(
            isPinned = true,
            pinnedAt = LocalDateTime.now()
        )
        return Post.create(newProps, _id)
    }

    /**
     * 取消置顶
     */
    fun unpin(): Result<Post> {
        if (!props.isPinned) {
            return Result.failure(AppError("帖子未被置顶"))
        }
        val newProps = props.copy(
            isPinned = false,
            pinnedAt = null
        )
        return Post.create(newProps, _id)
    }

    /**
     * 加精帖子
     */
    fun feature(): Result<Post> {
        if (props.isFeatured) {
            return Result.failure(AppError("帖子已被加精"))
        }
        val newProps = props.copy(
            isFeatured = true,
            featuredAt = LocalDateTime.now()
        )
        return Post.create(newProps, _id)
    }

    /**
     * 取消加精
     */
    fun unfeature(): Result<Post> {
        if (!props.isFeatured) {
            return Result.failure(AppError("帖子未被加精"))
        }
        val newProps = props.copy(
            isFeatured = false,
            featuredAt = null
        )
        return Post.create(newProps, _id)
    }

    /**
     * 隐藏帖子
     */
    fun hide(hiddenBy: MemberId): Result<Post> {
        if (props.status == PostStatus.HIDDEN) {
            return Result.failure(AppError("帖子已被隐藏"))
        }
        val newProps = props.copy(
            status = PostStatus.HIDDEN,
            hiddenAt = LocalDateTime.now(),
            hiddenBy = hiddenBy
        )
        return Post.create(newProps, _id)
    }

    /**
     * 显示帖子（取消隐藏）
     */
    fun show(): Result<Post> {
        if (props.status != PostStatus.HIDDEN) {
            return Result.failure(AppError("帖子未被隐藏"))
        }
        val newProps = props.copy(
            status = PostStatus.NORMAL,
            hiddenAt = null,
            hiddenBy = null
        )
        return Post.create(newProps, _id)
    }

    /**
     * 设置审核中状态
     */
    fun setUnderReview(): Result<Post> {
        if (props.status == PostStatus.UNDER_REVIEW) {
            return Result.failure(AppError("帖子已在审核中"))
        }
        val newProps = props.copy(status = PostStatus.UNDER_REVIEW)
        return Post.create(newProps, _id)
    }

    /**
     * 通过审核
     */
    fun approve(): Result<Post> {
        if (props.status != PostStatus.UNDER_REVIEW) {
            return Result.failure(AppError("帖子不在审核中"))
        }
        val newProps = props.copy(status = PostStatus.NORMAL)
        return Post.create(newProps, _id)
    }

    companion object {
        fun create(props: PostProps, id: UniqueEntityId? = null): Result<Post> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.memberId, "memberId"),
                    Guard.GuardArgument(props.slug, "slug"),
                    Guard.GuardArgument(props.title, "title"),
                    Guard.GuardArgument(props.type, "type"),
                    Guard.GuardArgument(props.dateTimePosted, "dateTimePosted")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            return Result.success(Post(props, id))
        }
    }
}
