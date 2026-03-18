package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostType
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
    var points: Int,
    val dateTimePosted: LocalDateTime
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

    /**
     * 更新基础积分（总点赞-总点踩）
     * 用于从持久化层加载最新基础积分
     */
    fun updateScore(totalNumUpvotes: Int, totalNumDownvotes: Int) {
        props.points = totalNumUpvotes - totalNumDownvotes
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
            dateTimePosted = props.dateTimePosted
        )
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
