package dev.saraki.wofuf.modules.forum.domain.valueObjects

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
    val rootCommentId: CommentId?,  // 所属主评论ID（用于Bilibili风格）
    val shortId: String?,           // 短 ID（用于显示和引用）
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

    /**
     * 所属主评论ID（用于Bilibili风格评论）
     * 主评论返回 null
     * 子评论返回所属主评论的ID
     */
    val rootCommentId: CommentId?
        get() = props.rootCommentId

    /**
     * 短 ID（用于显示和引用）
     */
    val shortId: String?
        get() = props.shortId

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