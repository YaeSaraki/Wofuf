package dev.saraki.wofuf.modules.forum.useCases.admin.posts.unfeaturePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Posts.UNFEATURE)
class UnfeaturePostController(
    private val unfeaturePostUseCase: UnfeaturePostUseCase
) : BaseController() {

    @PostMapping
    fun unfeaturePost(@PathVariable postId: String): ApiResponse<UnfeaturePostDto.Response> {
        val result = unfeaturePostUseCase.execute(
            UnfeaturePostDto.Request(postId = postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
