package dev.saraki.wofuf.modules.forum.useCases.admin.posts.hidePost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class HidePostErrors {
    class PostIdEmptyError : Result.Failure<HidePostDto.Response>(
        exception = UseCaseError(code = "POST_ID_EMPTY_ERROR", message = "Post ID cannot be empty")
    )

    class HiddenByMemberIdEmptyError : Result.Failure<HidePostDto.Response>(
        exception = UseCaseError(code = "HIDDEN_BY_MEMBER_ID_EMPTY_ERROR", message = "Hidden by member ID cannot be empty")
    )

    class InvalidPostIdError(val postId: String) : Result.Failure<HidePostDto.Response>(
        exception = UseCaseError(code = "INVALID_POST_ID_ERROR", message = "Invalid post ID format: $postId")
    )

    class InvalidMemberIdError(val memberId: String) : Result.Failure<HidePostDto.Response>(
        exception = UseCaseError(code = "INVALID_MEMBER_ID_ERROR", message = "Invalid member ID format: $memberId")
    )

    class PostNotFoundError(val postId: String) : Result.Failure<HidePostDto.Response>(
        exception = UseCaseError(code = "POST_NOT_FOUND_ERROR", message = "Couldn't find a post by postId {$postId}")
    )

    class HideFailedError(val postId: String, val reason: String) : Result.Failure<HidePostDto.Response>(
        exception = UseCaseError(code = "HIDE_FAILED_ERROR", message = "Failed to hide post {$postId}: $reason")
    )

    class SaveFailedError(val postId: String) : Result.Failure<HidePostDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save hidden post {$postId}")
    )
}
