package dev.saraki.wofuf.modules.forum.useCases.posts.editPost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Controller for editing an existing post
 */
@RestController
@RequestMapping(ForumApiConstantV1.Posts.BY_ID)
class EditPostController(
    private val editPostUseCase: EditPostUseCase
) : BaseController() {

    @PutMapping
    fun editPost(
        @PathVariable postId: String,
        @RequestBody request: EditPostDto.Request
    ): ApiResponse<EditPostDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val authentication = SecurityContextHolder.getContext().authentication
        val currentUserId = authentication?.principal as? String
            ?: throw IllegalStateException("用户未登录")

        val result = editPostUseCase.execute(
            EditPostDto.Request(
                postId = postId,
                currentUserId = currentUserId,
                title = request.title,
                text = request.text,
                link = request.link
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
