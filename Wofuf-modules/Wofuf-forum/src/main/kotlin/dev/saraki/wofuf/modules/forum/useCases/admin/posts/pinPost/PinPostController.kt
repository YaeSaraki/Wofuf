package dev.saraki.wofuf.modules.forum.useCases.admin.posts.pinPost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 * @description Controller for pinning a post
 */
@RestController
@RequestMapping(ForumApiConstantV1.Admin.Posts.PIN)
class PinPostController(
    private val pinPostUseCase: PinPostUseCase
) : BaseController() {

    @PostMapping
    fun pinPost(@PathVariable postId: String): ApiResponse<PinPostDto.Response> {
        val result = pinPostUseCase.execute(
            PinPostDto.Request(postId = postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
