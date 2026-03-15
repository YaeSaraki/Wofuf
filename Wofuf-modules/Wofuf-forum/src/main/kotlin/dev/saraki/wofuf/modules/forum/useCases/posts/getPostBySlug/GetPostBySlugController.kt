package dev.saraki.wofuf.modules.forum.useCases.posts.getPostBySlug

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Controller for getting a post by slug
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.BY_SLUG)
class GetPostBySlugController(
    private val getPostBySlugUseCase: GetPostBySlugUseCase
) : BaseController() {

    @GetMapping
    fun getPostBySlug(
        @PathVariable postSlug: String,
        @RequestParam(required = false) userId: String?
    ): ApiResponse<GetPostBySlugDto.Response> {
        val result = getPostBySlugUseCase.execute(
            GetPostBySlugDto.Request(
                postSlug = postSlug,
                userId = userId
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
