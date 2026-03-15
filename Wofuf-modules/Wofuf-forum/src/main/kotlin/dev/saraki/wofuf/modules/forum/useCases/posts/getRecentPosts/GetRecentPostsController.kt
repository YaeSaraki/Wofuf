package dev.saraki.wofuf.modules.forum.useCases.posts.getRecentPosts

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
 * @description Controller for getting recent posts
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.RECENT)
class GetRecentPostsController(
    private val getRecentPostsUseCase: GetRecentPostsUseCase
) : BaseController() {

    @GetMapping
    fun getRecentPosts(
        @RequestParam(required = false) offset: Int?,
        @RequestParam(required = false) userId: String?
    ): ApiResponse<GetRecentPostsDto.Response> {
        val result = getRecentPostsUseCase.execute(
            GetRecentPostsDto.Request(
                offset = offset ?: 10,
                userId = userId
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
