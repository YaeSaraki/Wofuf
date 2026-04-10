package dev.saraki.wofuf.modules.forum.useCases.admin.comments.getComments

import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/forum/admin/comments")
class GetCommentsController(
    private val getCommentsUseCase: GetCommentsUseCase,
) : BaseController() {

    @GetMapping
    fun getComments(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "false") includeHidden: Boolean
    ): ApiResponse<GetCommentsDto.Response> {
        val request = GetCommentsDto.Request(
            page = page,
            size = size,
            search = search,
            includeHidden = includeHidden
        )

        val result = getCommentsUseCase.execute(request).getOrThrow()
        return ApiResponse.success(result)
    }
}
