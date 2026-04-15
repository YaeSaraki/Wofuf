package dev.saraki.wofuf.modules.forum.useCases.admin.comments.batchShowComments

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Comments.BATCH_SHOW)
class BatchShowCommentsController(
    private val batchShowCommentsUseCase: BatchShowCommentsUseCase
) : BaseController() {

    @PostMapping
    fun batchShowComments(@RequestBody request: BatchShowCommentsRequest): ApiResponse<BatchShowCommentsDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication?.principal as? String
            ?: return ApiResponse.error("用户未登录")

        val result = batchShowCommentsUseCase.execute(
            BatchShowCommentsDto.Request(
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

data class BatchShowCommentsRequest(
    val commentIds: List<String>
)
