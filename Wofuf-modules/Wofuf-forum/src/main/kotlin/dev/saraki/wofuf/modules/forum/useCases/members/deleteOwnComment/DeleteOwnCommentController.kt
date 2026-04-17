package dev.saraki.wofuf.modules.forum.useCases.members.deleteOwnComment

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.infra.security.getCurrentUserId
import dev.saraki.wofuf.modules.forum.infra.security.requireCurrentUserId
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
class DeleteOwnCommentController(
    private val deleteOwnCommentUseCase: DeleteOwnCommentUseCase,
) : BaseController() {

    @DeleteMapping(ForumApiConstantV1.Comments.BY_ID)
    fun deleteOwnComment(
        @PathVariable commentId: String
    ): ApiResponse<DeleteOwnCommentDto.Response> {
        // Get current user
        val currentUserId = try {
            requireCurrentUserId()
        } catch (e: IllegalStateException) {
            return ApiResponse.error("Unauthorized")
        }

        val result = deleteOwnCommentUseCase.execute(
            DeleteOwnCommentDto.Request(
                commentId = commentId,
                currentUserId = currentUserId
            )
        )

        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrNull()?.message ?: "Delete failed")
        }
    }
}
