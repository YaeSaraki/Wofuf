package dev.saraki.wofuf.modules.forum.useCases.admin.comments.hideComment

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class HideCommentErrors {
    class CommentIdEmptyError : Result.Failure<HideCommentDto.Response>(
        exception = UseCaseError(code = "COMMENT_ID_EMPTY_ERROR", message = "Comment ID cannot be empty")
    )

    class HiddenByMemberIdEmptyError : Result.Failure<HideCommentDto.Response>(
        exception = UseCaseError(code = "HIDDEN_BY_MEMBER_ID_EMPTY_ERROR", message = "Hidden by member ID cannot be empty")
    )

    class InvalidCommentIdError(val commentId: String) : Result.Failure<HideCommentDto.Response>(
        exception = UseCaseError(code = "INVALID_COMMENT_ID_ERROR", message = "Invalid comment ID format: $commentId")
    )

    class InvalidMemberIdError(val memberId: String) : Result.Failure<HideCommentDto.Response>(
        exception = UseCaseError(code = "INVALID_MEMBER_ID_ERROR", message = "Invalid member ID format: $memberId")
    )

    class CommentNotFoundError(val commentId: String) : Result.Failure<HideCommentDto.Response>(
        exception = UseCaseError(code = "COMMENT_NOT_FOUND_ERROR", message = "Couldn't find a comment by commentId {$commentId}")
    )

    class HideFailedError(val commentId: String, val reason: String) : Result.Failure<HideCommentDto.Response>(
        exception = UseCaseError(code = "HIDE_FAILED_ERROR", message = "Failed to hide comment {$commentId}: $reason")
    )

    class SaveFailedError(val commentId: String) : Result.Failure<HideCommentDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save hidden comment {$commentId}")
    )
}
