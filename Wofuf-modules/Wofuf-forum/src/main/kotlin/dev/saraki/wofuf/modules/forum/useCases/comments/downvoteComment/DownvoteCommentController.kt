package dev.saraki.wofuf.modules.forum.useCases.comments.downvoteComment

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import dev.saraki.wofuf.modules.forum.infra.security.requireCurrentUserId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Comments.DOWNVOTE)
class DownvoteCommentController(
    private val downvoteCommentUseCase: DownvoteCommentUseCase
) : BaseController() {

    @PostMapping
    fun downvoteComment(@PathVariable commentId: String): ApiResponse<DownvoteCommentDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val userId = requireCurrentUserId()

        val result = downvoteCommentUseCase.execute(
            DownvoteCommentDto.Request(commentId = commentId, userId = userId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
