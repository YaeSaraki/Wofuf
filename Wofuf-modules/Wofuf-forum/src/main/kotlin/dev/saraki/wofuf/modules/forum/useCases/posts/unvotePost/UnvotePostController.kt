package dev.saraki.wofuf.modules.forum.useCases.posts.unvotePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/17
 * @description Controller for removing a vote from a post
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.UNVOTE)
class UnvotePostController(
    private val unvotePostUseCase: UnvotePostUseCase
) : BaseController() {

    @PutMapping
    fun unvotePost(
        @PathVariable postId: String,
        @RequestBody request: UnvotePostRequest
    ): ApiResponse<UnvotePostDto.Response> {
        val result = unvotePostUseCase.execute(
            UnvotePostDto.Request(
                postId = postId,
                userId = request.userId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class UnvotePostRequest(
    val userId: String,
)
