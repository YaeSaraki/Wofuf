package dev.saraki.wofuf.modules.forum.useCases.members.getMemberById

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 * @author YaeSaraki
 * @date 2026/4/16
 * @description Controller for getting a member by ID
 */
@RestController
@RequestMapping(ForumApiConstantV1.Members.BY_ID)
class GetMemberByIdController(
    private val getMemberByIdUseCase: GetMemberByIdUseCase
) : BaseController() {

    @GetMapping
    fun getMemberById(
        @PathVariable memberId: String
    ): ApiResponse<GetMemberByIdDto.Response> {
        val result = getMemberByIdUseCase.execute(
            GetMemberByIdDto.Request(memberId = memberId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}