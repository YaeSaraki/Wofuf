package dev.saraki.wofuf.modules.forum.useCases.comments.upvoteComment

import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
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
 * @description Upvote a comment
 */
@Service
class UpvoteCommentUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
) : UseCase<UpvoteCommentDto.Request, UpvoteCommentDto.Response> {
    override fun execute(request: UpvoteCommentDto.Request): Result<UpvoteCommentDto.Response> {
        if (request.commentId.isBlank()) {
            return UpvoteCommentErrors.CommentIdEmptyError()
        }
        if (request.userId.isBlank()) {
            return UpvoteCommentErrors.UserIdEmptyError()
        }

        // Validate comment ID
        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return UpvoteCommentErrors.CommentNotFoundError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        // Get comment
        val comment = commentRepo.findCommentByCommentId(commentId) ?: return UpvoteCommentErrors.CommentNotFoundError(request.commentId)

        // Get member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return UpvoteCommentErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId) ?: return UpvoteCommentErrors.MemberNotFoundError(request.userId)

        // Check if already upvoted (simplified check)
        val existingVote = comment.getVotes().getItems().any { it.memberId == member.memberId && it.isUpVote() }
        if (existingVote) {
            return UpvoteCommentErrors.AlreadyUpvotedError(request.commentId, request.userId)
        }

        // Create upvote
        val voteOrError = CommentVote.createUpvote(
            commentId = commentId,
            memberId = member.memberId,
        )
        if (voteOrError.isFailure) {
            return UpvoteCommentErrors.UpvoteFailedError(request.commentId)
        }
        val vote = voteOrError.getOrThrow()

        // Add vote to comment
        val addResult = comment.addVote(vote)
        if (addResult.isFailure) {
            return UpvoteCommentErrors.UpvoteFailedError(request.commentId)
        }

        // Save the updated comment
        val updatedComment = commentRepo.save(comment)

        return Result.success(UpvoteCommentDto.Response(newPoints = updatedComment.points))
    }
}
