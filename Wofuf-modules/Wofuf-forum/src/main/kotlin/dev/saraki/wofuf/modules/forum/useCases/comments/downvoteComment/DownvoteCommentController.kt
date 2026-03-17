package dev.saraki.wofuf.modules.forum.useCases.comments.downvoteComment

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Comments.DOWNVOTE)
class DownvoteCommentController(
    private val downvoteCommentUseCase: DownvoteCommentUseCase
) : BaseController() {

    @PutMapping
    fun downvoteComment(
        @PathVariable commentId: String,
        @RequestBody request: DownvoteCommentRequest
    ): ApiResponse<DownvoteCommentDto.Response> {
        val result = downvoteCommentUseCase.execute(
            DownvoteCommentDto.Request(
                commentId = commentId,
                userId = request.userId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class DownvoteCommentRequest(
    val userId: String,
)
