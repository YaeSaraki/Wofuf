package dev.saraki.wofuf.modules.forum.useCases.admin.posts.approvePost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class ApprovePostErrors {
    class PostIdEmptyError : Result.Failure<ApprovePostDto.Response>(
        exception = UseCaseError(code = "POST_ID_EMPTY_ERROR", message = "Post ID cannot be empty")
    )

    class InvalidPostIdError(val postId: String) : Result.Failure<ApprovePostDto.Response>(
        exception = UseCaseError(code = "INVALID_POST_ID_ERROR", message = "Invalid post ID format: $postId")
    )

    class PostNotFoundError(val postId: String) : Result.Failure<ApprovePostDto.Response>(
        exception = UseCaseError(code = "POST_NOT_FOUND_ERROR", message = "Couldn't find a post by postId {$postId}")
    )

    class ApproveFailedError(val postId: String, val reason: String) : Result.Failure<ApprovePostDto.Response>(
        exception = UseCaseError(code = "APPROVE_FAILED_ERROR", message = "Failed to approve post {$postId}: $reason")
    )

    class SaveFailedError(val postId: String) : Result.Failure<ApprovePostDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save approved post {$postId}")
    )
}
