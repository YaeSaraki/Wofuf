package dev.saraki.wofuf.modules.forum.useCases.admin.members.getBannedMembers

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Members.BANNED)
class GetBannedMembersController(
    private val getBannedMembersUseCase: GetBannedMembersUseCase
) : BaseController() {

    @GetMapping
    fun getBannedMembers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<GetBannedMembersDto.Response> {
        val result = getBannedMembersUseCase.execute(
            GetBannedMembersDto.Request(page = page, size = size)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
