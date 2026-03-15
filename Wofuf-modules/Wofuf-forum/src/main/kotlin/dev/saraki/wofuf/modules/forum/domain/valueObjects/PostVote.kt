package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.Entity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 13:39
 *   @description:
 */
data class PostVoteProps(
    val postId: PostId,
    val memberId: MemberId,
    val type: VoteType
)

class PostVote private constructor(
    props: PostVoteProps,
    id: UniqueEntityId?
) : Entity<PostVoteProps>(props, id) {
    val postId: PostId
        get() = props.postId

    val memberId: MemberId
        get() = props.memberId

    val type: VoteType
        get() = props.type

    fun isUpVote(): Boolean = type == VoteType.UPVOTE

    fun isDownVote(): Boolean = type == VoteType.DOWNVOTE

    companion object {
        fun create(props: PostVoteProps, id: UniqueEntityId?): Result<PostVote> {
            val guardResult = Guard.againstNullOrUndefined(props.postId, "PostVote.postId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return Result.success(PostVote(props, id))
        }

        fun createUpvote(postId: PostId, memberId: MemberId): Result<PostVote> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(postId, "postId"),
                    Guard.GuardArgument(memberId, "memberId")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return create(PostVoteProps(postId, memberId, VoteType.UPVOTE), null)
        }

        fun createDownvote(postId: PostId, memberId: MemberId): Result<PostVote> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(postId, "postId"),
                    Guard.GuardArgument(memberId, "memberId")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return create(PostVoteProps(postId, memberId, VoteType.DOWNVOTE), null)
        }
    }
}
