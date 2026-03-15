package dev.saraki.wofuf.modules.forum.useCases.comments.replyToComment

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:15
 *   @description:
 */
@RestController
@RequestMapping(ForumApiConstantV1.Comments.REPLIES)
class ReplyToCommentController(
    private val replyToCommentUseCase: ReplyToCommentUseCase
) : BaseController() {

    @PostMapping()
    fun replyToComment(
        @PathVariable parentCommentId: String,
        @RequestBody request: ReplyToCommentRequest
    ): ApiResponse<ReplyToCommentDto.Response> {
        val result = replyToCommentUseCase.execute(
            ReplyToCommentDto.Request(
                postSlug = request.postSlug,
                userId = request.userId,
                comment = request.comment,
                parentCommentId = parentCommentId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class ReplyToCommentRequest(
    val postSlug: String,
    val userId: String,
    val comment: String,
)
