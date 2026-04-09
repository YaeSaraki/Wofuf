package dev.saraki.wofuf.modules.forum.useCases.admin.comments.showComment

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class ShowCommentErrors {
    class CommentIdEmptyError : Result.Failure<ShowCommentDto.Response>(
        exception = UseCaseError(code = "COMMENT_ID_EMPTY_ERROR", message = "Comment ID cannot be empty")
    )

    class InvalidCommentIdError(val commentId: String) : Result.Failure<ShowCommentDto.Response>(
        exception = UseCaseError(code = "INVALID_COMMENT_ID_ERROR", message = "Invalid comment ID format: $commentId")
    )

    class CommentNotFoundError(val commentId: String) : Result.Failure<ShowCommentDto.Response>(
        exception = UseCaseError(code = "COMMENT_NOT_FOUND_ERROR", message = "Couldn't find a comment by commentId {$commentId}")
    )

    class ShowFailedError(val commentId: String, val reason: String) : Result.Failure<ShowCommentDto.Response>(
        exception = UseCaseError(code = "SHOW_FAILED_ERROR", message = "Failed to show comment {$commentId}: $reason")
    )

    class SaveFailedError(val commentId: String) : Result.Failure<ShowCommentDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save shown comment {$commentId}")
    )
}
