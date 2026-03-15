package dev.saraki.wofuf.modules.forum.useCases.posts.getPopularPosts

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Controller for getting popular posts
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.POPULAR)
class GetPopularPostsController(
    private val getPopularPostsUseCase: GetPopularPostsUseCase
) : BaseController() {

    @GetMapping
    fun getPopularPosts(
        @RequestParam(required = false) offset: Int?,
        @RequestParam(required = false) userId: String?
    ): ApiResponse<GetPopularPostsDto.Response> {
        val result = getPopularPostsUseCase.execute(
            GetPopularPostsDto.Request(
                offset = offset ?: 10,
                userId = userId
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
