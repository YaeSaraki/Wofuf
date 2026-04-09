package dev.saraki.wofuf.modules.forum.useCases.admin.posts.showPost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Posts.SHOW)
class ShowPostController(
    private val showPostUseCase: ShowPostUseCase
) : BaseController() {

    @PostMapping
    fun showPost(@PathVariable postId: String): ApiResponse<ShowPostDto.Response> {
        val result = showPostUseCase.execute(
            ShowPostDto.Request(postId = postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
