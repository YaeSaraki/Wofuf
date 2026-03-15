package dev.saraki.wofuf.modules.forum.useCases.comments.upvoteComment

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Upvote a comment
 */
@RestController
@RequestMapping(ForumApiConstantV1.Comments.UPVOTE)
class UpvoteCommentController(
    private val upvoteCommentUseCase: UpvoteCommentUseCase
) : BaseController() {

    @PutMapping
    fun upvoteComment(
        @PathVariable commentId: String,
        @RequestBody request: UpvoteCommentRequest
    ): ApiResponse<UpvoteCommentDto.Response> {
        val result = upvoteCommentUseCase.execute(
            UpvoteCommentDto.Request(
                commentId = commentId,
                userId = request.userId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class UpvoteCommentRequest(
    val userId: String,
)
