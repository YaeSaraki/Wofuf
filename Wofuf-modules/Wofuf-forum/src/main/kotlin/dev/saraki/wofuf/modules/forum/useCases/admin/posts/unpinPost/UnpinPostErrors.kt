package dev.saraki.wofuf.modules.forum.useCases.admin.posts.unpinPost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class UnpinPostErrors {
    class PostIdEmptyError : Result.Failure<UnpinPostDto.Response>(
        exception = UseCaseError(code = "POST_ID_EMPTY_ERROR", message = "Post ID cannot be empty")
    )

    class InvalidPostIdError(val postId: String) : Result.Failure<UnpinPostDto.Response>(
        exception = UseCaseError(code = "INVALID_POST_ID_ERROR", message = "Invalid post ID format: $postId")
    )

    class PostNotFoundError(val postId: String) : Result.Failure<UnpinPostDto.Response>(
        exception = UseCaseError(code = "POST_NOT_FOUND_ERROR", message = "Couldn't find a post by postId {$postId}")
    )

    class UnpinFailedError(val postId: String, val reason: String) : Result.Failure<UnpinPostDto.Response>(
        exception = UseCaseError(code = "UNPIN_FAILED_ERROR", message = "Failed to unpin post {$postId}: $reason")
    )

    class SaveFailedError(val postId: String) : Result.Failure<UnpinPostDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save unpinned post {$postId}")
    )
}
