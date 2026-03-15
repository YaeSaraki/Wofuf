package dev.saraki.wofuf.modules.forum.useCases.posts.downvotePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Posts.DOWNVOTE)
class DownvotePostController(
    private val downvotePostUseCase: DownvotePostUseCase
) : BaseController() {

    @PutMapping
    fun downvotePost(
        @PathVariable postId: String,
        @RequestBody request: DownvotePostRequest
    ): ApiResponse<DownvotePostDto.Response> {
        val result = downvotePostUseCase.execute(
            DownvotePostDto.Request(
                postId = postId,
                userId = request.userId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class DownvotePostRequest(
    val userId: String,
)
