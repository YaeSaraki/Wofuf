package dev.saraki.wofuf.modules.forum.useCases.members.deleteOwnComment

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class DeleteOwnCommentUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
) : UseCase<DeleteOwnCommentDto.Request, DeleteOwnCommentDto.Response> {

    override fun execute(request: DeleteOwnCommentDto.Request): Result<DeleteOwnCommentDto.Response> {
        if (request.commentId.isBlank()) {
            return DeleteOwnCommentErrors.CommentIdEmptyError()
        }

        if (request.currentUserId.isBlank()) {
            return DeleteOwnCommentErrors.UnauthorizedError()
        }

        // Find member by userId
        val userId = UserId.create(UniqueEntityId(request.currentUserId)).getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return DeleteOwnCommentErrors.MemberNotFoundError()

        // Create comment ID
        val commentId = CommentId.create(UniqueEntityId(request.commentId)).getOrThrow()

        // Verify comment exists and belongs to this member
        val comment = commentRepo.findCommentByCommentId(commentId)
        if (comment == null) {
            return DeleteOwnCommentErrors.CommentNotFoundOrNotOwnedError()
        }

        // Delete only if owned by this member
        val deleted = commentRepo.deleteByIdAndMemberId(commentId, member.memberId)
        if (!deleted) {
            return DeleteOwnCommentErrors.CommentNotFoundOrNotOwnedError()
        }

        return Result.success(
            DeleteOwnCommentDto.Response(
                success = true,
                message = "Comment deleted successfully"
            )
        )
    }
}
