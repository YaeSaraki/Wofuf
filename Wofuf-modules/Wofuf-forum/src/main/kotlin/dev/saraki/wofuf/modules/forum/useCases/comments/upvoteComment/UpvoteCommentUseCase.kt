package dev.saraki.wofuf.modules.forum.useCases.comments.upvoteComment

import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.CommentVotesRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Upvote a comment with toggle support
 */
@Service
class UpvoteCommentUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
    private val commentVotesRepo: CommentVotesRepo,
) : UseCase<UpvoteCommentDto.Request, UpvoteCommentDto.Response> {
    override fun execute(request: UpvoteCommentDto.Request): Result<UpvoteCommentDto.Response> {
        // 1. Validate inputs
        if (request.commentId.isBlank()) {
            return UpvoteCommentErrors.CommentIdEmptyError()
        }
        if (request.userId.isBlank()) {
            return UpvoteCommentErrors.UserIdEmptyError()
        }

        // 2. Validate and create CommentId
        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return UpvoteCommentErrors.CommentNotFoundError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        // 3. Get the comment
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return UpvoteCommentErrors.CommentNotFoundError(request.commentId)

        // 4. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return UpvoteCommentErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return UpvoteCommentErrors.MemberNotFoundError(request.userId)

        // 5. Query existing vote from database
        val existingVote = commentVotesRepo.findByCommentIdAndMemberId(commentId, member.memberId)

        // 6. Check if already upvoted in database
        if (existingVote != null && existingVote.isUpVote()) {
            // User wants to remove their upvote (toggle off)
            commentVotesRepo.delete(existingVote)
        } else {
            // 7. Delete any existing downvote (switching from downvote to upvote)
            if (existingVote != null && existingVote.isDownVote()) {
                commentVotesRepo.delete(existingVote)
            }

            // 8. Create upvote
            val voteOrError = CommentVote.createUpvote(
                commentId = commentId,
                memberId = member.memberId,
            )
            if (voteOrError.isFailure) {
                return UpvoteCommentErrors.UpvoteFailedError(request.commentId)
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
        return Result.success(UpvoteCommentDto.Response(newPoints = totalUpvotes - totalDownvotes))
    }
}
