package dev.saraki.wofuf.modules.forum.useCases.comments.downvoteComment

import dev.saraki.wofuf.modules.forum.domain.services.CommentVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
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
 * @description Downvote a comment with toggle support (uses domain service)
 */
@Service
class DownvoteCommentUseCase(
    private val memberRepo: MemberRepo,
    private val commentVoteDomainService: CommentVoteDomainService,
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

        // 3. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return DownvoteCommentErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return DownvoteCommentErrors.MemberNotFoundError(request.userId)

        // 4. Use domain service to handle downvote
        val result = commentVoteDomainService.downvote(commentId, member.memberId)

        if (result.isFailure) {
            return DownvoteCommentErrors.DownvoteFailedError(request.commentId)
        }

        val voteResult = result.getOrThrow()

        // 5. Return success response
        return Result.success(DownvoteCommentDto.Response(newPoints = voteResult.newPoints))
    }
}
