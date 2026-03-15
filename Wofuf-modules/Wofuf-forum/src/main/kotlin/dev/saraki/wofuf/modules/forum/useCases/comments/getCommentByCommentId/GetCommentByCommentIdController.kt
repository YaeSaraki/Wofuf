package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByCommentId

import dev.saraki.wofuf.modules.forum.ForumApplication
import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/14 21:00
 *   @description:
 */
@RestController
@RequestMapping(ForumApiConstantV1.Comments.BY_ID)
class GetCommentByCommentIdController(
    private val getCommentByCommentIdUseCase: GetCommentByCommentIdUseCase
) : BaseController() {

    @GetMapping()
    fun getCommentByCommentId(
        @PathVariable commentId: String,
        @RequestParam(required = false) userId: String?
    ): ApiResponse<GetCommentByCommentIdDto.Response> {
        val result = getCommentByCommentIdUseCase.execute(
            GetCommentByCommentIdDto.Request(
                commentId = commentId,
                userId = userId,
            )
        ).getOrThrow()
        return ApiResponse.success(
            (result)
        )
    }
}
