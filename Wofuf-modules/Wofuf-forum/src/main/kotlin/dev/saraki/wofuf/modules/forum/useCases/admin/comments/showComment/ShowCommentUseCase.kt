package dev.saraki.wofuf.modules.forum.useCases.admin.comments.showComment

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class ShowCommentUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
) : UseCase<ShowCommentDto.Request, ShowCommentDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_DELETE_ANY, "Only users with COMMENT_DELETE_ANY permission can show comments")
    override fun execute(request: ShowCommentDto.Request): Result<ShowCommentDto.Response> {
        // 1. 验证请求
        if (request.commentId.isBlank()) {
            return ShowCommentErrors.CommentIdEmptyError()
        }

        if (request.userId.isBlank()) {
            return ShowCommentErrors.UserIdEmptyError()
        }

        // 2. 解析 commentId
        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return ShowCommentErrors.InvalidCommentIdError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        // 3. 解析 userId 并查找 member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return ShowCommentErrors.InvalidUserIdError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()

        val member = memberRepo.findMemberByUserId(userId)
            ?: return ShowCommentErrors.MemberNotFoundError(request.userId)

        // 4. 查找评论
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return ShowCommentErrors.CommentNotFoundError(request.commentId)

        // 5. 幂等操作：如果未隐藏，直接返回成功
        if (!comment.isHidden) {
            return Result.success(
                ShowCommentDto.Response(
                    commentId = request.commentId,
                    isHidden = false,
                    message = "Comment is not hidden"
                )
            )
        }

        // 6. 执行显示操作
        val showResult = comment.show()
        if (showResult.isFailure) {
            return ShowCommentErrors.ShowFailedError(request.commentId, showResult.exceptionOrThrow().message ?: "Unknown error")
        }

        // 7. 保存
        try {
            val savedComment = commentRepo.save(showResult.getOrThrow())
            return Result.success(
                ShowCommentDto.Response(
                    commentId = request.commentId,
                    isHidden = savedComment.isHidden,
                    message = "Comment shown successfully"
                )
            )
        } catch (e: Exception) {
            return ShowCommentErrors.SaveFailedError(request.commentId)
        }
    }
}
