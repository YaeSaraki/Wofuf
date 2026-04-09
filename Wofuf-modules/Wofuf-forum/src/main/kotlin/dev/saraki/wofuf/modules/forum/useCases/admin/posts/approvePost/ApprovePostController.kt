package dev.saraki.wofuf.modules.forum.useCases.admin.posts.approvePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Posts.APPROVE)
class ApprovePostController(
    private val approvePostUseCase: ApprovePostUseCase
) : BaseController() {

    @PostMapping
    fun approvePost(@PathVariable postId: String): ApiResponse<ApprovePostDto.Response> {
        val result = approvePostUseCase.execute(
            ApprovePostDto.Request(postId = postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
