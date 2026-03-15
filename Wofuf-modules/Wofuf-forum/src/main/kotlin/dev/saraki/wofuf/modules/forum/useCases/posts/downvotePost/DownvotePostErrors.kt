package dev.saraki.wofuf.modules.forum.useCases.posts.downvotePost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Error classes for downvote post use case
 */
class DownvotePostErrors {

    /** Post ID is empty */
    class PostIdEmptyError : Result.Failure<DownvotePostDto.Response>(
        exception = UseCaseError(
            code = "POST_ID_EMPTY_ERROR",
            message = "Post ID cannot be empty"
        )
    )

    /** User ID is empty */
    class UserIdEmptyError : Result.Failure<DownvotePostDto.Response>(
        exception = UseCaseError(
            code = "USER_ID_EMPTY_ERROR",
            message = "User ID cannot be empty"
        )
    )

    /** Post not found */
    class PostNotFoundError(val postId: String) : Result.Failure<DownvotePostDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND_ERROR",
            message = "Couldn't find a post by postId {$postId}"
        )
    )

    /** Member not found */
    class MemberNotFoundError(val userId: String) : Result.Failure<DownvotePostDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND_ERROR",
            message = "Couldn't find a member by userId {$userId}"
        )
    )

    /** Already downvoted */
    class AlreadyDownvotedError(val postId: String, val userId: String) : Result.Failure<DownvotePostDto.Response>(
        exception = UseCaseError(
            code = "ALREADY_DOWNVOTED_ERROR",
            message = "User {$userId} has already downvoted post {$postId}"
        )
    )

    /** Downvote failed */
    class DownvoteFailedError(val postId: String) : Result.Failure<DownvotePostDto.Response>(
        exception = UseCaseError(
            code = "DOWNVOTE_FAILED_ERROR",
            message = "Failed to downvote post {$postId}"
        )
    )
}
