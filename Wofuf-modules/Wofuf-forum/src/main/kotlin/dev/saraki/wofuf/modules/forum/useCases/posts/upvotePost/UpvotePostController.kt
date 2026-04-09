package dev.saraki.wofuf.modules.forum.useCases.posts.upvotePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import dev.saraki.wofuf.modules.forum.infra.security.requireCurrentUserId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Posts.UPVOTE)
class UpvotePostController(
    private val upvotePostUseCase: UpvotePostUseCase
) : BaseController() {

    @PutMapping
    fun upvotePost(@PathVariable postId: String): ApiResponse<UpvotePostDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val userId = requireCurrentUserId()

        val result = upvotePostUseCase.execute(
            UpvotePostDto.Request(postId = postId, userId = userId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
