package dev.saraki.wofuf.modules.forum.useCases.admin.comments.batchHideComments

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Comments.BATCH_HIDE)
class BatchHideCommentsController(
    private val batchHideCommentsUseCase: BatchHideCommentsUseCase
) : BaseController() {

    @Autowired
    private lateinit var memberRepo: MemberRepo

    @PostMapping
    fun batchHideComments(@RequestBody request: BatchHideCommentsRequest): ApiResponse<BatchHideCommentsDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val authentication = SecurityContextHolder.getContext().authentication
        val userIdString = authentication?.principal as? String
            ?: return ApiResponse.error("用户未登录")

        // 查找当前用户对应的 member
        val userId = UserId.create(UniqueEntityId(userIdString)).getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return ApiResponse.error("用户信息不存在")

        val result = batchHideCommentsUseCase.execute(
            BatchHideCommentsDto.Request(
                commentIds = request.commentIds,
                userId = userIdString,
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
