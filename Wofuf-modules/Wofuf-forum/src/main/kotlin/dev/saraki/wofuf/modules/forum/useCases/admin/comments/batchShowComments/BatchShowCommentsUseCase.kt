package dev.saraki.wofuf.modules.forum.useCases.admin.comments.batchShowComments

import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.services.OperationLogService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class BatchShowCommentsUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
    private val operationLogService: OperationLogService,
) : UseCase<BatchShowCommentsDto.Request, BatchShowCommentsDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_DELETE_ANY, "Only users with COMMENT_DELETE_ANY permission can batch show comments")
    override fun execute(request: BatchShowCommentsDto.Request): Result<BatchShowCommentsDto.Response> {
        // 1. 验证请求
        if (request.commentIds.isEmpty()) {
            return BatchShowCommentsErrors.CommentIdsEmptyError()
        }

        if (request.userId.isBlank()) {
            return BatchShowCommentsErrors.UserIdEmptyError()
        }

        // 2. 解析 userId 并查找 member（批量操作只需要确认操作者存在）
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return BatchShowCommentsErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()

        val member = memberRepo.findMemberByUserId(userId)
            ?: return BatchShowCommentsErrors.MemberNotFoundError(request.userId)

        // 3. 批量处理每个评论
        val results = mutableListOf<BatchShowCommentsDto.BatchResult>()
        var successCount = 0
        var failCount = 0

        for (commentIdStr in request.commentIds) {
            val result = showSingleComment(commentIdStr, member.memberId)
            if (result.isSuccess) {
                successCount++
            } else {
                failCount++
            }
            results.add(result.getOrThrow())
        }

        return Result.success(
            BatchShowCommentsDto.Response(
                successCount = successCount,
                failCount = failCount,
                results = results,
                message = "Batch show completed: $successCount succeeded, $failCount failed"
            )
        )
    }

    private fun showSingleComment(commentIdStr: String, operatorMemberId: MemberId): Result<BatchShowCommentsDto.BatchResult> {
        // 解析 commentId
        val commentIdOrError = CommentId.create(UniqueEntityId(commentIdStr))
        if (commentIdOrError.isFailure) {
            return Result.success(
                BatchShowCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = true,
                    success = false,
                    message = "Invalid comment ID"
                )
            )
        }
        val commentId = commentIdOrError.getOrThrow()

        // 查找评论
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return Result.success(
                BatchShowCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = true,
                    success = false,
                    message = "Comment not found"
                )
            )

        // 幂等操作：如果未隐藏，直接返回成功
        if (!comment.isHidden) {
            return Result.success(
                BatchShowCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = false,
                    success = true,
                    message = "Already visible"
                )
            )
        }

        // 执行显示操作
        val showResult = comment.show()
        if (showResult.isFailure) {
            return Result.success(
                BatchShowCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = true,
                    success = false,
                    message = showResult.exceptionOrNull()?.message ?: "Unknown error"
                )
            )
        }

        // 保存
        try {
            val savedComment = commentRepo.save(showResult.getOrThrow())

            // 记录操作日志
            operationLogService.logCommentAction(
                operationType = OperationType.COMMENT_SHOW,
                commentId = commentIdStr,
                operatorId = operatorMemberId,
                details = "Batch show: ${comment.text.value.take(50)}"
            )

            return Result.success(
                BatchShowCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = savedComment.isHidden,
                    success = true,
                    message = "Shown successfully"
                )
            )
        } catch (e: Exception) {
            return Result.success(
                BatchShowCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = true,
                    success = false,
                    message = "Save failed"
                )
            )
        }
    }
}
