package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.ValueObject
import java.time.LocalDateTime


/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 22:42
 *   @description:
 */
data class CommentDetailsProps(
    val commentId: CommentId,
    val text: CommentText,
    val member: MemberDetails,
    val createdAt: LocalDateTime,
    val postSlug: PostSlug,
    val parentCommentId: CommentId? = null,
    val points: Int = 0,
    val wasUpvotedByMe: Boolean?,
    val wasDownvotedByMe: Boolean?,
)

class CommentDetails private constructor(
    props: CommentDetailsProps,
) : ValueObject<CommentDetailsProps>(props) {

    val commentId: CommentId
        get() = props.commentId

    val text: CommentText
        get() = props.text

    val member: MemberDetails
        get() = props.member

    val createdAt: LocalDateTime
        get() = props.createdAt

    val postSlug: PostSlug
        get() = props.postSlug

    val parentCommentId: CommentId?
        get() = props.parentCommentId

    val points: Int
        get() = props.points
    val wasUpvotedByMe: Boolean?
        get() = props.wasUpvotedByMe

    val wasDownvotedByMe: Boolean?
        get() = props.wasDownvotedByMe

    companion object {
        fun create(props: CommentDetailsProps): Result<CommentDetails> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.commentId, "commentId"),
                    Guard.GuardArgument(props.text, "text"),
                    Guard.GuardArgument(props.member, "member"),
                    Guard.GuardArgument(props.postSlug, "postSlug"),
                )
            )
            if (guardResult.isFailure) {
                guardResult.exceptionOrThrow()
            }
            return Result.success(CommentDetails(props))
        }
    }
}

