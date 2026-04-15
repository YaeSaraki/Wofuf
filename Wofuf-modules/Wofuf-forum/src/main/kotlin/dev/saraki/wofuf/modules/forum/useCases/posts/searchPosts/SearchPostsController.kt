package dev.saraki.wofuf.modules.forum.useCases.posts.searchPosts

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for searching posts
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.SEARCH)
class SearchPostsController(
    private val searchPostsUseCase: SearchPostsUseCase
) : BaseController() {

    @GetMapping
    fun searchPosts(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false) category: String?
    ): ApiResponse<SearchPostsDto.Response> {
        val result = searchPostsUseCase.execute(
            SearchPostsDto.Request(
                query = query,
                page = page,
                size = size,
                category = category
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
