package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByPostSlug

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:12
 *   @description:
 */
class GetCommentByPostSlugErrors {

    // Post slug is empty
    class PostSlugEmptyError() : Result.Failure<GetCommentByPostSlugDto.Response>(
        exception = UseCaseError(
            code = "POST_SLUG_EMPTY",
            message = "Post slug cannot be empty"
        )
    )

    // Post not found
    class PostNotFoundError() : Result.Failure<GetCommentByPostSlugDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND",
            message = "Post not found"
        )
    )

    // Comments not found
    class CommentsNotFoundError() : Result.Failure<GetCommentByPostSlugDto.Response>(
        exception = UseCaseError(
            code = "COMMENTS_NOT_FOUND",
            message = "Comments not found for this post"
        )
    )
}
