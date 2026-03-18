package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentVoteId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 13:11
 *   @description: CommentVote aggregate root entity
 */

data class CommentVoteProps(
    val commentId: CommentId,
    val memberId: MemberId,
    var voteType: VoteType
)

class CommentVote private constructor(
    props: CommentVoteProps,
    id: UniqueEntityId?
) : AggregateRoot<CommentVoteProps>(props, id) {

    val commentVoteId: CommentVoteId
        get() = CommentVoteId.create(_id).getOrThrow()

    val commentId: CommentId
        get() = props.commentId

    val memberId: MemberId
        get() = props.memberId

    var voteType: VoteType
        get() = props.voteType
        set(value) {
            props.voteType = value
        }

    fun isUpVote(): Boolean {
        return voteType == VoteType.UPVOTE
    }

    fun isDownVote(): Boolean {
        return voteType == VoteType.DOWNVOTE
    }

    fun changeToUpVote() {
        this.voteType = VoteType.UPVOTE
    }

    fun changeToDownVote() {
        this.voteType = VoteType.DOWNVOTE
    }

    companion object {
        fun create(
            commentId: CommentId,
            memberId: MemberId,
            voteType: VoteType,
            id: UniqueEntityId?
        ): Result<CommentVote> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(commentId, "commentId"),
                    Guard.GuardArgument(memberId, "memberId"),
                    Guard.GuardArgument(voteType, "voteType")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(CommentVote(CommentVoteProps(commentId, memberId, voteType), id))
        }

        fun createUpvote(commentId: CommentId, memberId: MemberId): Result<CommentVote> {
            return create(commentId, memberId, VoteType.UPVOTE, null)
        }

        fun createDownvote(commentId: CommentId, memberId: MemberId): Result<CommentVote> {
            return create(commentId, memberId, VoteType.DOWNVOTE, null)
        }
    }
}
