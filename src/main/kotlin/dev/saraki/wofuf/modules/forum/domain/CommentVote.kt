package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.Entity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 13:11
 *   @description:
 */
data class CommentVoteProps(
    val commentId: CommentId,
    val memberId: MemberId,
    val type: VoteType,
)

class CommentVote private constructor(
    props: CommentVoteProps,
    id: UniqueEntityId? = null
) : Entity<CommentVoteProps>(props, id) {
    val commentId: CommentId get() = props.commentId
    val memberId: MemberId get() = props.memberId
    val type: VoteType get() = props.type

    fun isUpVote(): Boolean {
        return type == VoteType.UPVOTE
    }

    fun isDownVote(): Boolean {
        return type == VoteType.DOWNVOTE
    }

    companion object {
        fun create(
            commentId: CommentId,
            memberId: MemberId,
            type: VoteType
        ): Result<CommentVote> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(commentId, "commentId"),
                    Guard.GuardArgument(memberId, "memberId"),
                    Guard.GuardArgument(type, "type")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(CommentVote(CommentVoteProps(commentId, memberId, type)))
        }

        fun createUpvote(
            commentId: CommentId,
            memberId: MemberId
        ): Result<CommentVote> {
            val memberGuard = Guard.againstNullOrUndefined(memberId, "memberId")
            val commentGuard = Guard.againstNullOrUndefined(commentId, "commentId")

            if (memberGuard.isFailure || commentGuard.isFailure) {
                return Result.failure(memberGuard.exceptionOrNull() ?: commentGuard.exceptionOrNull()!!)
            }
            return create(commentId, memberId, VoteType.UPVOTE)
        }

        fun createDownvote(
            commentId: CommentId,
            memberId: MemberId
        ): Result<CommentVote> {
            val memberGuard = Guard.againstNullOrUndefined(memberId, "memberId")
            val commentGuard = Guard.againstNullOrUndefined(commentId, "commentId")

            if (memberGuard.isFailure || commentGuard.isFailure) {
                return Result.failure(memberGuard.exceptionOrNull() ?: commentGuard.exceptionOrNull()!!)
            }
            return create(commentId, memberId, VoteType.DOWNVOTE)
        }
    }
}
