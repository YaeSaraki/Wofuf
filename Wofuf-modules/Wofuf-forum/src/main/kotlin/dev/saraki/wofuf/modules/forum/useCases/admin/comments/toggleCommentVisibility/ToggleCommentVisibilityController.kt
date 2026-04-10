package dev.saraki.wofuf.modules.forum.useCases.admin.comments.toggleCommentVisibility

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Comments.TOGGLE)
class ToggleCommentVisibilityController(
    private val toggleCommentVisibilityUseCase: ToggleCommentVisibilityUseCase
) : BaseController() {

    @PostMapping
    fun toggleCommentVisibility(@PathVariable commentId: String): ApiResponse<ToggleCommentVisibilityDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication?.principal as? String
            ?: return ApiResponse.error("用户未登录")

        val result = toggleCommentVisibilityUseCase.execute(
            ToggleCommentVisibilityDto.Request(commentId = commentId, userId = userId)
        )

        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrThrow())
        }
    }
}
