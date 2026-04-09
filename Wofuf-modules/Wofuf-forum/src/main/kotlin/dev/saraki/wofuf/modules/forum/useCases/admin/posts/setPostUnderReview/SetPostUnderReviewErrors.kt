package dev.saraki.wofuf.modules.forum.useCases.admin.posts.setPostUnderReview

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class SetPostUnderReviewErrors {
    class PostIdEmptyError : Result.Failure<SetPostUnderReviewDto.Response>(
        exception = UseCaseError(code = "POST_ID_EMPTY_ERROR", message = "Post ID cannot be empty")
    )

    class InvalidPostIdError(val postId: String) : Result.Failure<SetPostUnderReviewDto.Response>(
        exception = UseCaseError(code = "INVALID_POST_ID_ERROR", message = "Invalid post ID format: $postId")
    )

    class PostNotFoundError(val postId: String) : Result.Failure<SetPostUnderReviewDto.Response>(
        exception = UseCaseError(code = "POST_NOT_FOUND_ERROR", message = "Couldn't find a post by postId {$postId}")
    )

    class ReviewFailedError(val postId: String, val reason: String) : Result.Failure<SetPostUnderReviewDto.Response>(
        exception = UseCaseError(code = "REVIEW_FAILED_ERROR", message = "Failed to set post under review {$postId}: $reason")
    )

    class SaveFailedError(val postId: String) : Result.Failure<SetPostUnderReviewDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save post {$postId}")
    )
}
