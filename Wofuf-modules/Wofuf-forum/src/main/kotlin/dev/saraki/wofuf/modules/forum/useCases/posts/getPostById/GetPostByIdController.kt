package dev.saraki.wofuf.modules.forum.useCases.posts.getPostById

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @date 2026/4/16
 * @description Controller for getting a post by ID
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.BY_ID)
class GetPostByIdController(
    private val getPostByIdUseCase: GetPostByIdUseCase
) : BaseController() {

    @GetMapping
    fun getPostById(
        @PathVariable postId: String,
        @RequestParam(required = false) userId: String?
    ): ApiResponse<GetPostByIdDto.Response> {
        val result = getPostByIdUseCase.execute(
            GetPostByIdDto.Request(
                postId = postId,
                userId = userId
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}