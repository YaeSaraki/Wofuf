package dev.saraki.wofuf.modules.forum.useCases.members.getCurrentMember

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Get current member information
 */
@RestController
@RequestMapping(ForumApiConstantV1.Members.CURRENT)
class GetCurrentMemberController(
    private val getCurrentMemberUseCase: GetCurrentMemberUseCase
) : BaseController() {

    @GetMapping
    fun getCurrentMember(@RequestHeader("userId") userId: String): ApiResponse<GetCurrentMemberDto.Response> {
        val result = getCurrentMemberUseCase.execute(
            GetCurrentMemberDto.Request(userId = userId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
