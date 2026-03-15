package dev.saraki.wofuf.modules.forum.useCases.posts.deletePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Controller for deleting a post
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.BY_ID)
class DeletePostController(
    private val deletePostUseCase: DeletePostUseCase
) : BaseController() {

    @DeleteMapping
    fun deletePost(@PathVariable postId: String): ApiResponse<DeletePostDto.Response> {
        val result = deletePostUseCase.execute(
            DeletePostDto.Request(postId = postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
