package dev.saraki.wofuf.modules.forum.useCases.admin.posts.pinPost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 * @description Error classes for pin post use case
 */
class PinPostErrors {

    /** Post ID is empty */
    class PostIdEmptyError : Result.Failure<PinPostDto.Response>(
        exception = UseCaseError(
            code = "POST_ID_EMPTY_ERROR",
            message = "Post ID cannot be empty"
        )
    )

    /** Invalid post ID format */
    class InvalidPostIdError(val postId: String) : Result.Failure<PinPostDto.Response>(
        exception = UseCaseError(
            code = "INVALID_POST_ID_ERROR",
            message = "Invalid post ID format: $postId"
        )
    )

    /** Post not found */
    class PostNotFoundError(val postId: String) : Result.Failure<PinPostDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND_ERROR",
            message = "Couldn't find a post by postId {$postId}"
        )
    )

    /** Pin failed */
    class PinFailedError(val postId: String, val reason: String) : Result.Failure<PinPostDto.Response>(
        exception = UseCaseError(
            code = "PIN_FAILED_ERROR",
            message = "Failed to pin post {$postId}: $reason"
        )
    )

    /** Save failed */
    class SaveFailedError(val postId: String) : Result.Failure<PinPostDto.Response>(
        exception = UseCaseError(
            code = "SAVE_FAILED_ERROR",
            message = "Failed to save pinned post {$postId}"
        )
    )
}
