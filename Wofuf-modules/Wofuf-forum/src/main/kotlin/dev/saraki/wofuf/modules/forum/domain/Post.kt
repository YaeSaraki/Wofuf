package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
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
 *   @description:
 */
data class PostProps(
    val memberId: MemberId,
    val slug: PostSlug,
    val title: PostTitle,
    val type: PostType,
    val text: PostText?,
    val link: PostLink?,
    val comments: Comments,
    val votes: PostVotes,
    val totalNumComments: Int?,
    val points: Int,
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

    val text: PostText?
        get() = props.text

    val link: PostLink?
        get() = props.link

    val comments: Comments
        get() = props.comments

    val votes: PostVotes
        get() = props.votes

    val totalNumComments: Int?
        get() = props.totalNumComments

    val points: Int
        get() = props.points

    val dateTimePosted: LocalDateTime
        get() = props.dateTimePosted

    fun addComment(memberId: MemberId, postId: PostId, text: CommentText, parentCommentId: CommentId?): Result<Post> {
        val commentProps = CommentProps(
            postId = postId,
            text = text,
            memberId = memberId,
            parentCommentId = parentCommentId,
            points = 0,
            votes = CommentVotes.create(),
        )
        val comment = Comment.create(commentProps).getOrThrow()
        this.comments.add(comment)
        return Result.success(this)
    }

    companion object {
        fun create(props: PostProps, id: UniqueEntityId? = null): Result<Post> {
            Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.memberId, "memberId"),
                    Guard.GuardArgument(props.slug, "slug"),
                    Guard.GuardArgument(props.title, "title"),
                    Guard.GuardArgument(props.type, "type"),
                )
            )
            return Result.success(Post(props, id))
        }


    }
}
