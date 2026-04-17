package dev.saraki.wofuf.modules.forum.useCases.admin.posts.featurePost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class FeaturePostErrors {
    class PostIdEmptyError : Result.Failure<FeaturePostDto.Response>(
        exception = UseCaseError(code = "POST_ID_EMPTY_ERROR", message = "Post ID cannot be empty")
    )

    class InvalidPostIdError(val postId: String) : Result.Failure<FeaturePostDto.Response>(
        exception = UseCaseError(code = "INVALID_POST_ID_ERROR", message = "Invalid post ID format: $postId")
    )

    /** Invalid operator */
    class InvalidOperatorError : Result.Failure<FeaturePostDto.Response>(
        exception = UseCaseError(
            code = "INVALID_OPERATOR_ERROR",
            message = "Invalid operator ID"
        )
    )

    class PostNotFoundError(val postId: String) : Result.Failure<FeaturePostDto.Response>(
        exception = UseCaseError(code = "POST_NOT_FOUND_ERROR", message = "Couldn't find a post by postId {$postId}")
    )

    class FeatureFailedError(val postId: String, val reason: String) : Result.Failure<FeaturePostDto.Response>(
        exception = UseCaseError(code = "FEATURE_FAILED_ERROR", message = "Failed to feature post {$postId}: $reason")
    )

    class SaveFailedError(val postId: String) : Result.Failure<FeaturePostDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save featured post {$postId}")
    )
}
