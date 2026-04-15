package dev.saraki.wofuf.modules.forum.useCases.admin.comments.batchHideComments

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class BatchHideCommentsErrors {
    class CommentIdsEmptyError : Result.Failure<BatchHideCommentsDto.Response>(
        exception = UseCaseError(code = "COMMENT_IDS_EMPTY", message = "Comment IDs list cannot be empty")
    )

    class UserIdEmptyError : Result.Failure<BatchHideCommentsDto.Response>(
        exception = UseCaseError(code = "USER_ID_EMPTY", message = "User ID cannot be empty")
    )

    class InvalidCommentIdError(val commentId: String) : Result.Failure<BatchHideCommentsDto.Response>(
        exception = UseCaseError(code = "INVALID_COMMENT_ID", message = "Invalid comment ID: $commentId")
    )

    class MemberNotFoundError(val userId: String) : Result.Failure<BatchHideCommentsDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND", message = "Member not found for user ID: $userId")
    )

    class CommentNotFoundError(val commentId: String) : Result.Failure<BatchHideCommentsDto.Response>(
        exception = UseCaseError(code = "COMMENT_NOT_FOUND", message = "Comment not found: $commentId")
    )

    class HideFailedError(val commentId: String, val reason: String) : Result.Failure<BatchHideCommentsDto.Response>(
        exception = UseCaseError(code = "HIDE_FAILED", message = "Failed to hide comment $commentId: $reason")
    )
}
