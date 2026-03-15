package dev.saraki.wofuf.modules.forum.useCases.posts.upvotePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Posts.UPVOTE)
class UpvotePostController(
    private val upvotePostUseCase: UpvotePostUseCase
) : BaseController() {

    @PutMapping
    fun upvotePost(
        @PathVariable postId: String,
        @RequestBody request: UpvotePostRequest
    ): ApiResponse<UpvotePostDto.Response> {
        val result = upvotePostUseCase.execute(
            UpvotePostDto.Request(
                postId = postId,
                userId = request.userId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class UpvotePostRequest(
    val userId: String,
)
