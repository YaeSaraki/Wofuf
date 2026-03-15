package dev.saraki.wofuf.modules.forum.useCases.posts.getPostBySlug

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Error classes for get post by slug use case
 */
class GetPostBySlugErrors {

    /** Post slug is empty */
    class PostSlugEmptyError : Result.Failure<GetPostBySlugDto.Response>(
        exception = UseCaseError(
            code = "POST_SLUG_EMPTY_ERROR",
            message = "Post slug cannot be empty"
        )
    )

    /** Post not found */
    class PostNotFoundError(val postSlug: String) : Result.Failure<GetPostBySlugDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND_ERROR",
            message = "Couldn't find a post by slug {$postSlug}"
        )
    )

    /** Member not found for post */
    class MemberNotFoundError(val postSlug: String) : Result.Failure<GetPostBySlugDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND_ERROR",
            message = "Couldn't find the member associated with post {$postSlug}"
        )
    )
}
