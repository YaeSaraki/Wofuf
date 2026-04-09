package dev.saraki.wofuf.modules.forum.useCases.admin.comments.showComment

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class ShowCommentUseCase(
    private val commentRepo: CommentRepo,
) : UseCase<ShowCommentDto.Request, ShowCommentDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_DELETE_ANY, "Only users with COMMENT_DELETE_ANY permission can show comments")
    override fun execute(request: ShowCommentDto.Request): Result<ShowCommentDto.Response> {
        if (request.commentId.isBlank()) {
            return ShowCommentErrors.CommentIdEmptyError()
        }

        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return ShowCommentErrors.InvalidCommentIdError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return ShowCommentErrors.CommentNotFoundError(request.commentId)

        val showResult = comment.show()
        if (showResult.isFailure) {
            return ShowCommentErrors.ShowFailedError(request.commentId, showResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            commentRepo.save(showResult.getOrThrow())
        } catch (e: Exception) {
            return ShowCommentErrors.SaveFailedError(request.commentId)
        }

        return Result.success(ShowCommentDto.Response(commentId = request.commentId, isHidden = false))
    }
}
