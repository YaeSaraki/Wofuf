package dev.saraki.wofuf.modules.forum.useCases.admin.comments.hideComment

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
class HideCommentUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
) : UseCase<HideCommentDto.Request, HideCommentDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_DELETE_ANY, "Only users with COMMENT_DELETE_ANY permission can hide comments")
    override fun execute(request: HideCommentDto.Request): Result<HideCommentDto.Response> {
        // 1. 验证请求
        if (request.commentId.isBlank()) {
            return HideCommentErrors.CommentIdEmptyError()
        }

        if (request.userId.isBlank()) {
            return HideCommentErrors.UserIdEmptyError()
        }

        // 2. 解析 commentId
        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return HideCommentErrors.InvalidCommentIdError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        // 3. 解析 userId 并查找 member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return HideCommentErrors.InvalidUserIdError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()

        val member = memberRepo.findMemberByUserId(userId)
            ?: return HideCommentErrors.MemberNotFoundError(request.userId)

        // 4. 查找评论
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return HideCommentErrors.CommentNotFoundError(request.commentId)

        // 5. 幂等操作：如果已隐藏，直接返回成功
        if (comment.isHidden) {
            return Result.success(
                HideCommentDto.Response(
                    commentId = request.commentId,
                    isHidden = true,
                    message = "Comment is already hidden"
                )
            )
        }

        // 6. 执行隐藏操作
        val hideResult = comment.hide(member.memberId)
        if (hideResult.isFailure) {
            return HideCommentErrors.HideFailedError(request.commentId, hideResult.exceptionOrThrow().message ?: "Unknown error")
        }

        // 7. 保存
        try {
            val savedComment = commentRepo.save(hideResult.getOrThrow())
            return Result.success(
                HideCommentDto.Response(
                    commentId = request.commentId,
                    isHidden = savedComment.isHidden,
                    message = "Comment hidden successfully"
                )
            )
        } catch (e: Exception) {
            return HideCommentErrors.SaveFailedError(request.commentId)
        }
    }
}
