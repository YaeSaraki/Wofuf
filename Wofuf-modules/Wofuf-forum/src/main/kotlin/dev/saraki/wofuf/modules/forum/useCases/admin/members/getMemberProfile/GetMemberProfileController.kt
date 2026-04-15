package dev.saraki.wofuf.modules.forum.useCases.admin.members.getMemberProfile

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Members.PROFILE)
class GetMemberProfileController(
    private val getMemberProfileUseCase: GetMemberProfileUseCase
) : BaseController() {

    @GetMapping
    fun getMemberProfile(
        @PathVariable memberId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<GetMemberProfileDto.Response> {
        val result = getMemberProfileUseCase.execute(
            GetMemberProfileDto.Request(
                memberId = memberId,
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
