package dev.saraki.wofuf.modules.forum.useCases.admin.comments.toggleCommentVisibility

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class ToggleCommentVisibilityErrors {
    class CommentIdEmptyError : Result.Failure<ToggleCommentVisibilityDto.Response>(
        exception = UseCaseError(code = "COMMENT_ID_EMPTY", message = "Comment ID cannot be empty")
    )

    class UserIdEmptyError : Result.Failure<ToggleCommentVisibilityDto.Response>(
        exception = UseCaseError(code = "USER_ID_EMPTY", message = "User ID cannot be empty")
    )

    class InvalidCommentIdError(val commentId: String) : Result.Failure<ToggleCommentVisibilityDto.Response>(
        exception = UseCaseError(code = "INVALID_COMMENT_ID", message = "Invalid comment ID format: $commentId")
    )

    class InvalidUserIdError(val userId: String) : Result.Failure<ToggleCommentVisibilityDto.Response>(
        exception = UseCaseError(code = "INVALID_USER_ID", message = "Invalid user ID format: $userId")
    )

    class MemberNotFoundError(val userId: String) : Result.Failure<ToggleCommentVisibilityDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND", message = "Member not found for user: $userId")
    )

    class CommentNotFoundError(val commentId: String) : Result.Failure<ToggleCommentVisibilityDto.Response>(
        exception = UseCaseError(code = "COMMENT_NOT_FOUND", message = "Comment not found: $commentId")
    )

    class ToggleFailedError(val commentId: String, val reason: String) : Result.Failure<ToggleCommentVisibilityDto.Response>(
        exception = UseCaseError(code = "TOGGLE_FAILED", message = "Failed to toggle comment $commentId visibility: $reason")
    )

    class SaveFailedError(val commentId: String) : Result.Failure<ToggleCommentVisibilityDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED", message = "Failed to save comment: $commentId")
    )
}
