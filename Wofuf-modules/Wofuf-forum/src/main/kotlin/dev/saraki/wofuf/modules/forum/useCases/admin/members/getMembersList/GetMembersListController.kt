package dev.saraki.wofuf.modules.forum.useCases.admin.members.getMembersList

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Members.LIST)
class GetMembersListController(
    private val getMembersListUseCase: GetMembersListUseCase
) : BaseController() {

    @GetMapping
    fun getMembersList(
        @RequestParam(required = false) nickname: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<GetMembersListDto.Response> {
        val result = getMembersListUseCase.execute(
            GetMembersListDto.Request(
                nickname = nickname,
                page = page,
                size = size
            )
        )

        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrThrow())
        }
    }
}
