package dev.saraki.wofuf.modules.forum.useCases.admin.comments.batchHideComments

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
class BatchHideCommentsUseCase(
    private val commentRepo: CommentRepo,
    private val memberRepo: MemberRepo,
    private val operationLogService: OperationLogService,
) : UseCase<BatchHideCommentsDto.Request, BatchHideCommentsDto.Response> {

    @RequirePermission(PermissionPoint.COMMENT_DELETE_ANY, "Only users with COMMENT_DELETE_ANY permission can batch hide comments")
    override fun execute(request: BatchHideCommentsDto.Request): Result<BatchHideCommentsDto.Response> {
        // 1. 验证请求
        if (request.commentIds.isEmpty()) {
            return BatchHideCommentsErrors.CommentIdsEmptyError()
        }

        if (request.userId.isBlank()) {
            return BatchHideCommentsErrors.UserIdEmptyError()
        }

        // 2. 解析 userId 并查找 member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return BatchHideCommentsErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()

        val member = memberRepo.findMemberByUserId(userId)
            ?: return BatchHideCommentsErrors.MemberNotFoundError(request.userId)

        // 3. 批量处理每个评论
        val results = mutableListOf<BatchHideCommentsDto.BatchResult>()
        var successCount = 0
        var failCount = 0

        for (commentIdStr in request.commentIds) {
            val result = hideSingleComment(commentIdStr, member.memberId)
            if (result.isSuccess) {
                successCount++
            } else {
                failCount++
            }
            results.add(result.getOrThrow())
        }

        return Result.success(
            BatchHideCommentsDto.Response(
                successCount = successCount,
                failCount = failCount,
                results = results,
                message = "Batch hide completed: $successCount succeeded, $failCount failed"
            )
        )
    }

    private fun hideSingleComment(commentIdStr: String, operatorMemberId: MemberId): Result<BatchHideCommentsDto.BatchResult> {
        // 解析 commentId
        val commentIdOrError = CommentId.create(UniqueEntityId(commentIdStr))
        if (commentIdOrError.isFailure) {
            return Result.success(
                BatchHideCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = false,
                    success = false,
                    message = "Invalid comment ID"
                )
            )
        }
        val commentId = commentIdOrError.getOrThrow()

        // 查找评论
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return Result.success(
                BatchHideCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = false,
                    success = false,
                    message = "Comment not found"
                )
            )

        // 幂等操作：如果已隐藏，直接返回成功
        if (comment.isHidden) {
            return Result.success(
                BatchHideCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = true,
                    success = true,
                    message = "Already hidden"
                )
            )
        }

        // 执行隐藏操作
        val hideResult = comment.hide(operatorMemberId)
        if (hideResult.isFailure) {
            return Result.success(
                BatchHideCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = false,
                    success = false,
                    message = hideResult.exceptionOrNull()?.message ?: "Unknown error"
                )
            )
        }

        // 保存
        try {
            val savedComment = commentRepo.save(hideResult.getOrThrow())

            // 记录操作日志
            operationLogService.logCommentAction(
                operationType = OperationType.COMMENT_HIDE,
                commentId = commentIdStr,
                operatorId = operatorMemberId,
                details = "Batch hide: ${comment.text.value.take(50)}"
            )

            return Result.success(
                BatchHideCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = savedComment.isHidden,
                    success = true,
                    message = "Hidden successfully"
                )
            )
        } catch (e: Exception) {
            return Result.success(
                BatchHideCommentsDto.BatchResult(
                    commentId = commentIdStr,
                    isHidden = false,
                    success = false,
                    message = "Save failed"
                )
            )
        }
    }
}
