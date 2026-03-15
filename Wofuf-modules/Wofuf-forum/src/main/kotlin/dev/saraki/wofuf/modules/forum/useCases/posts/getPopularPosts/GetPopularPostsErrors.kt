package dev.saraki.wofuf.modules.forum.useCases.posts.getPopularPosts

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Error classes for get popular posts use case
 */
class GetPopularPostsErrors {

    /** Invalid offset value */
    class InvalidOffsetError(val offset: Int?) : Result.Failure<GetPopularPostsDto.Response>(
        exception = UseCaseError(
            code = "INVALID_OFFSET_ERROR",
            message = "Offset must be a positive number, got: $offset"
        )
    )
}
