package dev.saraki.wofuf.modules.forum.useCases.comments.downvoteComment

import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class DownvoteCommentUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
) : UseCase<DownvoteCommentDto.Request, DownvoteCommentDto.Response> {

    override fun execute(request: DownvoteCommentDto.Request): Result<DownvoteCommentDto.Response> {
        if (request.commentId.isBlank()) {
            return DownvoteCommentErrors.CommentIdEmptyError()
        }
        if (request.userId.isBlank()) {
            return DownvoteCommentErrors.UserIdEmptyError()
        }

        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return DownvoteCommentErrors.CommentNotFoundError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return DownvoteCommentErrors.CommentNotFoundError(request.commentId)

        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return DownvoteCommentErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return DownvoteCommentErrors.MemberNotFoundError(request.userId)

        val existingDownvote = comment.getVotes().getItems().any { 
            it.memberId == member.memberId && it.isDownVote() 
        }
        if (existingDownvote) {
            return DownvoteCommentErrors.AlreadyDownvotedError(request.commentId, request.userId)
        }

        val existingUpvote = comment.getVotes().getItems().find { 
            it.memberId == member.memberId && it.isUpVote() 
        }
        if (existingUpvote != null) {
            comment.removeVote(existingUpvote)
        }

        val voteOrError = CommentVote.createDownvote(
            commentId = commentId,
            memberId = member.memberId,
        )
        if (voteOrError.isFailure) {
            return DownvoteCommentErrors.DownvoteFailedError(request.commentId)
        }
        val vote = voteOrError.getOrThrow()

        val addResult = comment.addVote(vote)
        if (addResult.isFailure) {
            return DownvoteCommentErrors.DownvoteFailedError(request.commentId)
        }

        val updatedComment = commentRepo.save(comment)
        return Result.success(DownvoteCommentDto.Response(newPoints = updatedComment.points ?: 0))
    }
}
