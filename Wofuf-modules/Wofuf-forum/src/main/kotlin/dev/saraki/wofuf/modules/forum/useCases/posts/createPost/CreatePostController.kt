package dev.saraki.wofuf.modules.forum.useCases.posts.createPost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Create a new post
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.ROOT)
class CreatePostController(
    private val createPostUseCase: CreatePostUseCase
) : BaseController() {

    @PostMapping
    fun createPost(@RequestBody request: CreatePostDto.Request): ApiResponse<CreatePostDto.Response> {
        val result = createPostUseCase.execute(request).getOrThrow()
        return ApiResponse.success(result)
    }
}
