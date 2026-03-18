package dev.saraki.wofuf.modules.forum.useCases.comments.upvoteComment

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
 * @date 2026/3/15 16:15
 * @description Upvote a comment with toggle support (uses domain service)
 */
@Service
class UpvoteCommentUseCase(
    private val memberRepo: MemberRepo,
    private val commentVoteDomainService: CommentVoteDomainService,
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

        // 3. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return UpvoteCommentErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return UpvoteCommentErrors.MemberNotFoundError(request.userId)

        // 4. Use domain service to handle upvote
        val result = commentVoteDomainService.upvote(commentId, member.memberId)

        if (result.isFailure) {
            return UpvoteCommentErrors.UpvoteFailedError(request.commentId)
        }

        val voteResult = result.getOrThrow()

        // 5. Return success response
        return Result.success(UpvoteCommentDto.Response(newPoints = voteResult.newPoints))
    }
}
