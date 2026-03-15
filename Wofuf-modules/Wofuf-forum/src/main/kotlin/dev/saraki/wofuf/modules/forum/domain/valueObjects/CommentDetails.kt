package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.modules.forum.domain.CommentVotes
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.ValueObject
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 12:42
 *   @description:
 */
data class CommentDetailsProps(
    val commentId: CommentId,
    val text: String,
    val memberDetails: MemberDetails,
    val postSlug: PostSlug,
    val postTitle: PostTitle,
    val parentCommentId: CommentId?,
    val points: Int,
    val createdAt: LocalDateTime,
)

class CommentDetails private constructor(
    props: CommentDetailsProps
) : ValueObject<CommentDetailsProps>(props) {
    val commentId: CommentId
        get() = props.commentId

    val text: String
        get() = props.text

    val memberDetails: MemberDetails
        get() = props.memberDetails

    val postSlug: PostSlug
        get() = props.postSlug

    val postTitle: PostTitle
        get() = props.postTitle

    val parentCommentId: CommentId?
        get() = props.parentCommentId

    val points: Int
        get() = props.points

    val createdAt: LocalDateTime
        get() = props.createdAt

    companion object {
        fun create(props: CommentDetailsProps): Result<CommentDetails> {
             val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.text, "text"),
                    Guard.GuardArgument(props.memberDetails, "memberDetails"),
                    Guard.GuardArgument(props.postSlug, "postSlug"),
                    Guard.GuardArgument(props.postTitle, "postTitle"),
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return Result.success(CommentDetails(props))
        }
    }
}