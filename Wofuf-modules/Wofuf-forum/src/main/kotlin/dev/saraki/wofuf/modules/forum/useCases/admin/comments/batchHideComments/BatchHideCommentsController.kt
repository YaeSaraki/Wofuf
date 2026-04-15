package dev.saraki.wofuf.modules.forum.useCases.admin.comments.batchHideComments

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Comments.BATCH_HIDE)
class BatchHideCommentsController(
    private val batchHideCommentsUseCase: BatchHideCommentsUseCase
) : BaseController() {

    @PostMapping
    fun batchHideComments(@RequestBody request: BatchHideCommentsRequest): ApiResponse<BatchHideCommentsDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication?.principal as? String
            ?: return ApiResponse.error("用户未登录")

        val result = batchHideCommentsUseCase.execute(
            BatchHideCommentsDto.Request(
                commentIds = request.commentIds,
                userId = userId
            )
        )

        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrThrow())
        }
    }
}

data class BatchHideCommentsRequest(
    val commentIds: List<String>
)
