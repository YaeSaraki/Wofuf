package dev.saraki.wofuf.modules.forum.useCases.posts.unvotePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import dev.saraki.wofuf.modules.forum.infra.security.requireCurrentUserId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Posts.UNVOTE)
class UnvotePostController(
    private val unvotePostUseCase: UnvotePostUseCase
) : BaseController() {

    @PutMapping
    fun unvotePost(@PathVariable postId: String): ApiResponse<UnvotePostDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val userId = requireCurrentUserId()

        val result = unvotePostUseCase.execute(
            UnvotePostDto.Request(postId = postId, userId = userId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
