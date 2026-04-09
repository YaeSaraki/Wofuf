package dev.saraki.wofuf.modules.forum.useCases.admin.comments.hideComment

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class HideCommentUseCase(
    private val commentRepo: CommentRepo,
) : UseCase<HideCommentDto.Request, HideCommentDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_DELETE_ANY, "Only users with COMMENT_DELETE_ANY permission can hide comments")
    override fun execute(request: HideCommentDto.Request): Result<HideCommentDto.Response> {
        if (request.commentId.isBlank()) {
            return HideCommentErrors.CommentIdEmptyError()
        }

        if (request.hiddenByMemberId.isBlank()) {
            return HideCommentErrors.HiddenByMemberIdEmptyError()
        }

        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return HideCommentErrors.InvalidCommentIdError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        val hiddenByMemberIdOrError = MemberId.create(UniqueEntityId(request.hiddenByMemberId))
        if (hiddenByMemberIdOrError.isFailure) {
            return HideCommentErrors.InvalidMemberIdError(request.hiddenByMemberId)
        }
        val hiddenByMemberId = hiddenByMemberIdOrError.getOrThrow()

        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return HideCommentErrors.CommentNotFoundError(request.commentId)

        val hideResult = comment.hide(hiddenByMemberId)
        if (hideResult.isFailure) {
            return HideCommentErrors.HideFailedError(request.commentId, hideResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            commentRepo.save(hideResult.getOrThrow())
        } catch (e: Exception) {
            return HideCommentErrors.SaveFailedError(request.commentId)
        }

        return Result.success(HideCommentDto.Response(commentId = request.commentId, isHidden = true))
    }
}
