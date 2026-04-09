package dev.saraki.wofuf.modules.forum.useCases.admin.posts.unpinPost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Posts.UNPIN)
class UnpinPostController(
    private val unpinPostUseCase: UnpinPostUseCase
) : BaseController() {

    @PostMapping
    fun unpinPost(@PathVariable postId: String): ApiResponse<UnpinPostDto.Response> {
        val result = unpinPostUseCase.execute(
            UnpinPostDto.Request(postId = postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
