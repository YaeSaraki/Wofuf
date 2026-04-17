package dev.saraki.wofuf.modules.forum.useCases.posts.getPostById

import dev.saraki.wofuf.modules.forum.useCases.posts.getPostById.GetPostByIdDto.Response
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @date 2026/4/16
 * @description Error classes for get post by ID use case
 */
class GetPostByIdErrors {

    /** Post ID is empty */
    class PostIdEmptyError : Result.Failure<Response>(
        exception = UseCaseError(
            code = "POST_ID_EMPTY_ERROR",
            message = "Post ID cannot be empty"
        )
    )

    /** Invalid post ID format */
    class InvalidPostIdError(val postId: String) : Result.Failure<Response>(
        exception = UseCaseError(
            code = "INVALID_POST_ID_ERROR",
            message = "Invalid post ID format: $postId"
        )
    )

    /** Post not found */
    class PostNotFoundError(val postId: String) : Result.Failure<Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND_ERROR",
            message = "Couldn't find a post by ID {$postId}"
        )
    )

    /** Member not found for post */
    class MemberNotFoundError(val postId: String) : Result.Failure<Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND_ERROR",
            message = "Couldn't find the member associated with post {$postId}"
        )
    )
}