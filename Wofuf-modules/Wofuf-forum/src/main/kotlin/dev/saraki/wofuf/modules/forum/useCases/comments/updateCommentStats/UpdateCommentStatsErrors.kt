package dev.saraki.wofuf.modules.forum.useCases.comments.updateCommentStats

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class UpdateCommentStatsErrors {

    // Comment ID is empty
    class CommentIdEmptyError() : Result.Failure<UpdateCommentStatsDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_ID_EMPTY",
            message = "Comment ID cannot be empty"
        )
    )

    // Comment not found
    class CommentNotFoundError(val commentId: String) : Result.Failure<UpdateCommentStatsDto.Response>(
        exception = UseCaseError(
            code = "COMMENT_NOT_FOUND",
            message = "Couldn't find a comment by commentId {$commentId}"
        )
    )

    // Update failed
    class UpdateFailedError(val commentId: String) : Result.Failure<UpdateCommentStatsDto.Response>(
        exception = UseCaseError(
            code = "UPDATE_FAILED",
            message = "Failed to update comment stats for comment {$commentId}"
        )
    )
}
