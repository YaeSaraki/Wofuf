package dev.saraki.wofuf.modules.forum.useCases.comments.downvoteComment

import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.CommentVotesRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Downvote a comment with toggle support
 */
@Service
class DownvoteCommentUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
    private val commentVotesRepo: CommentVotesRepo,
) : UseCase<DownvoteCommentDto.Request, DownvoteCommentDto.Response> {

    override fun execute(request: DownvoteCommentDto.Request): Result<DownvoteCommentDto.Response> {
        // 1. Validate inputs
        if (request.commentId.isBlank()) {
            return DownvoteCommentErrors.CommentIdEmptyError()
        }
        if (request.userId.isBlank()) {
            return DownvoteCommentErrors.UserIdEmptyError()
        }

        // 2. Validate and create CommentId
        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return DownvoteCommentErrors.CommentNotFoundError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        // 3. Get the comment
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return DownvoteCommentErrors.CommentNotFoundError(request.commentId)

        // 4. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return DownvoteCommentErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return DownvoteCommentErrors.MemberNotFoundError(request.userId)

        // 5. Query existing vote from database
        val existingVote = commentVotesRepo.findByCommentIdAndMemberId(commentId, member.memberId)

        // 6. Check if already downvoted in database
        if (existingVote != null && existingVote.isDownVote()) {
            // User wants to remove their downvote (toggle off)
            commentVotesRepo.delete(existingVote)
        } else {
            // 7. Delete any existing upvote (switching from upvote to downvote)
            if (existingVote != null && existingVote.isUpVote()) {
                commentVotesRepo.delete(existingVote)
            }

            // 8. Create downvote
            val voteOrError = CommentVote.createDownvote(
                commentId = commentId,
                memberId = member.memberId,
            )
            if (voteOrError.isFailure) {
                return DownvoteCommentErrors.DownvoteFailedError(request.commentId)
            }
            val vote = voteOrError.getOrThrow()

            // 9. Save the new vote
            commentVotesRepo.save(vote)
        }

        // 10. Update comment points and save
        val totalUpvotes = commentVotesRepo.countCommentUpvotesByCommentId(commentId)
        val totalDownvotes = commentVotesRepo.countCommentDownvotesByCommentId(commentId)
        comment.updateScore(totalUpvotes, totalDownvotes)
        commentRepo.save(comment)

        // 11. Return success response
        return Result.success(DownvoteCommentDto.Response(newPoints = totalUpvotes - totalDownvotes))
    }
}
