package dev.saraki.wofuf.modules.forum.useCases.admin.comments.toggleCommentVisibility

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class ToggleCommentVisibilityUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
) : UseCase<ToggleCommentVisibilityDto.Request, ToggleCommentVisibilityDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_DELETE_ANY, "Only users with COMMENT_DELETE_ANY permission can toggle comment visibility")
    override fun execute(request: ToggleCommentVisibilityDto.Request): Result<ToggleCommentVisibilityDto.Response> {
        // 1. 验证请求
        if (request.commentId.isBlank()) {
            return ToggleCommentVisibilityErrors.CommentIdEmptyError()
        }

        if (request.userId.isBlank()) {
            return ToggleCommentVisibilityErrors.UserIdEmptyError()
        }

        // 2. 解析 commentId
        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return ToggleCommentVisibilityErrors.InvalidCommentIdError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        // 3. 解析 userId 并查找 member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return ToggleCommentVisibilityErrors.InvalidUserIdError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()

        val member = memberRepo.findMemberByUserId(userId)
            ?: return ToggleCommentVisibilityErrors.MemberNotFoundError(request.userId)

        // 4. 查找评论
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return ToggleCommentVisibilityErrors.CommentNotFoundError(request.commentId)

        // 5. 记录操作前的状态用于日志
        val wasHidden = comment.isHidden

        // 6. 执行切换操作
        val toggleResult = comment.toggleVisibility(member.memberId)
        if (toggleResult.isFailure) {
            return ToggleCommentVisibilityErrors.ToggleFailedError(
                request.commentId,
                toggleResult.exceptionOrThrow().message ?: "Unknown error"
            )
        }

        // 7. 保存
        try {
            val savedComment = commentRepo.save(toggleResult.getOrThrow())
            val action = if (wasHidden) "shown" else "hidden"
            return Result.success(
                ToggleCommentVisibilityDto.Response(
                    commentId = request.commentId,
                    isHidden = savedComment.isHidden,
                    message = "Comment $action successfully"
                )
            )
        } catch (e: Exception) {
            return ToggleCommentVisibilityErrors.SaveFailedError(request.commentId)
        }
    }
}
