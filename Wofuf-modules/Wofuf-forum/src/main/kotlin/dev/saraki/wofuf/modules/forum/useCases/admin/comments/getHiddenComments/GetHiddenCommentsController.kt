package dev.saraki.wofuf.modules.forum.useCases.admin.comments.getHiddenComments

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Comments.HIDDEN)
class GetHiddenCommentsController(
    private val getHiddenCommentsUseCase: GetHiddenCommentsUseCase
) : BaseController() {

    @GetMapping
    fun getHiddenComments(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<GetHiddenCommentsDto.Response> {
        val result = getHiddenCommentsUseCase.execute(
            GetHiddenCommentsDto.Request(page = page, size = size)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
