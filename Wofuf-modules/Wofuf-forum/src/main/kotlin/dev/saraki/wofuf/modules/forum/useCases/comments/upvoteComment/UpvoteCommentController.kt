package dev.saraki.wofuf.modules.forum.useCases.comments.upvoteComment

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import dev.saraki.wofuf.modules.forum.infra.security.requireCurrentUserId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Comments.UPVOTE)
class UpvoteCommentController(
    private val upvoteCommentUseCase: UpvoteCommentUseCase
) : BaseController() {

    @PutMapping
    fun upvoteComment(@PathVariable commentId: String): ApiResponse<UpvoteCommentDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val userId = requireCurrentUserId()

        val result = upvoteCommentUseCase.execute(
            UpvoteCommentDto.Request(commentId = commentId, userId = userId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
