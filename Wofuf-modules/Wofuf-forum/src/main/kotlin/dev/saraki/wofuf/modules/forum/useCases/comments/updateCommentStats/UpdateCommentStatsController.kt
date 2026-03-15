package dev.saraki.wofuf.modules.forum.useCases.comments.updateCommentStats

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Update comment statistics
 */
@RestController
@RequestMapping(ForumApiConstantV1.Comments.STATS)
class UpdateCommentStatsController(
    private val updateCommentStatsUseCase: UpdateCommentStatsUseCase
) : BaseController() {

    @PutMapping
    fun updateCommentStats(@PathVariable commentId: String): ApiResponse<UpdateCommentStatsDto.Response> {
        val result = updateCommentStatsUseCase.execute(
            UpdateCommentStatsDto.Request(commentId = commentId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
