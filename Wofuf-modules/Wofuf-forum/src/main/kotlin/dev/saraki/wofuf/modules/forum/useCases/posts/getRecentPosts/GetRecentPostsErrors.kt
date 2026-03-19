package dev.saraki.wofuf.modules.forum.useCases.posts.getRecentPosts

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Error classes for get recent posts use case
 */
class GetRecentPostsErrors {

    /** Invalid offset value */
    class InvalidOffsetError(val offset: Int?) : Result.Failure<GetRecentPostsDto.Response>(
        exception = UseCaseError(
            code = "INVALID_OFFSET_ERROR",
            message = "Offset must be a positive number, got: $offset"
        )
    )

    class InvalidPageError(val page: Int?) : Result.Failure<GetRecentPostsDto.Response>(
        exception = UseCaseError(
            code = "INVALID_PAGE_ERROR",
            message = "Page must be a positive number, got: $page"
        )
    )
}
