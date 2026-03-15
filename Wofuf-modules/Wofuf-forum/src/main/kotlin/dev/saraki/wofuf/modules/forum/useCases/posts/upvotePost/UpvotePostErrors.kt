package dev.saraki.wofuf.modules.forum.useCases.posts.upvotePost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Error classes for upvote post use case
 */
class UpvotePostErrors {

    /** Post ID is empty */
    class PostIdEmptyError : Result.Failure<UpvotePostDto.Response>(
        exception = UseCaseError(
            code = "POST_ID_EMPTY_ERROR",
            message = "Post ID cannot be empty"
        )
    )

    /** User ID is empty */
    class UserIdEmptyError : Result.Failure<UpvotePostDto.Response>(
        exception = UseCaseError(
            code = "USER_ID_EMPTY_ERROR",
            message = "User ID cannot be empty"
        )
    )

    /** Post not found */
    class PostNotFoundError(val postId: String) : Result.Failure<UpvotePostDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND_ERROR",
            message = "Couldn't find a post by postId {$postId}"
        )
    )

    /** Member not found */
    class MemberNotFoundError(val userId: String) : Result.Failure<UpvotePostDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND_ERROR",
            message = "Couldn't find a member by userId {$userId}"
        )
    )

    /** Already upvoted */
    class AlreadyUpvotedError(val postId: String, val userId: String) : Result.Failure<UpvotePostDto.Response>(
        exception = UseCaseError(
            code = "ALREADY_UPVOTED_ERROR",
            message = "User {$userId} has already upvoted post {$postId}"
        )
    )

    /** Upvote failed */
    class UpvoteFailedError(val postId: String) : Result.Failure<UpvotePostDto.Response>(
        exception = UseCaseError(
            code = "UPVOTE_FAILED_ERROR",
            message = "Failed to upvote post {$postId}"
        )
    )
}
