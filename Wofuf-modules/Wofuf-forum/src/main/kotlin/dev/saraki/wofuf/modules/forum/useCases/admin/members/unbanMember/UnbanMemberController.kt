package dev.saraki.wofuf.modules.forum.useCases.admin.members.unbanMember

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Members.UNBAN)
class UnbanMemberController(
    private val unbanMemberUseCase: UnbanMemberUseCase
) : BaseController() {

    @PostMapping
    fun unbanMember(@PathVariable memberId: String): ApiResponse<UnbanMemberDto.Response> {
        val result = unbanMemberUseCase.execute(
            UnbanMemberDto.Request(memberId = memberId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
