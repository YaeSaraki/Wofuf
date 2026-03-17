package dev.saraki.wofuf.modules.forum.useCases.posts.unvotePost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/17
 *   @description: Error classes for unvote post use case
 */
class UnvotePostErrors {
    class PostIdEmptyError : Result.Failure<UnvotePostDto.Response>(
        exception = UseCaseError(
            code = "POST_ID_EMPTY_ERROR",
            message = "Post ID cannot be empty"
        )
    )

    class UserIdEmptyError : Result.Failure<UnvotePostDto.Response>(
        exception = UseCaseError(
            code = "USER_ID_EMPTY_ERROR",
            message = "User ID cannot be empty"
        )
    )

    class PostNotFoundError(val postId: String) : Result.Failure<UnvotePostDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND_ERROR",
            message = "Couldn't find a post by postId {$postId}"
        )
    )

    class MemberNotFoundError(val userId: String) : Result.Failure<UnvotePostDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND_ERROR",
            message = "Couldn't find a member by userId {$userId}"
        )
    )

    class NoVoteToRemoveError(val postId: String, val userId: String) : Result.Failure<UnvotePostDto.Response>(
        exception = UseCaseError(
            code = "NO_VOTE_TO_REMOVE_ERROR",
            message = "User {$userId} has not voted on post {$postId}"
        )
    )
}
