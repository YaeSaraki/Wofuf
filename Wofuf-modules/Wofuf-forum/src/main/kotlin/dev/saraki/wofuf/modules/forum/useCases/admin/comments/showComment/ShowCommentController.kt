package dev.saraki.wofuf.modules.forum.useCases.admin.comments.showComment

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Comments.SHOW)
class ShowCommentController(
    private val showCommentUseCase: ShowCommentUseCase
) : BaseController() {

    @PostMapping
    fun showComment(@PathVariable commentId: String): ApiResponse<ShowCommentDto.Response> {
        val result = showCommentUseCase.execute(
            ShowCommentDto.Request(commentId = commentId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
